$[/myProject/perl/perlObjects.pm]

use XML::XPath;
use Crypt::SSLeay;
use Time::Local;

$|=1;

# protocol required
my $poolName    = "$[poolName]";
my $current     = "$[current]";

# accelerator specific
my $ea_cm            = "$[cm]";
my $ea_resource_name = "$[resource_name]";
my $ea_cmtool        = "$[cmtool]";
my $ea_build_class   = "$[build_class]";
my $agentsPerInstance = "$[agentsPerInstance]";

my ($user,$pass)     = getCredential("$[credential]");

print "Connection config: $config\n";
print "cm          : $ea_cm\n";
print "cmtool      : $ea_cmtool\n";
print "user        : $user\n";
print "pass        : ***\n";
print "resource    : $ea_resource_name\n";
print "build class : $ea_build_class\n";

# check parameters
if ($ea_build_class eq "" && $ea_resource_name eq "") {
    print "Error:  Name or Build Class must be specified.\n";
    finish(0,1);
}
if ($ea_cm eq "" ) {
    print "Error: ElectricAccelerator CM must be specified.\n";
    finish(0,1);
}
if ($ea_cmtool eq "") {
    print "Error: cmtool must be specified.\n";
    finish(0,1);
}
if (!-f $ea_cmtool ) {
    print "Error: cmtool [$ea_cmtool] does not exist.\n";
    finish(0,1);
}

# there must be at least one agent per instance
if ($agentsPerInstance < 1) {
    print "Setting agents per instance to 1.\n";
    $agentsPerInstance = 1;
}
# if build class given, see if it has a resource
if ($ea_build_class ne "" ) {
    # query accelerator CM to find resource for the build class
    
    # first get all classes
    print "Looking up Build Class $ea_build_class\n";
    my $classes = cmTool($ea_cmtool, $ea_cm,$user,$pass, "getBuildClasses");
    
    # now use the classes data to get the build class id for the one of interest
    my $classId = getClassIdFromXML($classes,$ea_build_class);
    print "Found class id: $classId\n";
    if ($classId <0) { 
        print "Error: Build Class $ea_build_class not found.\n";
        finish(0,1);
    }
    
    # now get the resource from that class using id
    my $bc = cmTool($ea_cmtool, $ea_cm,$user,$pass, "getBuildClass $classId");
    my $bc_resource = getTag($bc,"resourceRequest");
    print "Found resource $bc_resource\n";
    if ($bc_resource eq "") {
         print "Error: Build Class $ea_build_class does not specify a resource.\n";
         finish(0,1);
    } else {
         # use the resource from the build class
         $ea_resource_name = $bc_resource;
    }
}

# query accelerator CM to find how many agents it wants
my $output = cmTool($ea_cmtool, $ea_cm,$user,$pass,
  "getResourceStats --maxResults 1 --order \"createTime desc\" --filter \"resource_name='" . $ea_resource_name . "'\"");

my $shortage = getTag($output,"agentClusterShortage");
my $demand   = getTag($output,"agentDemand");
my $avail    = getTag($output,"agentsAvailable");
my $inuse    = getTag($output,"agentsInUse");
my $recTime  = getTag($output, "createTime");
my $recEpoch = dateStrParseToGMTEpoch($recTime);
my $now = time();

print "Time of statistics record= $recEpoch\n";
print "Time now                 = $now\n";
my $elapsed = $now - $recEpoch;
print "Record is $elapsed seconds old\n";


print "Shortage: $shortage\n";
print "Demand: $demand\n";
print "Avail: $avail\n";
print "InUse: $inuse\n";

# cloud manager thinks in terms of instances. We
# need to convert to thinking in terms of agents 
# (there may be more than one agent per instance)
my $agentsAllocatedByCloudManager = $current * $agentsPerInstance;

# if cloud manager tells us it has created more agents
# than are currently available, something is wrong 
# limit this to the num avail.
if ($agentsAllocatedByCloudManager > $avail ) {
    $agentsAllocatedByCloudManager = $avail ;
}

# we are returning the number of instance we want cloud manager
# to manager for us so we have to count the ones already 
# started in our answer.
# Note: if the demand is 2 and avail is 2 we do not return 0, 
#       we return the current so CloudManager will not take
#       anything away from us
my $agents = $demand - $avail + $agentsAllocatedByCloudManager;
if ($agents < 0) {
    $agents = 0;
}

# cm writes a record every 5 minutes.  the data is only updated
# if there is activity (new builds requested or emake communicating
# with the CM during a build. If the record we get back from cm
# is older than 5 minutes (padded to 10) then there is not much
# going on the CM.  Unfortunately the CM's last record may report
# its last notion of activity. So we put this check in to override
# what the CM thinks and set demand to 0
if ($elapsed > 600) {
    print "No activity records for 10 minutes.  Assuming no agents needed.\n";
    $agents = 0;
}

# Change by Avan Mathur 4/2013
# Query dead agents and add them to list to be terminated and removed
my $xmlout = "";
my $hostName="";
my %resources;

addXML(\$xmlout,"<DeadResources>");
$output = cmTool($ea_cmtool, $ea_cm,$user,$pass,"getAgents --filter \"status!=1\"");
my $xp = XML::XPath->new($output);
my $nodeset = $xp->find("//agent");

# There are $agentsPerInstance number of agents per CloudManager resource
# for this plan.  Only mark this agent as dead if *all* of the agents for
# the host are dead.
foreach my $agent ($nodeset->get_nodelist) {
    $hostName = $agent->findvalue('hostName')->string_value;
    $resources{$hostName}++;
    if ($resources{$hostName} == $agentsPerInstance) {
        addXML(\$xmlout,"<Resource>");
        addXML(\$xmlout,"  <resourceName>" . xmlQuote($hostName) . "</resourceName>");
        addXML(\$xmlout,"</Resource>");
    }
}
addXML(\$xmlout,"</DeadResources>");
$::ec->setProperty("/myJob/CloudManager/deadResources", $xmlout);

# convert back from agents to instances
finish(int($agents/$agentsPerInstance),0);

##############################################################

sub finish {
    my ($instances,$exit) = @_;
    print "  Result=$instances\n";
    $::pdb->setProp("/myJob/CloudManager/query",$instances);
    exit $exit;
}


#########################################################################

sub cmTool($$$$$) {
    my ($cmtool, $cm, $user,$pass, $cmdFragment) = @_;

    my $cmd = "$cmtool --cm $cm login $user $pass 2>&1";
    print "CMTOOL login $user\n";
    my $output = `$cmd`;
    my $res = $?;
    if ($res !=  0) {
        print $output;
        exit 1;
    }
    print "OUTPUT:$output\n";
    $cmd = "$cmtool --cm $cm $cmdFragment";
    print "CMTOOL COMMAND:$cmd\n";
    $output = `$cmd`;
    $res = $?;
    if ($res != 0) {
        print $output;
        exit 1;
    }
    print "OUTPUT:$output\n";
    return $output;
}

sub getTag($$) {
    my ($output, $tag) = @_;
    my $xp = XML::XPath->new($output);
    my $result="";
    my $nodeset = $xp->find("//" . $tag);
    foreach my $node ($nodeset->get_nodelist) {
        $result = $node->string_value();
    }
    return $result;

}

sub getClassIdFromXML($$) {
    my ($output, $class) = @_;
    my $xp = XML::XPath->new($output);
    my $result="";
    my $nodeset = $xp->find("//response/buildClass");
    foreach my $node ($nodeset->get_nodelist) {
        my $className = $xp->findvalue('buildClassName', $node)->value();
        my $classId = $xp->findvalue('buildClassId', $node)->value();
        if ($className eq $class) {
            return $classId;
        }
    }
    return -1;

}

#-------------------------------------------------------------------------
#  getCredential
#-------------------------------------------------------------------------
sub getCredential($) {
     my ($credname) = @_;

     # Assumes we are running in a step and uses the current session
     # Port and Secure are needed for testing purposes.
     my $ec = ElectricCommander->new();
     $ec->abortOnError(0);

     my $jobStepId = $ENV{"COMMANDER_JOBSTEPID"};
     my $xPath = $ec->getFullCredential($credname,
                 {jobStepId => $jobStepId });
     if (!defined $xPath) {
         my $msg = $ec->getError();
         print("Error: retrieving credential $msg\n");
         exit(1);
     }

     # Get user and password from Credential
     my $user  = $xPath->findvalue('//credential/userName');
     my $pass  = $xPath->findvalue('//credential/password');

     return ($user,$pass);
}


#-------------------------------------------------------------------------
#  convert time string to number
#-------------------------------------------------------------------------
sub dateStrParseToGMTEpoch($) {
    my ($tstring) = @_;
    my $date = "";
    my $year = "";
    my $month = "";
    my $day = "";
    my $hour = "";
    my $min = "";
    my $sec = "";
    my $time = "";


    if (!defined $tstring ) { return 0; }
    if ($tstring eq "") { return 0; }
    if (length($tstring) < 15 ) { return 0; }

    # commander returns strings like "2007-3-24T10:32:32"

    # First split the calendar from the clock
    ($date, $time) = split /T/, $tstring, 2;

    # if bad data, return 0
    if ($date eq "" ) { return 0; }

    # Now break date up
    ($year,$month,$day) = split /-/, $date, 3;

    # Now break time up
    ($hour,$min, $sec) = split /:/, $time, 3;

    # now put them back together to get an epoch time
    my $epoch;
    $epoch = timegm($sec, $min, $hour,$day,$month-1,$year);
    return $epoch;
}

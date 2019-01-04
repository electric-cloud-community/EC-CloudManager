$[/myProject/perl/perlObjects.pm]

use XML::XPath;
use Crypt::SSLeay;

$|=1;

# protocol required 
my $hostnames    = "$[hosts]";

# accelerator specific
my $ea_cm            = "$[cm]";
my $ea_resource_name = "$[resource_name]";
my $ea_cmtool        = "$[cmtool]";
my $ea_build_class   = "$[build_class]";
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
    exit 1;
}
if ($ea_cm eq "" ) {
    print "Error: ElectricAccelerator CM must be specified.\n";
    exit 1;
}
if ($ea_cmtool eq "") {
    print "Error: cmtool must be specified.\n";
    exit 1;
}
if (!-f $ea_cmtool ) {
    print "Error: cmtool [$ea_cmtool] does not exist.\n";
    exit 1;
}


# for each host, remove the agent
foreach my $host (split(/;/,$hostnames)) {
    # delete all agents on the host that has been deleted
    print "Deleting agent for host $host\n";
    my $output = cmTool($ea_cmtool, $ea_cm,$user,$pass,
      "deleteAgents --filter \"agentName like '" . $host . "-%'\"");

    print "Delete Agent $host results:\n";
    print "$output\n";
}
exit 0;



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
    print "CMTOOL outpout:$output\n";
    $cmd = "$cmtool --cm $cm $cmdFragment";
    print "CMTOOL COMMAND:$cmd\n";
    $output = `$cmd`;
    $res = $?;
    if ($res != 0) {
        print $output;
        exit 1;
    }
    print "CMTOOL output:$output\n";
    return $output;
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



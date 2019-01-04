####################################################################
#
# ECDeployment: store/retrieve deployment records
#
####################################################################
package CMDeployment;

use JSON;
use Data::Dumper;
use ElectricCommander;
use ElectricCommander::PropDB;
use ElectricCommander::Util;

use base("ElectricCommander::PropDB");
$::json = JSON->new->allow_nonref;

####################################################################
# Object constructor for ECDeplyomnet
# Inputs
#   cmdr    = a previously initialized ElectricCommander handle
#   proj    = the project name (used in testing, otherwise blank)
####################################################################
sub new {
    my $class = shift;

    my $cmdr = shift;
    my $proj = shift;

    # for testing outside a step
    if ($proj eq "") {
        $proj = $ENV{"COMMANDER_CMRESPLAN_PROJECT"};
    }
    if ("$proj" eq "") {
        $proj = "/myProject";
    }

    my($self) = ElectricCommander::PropDB->new($cmdr,"$proj/tables/DEPLOYMENTS");
    bless ($self, $class);
    return $self;
}

####################################################################
####################################################################
# Manage records in a plan
####################################################################
####################################################################

####################################################################
# addDepRec -     add a Dep record (persistent)
####################################################################
sub addDepRec {
    my ($self,$plan,$handle,$custom, $optnow) = @_;
    
    $self->loadDep($plan);
    $self->newDepRec($plan,$handle,$custom, $optnow) ;
    $self->commitDep($plan);
}

####################################################################
# newDepRec -     add a Dep record (cache only)
####################################################################
sub newDepRec {
    my ($self,$plan,$handle,$custom,$optnow) = @_;

    # set start time based on our clock in case provider does not
    # record the start time of the deployment
    # if grow returns a start time named "start" it will take
    # precedence which is desired behavior
    my $now;
    if ($optnow ne "") {
        $now = $optnow;  # for testing
    } else {
        $now = time();
    }

    my ($sec,$min, $hour, $mday, $mon, $year, $wday, $yday, $isdst) = localtime($now);
    $year += 1900;
    $mon +=1;
    my $startstr = sprintf("%04d-%02d-%02dT%02d:%02d:%02d", $year,$mon,$mday,$hour,$min,$sec);

    foreach my $c (keys %{ $custom} ) {
        $self->setDepRecValue($plan,$handle, $c,$custom->{$c});
    }
    $self->{deps}{$plan}{$handle}{startstr} = "$startstr";
    $self->{deps}{$plan}{$handle}{start}    = "$now";
    $self->{deps}{$plan}{$handle}{state}    = "alive";
    $self->{deps}{$plan}{$handle}{stop}     = 0;
    $self->{deps}{$plan}{$handle}{pending}  = 0;
}

####################################################################
# endDepRec -     mark deployment as finished
#       
#  result - is this in state stopped or pending
####################################################################
sub endDepRec {
    my ($self,$plan,$handle,$result) = @_;
    
    my $now = time();
    if ($result eq "pending") {
        $self->{deps}{$plan}{$handle}{state}  = "pending";
        $self->{deps}{$plan}{$handle}{pending}  = "$now";
    } else {
        $self->{deps}{$plan}{$handle}{state}  = "stopped";
        $self->{deps}{$plan}{$handle}{stop}  = "$now";
    }
}

####################################################################
# delDepRec -     del a Dep record 
####################################################################
sub delDepRec {
    my ($self,$plan,$handle) = @_;
    
    delete($self->{deps}{$plan}{$handle});
}

####################################################################
# getDepRec -  get a Dep record (indexed by handle)
#
# returns pointer to hash of all values
####################################################################
sub getDepRec {
    my ($self,$plan,$handle) = @_;

    my $rec = $self->{deps}{$plan}{$handle};
    my $results;
    foreach my $c (keys %{ $rec} ) {
        $results->{$c} = $rec->{$c};
    }
    return $results;
}

####################################################################
# setDepRecValue -  set a specific value
####################################################################
sub setDepRecValue() {
    my ($self,$plan,$handle,$name,$value) = @_;

    $self->{deps}{$plan}{$handle}{$name} = $value;
}

####################################################################
# getDepRecs -  get a list of handles for a plan
#
# filter = alive | dead | pending 
#   (or any combination)
####################################################################
sub getDepRecs() {
    my ($self,$plan,$filter) = @_;

    my @keys;
    foreach my $t (keys %{$self->{deps}{$plan} } ) {
        my $state = $self->{deps}{$plan}{$t}{state};
        if ($filter =~ m/$state/) {
            push @keys, $t;
        }
    }
    return @keys;
}

####################################################################
# trimRecs - delete all recs older than configured limit
####################################################################
sub trimRecs {
    my ($self,$plan,$cutoff) = @_;

    foreach my $handle (keys % { $self->{deps}{$plan} } ) {
        my $stoptime = $self->{deps}{$plan}{$handle}{stop};
        my $starttime = $self->{deps}{$plan}{$handle}{start};
        if ($stoptime > 0 && $starttime < $cutoff) {
            delete($self->{deps}{$plan}{$handle});
        }
    }
}

####################################################################
# getDepsForPlan - get pointer to hash for a plan
####################################################################
sub getDepsForPlan {
    my ($self,$plan) = @_;
    return $self->{deps}{$plan};
}

####################################################################
# save GrowResponse
#    unpack a GrowResponse result from a grow request
#    break into muliple deployment records
#    record each deployment
####################################################################
sub saveGrowResponse {
    my ($self,$plan, $xml) = @_;

    $self->loadDep($plan);
    my $now = time();
    my $xPath = XML::XPath->new( xml => "$xml");
    my $nodeset = $xPath->find('//Deployment');
    foreach my $node ($nodeset->get_nodelist) {
        my $handle = $xPath->findvalue('handle',$node)->string_value;

        if ("$handle" eq "") {
            print "Error: no handle found for deployment\n";
            next;
        }

        # Get all the values returned
        my $tags = $xPath->findnodes('*',$node);
        my $custom;
        foreach my $tag ($tags->get_nodelist) {
            my $name = $tag->getName() ;
            my $val = $tag->string_value;
            $custom->{$name} = $val;
        }
        $self->newDepRec($plan, $handle, $custom);
    }   
    $self->commitDep($plan);
}

####################################################################
####################################################################
# Manage whole plans
####################################################################
####################################################################


####################################################################
# existsPlan
####################################################################
sub existsPlan {
    my ($self,$plan) = @_;
    return (defined ($self->{deps}{$plan}));
}

####################################################################
# modifyPlan
####################################################################
sub modifyPlan {
    my ($self,$plan,$j) = @_;

    # read into cache 
    $self->importOnePlan($plan,$j);

    # write new out to property sheet
    $self->commitLog($plan);
    return;
}

####################################################################
# delete - delete a plan
####################################################################
sub deletePlan {
    my ($self,$plan) = @_;

    # delete from cache
    delete($self->{deps}{$plan});

    # delete old from property sheet
    $self->delRow($plan);
    return;
}

####################################################################
# getAllPlanNames - from property sheet
####################################################################
sub getAllPlanNames {
    my ($self) = @_;

    my @planNames;
    my %plans = $self->getRows();
    foreach my $plan (keys %plans) {
        push @planNames, $plan;
    }
    return @planNames;
}

####################################################################
# loadAllDeps - iterate through list of plans and load Deps for each
####################################################################
sub loadAllDeps {
    my ($self) = @_;

    my @plans = $self->getAllPlanNames();
    foreach my $plan (@plans) {
        $self->loadDep($plan);
    }
}

####################################################################
# loadDep - load the Dep from a single plan
####################################################################
sub loadDep($){
    my ($self, $plan) = @_;
    if ($plan eq "") {
        print "error: no plan name for loadDep\n";
        return "";
    }
    my $jdep = $self->getJSON($plan);
    if ($jdep eq "") {
        # nothing stored
        return;
    }
    my $pdep = $::json->decode($jdep);
    $self->{deps}{$plan} = $pdep;
}

####################################################################
# commitDeps - for each plan in memory, write the Dep to property
####################################################################
sub commitDeps {
    my ($self) = @_;

    my @plans;
    foreach my $plan (keys %{$self->{deps}}) {
        push @plans, $plan;
    }
    foreach my $plan (@plans) {
        $self->commitDep($plan);
    }

}

####################################################################
# commitDep - write the Dep to the db
####################################################################
sub commitDep($) {
    my ($self,$plan) = @_;
    if ($plan eq "") {
        print "error: no plan name for commitDep\n";
        return "";
    }
    my $jdep = $self->getOnePlan($plan);
    $self->setJSON($plan,$jdep);
}

####################################################################
# getOnePlan - get one plan from property
####################################################################
sub getOnePlan($) {
    my ($self, $plan) = @_;
    my $pdep = $self->{deps}{$plan};
    my $jdep =  $::json->encode($pdep);
    return $jdep;
}

####################################################################
# getAllDeps - get all the plans in memory as json
####################################################################
sub getAllDeps() {
    my ($self) = @_;

    my @plans;
    foreach my $plan (keys % {$self->{deps}}) {
        push @plans, $self->{deps}{$plan};
    }
    return $::json->encode(\@plans);
}

####################################################################
# importOnePlan - take a json representation and overwrite cache
####################################################################
sub importOnePlan($$) {
    my ($self,$plan,$jplan) = @_;

    if ($plan eq "") { 
        print "error: no plan name found in importOnePlan\n";
        return;
    }
    # convert json to perl 
    my $pdep = $::json->decode($jplan);

    # set cache
    $self->{deps}{$plan} = $pdep;
}

####################################################################
# getCacheAsText -  used in debugging and testing
####################################################################
sub getCacheAsText() {
    my ($self) = @_;
    my $out = Dumper($self->{deps});
    return $out;
}

####################################################################
# The json string representation of a plan
####################################################################
sub getJSON {
    my ($self,$plan) = @_;
    return $self->getCol($plan, "json");
}
sub setJSON {
    my ($self,$plan,$j) = @_;
    return $self->setCol($plan, "json", "$j");
}

####################################################################
# Debug setting - set on plan
####################################################################
sub getDebug {
    my ($self,$plan) = @_;
    return $self->getCol($plan, "debug");
}

sub setDebug {
    my ($self,$plan,$d) = @_;
    return $self->setCol($plan, "debug", "$j");
}

####################################################################
# Get data for UI deployment list
####################################################################
sub getUIDeploymentList {
    my ($self, $plan) = @_;

    my $jtext = "{\"desc\":\"deployment list\",\"deps\":[";
    my $rows = 0;
    $self->loadDep($plan);
    my @recs = $self->getDepRecs($plan,"alive");
    foreach my $handle (@recs) {
        my $r = $self->getDepRec($plan,$handle);
        if ($rows > 0) {
            $jtext .= ",";
        }
        $jtext .= "{\"handle\":\"$handle\", \"start\":\" . $r->{start}*1000 . \", \"resource\":\"$r->{resource}\"}";
        $rows++;
    }
    $jtext .= "]}";
    return $jtext;
}

1;

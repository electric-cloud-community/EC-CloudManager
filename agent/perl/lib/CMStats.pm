####################################################################
#
# Stats: Manage statistics kept by CloudManager
#
# 
#   plan name
#   current deployed
#
####################################################################
package CMStats;

use strict;
use JSON;
use Data::Dumper;
use ElectricCommander;
use ElectricCommander::PropDB;
use ElectricCommander::Util;

use base("ElectricCommander::PropDB");
$::json = JSON->new->allow_nonref;

####################################################################
# Object constructor for ECStats
#
# Inputs
#   cmdr  = a previously initialized ElectricCommander handle
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

    my($self) = ElectricCommander::PropDB->new($cmdr,"$proj/tables/STATS");

    bless ($self, $class);
    return $self;
}

####################################################################
# existsStats
#
#   returns true if stats for plan exists
####################################################################
sub existsStats {
    my ($self,$plan) = @_;

    return (defined ($self->{cfgs}{$plan}));
}

####################################################################
# modifyStats
#
#   UI calls modify stats for a modify or rename operation
#   this method determines which should be called
####################################################################
sub modifyStats {
    my ($self,$oldname,$j) = @_;

    # j contains the json, get the name out of the data
    my $pcfg = $::json->decode($j);

    my $newname = $pcfg->{name};
    if ($newname eq "") {
        print "error: modifyStats could not find an old plan name in data\n";
        return;
    }
    if ($oldname eq "") {
        print "error: modifyStats oldname cannot be blank\n";
        return;
    }
    # does the name in the data match the name that 
    # we are going to save?
    # save copy of contents
    if ($oldname ne $newname) {
        # delete from cache and sheet
        $self->deleteStats($oldname);
    }
    
    # read into cache with newname
    $self->importOneStats($newname,$j);

    # write new out to property sheet
    $self->writeStats($newname);
    return;
}

####################################################################
# rename - rename a plan
#
#   This takes place immediately (write through)
#
#   rename a Resource Stats
####################################################################
sub renameStats {
    my ($self,$oldname,$newname) = @_;
    # save copy of contents
    my $j = $self->encodeOneStats($oldname);

    # delete from cache and sheet
    $self->deleteStats($oldname);

    # read into cache with newname
    $self->importOneStats($newname,$j);

    # write new out to property sheet
    $self->writeStats($newname);
    return;
}

####################################################################
# delete - delete a plan
#
#   Deletes from cache and from property sheet
#
####################################################################
sub deleteStats {
    my ($self,$plan) = @_;

    # delete from cache
    delete($self->{cfgs}{$plan});

    # delete old from property sheet
    $self->delRow($plan);
    return;
}

####################################################################
# The json string representation of stats
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
# Name
####################################################################
sub setName {
    my ($self,$plan) = @_;
    $self->{cfgs}{$plan}{name} = "$plan";
    return ;
}
sub getName {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{name} ;
}

####################################################################
# The description for this plan. 
####################################################################
sub setDescription {
    my ($self,$plan,$desc) = @_;
    $self->{cfgs}{$plan}{desc} = "$desc";
    return ;
}
sub getDescription {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{desc} ;
}


####################################################################
# Debug setting
####################################################################
sub getDebug {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{debug} ;
}

sub setDebug {
    my ($self,$plan,$d) = @_;
    $self->{cfgs}{$plan}{debug} = "$d";
}

####################################################################
# current
#    The current number of deployments for this plan. This is updated
#    each time a change is made in deployments
####################################################################
sub getCurrent {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{current};
}
sub setCurrent {
    my ($self,$plan, $count) = @_;
    $self->{cfgs}{$plan}{current} = "$count";
}

####################################################################
# bounded
#    The bounded number of deployments for this plan at this time.
#    Takes into account application requirements, tod limits,
#    usage limits.
####################################################################
sub getBounded {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{bounded};
}
sub setBounded {
    my ($self,$plan, $count) = @_;
    $self->{cfgs}{$plan}{bounded} = "$count";
}

####################################################################
# appRequest
#    The number of machines the application requests.
####################################################################
sub getAppRequest {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{appRequest} ;
}
sub setAppRequest {
    my ($self,$plan, $req) = @_;
    $self->{cfgs}{$plan}{appRequest} ="$req";
}


####################################################################
# COST DATA
####################################################################

####################################################################
# COST_CONSUMED
#   The hours consumed since the start of COST_PERIOD
#   This value is reset to 0 each time a new COST_PERIOD starts.
####################################################################
sub getCostConsumed {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{cost}{COST_CONSUMED};
}
sub setCostConsumed {
    my ($self,$plan, $hours) = @_;
    $self->{cfgs}{$plan}{cost}{COST_CONSUMED} = "$hours";
}

####################################################################
# COST_CONSUMED_TERMINATED
#   The hours consumed since the start of COST_PERIOD by all
#   instances that have been terminated already
#   This value is reset to 0 each time a new COST_PERIOD starts.
####################################################################
sub getCostConsumedTerminated {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{cost}{COST_CONSUMED_TERMINATED};
}
sub setCostConsumedTerminated {
    my ($self,$plan, $hours) = @_;
    $self->{cfgs}{$plan}{cost}{COST_CONSUMED_TERMINATED} = "$hours";
}

####################################################################
# COST_START
#   Epoch time the last COST_PERIOD started
####################################################################
sub getCostStart {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{cost}{COST_START};
}
sub setCostStart {
    my ($self,$plan, $epoch) = @_;
    $self->{cfgs}{$plan}{cost}{COST_START} = "$epoch";
}

####################################################################
# Configuration management
####################################################################
sub getAllStatsNames {
    my ($self) = @_;

    my @planNames;
    my %plans = $self->getRows();
    foreach my $plan (keys %plans) {
        push @planNames, $plan;
    }
    return @planNames;
}

sub loadAllStats {
    my ($self) = @_;

    my @plans = $self->getAllStatsNames();
    foreach my $plan (@plans) {
        $self->loadStats($plan);
    }
}

sub loadStats($){
    my ($self, $plan) = @_;
    if ($plan eq "") {
        print "error: no plan name for loadStats\n";
        return "";
    }
    my $jcfg = $self->getJSON($plan);
    my $pcfg = $::json->decode($jcfg);
    $self->{cfgs}{$plan} = $pcfg;
    $self->setName($plan);
}

sub commitStats {
    my ($self) = @_;

    my @plans;
    foreach my $plan (keys %{$self->{cfgs}}) {
        push @plans, $plan;
    }
    foreach my $plan (@plans) {
        $self->writeStats($plan);
    }

}

sub writeStats($) {
    my ($self,$plan) = @_;
    if ($plan eq "") {
        print "error: no plan name for writeStats\n";
        return "";
    }
    my $jcfg = $self->encodeOneStats($plan);
    $self->setJSON($plan,$jcfg);
}

sub encodeOneStats($) {
    my ($self, $plan) = @_;
    my $pcfg = $self->{cfgs}{$plan};
    $self->setName($plan);
    my $jcfg =  $::json->encode($pcfg);
    return $jcfg;
}


####################################################################
# get one plan
####################################################################
sub getOneStats($) {
    my ($self,$plan) = @_;

    return $self->encodeOneStats($plan);
}

####################################################################
# get all plans
####################################################################
sub getAllStats() {
    my ($self) = @_;

    my @plans;
    foreach my $plan (keys % {$self->{cfgs}}) {
        $self->setName($plan);
        push @plans, $self->{cfgs}{$plan};
    }
    return @plans;
}

####################################################################
# import a plan to cache 
#   jplan -  a single plan in json
#
#   recuse the hash tree and save all
#
####################################################################
sub importOneStats($$) {
    my ($self,$plan,$jplan) = @_;

    if ($plan eq "") { 
        print "error: no plan name found in importOneStats\n";
        return;
    }
    # convert json to perl 
    my $pcfg = $::json->decode($jplan);

    # set cache
    $self->{cfgs}{$plan} = $pcfg;
}



####################################################################

####################################################################
# getCacheAsText
#
# used in debugging and testing
####################################################################
sub getCacheAsText() {
    my ($self) = @_;
    my $out = Dumper($self->{cfgs});
    return $out;
}

1;

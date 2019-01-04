####################################################################
#
# ResPlan: Object definition of a resource plan for ElectricCommnader
# resource management.
#
# A resource plan contains the following
#   plan name
#   time of day settings
#   pool name and resource prefix
#   adjustConfiguration
#   requestConfiguration
#   requirementString
#   requirementProc
#
####################################################################
package CMResPlan;

use strict;
use JSON;
use Data::Dumper;
use ElectricCommander;
use ElectricCommander::PropDB;
use ElectricCommander::Util;

use base("ElectricCommander::PropDB");
$::json = JSON->new->allow_nonref;

####################################################################
# Object constructor for ECResPlan
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

    my($self) = ElectricCommander::PropDB->new($cmdr,"$proj/tables/CFG");

    bless ($self, $class);
    return $self;
}

####################################################################
# existsPlan
#
#   returns true if plan exists
####################################################################
sub existsPlan {
    my ($self,$plan) = @_;


    return (defined ($self->{cfgs}{$plan}));
}

####################################################################
# modifyPlan
#
#   UI calls modify plan for a modify or rename operation
#   this method determines which should be called
####################################################################
sub modifyPlan {
    my ($self,$oldname,$j) = @_;

    # j contains the json, get the name out of the data
    my $pcfg = $::json->decode($j);

    my $newname = $pcfg->{name};
    if ($newname eq "") {
        print "error: modifyPlan could not find an old plan name in data\n";
        return;
    }
    if ($oldname eq "") {
        print "error: modifyPlan oldname cannot be blank\n";
        return;
    }
    # does the name in the data match the name that 
    # we are going to save?
    # save copy of contents
    if ($oldname ne $newname) {
        # delete from cache and sheet
        $self->deletePlan($oldname);
    }
    
    # read into cache with newname
    $self->importOnePlan($newname,$j);

    # write new out to property sheet
    $self->writeCfg($newname);
    return;
}

####################################################################
# rename - rename a plan
#
#   This takes place immediately (write through)
#
#   rename a Resource Plan
####################################################################
sub renamePlan {
    my ($self,$oldname,$newname) = @_;
    # save copy of contents
    my $j = $self->encodeOnePlan($oldname);

    # delete from cache and sheet
    $self->deletePlan($oldname);

    # read into cache with newname
    $self->importOnePlan($newname,$j);

    # write new out to property sheet
    $self->writeCfg($newname);
    return;
}

####################################################################
# delete - delete a plan
#
#   Deletes from cache and from property sheet
#
####################################################################
sub deletePlan {
    my ($self,$plan) = @_;

    # delete from cache
    delete($self->{cfgs}{$plan});

    # delete old from property sheet
    $self->delRow($plan);
    return;
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
# KillLimit settings
#    Settings to limit when deplyoments are killed
####################################################################
sub getKillLimitPolicy {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{killLimits}{KillLimitPolicy} ;
}
sub setKillLimitPolicy {
    my ($self,$plan, $value) = @_;
    $self->{cfgs}{$plan}{killLimits}{KillLimitPolicy} = "$value" ;
}
sub getKillLimitMin {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{killLimits}{KillLimitMin} ;
}
sub setKillLimitMin {
    my ($self,$plan, $value) = @_;
    $self->{cfgs}{$plan}{killLimits}{KillLimitMin} = "$value" ;
}
sub getKillLimitMax {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{killLimits}{KillLimitMax} ;
}
sub setKillLimitMax {
    my ($self,$plan, $value) = @_;
    $self->{cfgs}{$plan}{killLimits}{KillLimitMax} = "$value" ;
}


####################################################################
# active
#    0 = inactive
#    else active
####################################################################
sub getActive {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{active} ;
}
sub setActive {
    my ($self,$plan, $state) = @_;
    $self->{cfgs}{$plan}{active} = "$state";
}

####################################################################
# reset
#    0 = no reset requested
#    else reset of stats requested
####################################################################
sub getReset {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{reset} ;
}
sub setReset {
    my ($self,$plan, $state) = @_;
    print "DEBUG:reset to $state\n";
    $self->{cfgs}{$plan}{reset} = "$state";
}

####################################################################
# poolName
####################################################################
sub getPoolName {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{poolName} ;
}
sub setPoolName {
    my ($self,$plan, $name) = @_;
    $self->{cfgs}{$plan}{poolName} ="$name";
}


####################################################################
# query
#   The procedure which is run to gather the application
#   required number of agents.  This is the application input
#   into the equation and may not be met based on other factors
#   returns project,procedure as array
#
####################################################################
sub getQueryProc {
    my ($self,$plan) = @_;
    my $proj = $self->{cfgs}{$plan}{query}{Proj} ;
    my $proc = $self->{cfgs}{$plan}{query}{Proc} ;
    return ($proj,$proc);
}
sub setQueryProc {
    my ($self,$plan, $proj, $proc) = @_;
    $self->{cfgs}{$plan}{query}{Proj} = "$proj" ;
    $self->{cfgs}{$plan}{query}{Proc} = "$proc";
}

sub setQueryCfgParam {
    my ($self,$plan, $name, $value) = @_;
    $self->setCfgParam($plan, "query","queryCfg", $name, $value);
    return;
}

sub getQueryCfgParam {
    my ($self,$plan, $name) = @_;
    return $self->getCfgParam($plan, "query","queryCfg",$name);
}
sub delQueryCfgParam {
    my ($self,$plan, $name) = @_;
    return $self->delCfgParam($plan, "query","queryCfg",$name);
}


# params are in an array of hashes so they take some special handling
sub setCfgParam($$$$$) {
    my ($self,$plan, $type, $subType, $param, $value) = @_;
    my $cfg = $self->{cfgs}{$plan}{$type}{$subType};
    foreach my $h (@ {$cfg} ) {
        if ($h->{name} eq "$param") {
            $h->{value} = "$value";
            return;
        }
    }
    # did not find it, so add to array
    push @{$self->{cfgs}{$plan}{$type}{$subType}}, { name=> $param, value=>$value};
    return;
}

# params are in an array of hashes so they take some special handling
sub getCfgParam($$$$) {
    my ($self,$plan, $type, $subType, $param) = @_;

    my $cfg = $self->{cfgs}{$plan}{$type}{$subType};
    foreach my $h (@ {$cfg} ) {
        if ($h->{name} eq "$param") {
            return $h->{value} ;
        }
    }
    return "";
}

# get all the params
sub getAllCfgParams($$$$) {
    my ($self,$plan, $type, $subType, $param) = @_;

    my %result;
    my $cfg = $self->{cfgs}{$plan}{$type}{$subType};
    foreach my $h (@ {$cfg} ) {
        $result{$h->{name}} = $h->{value} ;
    }
    return %result;
}

sub delCfgParam {
    my ($self,$plan, $type, $subType,  $param) = @_;

    my $cfg = $self->{cfgs}{$plan}{$type}{$subType};
    my $index=0;
    foreach my $h (@ {$cfg} ) {
        if ($h->{name} eq "$param") {
            splice(@{$self->{cfgs}{$plan}{$type}{$subType}}, $index, 1);
        }
        $index++;
    }
}

####################################################################
# adjustment
#   The procedure which is run to adjust the deployments
####################################################################
sub getGrowProc {
    my ($self,$plan) = @_;
    my $plugin = $self->{cfgs}{$plan}{adjust}{plugin} ;
    if ($plugin eq "") {
        return ("","");
    }
    my $proj = $self->getProp("/plugins/$plugin/project/projectName");
    my $proc = "CloudManagerGrow";
    return ($proj,$proc);
}

sub getShrinkProc {
    my ($self,$plan) = @_;
    my $plugin = $self->{cfgs}{$plan}{adjust}{plugin} ;
    if ($plugin eq "") {
        return ("","");
    }
    my $proj = $self->getProp("/plugins/$plugin/project/projectName");
    my $proc = "CloudManagerShrink";
    return ($proj,$proc);
}

sub getSyncProc {
    my ($self,$plan) = @_;
    my $plugin = $self->{cfgs}{$plan}{adjust}{plugin} ;
    if ($plugin eq "") {
        return ("","");
    }
    my $proj = $self->getProp("/plugins/$plugin/project/projectName");
    my $proc = "CloudManagerSync";
    return ($proj,$proc);
}


sub setAdjustProc {
    my ($self, $plan, $plugin) = @_;
    $self->{cfgs}{$plan}{adjust}{plugin} = "$plugin";
}
sub setGrowCfgParam {
    my ($self, $plan, $name, $value) = @_;
    $self->setCfgParam($plan,"adjust", "growCfg", $name,$value);
}
sub getGrowCfgParam {
    my ($self, $plan, $name) = @_;
    return $self->getCfgParam($plan, "adjust", "growCfg",$name);
}
sub delGrowCfgParam {
    my ($self, $plan, $name) = @_;
    return $self->delCfgParam($plan, "adjust", "growCfg",$name);
}

sub setShrinkCfgParam {
    my ($self, $plan, $name, $value) = @_;
    $self->setCfgParam($plan, "adjust", "shrinkCfg",$name, $value);
}
sub getShrinkCfgParam {
    my ($self, $plan, $name) = @_;
    return $self->getCfgParam($plan, "adjust", "shrinkCfg", $name);
}
sub delShrinkCfgParam {
    my ($self, $plan, $name) = @_;
    return $self->delCfgParam($plan, "adjust", "shrinkCfg", $name);
}



####################################################################
# COST DATA
####################################################################

####################################################################
# COST_PERIOD
#    Number of hours within which COST_MAX cannot be exceeded.
#    Default 728 (4.33 weeks or about a month)
#    Minimum of one day
####################################################################
sub getCostPeriod {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{cost}{COST_PERIOD}  || 728;
}
sub setCostPeriod {
    my ($self,$plan, $hours) = @_;
    if ($hours lt 24) { $hours = 24;}
    $self->{cfgs}{$plan}{cost}{COST_PERIOD} = "$hours";
}

####################################################################
# COST_MAX:
#    The maximum number of total resource hours per COST_PERIOD
#    allowed. At each check the COST_MAX is evaluated against
#    COST_TOTAL. If COST_TOTAL  >= COST_MAX all running resources
#    are terminated and no more are started until the start of the
#    next COST_PERIOD.
####################################################################
sub getCostMax {
    my ($self,$plan) = @_;
    return $self->{cfgs}{$plan}{cost}{COST_MAX};
}
sub setCostMax {
    my ($self,$plan, $days) = @_;
    if ($days lt 0) { $days = 0;}
    $self->{cfgs}{$plan}{cost}{COST_MAX} = "$days";
}

####################################################################
#  Time of Day rules
#   time of day limits
####################################################################
sub getTODMin {
    my ($self,$plan,$hour) = @_;
    return $self->{cfgs}{$plan}{tod}{$hour."-min"} || 0;
}
sub setTODMin {
    my ($self,$plan, $hour, $min) = @_;
    if ($hour < 0 || $hour > 23 ) {
        $hour = 0;
    }
    $self->{cfgs}{$plan}{tod}{$hour."-min"} = "$min";
}
sub getTODMax {
    my ($self,$plan,$hour) = @_;
    return $self->{cfgs}{$plan}{tod}{$hour."-max"} || 0;
}
sub setTODMax {
    my ($self,$plan, $hour, $max) = @_;
    if ($hour < 0 || $hour > 23 ) {
        $hour = 0;
    }
    $self->{cfgs}{$plan}{tod}{$hour."-max"} = "$max";
}

####################################################################
# Configuration management
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

sub loadAllCfgs {
    my ($self) = @_;

    my @plans = $self->getAllPlanNames();
    foreach my $plan (@plans) {
        $self->loadCfg($plan);
    }
}

sub loadCfg($){
    my ($self, $plan) = @_;
    if ($plan eq "") {
        print "error: no plan name for loadCfg\n";
        return "";
    }
    my $jcfg = $self->getJSON($plan);
    my $pcfg = $::json->decode($jcfg);
    $self->{cfgs}{$plan} = $pcfg;
    $self->setName($plan);
}

sub commitPlans {
    my ($self) = @_;

    my @plans;
    foreach my $plan (keys %{$self->{cfgs}}) {
        push @plans, $plan;
    }
    foreach my $plan (@plans) {
        $self->writeCfg($plan);
    }

}

sub writeCfg($) {
    my ($self,$plan) = @_;
    if ($plan eq "") {
        print "error: no plan name for writeCfg\n";
        return "";
    }
    my $jcfg = $self->encodeOnePlan($plan);
    $self->setJSON($plan,$jcfg);
}

sub encodeOnePlan($) {
    my ($self, $plan) = @_;
    my $pcfg = $self->{cfgs}{$plan};
    $self->setName($plan);
    my $jcfg =  $::json->encode($pcfg);
    return $jcfg;
}


####################################################################
# get one plan
####################################################################
sub getOnePlan($) {
    my ($self,$plan) = @_;

    return $self->encodeOnePlan($plan);
}

####################################################################
# get all plans
####################################################################
sub getAllPlans() {
    my ($self) = @_;

    my @plans;
    foreach my $plan (keys % {$self->{cfgs}}) {
        $self->setName($plan);
        push @plans, $self->{cfgs}{$plan};
    }
    return @plans;
}

####################################################################
# get all data in one shot for UI config screen
####################################################################
sub getUICfgData() {
    my ($self) = @_;
        
    my %cfg;
    my @plans   = $self->getAllPlans();
    my @plugins = $self->getCloudManagerPlugins();
    $cfg{cfg} = \@plans;
    $cfg{plugins} = \@plugins;
    my $jcfg = $::json->encode(\%cfg);
}

####################################################################
# import a plan to cache 
#   jplan -  a single plan in json
#
#   recuse the hash tree and save all
#
####################################################################
sub importOnePlan($$) {
    my ($self,$plan,$jplan) = @_;

    if ($plan eq "") { 
        print "error: no plan name found in importOnePlan\n";
        return;
    }
    # convert json to perl 
    my $pcfg = $::json->decode($jplan);
    #in case the hash does not contain a tod hash we'll need to create an empty one.
    if(!defined($pcfg->{tod})){
        $pcfg->{tod} = getEmptyLimit();
    }
    # set cache
    $self->{cfgs}{$plan} = $pcfg;
}

####################################################################
# returns a new hash of empty limits
####################################################################
sub getEmptyLimit{
    my  %tod = (
        'tod' => {
            "18-min"=> "0",
            "15-max"=> "0",
            "13-min"=> "0",
            "6-min"=> "0",
            "14-min"=> "0",
            "2-min"=> "0",
            "0-min"=> "0",
            "5-min"=> "0",
            "17-min"=> "0",
            "0-max"=> "0",
            "11-min"=> "0",
            "19-max"=> "0",
            "6-max"=> "0",
            "9-max"=> "0",
            "17-max"=> "0",
            "7-min"=> "0",
            "20-min"=> "0",
            "3-max"=> "0",
            "11-max"=> "0",
            "16-min"=> "0",
            "10-max"=> "0",
            "15-min"=> "0",
            "8-min"=> "0",
            "22-max"=> "0",
            "2-max"=> "0",
            "23-max"=> "0",
            "13-max"=> "0",
            "7-max"=> "0",
            "21-min"=> "0",
            "5-max"=> "0",
            "14-max"=> "0",
            "9-min"=> "0",
            "16-max"=> "0",
            "1-max"=> "0",
            "22-min"=> "0",
            "18-max"=> "0",
            "12-min"=> "0",
            "4-min"=> "0",
            "20-max"=> "0",
            "19-min"=> "0",
            "8-max"=> "0",
            "10-min"=> "0",
            "12-max"=> "0",
            "4-max"=> "0",
            "23-min"=> "0",
            "21-max"=> "0",
            "1-min"=> "0",
            "3-min"=> "0",
        }
    );
    return $tod{tod};
}

####################################################################
####################################################################
# utilities to help manage configurations
####################################################################
sub makeParam($$) {
    my ($name, $obj) =@_;
    return {
        parameterName => "$name",
        required => "$obj->{req}",
        description => "$obj->{desc}",
        default => "$obj->{def}"
    };
}

sub getCloudManagerPlugins() {
    my ($self) = @_;

    my @plugins;

    my $xPath = $self->getCmdr()->getPlugins();
    my $nodeset = $xPath->find("//plugin");
    foreach my $node ($nodeset->get_nodelist) {
        my $foundGrow=0;
        my $foundShrink=0;

        my $promoted =  $node->findvalue('promoted');
        if ($promoted ne "1") { next; }

        my $name =  $node->findvalue('pluginKey');
        my $proj =  $node->findvalue('projectName');

        my $growParams;
        my $shrinkParams;
        my $syncParams;;
        # look for grow
        $growParams = $self->existsProc($proj, "CloudManagerGrow", ("number","poolName") );
        if ($growParams eq "") {
            next;
        }
        $shrinkParams = $self->existsProc($proj, "CloudManagerShrink", ("deployments") );
        if ($shrinkParams eq "") {
            next;
        }
        $syncParams = $self->existsProc($proj, "CloudManagerSync", ("deployments") );
        if ($syncParams eq "") {
            next;
        }
        # this plugin meets the criteria

        my $plugin = {name => "$name"};
        foreach my $p (grep(!/^(number|poolName)$/, keys %{$growParams})) {
            push @{$plugin->{growParams}}, makeParam($p, $growParams->{$p});
        }
        foreach my $p (grep(!/^deployments$/, keys %{$shrinkParams})) {
            push @{$plugin->{shrinkParams}}, makeParam($p, $shrinkParams->{$p});
        }
        push @plugins, $plugin;
    }
    return @plugins;
}

sub existsProc() {
    my ($self, $proj, $proc, @required) = @_;

    # look for procedure with certain params
    my $xproc = $self->getCmdr()->getFormalParameters("$proj",{procedureName =>"$proc"});
    if (!$xproc) { return 0; }
    my $code = $xproc->findvalue('//code');
    if ($code eq "NoSuchProcedure") {
       return "";
    }

    my $formals;
    my $paramset = $xproc->find('//formalParameter');
    foreach my $paramnode ($paramset->get_nodelist) {
        my $name  =  $paramnode->findvalue('formalParameterName');
        my $req   =  $paramnode->findvalue('required');
        my $desc  =  $paramnode->findvalue('description');
        my $def   =  $paramnode->findvalue('defaultValue');
        $formals->{$name}{req}  = "$req";
        $formals->{$name}{desc} = "$desc";
        $formals->{$name}{def}  = "$def";
    }
    foreach my $p (@required) {
        if (defined $formals->{$p}{req} && $formals->{$p}{req} eq "1") {
            next;
        } else {
            return "";
        }
    }
    return $formals;
}

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

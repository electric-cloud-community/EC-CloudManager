package main;

###
### TEST DATA FOR EC-CloudManager
###
### Use with EC-PCE provider

$| = 1;

use JSON;
use ElectricCommander;
use ElectricCommander::PropDB ;
use ElectricCommander::PropMod ;

$::json = JSON->new->allow_nonref;

$::ec = new ElectricCommander();
$::ec->abortOnError(0);
$::pdb = new ElectricCommander::PropDB($::ec,"");

print "COMMANDER_PLUGIN_PERL=$ENV{COMMANDER_PLUGIN_PERL}\n";
if ("$ENV{COMMANDER_PLUGIN_PERL}" ne "") {
    # during tests
    print "Test INC Path\n";
    push @INC, "$ENV{COMMANDER_PLUGIN_PERL}";
} else {
    # during production
    push @INC, "$ENV{COMMANDER_PLUGINS}/@PLUGIN_NAME@/agent/perl";   
}
require CMDeployment;
require CMLog;
require CMResPlan;
require CMStats;

$::gCMProject = "/plugins/EC-CloudManager/project";
$ENV{COMMANDER_CMRESPLAN_PROJECT} = $::gCMProject;
$::ep = new CMResPlan($::ec);
$::st = new CMStats($::ec);

print "Loading test data ...\n";
## build up values for testing
addTestCfg("default");
addTestCfg("two");
addTestCfg("three");

print "Testing getUICfgData..\n";
my $jcfg = $::ep->getUICfgData();
print "$jcfg\n";

# change some values
$::ep->delQueryCfgParam("default","foo");

print "Commit plans\n";
$::ep->commitPlans();

## UI FUNCTIONS
$::ep2 = new CMResPlan($::ec);
print "Load plans\n";
$::ep2->loadAllCfgs() ;

my $jcfg = $::ep2->getOnePlan("default");


# rename a new plan
$jcfg =~ s/default/foo/;
$::ep2->modifyPlan("default",$jcfg);
$jcfg = $::ep2->getOnePlan("foo");

# modify a plan
$::ep2->modifyPlan("foo",$jcfg);

# disable all but foo
$::ep2->setActive("foo",1);
$::ep2->setActive("two",0);
$::ep2->setActive("three",0);
$::ep2->commitPlans();


### Now test Logs...
print "Removing all log records\n";
$::ec->deleteProperty($ENV{"COMMANDER_CMRESPLAN_PROJECT"} . "/tables/LOG");
$::log = new CMLog($::ec);
my $plan = "foo";
my $agelimit = 30;  # in days
my $now = time();
my $cutoff = $now - ($agelimit*24*60*60);

my $numlogs = 8640;
print "Adding $numlogs samples\n";
my $req = 5;
my $target = 5;
for (my $i=0; $i< $numlogs; $i++) {
    if ($i % 3 == 0) {
        #$target = int(rand(3)) + 5;
        $target = int((rand(10)+rand(10)+rand(10))/3);
    }
    if ($i % 2 == 0) {
        $req = int((rand(10)+rand(10)+rand(10))/3);
    }
    if ($i % 5 == 0) {
        if ($target > $req) {
            $target = int($req / 2);
        }
    }
    # start records 5 seconds before trim cutoff
    $::log->newLogRec($plan, 0,$req,$target,2,10,3,0.2, $cutoff + ($i - 5)*300);
}
$::log->commitLog($plan);

countLogRecords($plan,$numlogs);
$::log->trimRecs($plan,$cutoff);

print "After trim\n";
countLogRecords($plan,$numlogs-5);

print "Limit Report Day\n";
my $jreport = $::log->getLimitReport($plan,"day") . "\n";
print "$jreport\n";
#print "Validate JSON\n";
#my %preport = $::json->decode($jreport);
#print Dumper->($preport) . "\n";

print "Limit Report Week\n";
print $::log->getLimitReport($plan,"week") . "\n";
print "Limit Report Month\n";
print $::log->getLimitReport($plan,"month") . "\n";

print "Commit\n";
$::log->commitLog($plan);

### Now test Deployments
print "Removing all deployment records\n";
$::ec->deleteProperty($ENV{"COMMANDER_CMRESPLAN_PROJECT"} . "/tables/DEPLOYMENTS");
$::dep = new CMDeployment($::ec);
my $plan = "foo";
my $numdeps = 100;
print "Adding $numdeps samples\n";
for (my $i=0; $i< $numdeps; $i++) {
    # start records 5 seconds before trim cutoff
    my $cust;
    $cust->{foo} = "bar";
    $cust->{bar} = "baz";
    $cust->{resource} = "resource-$i";
    $::dep->newDepRec($plan, "i-fake$i",$cust,$cutoff +$i - 5);
}
$::dep->commitDep($plan);
countDepRecords($plan,$numdeps,"alive");
$::dep->trimRecs($plan,$cutoff);
print "After trim and all recs still running\n";
countDepRecords($plan,$numdeps,"alive");

# finish deployments
$now = time();
for (my $i=0; $i< $numdeps; $i++) {
    # start records 5 seconds before trim cutoff
    $::dep->endDepRec($plan, "i-fake$i","stop");
    $::dep->setDepRecValue($plan, "i-fake$i","stop", $now);
}
$::dep->trimRecs($plan,$cutoff,"stopped");

print "After trim and all recs stopped\n";
countDepRecords($plan,$numdeps-5,"stopped");
$::dep->commitDep($plan);

print "Deployment UI data\n";
print $::dep->getUIDeploymentList("foo") . "\n";

exit 0;


sub addTestCfg() {
    my ($plan) = @_;
    $::ep->setName($plan);
    $::st->setName($plan);

    $::ep->setDebug($plan,"4");

    $::ep->setDescription($plan,"Default plan for testing");

    $::st->setCostConsumed($plan,0);
    $::ep->setCostMax($plan,272);
    $::ep->setCostPeriod($plan,728);
    $::st->setCostStart($plan,0);

    $::ep->setKillLimitMax($plan,58);
    $::ep->setKillLimitMin($plan,45);
    $::ep->setKillLimitPolicy($plan,"never");

    $::ep->setActive($plan,1);
    $::ep->setAdjustProc($plan,"EC-PCE");
    $::st->setAppRequest($plan,0);
    $::st->setCurrent($plan,10);
    $::st->setBounded($plan,0);
    $::ep->setPoolName($plan,"CloudTest");
    $::ep->setQueryProc($plan,$::gCMProject,"DummyQuery");

    $::ep->setDebug($plan,4) ;
    for(my $hour = 0; $hour < 24; $hour++) {
        $::ep->setTODMax($plan,$hour,10);
        $::ep->setTODMin($plan,$hour,2);
    }

    $::ep->setGrowCfgParam($plan,"pce_config", "test");
    $::ep->setGrowCfgParam($plan,"pce_security_group","testgroup");
    $::ep->setGrowCfgParam($plan,"pce_image","ami-123456");
    $::ep->setGrowCfgParam($plan,"pce_device","/dev/sdh");
    $::ep->setGrowCfgParam($plan,"pce_zone","us-east-1b");
    $::ep->setGrowCfgParam($plan,"pce_instance_type","m1.small");
    $::ep->setGrowCfgParam($plan,"pce_security_group","testgroup");

    $::ep->setShrinkCfgParam($plan,"pce_config","test");


    $::ep->setQueryCfgParam($plan,"foo","foo");
    $::ep->setQueryCfgParam($plan,"goo","goo");
    $::ep->setQueryCfgParam($plan,"goo","goo");
}

sub manual() {
    my ($t) = @_;
    print "$t:";
    my $a = <STDIN>;
    return $a;
}

sub countLogRecords {
    my ($plan, $expected) = @_;
    my $num=0;
    my @recs = $::log->getLogRecs($plan);
    foreach my $r (@recs) {
        $num++;
    }
    if ($num == $expected) { 
        print "Found $num samples\n";
    } else {
        print "error: $num samples found, $expected expected\n";
    }
}

sub countDepRecords {
    my ($plan, $expected,$state) = @_;
    my $num=0;
    my @recs = $::dep->getDepRecs($plan,$state);
    foreach my $r (@recs) {
        $num++;
    }
    if ($num == $expected) { 
        print "Found $num samples in state $state\n";
    } else {
        print "error: $num samples found, $expected expected\n";
    }
}

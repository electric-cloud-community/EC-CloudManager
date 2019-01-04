#!/bin/sh
# -*- Perl -*-

exec "$COMMANDER_HOME/bin/ec-perl" -x "$0" "${@}"

#!perl
#
#-------------------------------------------------------------------------
# CloudService.cgi --
#
#-------------------------------------------------------------------------
#
# Copyright (c) 2011 Electric Cloud, Inc.
# All rights reserved

use strict;
use warnings;
use CGI;
use CGI::Carp qw(fatalsToBrowser);
use JSON;
use ElectricCommander;
use ElectricCommander::PropDB;
use ElectricCommander::PropMod;

my $project = '/plugins/EC-CloudManager/project';

my $ec = new ElectricCommander({abortOnError => 0});

if ($ENV{COMMANDER_PLUGIN_PERL}) {
    # during tests
    push @INC, $ENV{COMMANDER_PLUGIN_PERL};
} elsif ($ENV{COMMANDER_PLUGINS}) {
    # during production
    my $pluginName = "@PLUGIN_NAME@";
    push @INC, "$ENV{COMMANDER_PLUGINS}/$pluginName/agent/perl/lib";
}

require CMDeployment;
require CMLog;
require CMResPlan;
require CMStats;

# Extract the request parameters
my $query = new CGI;
$query->parse_params($ENV{'QUERY_STRING'});
my $action = $query->param("action") || "";
my $data = $query->param("POSTDATA") || "";

my $err;

sub okHeader {
    return $query->header(-type => 'application/json',
                     -expires => 'now',
                     -charset => 'utf-8');
}
sub fail {
    my $msg = shift;
    print $query->header('text/plain', '400 Bad Request');
    print $msg;
}

if ($action eq "getPlans") {
    my $cm = new CMResPlan($ec,$project);
    $cm->loadAllCfgs();
    print okHeader();
    print $cm->getUICfgData();

} elsif ($action eq "getPlan") {
    my $name = $query->param("name");

    my $cm = new CMResPlan($ec,$project);
    $cm->loadAllCfgs();
    print okHeader();
    print $cm->getOnePlan($name);

} elsif ($action eq "deletePlan") {
    my $name = $query->param("name");
    my $cm = new CMResPlan($ec, $project);
    $cm->loadAllCfgs();
    $cm->deletePlan($name);
    my $st = new CMStats($ec, $project);
    $st->loadAllStats();
    $st->deleteStats($name);
    print okHeader();

} elsif ($action eq "copyPlan") {
    my $name = $query->param("name"); 
    my $cm = new CMResPlan($ec, $project);
    $cm->loadAllCfgs();
    my $count = 2;
    my $count_flag = 0;
    ## check name doesn't contain 'copy' at the end
    if ($name !~ m/^.*\scopy.*$/ixms) {$name .= " copy";}
    if ($cm->existsPlan($name)) {$count_flag = 1;}
    ## extract count from name
    if ($name =~ m/^(.*\scopy)\s([\d]+)$/ixms) {       
        $name = $1;
        $count = $2;
    }
    until (!$cm->existsPlan("$name $count")) {
    $count++;
    }
    if($count_flag) {$name .= " $count";}
    $cm->importOnePlan($name,$data);
    $cm->writeCfg($name);
    print okHeader();  

} elsif ($action eq "modifyPlan" ) {
    my $name = $query->param("name");
    my $cm = new CMResPlan($ec, $project);
    $cm->loadAllCfgs();
    $cm->modifyPlan($name,$data);
    print okHeader();

} elsif ($action eq "createPlan") {
    my $name = $query->param("name");
    my $cm = new CMResPlan($ec, $project);
    $cm->loadAllCfgs();
    if ($cm->existsPlan($name) ) {
        # it exists already
        fail("Plan $name already exists\n\n$data");
        return;
    }
    $cm->importOnePlan($name,$data);
    $cm->writeCfg($name);
    print okHeader();

} elsif ($action eq "limitReportData") {
    my $name = $query->param("name");
    my $range = $query->param("range") || "day";
    my $lm = new CMLog($ec, $project);
    print okHeader();
    print $lm->getLimitReport($name, $range);

} elsif ($action eq "getDeps") {
    my $name = $query->param("name");
    my $deps = new CMDeployment($ec, $project);
    print okHeader();
    print $deps->getUIDeploymentList($name);

} elsif ($action eq "restartUsage") {
    # set flag to reset usage counters
    my $name = $query->param("name");
    my $cm = new CMResPlan($ec, $project);
    $cm->loadCfg($name);
    $cm->setReset($name,"1");
    $cm->writeCfg($name);
    print okHeader();
} elsif ($action eq "setSchedule") {
    my $disabled = $query->param("disabled");
    my $minutes = $query->param("minutes");
    my $xPath = $ec->modifySchedule($project,"RunProcessPlans",
        {interval => $minutes,
         intervalUnits => "minutes",
         scheduleDisabled => $disabled});
    print okHeader();
} elsif ($action eq "getSchedule") {
    $::json = JSON->new->allow_nonref;
    my $xPath = $ec->getSchedule($project,"RunProcessPlans");
    my $disabled = $xPath->findvalue("//response/schedule/scheduleDisabled")->string_value;
    my $minutes  = $xPath->findvalue("//response/schedule/interval")->string_value;
    my $hash;
    $hash->{disabled} = $disabled;
    $hash->{minutes} = $minutes;
    my $j = $::json->encode($hash);
    print okHeader();
    print $j;
} else {
    fail("Unknown command: $action\n\n$data");
}

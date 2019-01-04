$[/myProject/perl/perlObjects.pm]

use strict;
use POSIX qw(ceil floor);


##
## Process Resource Plans
##


$|=1;
$::gDebug = $::pdb->getProp("/myProject/debug") || "1";

sub main {
    # create data objects and load values
	$::rps = new CMResPlan($::ec);
    $::rps->loadAllCfgs();

	$::sts = new CMStats($::ec);
    $::sts->loadAllStats();

    $::log = new CMLog($::ec);
    $::log->loadAllLogs();

    $::dep = new CMDeployment($::ec);
    $::dep->loadAllDeps();

	my @plans = $::rps->getAllPlanNames();
	
    # for each plan
    foreach my $plan(@plans) {
        output(1,"==========Checking plan $plan ================");
        if (!$::rps->getActive($plan) ) { 
            output(1,"Skipping plan $plan");
            next; 
        }
        $::gDebug = $::rps->getDebug($plan);
        output(1,"Processing plan $plan");
        syncDeployments($plan);
        terminatePending($plan);
        updateCurrentUsage($plan);
        getApplicationRequest($plan);
        calculateLimits($plan);
        adjustDeployments($plan);
        trimRecords($plan);
    }

    # save all changes
    $::rps->commitPlans();
    $::sts->commitStats();
    $::log->commitLogs();
    $::dep->commitDeps();
}

##############################################################################
# terminatePending
#
# Give each plugin a chance to clean up pending resources
# Resources are not killed right away when a shrink action is taken,
# the resources are removed from any pool so no other jobs are scheduled and then
# the Cloud Manager marks them as pending. When this procedure runs
# each plugin gets a chance to find pending resources and if there is not anything
# running on them anymore it is actually deleted.
# 
##############################################################################
sub terminatePending {
    my ($plan) = @_;

    output(2, "Terminate pending items for plan [$plan]");
    my ($proj,$proc) = $::rps->getShrinkProc($plan);

    # get pending deployments
    $::dep->loadDep($plan);
    my @handles = $::dep->getDepRecs($plan,"pending");
    my $count = scalar @handles;
    output(2, "Found $count pending deployments for $plan");
    if ($count <= 0) { return; }

    my $xml = "";
    $xml .= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    $xml .= "<ShrinkRequest>";
    # for each deployment record
    foreach my $handle (@handles) {
        output(4, "...deployment $handle");
        $xml .= "  <Deployment>";
        my $storedParams = $::dep->getDepRec($plan,$handle);
        # get parameters stored in plan
        output(5, "...parameters stored in config");
        foreach my $sp (keys % { $storedParams } ) {
            output(5,"   $sp=$storedParams->{$sp}");
            $xml .=  "    <$sp>" . $storedParams->{$sp} . "</$sp>";
        }
        $xml .= "  </Deployment>";
    }
    $xml .= "</ShrinkRequest>";
    my $required;
    my $required;
    $required->{deployments} = "$xml";
    callShrink($plan,$proj,$proc,$required);
    $::dep->commitDep($plan);
    return;
}

##############################################################################
# syncDeployments
#
# This function constructs a list of deployments considered active
# for this plan so the resource plugin can tell us if it is still running
#
# Next it processes a return list to mark as pending
# 
##############################################################################
sub syncDeployments {
    my ($plan) = @_;

    output(2, "Sync running deployments for plan [$plan]");
    my ($proj,$proc) = $::rps->getSyncProc($plan);

    # get alive deployments
    $::dep->loadDep($plan);
    my @handles = $::dep->getDepRecs($plan,"alive");
    my $count = scalar @handles;
    output(2, "Found $count alive  deployments for $plan");
    if ($count <= 0) { return; }

    my $xml = "";
    $xml .= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    $xml .= "<SyncRequest>";
    # for each deployment record
    foreach my $handle (@handles) {
        output(4, "...deployment $handle");
        $xml .= "  <Deployment>";
        my $storedParams = $::dep->getDepRec($plan,$handle);
        # get parameters stored in plan
        output(5, "...parameters stored in config");
        foreach my $sp (keys % { $storedParams } ) {
            output(5,"   $sp=$storedParams->{$sp}");
            $xml .=  "    <$sp>" . $storedParams->{$sp} . "</$sp>";
        }
        $xml .= "  </Deployment>";
    }
    $xml .= "</SyncRequest>";
    my $required;
    my $required;
    $required->{deployments} = "$xml";
    callSync($plan,$proj,$proc,$required);
    $::dep->commitDep($plan);
    return;
}

##############################################################################
# updateCurrentUsage
#
# Calculate how many machines are currently deployed
# by examining the deployment list. This assumes that the list is kept
# in sync by the cloud "adjustDeployments" method
#
##############################################################################
sub updateCurrentUsage {
    my ($plan) = @_;

    output(1,"Updating usage for plan [$plan]");
    my $now = time();

	my %Metrics;

    # get cost times
    my $cost_start  = $::sts->getCostStart($plan);
    my $cost_period = $::rps->getCostPeriod($plan);

    # if reset counters flag set from UI
    # reset all usage to 0 and start new period
    my $resetFlag = $::rps->getReset($plan);
    if ($resetFlag eq "1") {
        $::rps->setReset($plan,"0");
        $cost_start = "";
    }
    
    # if none set or time passed, start over
    # there will be some time not accounted for 
    # since we just reset to now. the error will be
    # larger the less often this script runs. it
    # is assumed the script will run a few times an hour
    # which should make any gaps negligible
    if (!defined $cost_start or $cost_start eq "" or ($cost_start + ($cost_period * 60 * 60)) < $now) {
        # start is not aligned to any particular boundary 
        output(1, "Setting start of new cost period to now");
        $cost_start = $now;
        $::sts->setCostStart($plan,$cost_start);
        $::sts->setCostConsumed($plan,0);
        $::sts->setCostConsumedTerminated($plan,0);
    }
    $Metrics{$plan}{cost_start} = $cost_start;
    $Metrics{$plan}{running} = 0;
    $Metrics{$plan}{cost} = 0;

    # we are about to calculate the usage of running instances
    # first add in the amount of usage from instances that have been terminated
    my $old_instance_consumed = $::sts->getCostConsumedTerminated($plan);
    $Metrics{$plan}{cost} += $old_instance_consumed;

    # Get list of all deployment records
    $::dep->loadDep($plan);
    my @handles = $::dep->getDepRecs($plan,"alive");
    my $count = scalar @handles;
    output(1, "Found $count active deployments for $plan");

    # for each deployment record
    foreach my $handle (@handles) {
        output(5, "...deployment $handle");
        my $rec = $::dep->getDepRec($plan,$handle);
        my $start = $rec->{start};
        $Metrics{$plan}{running} += 1;
            
        # add usage in hours to total cost
        # in the current cost period
        my $usageStart = $start; 
        if ($usageStart < $Metrics{$plan}{cost_start}) {
            $usageStart = $Metrics{$plan}{cost_start};
        }
        my $usageStop = $now;
        my $consumed = ($usageStop - $usageStart) / (3600);
        $Metrics{$plan}{cost} += $consumed;
        output (5, "$handle now=$now start=$start usageStart=$usageStart consumed= $consumed");
    }    
    output(1,"Currently running in $plan = $Metrics{$plan}{running}");
    output(1,"Currently consumed in $plan = $Metrics{$plan}{cost}");
    $::sts->setCurrent($plan,$Metrics{$plan}{running});    
    $::sts->setCostConsumed($plan,$Metrics{$plan}{cost});
    $::dep->commitDep($plan);
}

##############################################################################
# getApplicationRequest
#
# Find out how many nodes the application would like to have. 
# 
##############################################################################
sub getApplicationRequest {
    my ($plan) = @_;
    
    
    output(1, "Getting application requirements for plan [$plan]");

    # get the proj/proc that will give us the answer
    my ($proj,$proc) = $::rps->getQueryProc($plan);
    output(2, "Query procedure: $proj/$proc");

    my $required;
    $required->{current}  = $::sts->getCurrent($plan);
    $required->{poolName} = $::rps->getPoolName($plan);

    my $jobId = runHookProcedure($plan, $proj,$proc,
        "query","queryCfg", $required);

    if ($jobId == 0) {
        output(0,"Query hook procedure failed.");
        next;
    }
    output(4,"Query job=$jobId");
    my $req = $::rps->getProp("/jobs/$jobId/CloudManager/query");

    # Change by Avan Mathur 4/2013
    # Create a list of deadResources based on return from Query procedure
    my $xmlout = $::rps->getProp("/jobs/$jobId/CloudManager/deadResources");
    my $xPath = XML::XPath->new( xml => $xmlout);
    my $nodeset = $xPath->find('//Resource');
    my @deadResources = ();
    foreach my $node ($nodeset->get_nodelist) {
        push(@deadResources, $xPath->findvalue('resourceName', $node)->string_value);
    }
    if ( $#deadResources >= 0) {
        output(0, "calling terminateDeadResources");
        terminateDeadResources($plan, \@deadResources);
    }


    # record request
    output(2,"Setting application request to $req");
    $::sts->setAppRequest($plan,$req); # set in procedure
}

##############################################################################
# terminateDeadResources
#
# If the Application Query returns a list of dead resources.  Check if those
# resources are CloudManager deployments.  If they are, terminate them by
# calling callShrink()
# 
# Added by Avan Mathur 4/2013
##############################################################################
sub terminateDeadResources{

    my $plan = $_[0];
    my @resources = @{$_[1]};
    my $xml = "";
    $xml .= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    $xml .= "<ShrinkRequest>";
    
    my $count = 0;

    my @handles = $::dep->getDepRecs($plan, "alive");
    my %storedParamsForCMResource = ();
    my $storedParams;
    foreach my $handle (@handles) {
        $storedParams = $::dep->getDepRec($plan, $handle);
        my $cmResource = $storedParams->{"Resource"};
        $storedParamsForCMResource{$cmResource} = $storedParams;
    }

    # Add dead resource to shrink list if it is a CloudManager deployment
    foreach my $resourceName (@resources) {
        if (!exists($storedParamsForCMResource{$resourceName})) {
            next;
        }

        $storedParams = $storedParamsForCMResource{$resourceName};
        $xml .= "  <Deployment>";
        foreach my $sp (keys % { $storedParams } ) {
            output(5,"   $sp=$storedParams->{$sp}");
            $xml .=  "    <$sp>" . xmlQuote( $storedParams->{$sp} ) . "</$sp>";
        }
        $xml .= "  </Deployment>";
        $count++;
    }
    
    $xml .= "</ShrinkRequest>";
    if ($count > 0 ) {
        my $required;
        my ($proj,$proc) = $::rps->getShrinkProc($plan);
        $required->{deployments} = "$xml";
        output(0, "Calling shrink procedure on dead resources.");
        callShrink($plan,$proj,$proc,$required);
    }
}

##############################################################################
# callApplicationCleanup
#
#  If application provides cleanup procedure, call it after
#  shrinking resources
# 
##############################################################################
sub callApplicationCleanup {
    my ($plan,  $hostnames) = @_;
    
    output(1, "Call application cleanup for plan [$plan]");
    if ($hostnames eq "") {
        output(5, "No hosts to cleanup.");
        return;
    }

    # get the proj/proc that will give us the answer
    my ($proj,$proc) = $::rps->getQueryProc($plan);
    $proc = $proc . "Cleanup";
    output(2, "Cleanup procedure: $proj/$proc");

    #See if a cleanup proc exists
    if (! doesProcExist($proj, $proc)) {
        output(5, "No cleanup procedure.");
        return; 
    };

    my $required;
    $required->{hosts}  = $hostnames;

    my $jobId = runHookProcedure($plan, $proj,$proc,
        "query","queryCfg", $required);

    if ($jobId == 0) {
        output(0,"Cleanup hook procedure failed.");
        next;
    }

}

sub doesProcExist {
    my ($proj, $proc) = @_;

    #See if a cleanup proc exists
    my $xPath = $::ec->getProcedure($proj,$proc);
    # test success of procedure
    if (! defined $xPath) { 
        return 0; 
    }  
    my $code = $xPath->findvalue('//code')->string_value;
    if ($code ne "") {
        return 0;
    }   
    return 1;
}

##############################################################################
# calculateLimits
#
# Revise the application request by time of day and usage limits
#
##############################################################################
sub calculateLimits {
    my ($plan) = @_;

    output(1,"Calculating limits for plan [$plan]");

    # Get TOD limits
    my ($sec,$min, $hour, $mday, $mon, $year, $wday, $yday, $isdst) = localtime(time);
    my $tod_min = $::rps->getTODMin($plan,"$hour");
    my $tod_max = $::rps->getTODMax($plan,"$hour");
    output(3, "tod_max=$tod_max  tod_min=$tod_min  hour=$hour");

    # Get cost info
    my $cost_max       = $::rps->getCostMax($plan);
    my $cost_consumed  = $::sts->getCostConsumed($plan);    
    my $cost_period    = $::rps->getCostPeriod($plan);    
    my $cost_start     = $::sts->getCostStart($plan);
    if (!defined $cost_start or $cost_start eq "") {
        $cost_start = time();
        $::sts->setCostStart($plan,$cost_start);
    }
    output(3, "cost_max=$cost_max"
      . " cost_consumed=$cost_consumed" 
      . " cost_period=$cost_period" 
      . " cost_start=$cost_start");

    # Get current provision count
    my $current = $::sts->getCurrent($plan);

    # calculations
    my $time_since_period = floor((time() - $cost_start) / 60 / 60);  # in hours
    my $time_remaining = max( 1, $cost_period - $time_since_period);
    my $cost_remaining = max(0,$cost_max - $cost_consumed); 
    my $cost_limit = $cost_remaining;
    output(3, "current=$current"
      . " time_since_period=$time_since_period"
      . " time_remaining=$time_remaining" 
      . " cost_remaining=$cost_remaining");


    # Get application desired size  (fake)
    my $desired = $::sts->getAppRequest($plan);
    output(3, "desired=$desired");

    # calculate new provision count
    my $bounded = $desired;
    output(3, "desired: bounded=$desired");

    # not bigger than max
    $bounded =  min($desired, $tod_max); 
    output(3, "tod_max: min($desired,$tod_max) bounded=$bounded");

    # not smaller than min
    $bounded = max ($bounded, $tod_min);
    output(3, "tod_min: max($bounded,$tod_min) bounded=$bounded");

    # limit by cost limit   
    # assume each new will consume an hour before cost limits force shutdown
    $bounded = min ($bounded, $cost_limit);
    output(3, "cost_limit: min($bounded,$cost_limit) bounded=$bounded");

    # limit by cost remaining   
    # assume each new will consume an hour before cost limits force shutdown
    $bounded = min ($bounded, $cost_remaining);
    output(3, "cost_remaining: min($bounded,$cost_remaining) bounded=$bounded");

    output(2, "Setting plan $plan bounded to $bounded");
    $::sts->setBounded($plan,$bounded);

    my $activity = $bounded-$current;

    $::log->addLogRec($plan,$current, $desired,$bounded,$tod_min,$tod_max,$activity,$cost_remaining);

    output(3, "Log: cur=$current req=$desired max=$tod_max min=$tod_min"
        . " bounded=$bounded cost=$cost_remaining activity=$activity");
}

##############################################################################
# adjustDeployments
#
# Call the registered cloud routine to adjust deployments up or down
# 
##############################################################################
sub adjustDeployments {
    my ($plan) = @_;

    output(2, "Adjusting deployments for plan [$plan]");

    my $bounded = $::sts->getBounded($plan);
    my $current = $::sts->getCurrent($plan);
    my $diff = $bounded - $current;
    my $deplist = "";

    if ($diff == 0) {
        output(2,"Nothing to do for plan $plan");
        next;
    }
    
    # adjust the deployments
    if ($diff < 0) {
        adjustShrink($plan,$diff);
    }  else { 
        adjustGrow($plan, $diff);
    }
    $::dep->commitDep($plan);
}

##############################################################################
# trimRecords
#   age off older items from LOG and DEPLOYMENTS
##############################################################################
sub trimRecords {
    my ($plan) = @_;

    # only keep items for 30 days
    # TODO make this a configuable parameter
    my $cutoff = time() - (30*24*60*60);

    # roll off older items
    $::log->trimRecs($plan,$cutoff);
    $::dep->trimRecs($plan,$cutoff);
}

##############################################################################
# adjustGrow
#   Call the registered cloud routine to grow deployments
##############################################################################
sub  adjustGrow {
    my ($plan, $diff) = @_;
    
    # get list of parameters in given procedure
    my ($proj,$proc) = $::rps->getGrowProc($plan);
    output(2, "Adding $diff more using grow procedure: $proj/$proc");

    my $required;
    $required->{number} = "$diff";
    $required->{poolName} = $::rps->getPoolName($plan);
    my $jobId = runHookProcedure($plan, $proj,$proc,
        "adjust", "growCfg", $required);

    if ($jobId == 0) {
        output(0,"Grow hook procedure failed.");
        return;
    }
    output(4,"Grow job=$jobId");
    my $xmlout = $::rps->getProp("/jobs/$jobId/CloudManager/grow");
    output(5,"xml from grow");
    output(5,"$xmlout");

    # record deployments
    output(4,"Saving Grow Responses");
    output(4,$xmlout);
    $::dep->saveGrowResponse($plan,$xmlout);
}

##############################################################################
# adjustShrink
#   Call the registered cloud routine to shrink deployments
##############################################################################
sub  adjustShrink {
    my ($plan, $diff) = @_;

    # get adjustment procedure
    my ($proj,$proc) = $::rps->getShrinkProc($plan);
    output(2, "Ending $diff with shrink procedure: $proj/$proc");

    # tell adjust proc what to kill
    my $xmlRequest = pickInstancesToEnd($plan,$diff);
    if ("$xmlRequest" eq "") {
        output(0,"No instances found to shrink");
        return;
    }
    output(3,"Deployments to shrink:$xmlRequest");

    my $required;
    $required->{deployments} = "$xmlRequest";
    callShrink($plan,$proj,$proc, $required);
}

##############################################################################
# callShrink
#   Call the shrink command for either adjustShrink or terminatePending
##############################################################################
sub callShrink {
    my ($plan, $proj, $proc, $required) = @_;

    output(2,"Calling shrink hook procedure $proj/$proc for $plan.");
    my $jobId = runHookProcedure($plan, $proj,$proc,
        "adjust", "shrinkCfg", $required);

    if ($jobId == 0) {
        output(0,"Shrink hook procedure failed.");
        return;
    }
    output(4,"Shrink job=$jobId");
    my $xmlout = $::rps->getProp("/jobs/$jobId/CloudManager/shrink");
    if ($xmlout eq "") {
        output(0,"No return data from shrink job.");
        return;
    }
    output(4,"$xmlout");

    my $hosts = "";
    # process the results to see what really got killed
    my $xPath = XML::XPath->new( xml => "$xmlout");
    my $nodeset = $xPath->find('//Deployment');
    foreach my $node ($nodeset->get_nodelist) {
        my $handle = $xPath->findvalue('handle',$node)->string_value;
        my $result = $xPath->findvalue('result',$node)->string_value;
        my $mesg   = $xPath->findvalue('message',$node)->string_value;
        my $rec = $::dep->getDepRec($plan, $handle);
        output(4, "Call shrink results: $handle, $result, $mesg\n");

        # result = error | stopped | pending
        if ($result ne "error" ) {
            my $start = $rec->{start};
            my $now   = time();
            my $elapsed = ($now-$start) / 3600;

            my $consumed = $::sts->getCostConsumedTerminated($plan);
            $consumed += $elapsed;
            $::sts->setCostConsumedTerminated($plan,$consumed);
            $::dep->endDepRec($plan,$handle, $result);

            ## save list of hostnames for cleanup 
            $hosts .= $rec->{hostname};
            $hosts .= ";";


        } else {
            # record error
            output(0,"error shrinking $plan:$handle - $mesg");
            $::dep->setDepRecValue($plan,$handle,"mesg",$mesg);
        }
    }
    $::dep->commitDep($plan);

    # call cleanup procedure
    callApplicationCleanup($plan, $hosts);
}


##############################################################################
# callSync
#   Call the Sync command 
##############################################################################
sub callSync {
    my ($plan, $proj, $proc, $required) = @_;

    output(2,"Calling Sync hook procedure $proj/$proc for $plan.");
    my $jobId = runHookProcedure($plan, $proj,$proc, "adjust", "shrinkCfg", $required);

    if ($jobId == 0) {
        output(0,"Sync hook procedure failed.");
        return;
    }
    output(4,"Sync job=$jobId");
    my $xmlout = $::rps->getProp("/jobs/$jobId/CloudManager/sync");
    if ($xmlout eq "") {
        output(0,"No return data from sync job.");
        return;
    }
    output(4,"$xmlout");

    my $hosts = "";
    # process the results to see what is still alive
    my $xPath = XML::XPath->new( xml => "$xmlout");
    my $nodeset = $xPath->find('//Deployment');
    foreach my $node ($nodeset->get_nodelist) {
        my $handle = $xPath->findvalue('handle',$node)->string_value;
        my $state = $xPath->findvalue('state',$node)->string_value;
        my $rec = $::dep->getDepRec($plan, $handle);
        if ($state eq "alive") {
            next;
        } elsif ($state eq "pending") {
            output(4, "Marking $handle as $state");
            my $start = $rec->{start};
            my $now   = time();
            my $elapsed = ($now-$start) / 3600;

            my $consumed = $::sts->getCostConsumedTerminated($plan);
            $consumed += $elapsed;
            $::sts->setCostConsumedTerminated($plan,$consumed);
            $::dep->endDepRec($plan,$handle, $state);

            ## save list of hostnames for cleanup 
            $hosts .= $rec->{hostname};
            $hosts .= ";";
        } else {
            # record error
            output(0,"error syncing $plan:$handle");
        }
    }

    # call cleanup procedure
    callApplicationCleanup($plan, $hosts);
}

##############################################################################
# runHookProcedure
#   run the hook procedure for grow, shrink, or query
#   Get values for required params from stored properties in cfg
##############################################################################
sub runHookProcedure() {
    my ($plan, $proj, $proc, $cfg, $subCfg,$required) = @_;

    my $paramList = getParams($proj,$proc);
    output(5, "Parameters for $subCfg");
    foreach my $p (keys % {$paramList}) {
        output(5,"   $p required=$paramList->{$p}{req}");
    }

    # make sure procedure has required parameters 
    foreach my $prop (keys % {$required}) {
        if ( ! defined($paramList->{$prop})) {
            print "error: Procedure $proj/$proc does not have required parameter $prop\n";
            return 0;
        }
    }

    # get parameters stored in plan
    output(5, "Parameters stored in config");
    my %storedParams = $::rps->getAllCfgParams($plan, $cfg, $subCfg);
    foreach my $sp (keys %storedParams) {
        output(5,"   $sp=$storedParams{$sp}");
    }
    
    my @parray;
    # for each parameter the procedure requires
    foreach my $paramName (keys % {$paramList}) {
        # add parameters required by this integration
        if (defined $required->{$paramName} && $required->{$paramName} ne "") {
            push @parray, {actualParameterName => "$paramName", value => "$required->{$paramName}"};
            next;
        }

         # otherwise it is a hook specific parameter
        my $value = $storedParams{$paramName};
        # if it is blank and required, throw error
        if ("$value" eq "" && $paramList->{$paramName}{req} eq "1") {
            print "error: required parameter $paramName not stored in config\n";
            return 0;
        }
        # push onto the array
        push @parray, {actualParameterName => "$paramName", value => "$value"};
    }

    output(4,"Parameters to runProcedure");
    foreach my $tempP (@parray) {
        output(4,"   $tempP->{actualParameterName}");
    }

    # now run the procedure
    output(4, "runProcedure $proj $proc");
    my $xPath = $::ec->runProcedure("$proj",
        { procedureName => "$proc", pollInterval => 1,  timeout => 3600,
          actualParameter => \@parray,
        }); 
    # test success of procedure
    if ($xPath) {
        my $code = $xPath->findvalue('//code')->string_value;
        if ($code ne "") {
            my $mesg = $xPath->findvalue('//message')->string_value;
            output(0, "Run procedure return code is '$code'\n$mesg");
            return 0;
        }   
    }   
    my $outcome = $xPath->findvalue('//outcome')->string_value;
    if ($outcome ne "success") {
        output(0, "The adjust procedure did not succeed.");
        return 0;
    }
        
    # get stored info
    my $jobId = $xPath->findvalue('//jobId')->string_value;
    return $jobId;
}

###############################################################
# getParams
#
#     get the formal parameters for a procedure
##############################################################
sub getParams {
    my ($proj,$proc) = @_;

    my $list ;
    my $xPath = $::ec->getFormalParameters("$proj", { procedureName => "$proc" });
    if ($xPath) {
        my $code = $xPath->findvalue('//code')->string_value;
        if ($code ne "") {
            my $mesg = $xPath->findvalue('//message')->string_value;
            output(0, "getFormalParameters return code is '$code'\n$mesg");
            return $list;
        }
    }
    my $nodeset = $xPath->find("//formalParameter");
    foreach my $node ($nodeset->get_nodelist) {
        my $name =  $node->findvalue('formalParameterName')->string_value;
        $list->{$name}{name} = $name;
        $list->{$name}{req}  = $node->findvalue('required')->string_value;
        $list->{$name}{def}  = $node->findvalue('defaultValue')->string_value;
        $list->{$name}{desc} = $node->findvalue('description')->string_value;
        $list->{$name}{type} = $node->findvalue('type')->string_value;
    }
    return $list;
}

sub addResource {
    my ($resourceName,$hostname,$workspace, $port, $poolName) = @_;
    my $rPath = $::ec->createResource("$resourceName", 
        {hostName => $hostname, workspaceName => $workspace,
        port=> $port, pools => $poolName});
    if ($rPath) {
        my $code = $rPath->findvalue('//code')->string_value;
        if ($code ne "") {
            my $mesg = $rPath->findvalue('//message')->string_value;
            output(0, "createResource return code is '$code'\n$mesg");
            return 0;
        }
    }
    output(1, "created resource $resourceName");
    return 1;
}

sub removeResource {
    my ($resName) = @_;
    # delete from pool
    if (!defined $resName || "$resName" eq "") {
        output(0, "deleteResource given blank resource name");
        return 0;
    }
    my $xPath = $::ec->deleteResource("$resName");
    if ($xPath) {
        my $code = $xPath->findvalue('//code')->string_value;
        if ($code ne "") {
            my $mesg = $xPath->findvalue('//message')->string_value;
            output(0, "deleteResource return code is '$code'\n$mesg");
            return 0;
        }
    }
    output(1,"resource $resName was removed");
    return 1;
}

###############################################################################
# pickInstanceToEnd
#
# Machines may be charged for a full hour, even if it only lived for one minute
# To be frugal, we provide settings to leave machines running, even if time to kill them
# because you have already been charged a full hour.
#
# KillLimitMin
# KillLimitMax
#
#   If you set KillLimitMin to 50 minutes, then no machine will be killed unless
#   it has been running for at least 50 minutes, even if the terminate count
#   says the depolyomnt must die. This keeps the deployment around for a while
#   under the theory that you have paid for it and the resource manager might
#   want more resources soon so it can be re-used. Only observed if
#   KillLimitPolicy is set to "try" or "always".
#
#   Because there are delays in processing this script and actually getting a deployment
#   killed, you may not want to kill an deployment that only has a few minutes left until the 
#   next full hour starts. Set KillLimitMax to a value that gives you enough processing
#   time. For instance, setting it to 58 will only kill deployments with 2 minutes remaining in the hour
#
# KillLimitPolicy 
#   "try":     Deployments whose minute of the hour fall between KillLimitMin and KillLimitMax
#              will be the first picked, then others by longest running
#
#   "always":  KillLimitMin and KillLimitMax will be used to pick candidate deployments
#              Deployments will not be killed if they do not fit the limits
#              Deployments could run forever so be careful. 
#
#   "never":   Ignore KillLimit values and just terminate the oldest running deployments
#
# Returns
#   XML text that has list of instances along with all of the 
#   data for each instance that was returned from Grow
#
###############################################################################
sub pickInstancesToEnd() {
    my ($plan,$count) = @_;    

    my $KillLimitPolicy  = $::rps->getKillLimitPolicy($plan);
    my $KillLimitMin     = $::rps->getKillLimitMin($plan);
    my $KillLimitMax     = $::rps->getKillLimitMax($plan);
    $count = 0 - $count;
    output(1, "Plan $plan - Attempting to terminate $count deployments. Policy=$KillLimitPolicy Min="
        . " $KillLimitMin Max=$KillLimitMax.");

        
    # Get list of all deployment records
    my @handles = $::dep->getDepRecs($plan,"alive");
    my %deplist;
    # for each deployment record
    output(5,"..scanning deployments");
    foreach my $depname (@handles) {
        my $rec = $::dep->getDepRec($plan,$depname);
        my $start = $rec->{start};

        output(5,"..candidate $depname start=$start");
        my $now = time();
        my $diff = min_left($now - $start);
        
        $deplist{"$depname"}{diff}     = $diff;
        $deplist{"$depname"}{start}    = $start;
        $deplist{"$depname"}{running}  = ($now-$start);
        
        $deplist{"$depname"}{eligible} = 1;
        # does this fit in kill limits
        if ($KillLimitPolicy ne "never") {
            my $inlimits = ($diff < $KillLimitMax && $diff > $KillLimitMin);
            if (!$inlimits) {
                output(5,"...$depname subject to kill limits");
                $deplist{"$depname"}{eligible} = 0;
            }
        }
    }
    
    output(5,"...picking deployments to end");
    my $found = $count;
    my @candidates;
    my $listSep = "";
    # now sort key descending by eligible and then run time
    foreach my $key 
        (sort { 
          $deplist{$b}{eligible} <=> $deplist{$a}{eligible}  ||
          $deplist{$b}{running} <=> $deplist{$a}{running}
         }
        (keys(%deplist))) {
        output(5, "instance=$key"
         . " start=$deplist{$key}{start}"
         . " eligible=$deplist{$key}{eligible}"
         . " diff=$deplist{$key}{diff}"
         . " running=$deplist{$key}{running}");
         
        #   figure out best candidates by taking from top of list
        if ($KillLimitPolicy eq "always" && !$deplist{$key}{eligible}) {
            # we have done all we can do, no more eligible deployments
            last;
        } else {
            # add to list of victims
            push @candidates, $key;
        }
        $found--;
        if ($found <=0) {last;}
    }
    if (scalar(@candidates) == 0) {
        return "";
    }

    ## now get all saved grow information for these keys and construct
    ## a single XML request
    my $xml = "";
    $xml .= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    $xml .= "<ShrinkRequest>";
    foreach my $key (@candidates) {
        $xml .= "  <Deployment>";
        my $storedParams = $::dep->getDepRec($plan,$key);
        # get parameters stored in plan
        output(5, "Parameters stored in config");
        foreach my $sp (keys % { $storedParams } ) {
            output(5,"   $sp=$storedParams->{$sp}");
            $xml .=  "    <$sp>" . $storedParams->{$sp} . "</$sp>";
        }
        $xml .= "  </Deployment>";
    }
    $xml .= "</ShrinkRequest>";
    return $xml;
}


##############################################################################
# helper routines
##############################################################################
sub min($$) {
     my ($x,$y) = @_;
     if ($x < $y) {return $x;}
     return $y;
}
 
sub max($$) {
      my ($x,$y) = @_;
      if ($x > $y) {return $x;}
      return $y;
}


##############################################################################
# min_left
#
# given an elapsed time in seconds
# return the floor number of minutes remaining in the latest full 
# hour since start
##############################################################################
sub min_left() {
    my ($timeval) = @_;
    my $minutes = $timeval/60/60;
    my $left = $minutes - int($minutes);
    $left = 60 - int($left * 60);
    return $left;
}

##############################################################################
# output
##############################################################################
sub output {
    my $level = shift;
    my $msg = shift;

    if ($level > $::gDebug) { return; }
    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
    printf STDERR "%4d-%02d-%02d %02d:%02d:%02d: %s\n", 
        $year+1900,$mon+1,$mday,$hour,$min,$sec, $msg;
}

main();

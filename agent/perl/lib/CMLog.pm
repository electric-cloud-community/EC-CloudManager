####################################################################
#
# ECLog: store/retrieve log records
#
#    A log is a recording of the actual, min, max, request, start/stops 
#    at each polling interval
#
#   The log records are kept in a json array which is stored
#   as a single property in Commander
#
####################################################################
package CMLog;

use JSON;
use Data::Dumper;
use DateTime;
use ElectricCommander;
use ElectricCommander::PropDB;
use ElectricCommander::Util;
use POSIX qw(INT_MAX LONG_MAX LONG_MIN);

use base("ElectricCommander::PropDB");
$::json = JSON->new->allow_nonref;

####################################################################
# Object constructor for ECLog
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

    my($self) = ElectricCommander::PropDB->new($cmdr,"$proj/tables/LOG");
    bless ($self, $class);
    return $self;
}

####################################################################
####################################################################
# Manage records in a plan
####################################################################
####################################################################

####################################################################
# addLogRec -     add a log record (persistent)
####################################################################
sub addLogRec {
    my ($self,$plan,$cur,$req,$target,$tod_min,$tod_max,$activity,$cost, $optnow) = @_;
    
    $self->loadLog($plan);
    $self->newLogRec($plan,$cur,$req,$target,$tod_min,$tod_max,$activity,$cost, $optnow) ;
    $self->commitLog($plan);
}

####################################################################
# newLogRec -     add a log record (cache only)
####################################################################
sub newLogRec {
    my ($self,$plan,$cur,$req,$target,$tod_min,$tod_max,$activity,$cost, $optnow) = @_;

    my $now;
    if ($optnow ne "") {
        $now = $optnow;  # for testing
    } else {
        $now = time();
    }

    my ($sec,$min, $hour, $mday, $mon, $year, $wday, $yday, $isdst) = localtime($now);
    $year += 1900;
    $mon +=1;
    my $timestr = sprintf("%04d-%02d-%02dT%02d:%02d:%02d", $year,$mon,$mday,$hour,$min,$sec);

    $self->{logs}{$plan}{$now}{timestr}   = "$timestr";
    $self->{logs}{$plan}{$now}{activity}  = "$activity";
    $self->{logs}{$plan}{$now}{previous}  = "$cur";
    $self->{logs}{$plan}{$now}{target}    = "$target";
    $self->{logs}{$plan}{$now}{max}       = "$max";
    $self->{logs}{$plan}{$now}{min}       = "$min";
    $self->{logs}{$plan}{$now}{req}       = "$req";
    $self->{logs}{$plan}{$now}{cost}      = "$cost";
}

####################################################################
# getLogRec -  get a log record (indexed by time)
#
# returns (timestr, activity, stops, actual, target, max, min, req, cost)
####################################################################
sub getLogRec {
    my ($self,$plan,$time) = @_;

    my $rec = $self->{logs}{$plan}{$time};
    return ($rec->{timestr} , $rec->{activity},
        $rec->{previous}, $rec->{target},
        $rec->{max}, $rec->{min}, $rec->{req}, $rec->{cost});
}

####################################################################
# getLogRecs -  get a list of keys for a plan
####################################################################
sub getLogRecs() {
    my ($self,$plan) = @_;

    my @keys;
    foreach my $t (sort keys %{$self->{logs}{$plan} } ) {
        push @keys, $t;
    }
    return @keys;
}

####################################################################
# trimRecs - delete all recs older than configured limit
####################################################################
sub trimRecs {
    my ($self,$plan,$cutoff) = @_;

    foreach my $rectime (keys % { $self->{logs}{$plan} } ) {
        if ($rectime < $cutoff) {
            delete($self->{logs}{$plan}{$rectime});
        }
    }
}

####################################################################
# getLogsForPlan - get pointer to hash for a plan
####################################################################
sub getLogsForPlan {
    my ($self,$plan) = @_;
    return $self->{logs}{$plan};
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
    return (defined ($self->{logs}{$plan}));
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
    delete($self->{logs}{$plan});

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
# loadAllLogs - iterate through list of plans and load logs for each
####################################################################
sub loadAllLogs {
    my ($self) = @_;

    my @plans = $self->getAllPlanNames();
    foreach my $plan (@plans) {
        $self->loadLog($plan);
    }
}

####################################################################
# loadLog - load the log from a single plan
####################################################################
sub loadLog($){
    my ($self, $plan) = @_;
    if ($plan eq "") {
        print "error: no plan name for loadLog\n";
        return "";
    }
    my $jlog = $self->getJSON($plan);
    if ($jlog eq "") {
        # nothing stored
        return;
    }
    my $plog = $::json->decode($jlog);
    $self->{logs}{$plan} = $plog;
}

####################################################################
# commitLogs - for each plan in memory, write the log to property
####################################################################
sub commitLogs {
    my ($self) = @_;

    my @plans;
    foreach my $plan (keys %{$self->{logs}}) {
        push @plans, $plan;
    }
    foreach my $plan (@plans) {
        $self->commitLog($plan);
    }

}

####################################################################
# commitLog - write the log to the db
####################################################################
sub commitLog($) {
    my ($self,$plan) = @_;
    if ($plan eq "") {
        print "error: no plan name for commitLog\n";
        return "";
    }
    my $jlog = $self->getOnePlan($plan);
    $self->setJSON($plan,$jlog);
}

####################################################################
# getOnePlan - get one plan from property
####################################################################
sub getOnePlan($) {
    my ($self, $plan) = @_;
    my $plog = $self->{logs}{$plan};
    my $jlog =  $::json->encode($plog);
    return $jlog;
}

####################################################################
# getAllLogs - get all the plans in memory as json
####################################################################
sub getAllLogs() {
    my ($self) = @_;

    my @plans;
    foreach my $plan (keys % {$self->{logs}}) {
        push @plans, $self->{logs}{$plan};
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
    my $plog = $::json->decode($jplan);

    # set cache
    $self->{logs}{$plan} = $plog;
}

####################################################################
# getCacheAsText -  used in debugging and testing
####################################################################
sub getCacheAsText() {
    my ($self) = @_;
    my $out = Dumper($self->{logs});
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
####################################################################
# Reports in Google Charts data format
#   http://code.google.com/apis/chart/interactive/docs/dev/implementing_data_source.html#responseformat
####################################################################
####################################################################

sub initAccumulator {
    my ($self,$a,$range) = @_;
    $self->resetAccumulator($a);
    $a->{yMin} = LONG_MAX;
    $a->{yMax} = LONG_MIN;
    $a->{last} = 0;  # last time stamp

    my $date = DateTime->now;
    # Adjust the time range to the nearest even boundaries
    if ($range eq 'day') {
        $a->{xMax}  = $date->add(hours=>1)->truncate(to => 'hour')->epoch() ;
        $a->{xMin}  = $date->add(hours=> -24)->epoch();
        $a->{range} = 60*60;  # summarize to an hour
    } elsif ($range eq 'week') {
        $a->{xMax}  = $date->add(days=>1)->truncate(to => 'day')->epoch() ;
        $a->{xMin}  = $date->add(days=> -7)->epoch();
        $a->{range} = 24*60*60;  # summarize by day
    } else {
        $a->{xMax}  = $date->add(days=>1)->truncate(to => 'day')->epoch() ;
        $a->{xMin}  = $date->add(months=> -1)->epoch();
        $a->{range} = 24*60*60;  # summarize by day
    }

}

sub resetAccumulator {
    my ($self,$a) = @_;
    foreach my $type (qw(req act)) {
        $a->{$type}{count} = 0;
        $a->{$type}{sum} = 0;
        $a->{$type}{min} = LONG_MAX;
        $a->{$type}{max} = 0;
    }
}

sub limitRecord {
    my ($self, $time, $a, $rows) = @_;

    my $text = "";
    if ($rows > 0) {
        $text .= ",";
    }

    my $jtime = $time * 1000;
    my $rAvg = sprintf("%.2f", $a->{req}{sum} / $a->{req}{count});
    my $rMax = sprintf("%d",$a->{req}{max});
    my $rMin = sprintf("%d",$a->{req}{min});

    my $aAvg = sprintf("%.2f", $a->{act}{sum} / $a->{act}{count});
    my $aMax = sprintf("%d",$a->{act}{max});
    my $aMin = sprintf("%d",$a->{act}{min});
    $text .= "{\"c\":[{\"v\":$jtime}"
        . ",{\"v\":$rAvg}"
        . ",{\"v\":$aAvg}"
        . "]}";

    # adjust yAxis to min/max of any data point
    $a->{yMax} = $rMax if $rMax > $a->{yMax};
    $a->{yMax} = $aMax if $aMax > $a->{yMax};
    $a->{yMin} = $rMin if $rMin < $a->{yMin};
    $a->{yMin} = $aMin if $aMin < $a->{yMin};

    $self->resetAccumulator($a);
    return $text;
}

####################################################################
# Basic limits report 
####################################################################
sub getLimitReport {
    my ($self,$plan, $range) = @_;

    my $accumulator;
    $accumulator->{desc} = "Hash to summarize values";
    $self->initAccumulator($accumulator,$range);

    
    my $avgColorDark = 'rgba(1,96,199,1)';
    my $avgColorLight = 'rgba(112,149,219,1)';
    my $reqColorDark = 'rgba(254,126,0,1)';
    my $reqColorLight = 'rgba(254,183,117,1)';
    # chart header
    my $pdata = 
          "{\"version\": \"1.0\",\"reqId\":\"0\",\"status\":\"ok\","
        . "\"table\":{"
        . "\"cols\":["
        . "{\"id\":\"col0\",\"label\":\"time\",\"type\":\"date\"}"
        . ",{\"id\":\"col1\",\"label\":\"Application Request\",\"type\":\"number\","
        . "\"p\":{\"type\":\"line\",\"color\":\"$reqColorDark\",\"hovertextTemplate\":\"\${xDate}<br>Application Request: \${y}\"}}"
        . ",{\"id\":\"col4\",\"label\":\"Plan Limited\",\"type\":\"number\","
        . "\"p\":{\"type\":\"line\",\"color\":\"$avgColorDark\",\"hovertextTemplate\":\"\${xDate}<br>Plan Limtted: \${y}\"}}"
        . "],"
        . "\"rows\":[";


    # load the data from propeties
    $self->loadLog($plan);
    
    # loop through all log records for this plan and format as rows
    my $rows = 0;
    my @recs = $self->getLogRecs($plan);   
    foreach my $time (@recs) {
        my ($timestr, $activity, $previous, $target, 
            $maximum, $minimum, $req, $cost) = $self->getLogRec($plan,$time);

        next if ($time > $accumulator->{xMax} || $time < $accumulator->{xMin});

        ##
        ## is it time to output a summarized value
        ##

        # first data point
        if ($accumulator->{last} == 0) {
            $accumulator->{last} = $time;
        } 

        my $elapsed = $time - $accumulator->{last};
        # is it time to emit a record
        if ($elapsed > $accumulator->{range} ) {
            $pdata .= $self->limitRecord($time, $accumulator,$rows);
            $accumulator->{last} = $time;
            $rows++;
        }

        ##
        ## add data to the accumulator
        ##
        $accumulator->{req}{count}++;
        $accumulator->{req}{sum} += $req;
        $accumulator->{req}{min} = $req if $req < $accumulator->{req}{min} ;
        $accumulator->{req}{max} = $req if $req > $accumulator->{req}{max} ;

        $accumulator->{act}{count}++;
        $accumulator->{act}{sum} += $target;
        $accumulator->{act}{min} = $target if $target < $accumulator->{act}{min} ;
        $accumulator->{act}{max} = $target if $target > $accumulator->{act}{max} ;
    
    }
    # if any data not yet recorded
    if ($accumulator->{req}{count} > 0) {
        $pdata .= $self->limitRecord(@recs[$#recs], $accumulator,$rows);
        $rows++;
    }

    $accumulator->{yMin} = 0;
    if ($rows) {
        # Round up to the nearest multiple of 10
        $accumulator->{yMax} = ((int($accumulator->{yMax} / 10) + 1)*10);
    } else {
        $accumulator->{yMax} = 10;
    }

    $pdata .= "],"
    . "\"p\":{"
    . "\"xAxis\":{\"hasGridLines\":true,\"tickLabelFormat\":\"=(Date)dd/HH:mm\","
    . "\"axisMin\":" . $accumulator->{xMin} * 1000 . ","
    . "\"axisMax\":" . $accumulator->{xMax} * 1000 . ","
    . "\"tickCount\":13},"
    . "\"yAxis\":{\"hasGridLines\":true,\"tickCount\":11,"
    . "\"axisMin\":" . sprintf("%d",$accumulator->{yMin} ) . ","
    . "\"axisMax\":" . sprintf("%d",$accumulator->{yMax} ) . "},"
    . "\"chartTitle\":\"History\",\"xChartSize\":700,\"yChartSize\":300}}}";
    return $pdata;
}

1;

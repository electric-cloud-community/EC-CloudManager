$[/myProject/perl/perlObjects.pm]

use strict;

$|=1;

# protocol required
my $poolName    = "$[poolName]";
my $current     = "$[current]";

# commander specific
my $buffer      = "$[buffer]";
my $incDead     = "$[includeDead]";
my $incDisabled = "$[includeDisabled]";

if ($buffer < 0) {
    print "error: buffer must be greater than or equal to 0\n";
    exit 1;
}

print "Parameters\n";
print "poolName    : $poolName\n";
print "buffer      : $buffer\n";
print "incDead     : $incDead\n";
print "incDisabled : $incDisabled\n";
print "current     : $current\n";

## collect the usage metrics for the pool
my $result = getResourceUsage($poolName, $incDead, $incDisabled);


# try to get size to 
my $newsize = $current + ($buffer - ($result->{usable} - $result->{running}));
if ($newsize < 0) { $newsize = 0; }

# if we only are counting 
foreach my $metric ("running", "defined", "usable") {
    print "$metric = $result->{$metric}\n";
    $::pdb->setProp("/myJob/CloudManager/metrics/$metric",$result->{$metric});
}

print "  Result=$newsize\n";
$::pdb->setProp("/myJob/CloudManager/query",$newsize);
exit 0;

######################
##  getResourceUsage
######################
sub getResourceUsage() {
    my ($pool, $incDead, $incDisabled) = @_;

    my $result;
    $result->{running} = 0;
    $result->{defined} = 0;
    $result->{usable} = 0;

    my $running;
    my $foundPool = 0;
    my $xmlout = "";
    addXML(\$xmlout,"<DeadResources>");

    # find out how many resources in the pool are running
    my $xPath = $::ec->getResourceUsage();
    my $nodeset = $xPath->find('//responses/response/resourceUsage');
    foreach my $node ($nodeset->get_nodelist) {
        my $name = $xPath->findvalue('resourceName',$node);
        my $poolName = $xPath->findvalue('resourcePoolName',$node);
        if ($poolName ne $pool) { next; }
        $result->{running}++;
        $running->{$name} = 1;
    }

    # find out how many resources defined
    $xPath = $::ec->getResourcePool($pool);
    $nodeset = $xPath->find('//resourceName');
    foreach my $node ($nodeset->get_nodelist) {
        $result->{defined}++;
        my $resource = $xPath->getNodeText($node);
        my $rPath = $::ec->getResource($resource);
        my $active = $rPath->findvalue('//resourceDisabled')->string_value;
        if ($active) {
            $active = "0";
        } else {
            $active = "1";
        }
        my $alive = $rPath->findvalue('//agentState/alive')->string_value;

    
        # if this resource is dead, and is not counted to total usable 
        # it should be marked for removal by adding to dead Resources list
        # change by Avan Mathur 4/2013
        if ($alive eq "0" && !$incDead)     { 
            addXML(\$xmlout, "<Resource>");
            addXML(\$xmlout, "  <resourceName>" . xmlQuote($resource) . "</resourceName>");
            addXML(\$xmlout, "</Resource>");
            next; 
        }
        if ($active eq "0" && !$incDisabled) { next; }

        $result->{usable}++;
    }
    # Change by Avan Mathur 4/2013
    # add Dead resources list
    addXML(\$xmlout, "</DeadResources>");
    $::ec->setProperty("/myJob/CloudManager/deadResources", $xmlout);

    return $result;
}



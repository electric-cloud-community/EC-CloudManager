package main;

use ElectricCommander;
use ElectricCommander::PropDB ;
use ElectricCommander::PropMod ;
use ElectricCommander::Util qw(xmlQuote);

$::ec = new ElectricCommander();
$::ec->abortOnError(0);
$::pdb = new ElectricCommander::PropDB($::ec,"");

if ("$ENV{COMMANDER_PLUGIN_PERL}" ne "") {
    # during tests
    push @INC, "$ENV{COMMANDER_PLUGIN_PERL}";
} else {
    # during production
    my $pluginName = "@PLUGIN_NAME@";
    push @INC, "$ENV{COMMANDER_PLUGINS}/$pluginName/agent/perl/lib";
}
sub addXML {
   my ($xml, $text) = @_;
   $$xml .= $text;
   $$xml .= "\n";
}
require CMDeployment;
require CMLog;
require CMResPlan;
require CMStats;


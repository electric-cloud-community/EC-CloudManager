# -*- Perl -*-
if ($upgradeAction eq 'upgrade') {
    my $query = $commander->newBatch();
    my $cfgs = $query->getProperty(
        "/plugins/$otherPluginName/project/tables");

    local $self->{abortOnError} = 0;
    $query->submit();

    # Copy configurations from $otherPluginName
    if ($query->findvalue($cfgs,'code') ne 'NoSuchProperty') {
        $batch->clone({
            path => "/plugins/$otherPluginName/project/tables",
            cloneName => "/plugins/$pluginName/project/tables"
        }); 
    }   

}

if ($promoteAction eq 'promote') {
    $view->add(["Cloud", "Cloud Manager"],
               { url => 'pages/@PLUGIN_KEY@/configure' });
} elsif ($promoteAction eq 'demote') {
    $view->remove(["Cloud", "Cloud Manager"]);
}

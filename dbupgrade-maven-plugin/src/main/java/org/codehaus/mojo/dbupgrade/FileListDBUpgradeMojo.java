package org.codehaus.mojo.dbupgrade;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Upgrade incrementally using file list
 * @goal filelist-upgrade
 * @requiresProject false
 */
public class FileListDBUpgradeMojo
    extends AbstractDBUpgradeMojo
{
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

    }
}

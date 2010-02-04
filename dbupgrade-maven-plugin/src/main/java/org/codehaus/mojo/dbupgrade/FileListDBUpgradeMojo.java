package org.codehaus.mojo.dbupgrade;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.dbupgrade.file.FileDBUpgradeLifecycle;

/**
 * Upgrade incrementally using file list
 * @goal filelist-upgrade
 * @requiresProject false
 */
public class FileListDBUpgradeMojo
    extends AbstractDBUpgradeMojo
{
    /**
     * @parameter
     */
    private org.codehaus.mojo.dbupgrade.file.DBUpgradeConfiguration config;
    
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            FileDBUpgradeLifecycle dbupgrade = new FileDBUpgradeLifecycle( config );
            dbupgrade.upgrade();
        }
        catch ( DBUpgradeException e )
        {
            throw new MojoExecutionException( "Unable to run upgrade", e );
        }
    }
}

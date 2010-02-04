package org.codehaus.mojo.dbupgrade;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.dbupgrade.generic.GenericDBUpgradeLifecycle;

/**
 * Upgrade incrementally Using Generic Method
 * @goal generic-upgrade
 * @requiresProject false
 */
public class GenericDBUpgradeMojo
    extends AbstractDBUpgradeMojo
{
    /**
     * @parameter
     */
    private org.codehaus.mojo.dbupgrade.generic.DBUpgradeConfiguration config;
    
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            GenericDBUpgradeLifecycle dbupgrade = new GenericDBUpgradeLifecycle( config );
            dbupgrade.upgrade();
        }
        catch ( DBUpgradeException e )
        {
            throw new MojoExecutionException( "Unable to run upgrade", e );
        }
        
    }
}

package org.codehaus.mojo.dbupdate2;

import java.io.File;

import org.codehaus.mojo.dbupgrade.sqlexec.SQLExecConfig;


public class DBUpgradeConfiguration
    extends SQLExecConfig
{
 
    private File upgradeFile;
    
    private File workingDirectory;

    /**
     * Table name to be used to look for version for
     */
    private String versionTableName = "version";

    /**
     * Column name in versionTableName to be used to look for version info
     */
    private String versionColumnName = "lastUpdateName";
    
    private int connectionRetries = 10 ; 
    
        
    public void setVersionTableName( String versionTableName )
    {
        this.versionTableName = versionTableName;
    }
    
    public String getVersionTableName( )
    {
        return this.versionTableName;
    }      
    
    public void setVersionColumnName( String versionCollumnName )
    {
        this.versionColumnName = versionCollumnName;
    }
    
    public String getVersionColumnName( )
    {
        return this.versionColumnName;
    }

    public void setConnectionRetries( int retries )
    {
        this.connectionRetries = retries;
    }

    public int getConnectionRetries( )
    {
        return this.connectionRetries;
    }

    public File getUpgradeFile()
    {
        return upgradeFile;
    }

    public void setUpgradeFile( File upgradeFile )
    {
        this.upgradeFile = upgradeFile;
    }

    public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public void setWorkingDirectory( File workingDirectory )
    {
        this.workingDirectory = workingDirectory;
    }
    
}

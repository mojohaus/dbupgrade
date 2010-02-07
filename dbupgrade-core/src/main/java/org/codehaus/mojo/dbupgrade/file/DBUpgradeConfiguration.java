package org.codehaus.mojo.dbupgrade.file;

import java.io.File;

import org.codehaus.mojo.dbupgrade.sqlexec.SQLExecConfig;

/*
 * Copyright 2000-2010 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

public class DBUpgradeConfiguration
    extends SQLExecConfig
{
 
    private File upgradeFile;
    
    private File scriptDirectory;

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

    public File getScriptDirectory()
    {
        return scriptDirectory;
    }

    public void setScriptDirectory( File dir )
    {
        this.scriptDirectory = dir;
    }
    
}

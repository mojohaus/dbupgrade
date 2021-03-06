package org.codehaus.mojo.dbupgrade;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.mojo.dbupgrade.file.FileDBUpgradeLifecycle;
import org.codehaus.mojo.dbupgrade.file.FileListDBUpgradeConfiguration;

/**
 * This class hooks up user's sql upgrade script locations contained in a text file ( ie the text file contains a list
 * of SQL script paths ). After a SQL script is executed, its names is stored in your configurable database version
 * table. DBUpgrade uses database version's value ( a SQL script name ) to pickup the next upgrade script, if any.
 * <p>
 * Alternatively this can also scan the script directory recursively in a lexicographical order to create a list of SQL
 * scripts to execute in case the upgrade file list is not supplied.
 */
@Mojo( name = "filelist-upgrade", requiresProject = false, threadSafe = true )
public class FileListDBUpgradeMojo
    extends AbstractDBUpgradeMojo
{
    /**
     * Necessary configuration to run database upgrade.
     */
    @Parameter( required = true )
    private FileListDBUpgradeConfiguration config;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            FileDBUpgradeLifecycle dbupgrade = new FileDBUpgradeLifecycle( config );
            dbupgrade.upgrade();
            if ( config.getUpgradeFile() != null )
            {
                this.getLog().info( "Database upgrade using file list method: " + config.getUpgradeFile() );
            }
            else
            {
                this.getLog().info( "Database upgrade using parse script directory method: "
                                        + config.getScriptDirectory() );
            }
        }
        catch ( DBUpgradeException e )
        {
            throw new MojoExecutionException( getExceptionMessages( e ) );
        }
    }
}

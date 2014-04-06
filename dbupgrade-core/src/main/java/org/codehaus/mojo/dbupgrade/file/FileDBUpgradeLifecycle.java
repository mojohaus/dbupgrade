package org.codehaus.mojo.dbupgrade.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.FileUtils;
import org.codehaus.mojo.dbupgrade.DBUpgradeException;
import org.codehaus.mojo.dbupgrade.DBUpgradeLifecycle;
import org.codehaus.mojo.dbupgrade.sqlexec.DefaultSQLExec;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

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

/**
 * This class hooks up user's sql upgrade script locations contained in a text file ( ie the text file contains a list
 * of SQL script paths ). After a SQL script is executed, its names is stored in your configurable database version
 * table. DBUpgrade uses database version's value ( a SQL script name ) to pickup the next upgrade script, if any.
 * <p>
 * Alternatively this can also scan the script directory recursively in a lexicographical order to create a list of SQL
 * scripts to execute. This behavior can be triggered by not supplying the upgrade file list in the config.
 */
public class FileDBUpgradeLifecycle
    implements DBUpgradeLifecycle
{

    private DefaultSQLExec sqlexec;

    private FileListDBUpgradeConfiguration config;

    private String initialDBVersion = null;

    public FileDBUpgradeLifecycle( FileListDBUpgradeConfiguration config )
        throws DBUpgradeException
    {
        this.config = config;
        this.sqlexec = new DefaultSQLExec( config );
        this.initializeDBVersion();
    }

    /**
     * Done with this instance
     */
    public void close()
    {
        this.sqlexec.close();
    }

    /**
     * Execute DB Upgrade lifecycle phases
     */
    public int upgrade()
        throws DBUpgradeException
    {
        Collection<String> upgradeFileNames;

        if ( config.getUpgradeFile() != null )
        {
            upgradeFileNames = getUpgradeListFromFile();
        }
        else
        {
            upgradeFileNames = getUpgradeListFromScriptDir();
        }

        return upgrade( upgradeFileNames );
    }

    // //////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////

    private int upgrade( Collection<String> fileNames )
        throws DBUpgradeException
    {
        int upgraderCount = 0;
        Iterator<String> it = fileNames.iterator();
        // find where we left off last upgrade
        if ( !StringUtils.isBlank( this.initialDBVersion ) )
        {
            boolean versionFileFound = false;

            while ( it.hasNext() )
            {
                String upgradeFileName = it.next();
                if ( initialDBVersion.equals( upgradeFileName ) )
                {
                    versionFileFound = true;
                    break; // so that we can continue with upgrade
                }
            }

            if ( !versionFileFound )
            {
                throw new DBUpgradeException( "Database version value: " + initialDBVersion
                    + " not found. Are you upgrading the right database?" );
            }
        }

        // Continue with the upgrade from where it was left last
        while ( it.hasNext() )
        {
            String upgradeFileName = it.next();
            upgrade( config.getScriptDirectory(), upgradeFileName );
            upgraderCount++;
        }
        return upgraderCount;
    }

    private Collection<String> getUpgradeListFromFile()
        throws DBUpgradeException
    {
        List<String> upgradeFileList = new ArrayList<String>();
        try
        {
            List<String> lines = FileUtils.readLines( config.getUpgradeFile() );
            for ( String line : lines ) {
                line = line.trim();
                if ( StringUtils.isBlank( line ) || line.startsWith( "#" ) )
                {
                    continue;
                }
                upgradeFileList.add( line );
            }
        }
        catch ( IOException e )
        {
            throw new DBUpgradeException( "Unable to get file list from " + this.config.getUpgradeFile(), e );
        }

        return upgradeFileList;
    }

    private Collection<String> getUpgradeListFromScriptDir()
        throws DBUpgradeException
    {
        SortedSet<String> upgradeFileNameSet = new TreeSet<String>( new Comparator<String>()
        {
            public int compare( String a, String b )
            {
                return a.compareTo( b );
            }
        } );

        Collection<File> fileList = FileUtils.listFiles( config.getScriptDirectory(), new String[] { "sql" }, true );

        for ( File file : fileList )
        {
            String diff =
                StringUtils.difference( config.getScriptDirectory().getAbsolutePath(), file.getAbsolutePath() );
            diff = StringUtils.substring( diff, 1 ); // remove the starting / after the diff
            diff = StringUtils.replace( diff, File.separator, "/" );
            upgradeFileNameSet.add( diff );
        }

        return upgradeFileNameSet;
    }

    private Connection getConnection()
        throws DBUpgradeException
    {
        try
        {
            return sqlexec.getConnection();
        }
        catch ( SQLException e )
        {
            throw new DBUpgradeException( e.getMessage(), e );
        }
    }

    private void initializeVersionTable()
        throws DBUpgradeException
    {
        try
        {
            this.createVersionTable();
            this.createInitialVersion();
        }
        catch ( SQLException e )
        {
            throw new DBUpgradeException( "Unable to create version table:" + config.getVersionTableName() + ".", e );
        }

    }

    private void initializeDBVersion()
        throws DBUpgradeException
    {
        Statement stm = null;
        ResultSet rs = null;

        try
        {
            stm = sqlexec.getConnection().createStatement();
            rs = stm.executeQuery( "SELECT " + config.getVersionColumnName() + " FROM " + config.getVersionTableName() );
            if ( !rs.next() )
            {
                this.createInitialVersion();
            }
        }
        catch ( SQLException e )
        {
            initializeVersionTable();
        }
        finally
        {
            DbUtils.closeQuietly( rs );
            DbUtils.closeQuietly( stm );
        }

        this.initialDBVersion = this.getDBVersion();

    }

    private void createVersionTable()
        throws SQLException
    {
        sqlexec.execute( "create table " + config.getVersionTableName() + " ( " + config.getVersionColumnName()
            + " varchar(256) )" );
        sqlexec.commit();
    }

    private void createInitialVersion()
        throws SQLException
    {
        sqlexec.execute( "insert into " + config.getVersionTableName() + " ( " + config.getVersionColumnName()
            + " ) values ( '' )" );
        sqlexec.commit();
    }

    private String getDBVersion()
        throws DBUpgradeException
    {
        Statement statement = null;
        String version;
        ResultSet rs = null;

        Connection connection = getConnection();

        try
        {
            statement = connection.createStatement();

            rs =
                statement.executeQuery( "SELECT distinct(" + config.getVersionColumnName() + ") FROM "
                    + config.getVersionTableName() );

            if ( rs.next() )
            {
                version = rs.getString( 1 );
                if ( rs.next() )
                {
                    throw new DBUpgradeException( "Multiple versions found in " + config.getVersionTableName()
                        + " table." );
                }
            }
            else
            {
                throw new DBUpgradeException( "Version row not found in: " + config.getVersionTableName() + " table." );
            }
        }
        catch ( SQLException e )
        {
            sqlexec.rollbackQuietly();
            throw new DBUpgradeException( "Version row not found in: " + config.getVersionTableName() + " table." );
        }

        finally
        {
            DbUtils.closeQuietly( rs );
            DbUtils.closeQuietly( statement );
        }

        return version;
    }

    private void upgrade( File scriptDirectory, String upgradeFileName )
        throws DBUpgradeException
    {
        File upgradeFile = new File( scriptDirectory, upgradeFileName );

        if ( this.config.isVerbose() )
        {
            System.out.println( "Executing: " + upgradeFile + " ..." );
        }

        try
        {
            sqlexec.execute( upgradeFile, config.isDisableSQLParser() );
            if ( !StringUtils.isBlank( config.getPostIncrementalStatement() ) )
            {
                sqlexec.execute( config.getPostIncrementalStatement() );
            }
            sqlexec.execute( "update " + this.config.getVersionTableName() + " set "
                + this.config.getVersionColumnName() + " ='" + upgradeFileName + "'" );
            sqlexec.commit();
        }
        catch ( Exception e )
        {
            sqlexec.rollbackQuietly();
            throw new DBUpgradeException( "Unable to perform file upgrade: " + upgradeFile + ".", e );
        }
    }

}

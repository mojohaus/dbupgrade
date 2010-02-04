package org.codehaus.mojo.dbupgrade.generic;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.mojo.dbupgrade.DBUpgradeException;
import org.codehaus.mojo.dbupgrade.DBUpgradeLifecycle;
import org.codehaus.mojo.dbupgrade.Util;
import org.codehaus.mojo.dbupgrade.sqlexec.DefaultSQLExec;

/*
 * Copyright 2000-20010 The Apache Software Foundation
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
 * Incremental Database upgrade implementation, capable of understanding both java and sql format
 * Original source is from http://code.google.com/p/dbmigrate
 * @author dan.tran
 */
public class GenericDBUpgradeLifecycle
    implements DBUpgradeLifecycle
{

    private final Log log = LogFactory.getLog( GenericDBUpgradeLifecycle.class );

    private DefaultSQLExec sqlexec;

    private DBUpgradeConfiguration config;

    public void setSqlexec( DefaultSQLExec sqlexec )
    {
        this.sqlexec = sqlexec;
    }

    public GenericDBUpgradeLifecycle( DBUpgradeConfiguration config )
        throws DBUpgradeException
    {
        this.config = config;
        this.sqlexec = new DefaultSQLExec( config );
        this.initDBUpgrade();
    }

    /**
     * Execute DB Upgrade lifecycle phases
     */
    public int upgrade()
        throws DBUpgradeException
    {
        try
        {
            runJavaUpgrader( config, "PreDBUpgrade" );

            this.incrementalUpgrade( config );

            runJavaUpgrader( config, "PostDBUpgrade" );
        }
        catch ( SQLException e )
        {
            throw new DBUpgradeException( "Unable to upgrade", e );
        }

        return 0; //until we figure out how to do this
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

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

    private void initDBUpgrade()
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
                this.createInitialVersion( config );
            }
        }
        catch ( SQLException e )
        {
            try
            {
                this.createVersionTable( config );
                this.createInitialVersion( config );
            }
            catch ( SQLException ex )
            {
                throw new DBUpgradeException( "Unable to intialize version table.", ex );
            }
        }
        finally
        {
            DbUtils.closeQuietly( rs );
            DbUtils.closeQuietly( stm );
        }

    }

    private void createVersionTable( DBUpgradeConfiguration conf )
        throws SQLException
    {
        sqlexec.execute( "create table " + conf.getVersionTableName() + " ( " + conf.getVersionColumnName()
            + " integer )" );
    }

    private void createInitialVersion( DBUpgradeConfiguration conf )
        throws SQLException
    {
        sqlexec.execute( "insert into " + conf.getVersionTableName() + " ( " + conf.getVersionColumnName()
            + " ) values ( " + config.getInitialVersion() + " )" );
    }

    private void incrementalUpgrade( DBUpgradeConfiguration config )
        throws DBUpgradeException
    {
        int latestVersion = 0;

        String packageName = config.getPackageName();
        String versionResourcePath = packageName.replace( '.', '/' ) + "/" + config.getVersionResourceName();

        InputStream versionResource = this.getClass().getClassLoader().getResourceAsStream( versionResourcePath );
        if ( versionResource == null )
        {
            throw new DBUpgradeException( "Could not find " + versionResourcePath + " resource in classpath" );
        }

        try
        {
            Properties properties = new Properties();
            properties.load( versionResource );
            latestVersion = Integer.parseInt( properties.getProperty( "version" ) );
        }
        catch ( IOException e )
        {
            throw new DBUpgradeException( "Could not read " + versionResourcePath + " resource in classpath", e );
        }

        while ( !internalUpgrade( latestVersion, config, getConnection() ) );

    }

    /**
     * get version for DB and check
     * 
     * @param connection
     * @param latestVersion
     * @return
     * @throws DBUpgradeException
     */
    private int getVersion( int latestVersion, DBUpgradeConfiguration config, Connection connection )
        throws DBUpgradeException
    {
        Statement statement = null;
        int version;
        ResultSet rs = null;
        try
        {
            statement = connection.createStatement();

            // lock the table?

            try
            {
                rs = statement.executeQuery( "SELECT distinct(" + config.getVersionColumnName() + ") FROM "
                    + config.getVersionTableName() );
            }
            catch ( SQLException e )
            {
                //postgres requires this rollback
                Util.rollback( connection );

                //version table is not available ,assume version 0
                version = 0;
                return version;
            }

            if ( !rs.next() )
            {
                // if no version present, assume version = 0
                version = 0;
            }
            else
            {
                version = rs.getInt( 1 );

                if ( rs.next() )
                {
                    throw new DBUpgradeException( "Multiple versions found in " + config.getVersionTableName()
                        + " table" );
                }

                if ( latestVersion < version )
                {
                    throw new DBUpgradeException( "Downgrade your database from " + version + " to " + latestVersion
                        + " is not supported." );
                }
            }
        }
        catch ( SQLException e )
        {
            Util.rollback( connection );
            throw new DBUpgradeException( "Could not execute version query", e );
        }
        finally
        {
            DbUtils.closeQuietly( rs );
            DbUtils.closeQuietly( statement );
        }

        return version;
    }

    /**
     * Upgrade to the next version
     * 
     * @param configuration
     * @return false when there are more upgrade to do
     * @throws DBUpgradeException
     */
    private boolean internalUpgrade( int latestVersion, DBUpgradeConfiguration config, Connection connection )
        throws DBUpgradeException
    {

        ResultSet rs = null;
        Statement statement = null;

        try
        {
            connection.setAutoCommit( false );

            int version = getVersion( latestVersion, config, connection );
            int toVersion = version + 1;

            if ( version == latestVersion )
            {
                //no more upgrade to do
                return true;
            }

            DBUpgrade upgrade = this.getUpgrader( version, toVersion, config, connection );
            upgrade.setSqlexec( sqlexec );

            try
            {
                log.info( "Database Upgrade: " + config.getDialect() + ":" + upgrade );
                upgrade.upgradeDB( config.getDialect() );
            }
            catch ( Exception e )
            {
                log.error( e );
                Util.rollback( connection );
                throw new DBUpgradeException( "Failed to upgrade from version: " + version + " to " + toVersion, e );
            }

            statement = connection.createStatement();

            rs = statement.executeQuery( "SELECT distinct(" + config.getVersionColumnName() + ") FROM "
                + config.getVersionTableName() );

            if ( !rs.next() )
            {
                Util.rollback( connection );
                throw new DBUpgradeException( "Unable to look up version info in database after upgrade" );
            }
            else
            {
                int currentDBVersion = rs.getInt( 1 );

                if ( currentDBVersion != toVersion )
                {
                    Util.rollback( connection );
                    throw new DBUpgradeException( "Version in database is: " + currentDBVersion
                        + " which is not corrected incremented after upgrading from: " + currentDBVersion );
                }
            }

            //all good
            connection.commit();
        }
        catch ( SQLException e )
        {
            //more likely version table was not created during the first upgrade at version 0
            Util.rollback( connection );
            throw new DBUpgradeException( "Failed to upgrade due to database exception", e );
        }
        finally
        {
            DbUtils.closeQuietly( rs );
            DbUtils.closeQuietly( statement );
        }

        return false;
    }

    private String versionToString( int version )
    {
        String ret = "";

        if ( version < 0 )
        {
            ret = "_";
            version = ( -1 ) * version;
        }
        return ret + version;
    }

    private DBUpgrade getJavaUpgrader( int fromVer, int toVer, DBUpgradeConfiguration config )
    {
        String fromVersion = versionToString( fromVer );
        String toVersion = versionToString( toVer );

        String className = config.getPackageName() + "." + config.getUpgraderPrefix() + fromVersion + "to" + toVersion;

        DBUpgrade upgrader = null;

        upgrader = this.getJavaUpgrader( className );

        if ( upgrader == null )
        {
            String dialect = config.getDialect();
            className = config.getPackageName() + "." + dialect + "." + config.getUpgraderPrefix() + fromVersion + "to"
                + toVersion;

            upgrader = this.getJavaUpgrader( className );
        }

        return upgrader;
    }

    private DBUpgradeUsingSQL getSqlUpgrader( int fromVer, int toVer, DBUpgradeConfiguration config )
    {
        DBUpgradeUsingSQL upgrader = null;

        String fromVersion = versionToString( fromVer );
        String toVersion = versionToString( toVer );

        String sqlResourceName = config.getPackageNameSlashFormat() + "/" + config.getUpgraderPrefix() + fromVersion
            + "to" + toVersion + ".sql";
        upgrader = this.getSqlUpgrader( sqlResourceName );

        if ( upgrader == null )
        {
            sqlResourceName = config.getPackageNameSlashFormat() + "/" + config.getDialect() + "/"
                + config.getUpgraderPrefix() + fromVersion + "to" + toVersion + ".sql";
            upgrader = this.getSqlUpgrader( sqlResourceName );
        }

        return upgrader;
    }

    private DBUpgradeUsingSQL getSqlUpgrader( String resourceName )
    {
        DBUpgradeUsingSQL upgrader = null;

        InputStream test = this.getClass().getClassLoader().getResourceAsStream( resourceName );

        if ( test != null )
        {
            try
            {
                test.close();
            }
            catch ( IOException ioe )
            {
                // whatever
            }
            upgrader = new DBUpgradeUsingSQL( resourceName );
        }

        return upgrader;
    }

    /**
     * search of available upgrader
     * 
     * @param version
     * @param toVersion
     * @param config
     * @param connection
     * @return
     * @throws DBUpgradeException
     */
    private DBUpgrade getUpgrader( int version, int toVersion, DBUpgradeConfiguration config, Connection connection )
    {

        DBUpgrade upgrader = null;

        upgrader = this.getJavaUpgrader( version, toVersion, config );

        if ( upgrader == null )
        {
            upgrader = this.getSqlUpgrader( version, toVersion, config );
        }

        if ( upgrader == null )
        {
            throw new RuntimeException( "Unable to find a DBUpgrader capable of upgrading from version " + version
                + " to version " + toVersion );

        }

        return upgrader;
    }

    private void runJavaUpgrader( DBUpgradeConfiguration config, String upgraderName )
        throws DBUpgradeException, SQLException
    {
        String className = null;

        Connection connection = getConnection();

        className = config.getPackageName() + "." + upgraderName;
        runUpgrade( config, this.getJavaUpgrader( className ), connection );

        className = config.getPackageName() + "." + config.getDialect() + "." + upgraderName;
        runUpgrade( config, this.getJavaUpgrader( className ), connection );
    }

    private void runUpgrade( DBUpgradeConfiguration config, DBUpgrade upgrader, Connection connection )
        throws DBUpgradeException
    {
        if ( upgrader != null )
        {
            try
            {
                upgrader.upgradeDB( config.getDialect() );
                connection.commit();
            }
            catch ( Exception e )
            {
                Util.rollback( connection );
                throw new DBUpgradeException( "Unable to perform update: ", e );
            }
        }
    }

    /**
     * return a DBUpgrade instance base on class name
     * 
     * @param config
     * @param className
     * @return
     * @throws DBUpgradeException
     */
    private DBUpgrade getJavaUpgrader( String className )
    {
        DBUpgrade upgrader = null;

        Class<?> clazz = null;

        try
        {
            clazz = Class.forName( className );
        }
        catch ( ClassNotFoundException e )
        {
            //user does not supply the upgrader, return null so that the upgrade lifecycle can safely ignore it
            return null;
        }

        try
        {
            upgrader = (DBUpgrade) clazz.newInstance();
        }
        catch ( Exception e )
        {
            //all bets are off, user supplies the unexpected class type
            throw new RuntimeException( e );
        }

        return upgrader;

    }
}

package org.codehaus.mojo.dbupgrade.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbutils.DbUtils;
import org.codehaus.mojo.dbupgrade.DBUpgradeException;
import org.codehaus.mojo.dbupgrade.sqlexec.DefaultSQLExec;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;


/**
 * Incremental Database upgrade implementation,
 * @author dan.tran
 */
public class FileDBUpgradeLifecycle
    implements DBUpgradeLifecycle
{

    private DefaultSQLExec sqlexec;

    private DBUpgradeConfiguration config;

    private String initialDBVersion = null;

    public void setSqlexec( DefaultSQLExec sqlexec )
    {
        this.sqlexec = sqlexec;
    }

    public FileDBUpgradeLifecycle( DBUpgradeConfiguration config )
        throws DBUpgradeException
    {
        this.config = config;
        this.sqlexec = new DefaultSQLExec( config );
        this.initializeDBVersion();
    }

    /**
     * Execute DB Upgrade lifecycle phases
     */
    public int upgrade()
        throws DBUpgradeException
    {
        int upgraderCount = 0;
        
        FileReader fileReader = null;

        try
        {
            fileReader = new FileReader( config.getUpgradeFile() );
            BufferedReader reader = new BufferedReader( fileReader );

            String line = null;

            //find where we left off last upgrade
            if ( !StringUtils.isBlank( this.initialDBVersion ) )
            {
                line = reader.readLine();
                while ( line != null )
                {
                    line = line.trim();
                    if ( StringUtils.isBlank( line ) )
                    {
                        continue;
                    }

                    if ( initialDBVersion.equals( line ) )
                    {
                        break; //so that we can continue with upgrade
                    }

                    upgrade( config.getWorkingDirectory(), line.trim() );

                    line = reader.readLine();
                }
            }

            
            //continue on with last upgrade
            line = reader.readLine();
            while ( line != null )
            {
                line = line.trim();
                if ( StringUtils.isBlank( line ) )
                {
                    continue;
                }

                upgrade( config.getWorkingDirectory(), line.trim() );
                upgraderCount++;

                line = reader.readLine();
            }

        }
        catch ( IOException e )
        {
            throw new DBUpgradeException( "Unable to run upgrade on: " + this.config.getUpgradeFile(), e );
        }
        finally
        {
            IOUtil.close( fileReader );
        }
        
        return upgraderCount;
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
            throw new DBUpgradeException( "Unable to create version table", e );
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
            + " varchar )" );
    }

    private void createInitialVersion()
        throws SQLException
    {
        sqlexec.execute( "insert into " + config.getVersionTableName() + " ( " + config.getVersionColumnName()
            + " ) values ( '' )" );
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

            rs = statement.executeQuery( "SELECT distinct(" + config.getVersionColumnName() + ") FROM "
                + config.getVersionTableName() );

            if ( rs.next() )
            {
                version = rs.getString( 1 );
                if ( rs.next() )
                {
                    throw new DBUpgradeException( "Multiple versions found in " + config.getVersionTableName()
                        + " table" );
                }
            }
            else
            {
                throw new DBUpgradeException( "Version row not found" );
            }
        }
        catch ( SQLException e )
        {
            sqlexec.roolbackQuietly();
            throw new DBUpgradeException( "Version row not found" );
        }

        finally
        {
            DbUtils.closeQuietly( rs );
            DbUtils.closeQuietly( statement );
        }

        return version;
    }

    private void upgrade( File workingDirectory, String upgradeFileName )
        throws DBUpgradeException
    {
        File upgradeFile = new File( workingDirectory, upgradeFileName );

        try
        {
            sqlexec.execute( upgradeFile );
            sqlexec.execute( "update version set version = '" + upgradeFileName + "'" );
            sqlexec.getConnection().commit();
        }
        catch ( SQLException e )
        {
            sqlexec.roolbackQuietly();
            throw new DBUpgradeException( "Unable to run upgrade on: " + upgradeFile, e );
        }
    }
}

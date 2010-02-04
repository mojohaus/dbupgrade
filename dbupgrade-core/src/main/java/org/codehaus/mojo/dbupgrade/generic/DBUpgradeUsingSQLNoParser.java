package org.codehaus.mojo.dbupgrade.generic;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.codehaus.mojo.dbupgrade.DBUpgradeException;

/**
 * Submit the resource to jdbc in one shot
 * @author dtran
 *
 */
public class DBUpgradeUsingSQLNoParser
    extends AbstractDBUpgrade
{
    private String sqlResouceName;

    public DBUpgradeUsingSQLNoParser( String sqlResourceName )
    {
        this.sqlResouceName = sqlResourceName;
    }

    public void upgradeDB( String dialect )
        throws DBUpgradeException
    {
        InputStream is = null;
        Statement statement = null;
        String sql = null;

        try
        {
            is = DBUpgradeUsingSQL.class.getClassLoader().getResourceAsStream( this.sqlResouceName );
            sql = IOUtils.toString( is );
            
            statement = sqlexec.getConnection().createStatement();
            statement.setEscapeProcessing( false );
           
            
            if ( statement.execute( sql ) )
            {
                //we expect a false return since the execution has no result set
                throw new DBUpgradeException( "Unable execute SQL Statement:" + sql );
            }

        }
        catch ( IOException e )
        {
            throw new DBUpgradeException( "Unable load SQL Statement from resource:" + sqlResouceName, e );
        }
        catch ( SQLException e )
        {
            throw new DBUpgradeException( "Unable execute SQL Statement:" + sql, e  );
        }
        finally
        {
            if ( statement != null )
            {
                try
                {
                    statement.close();
                }
                catch ( SQLException e )
                {

                }
            }
        }

    }
    
    public String toString()
    {
        return "DBUpgradeUsingSQLNoParser:" + sqlResouceName;
    }
    
}

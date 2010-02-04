package org.codehaus.mojo.dbupgrade;

import java.sql.SQLException;

public class DBUpgradeUsingSQL
    extends AbstractDBUpgrade
{
    private String sqlResouceName;

    public DBUpgradeUsingSQL( String sqlResourceName )
    {
        this.sqlResouceName = sqlResourceName;
    }

    public void upgradeDB( String dialect )
        throws DBUpgradeException
    {
        try
        {
            this.sqlexec.execute( DBUpgradeUsingSQL.class.getClassLoader().getResourceAsStream( this.sqlResouceName ) );
        }
        catch ( SQLException e )
        {
            throw new DBUpgradeException( "Unable to upgrade database type: " + dialect, e );
        }
    }

    public String toString()
    {
        return "DBUpgradeUsingSQL:" + sqlResouceName;
    }
}

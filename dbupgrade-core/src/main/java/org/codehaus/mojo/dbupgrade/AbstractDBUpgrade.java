package org.codehaus.mojo.dbupgrade;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.codehaus.mojo.dbupgrade.sqlexec.SQLExec;


public abstract class AbstractDBUpgrade
    implements DBUpgrade
{

    protected SQLExec sqlexec;

    public void setSqlexec( SQLExec sqlexec )
    {
        this.sqlexec = sqlexec;
    }

    protected void executeSQL( InputStream istream )
        throws DBUpgradeException
    {
        try
        {
            sqlexec.execute( istream );
        }
        catch ( SQLException e )
        {
            throw new DBUpgradeException( e.getMessage(), e );
        }
    }

    protected void executeSQL( String sqlString )
        throws DBUpgradeException
    {
        try
        {
            sqlexec.execute( sqlString );
        }
        catch ( SQLException e )
        {
            throw new DBUpgradeException( e.getMessage(), e );
        }
    }

    /**
     * run a sql resource file in the same package of a class of option commit
     * @param c
     * @param clazz
     * @param name
     * @param commit
     * @throws DBUpgradeException
     */
    protected void executeSQL( Connection c, Class<?> clazz, String dialect, boolean commit )
        throws DBUpgradeException
    {
        String packageName = clazz.getPackage().getName().replace( ".", "/" );
        String resourceName = packageName + "/" + dialect;

        DBUpgradeUsingSQL dbupgrader = new DBUpgradeUsingSQL( resourceName );
        dbupgrader.upgradeDB( dialect );

        try
        {
            if ( commit )
            {
                c.commit();
            }
        }
        catch ( SQLException e )
        {
            throw new DBUpgradeException( "Unable to commit SQL works in resource: " + resourceName );
        }
    }

    protected void executeSQLNoParser( Connection c, Class<?> clazz, String dialect, boolean commit )
        throws DBUpgradeException
    {
        String packageName = clazz.getPackage().getName().replace( ".", "/" );
        String resourceName = packageName + "/" + dialect;

        DBUpgrade dbupgrader = new DBUpgradeUsingSQLNoParser( resourceName );
        dbupgrader.upgradeDB( dialect );

        try
        {
            if ( commit )
            {
                c.commit();
            }
        }
        catch ( SQLException e )
        {
            throw new DBUpgradeException( "Unable to commit SQL works in resource: " + resourceName );
        }
    }

}

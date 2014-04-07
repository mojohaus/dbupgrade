package org.codehaus.mojo.dbupgrade.sqlexec;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLUtils
{
    private SQLUtils()
    {

    }

    public static void closeQuietly( Statement o )
    {
        if ( o != null )
        {
            try
            {
                o.close();
            }
            catch ( SQLException e )
            {
            }
        }
    }

    public static void closeQuietly( ResultSet o )
    {
        if ( o != null )
        {
            try
            {
                o.close();
            }
            catch ( SQLException e )
            {
            }
        }
    }

    public static void closeQuietly( Connection o )
    {
        if ( o != null )
        {
            try
            {
                o.close();
            }
            catch ( SQLException e )
            {
            }
        }
    }

}

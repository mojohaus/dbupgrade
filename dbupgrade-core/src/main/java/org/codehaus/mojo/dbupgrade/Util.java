package org.codehaus.mojo.dbupgrade;

import java.sql.Connection;
import java.sql.SQLException;

public class Util
{

    public static void rollback( Connection c )
    {
        try
        {
            c.rollback();
        }
        catch ( SQLException e )
        {
            //unexpected exception, throw runtime to get more attention
            throw new RuntimeException( "Unable to rollback upgrade." );
        }
    }    

}

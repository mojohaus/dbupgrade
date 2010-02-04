package org.codehaus.mojo.dbupgrade;

public class DBUpgradeException
    extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public DBUpgradeException( String message )
    {
        super( message );
    }

    public DBUpgradeException( String message, Throwable nested )
    {
        super( message, nested );
    }

}

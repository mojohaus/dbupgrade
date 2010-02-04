package org.codehaus.mojo.dbupgrade.test1.hsqldb;

import org.codehaus.mojo.dbupgrade.AbstractDBUpgrade;
import org.codehaus.mojo.dbupgrade.DBUpgradeException;

public class DBUpgrade1to2
    extends AbstractDBUpgrade
{
    public void upgradeDB( String dialect )
        throws DBUpgradeException
    {
        System.out.println( this.getClass().getName() );
        String sql = "update version set version = 2";
        executeSQL( sql );
    }

}

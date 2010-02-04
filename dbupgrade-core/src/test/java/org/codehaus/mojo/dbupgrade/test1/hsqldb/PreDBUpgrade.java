package org.codehaus.mojo.dbupgrade.test1.hsqldb;

import org.codehaus.mojo.dbupgrade.AbstractDBUpgrade;
import org.codehaus.mojo.dbupgrade.DBUpgradeException;

public class PreDBUpgrade
    extends AbstractDBUpgrade
{
    public void upgradeDB( String dialect )
        throws DBUpgradeException
    {
        try
        {
            this.executeSQL( "create table version ( version integer ) " );
            this.executeSQL( "insert into version values ( '-2' )" );
        }
        catch ( Exception e )
        {
            //should ignore if we can not create table
        }
    }

}

package org.codehaus.mojo.dbupgrade.test1.hsqldb;

import org.codehaus.mojo.dbupgrade.AbstractDBUpgrade;
import org.codehaus.mojo.dbupgrade.DBUpgradeException;

public class PostDBUpgrade
    extends AbstractDBUpgrade
{
    public void upgradeDB( String dialect )
        throws DBUpgradeException
    {
        System.out.println( "PostDBUpgradeDB is available" );
    }

}

package org.codehaus.mojo.dbupgrade.generic.test1.hsqldb;

import org.codehaus.mojo.dbupgrade.DBUpgradeException;
import org.codehaus.mojo.dbupgrade.generic.AbstractDBUpgrade;

public class PostDBUpgrade
    extends AbstractDBUpgrade
{
    public void upgradeDB( String dialect )
        throws DBUpgradeException
    {
        System.out.println( "PostDBUpgradeDB is available" );
    }

}

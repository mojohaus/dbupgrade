package org.codehaus.mojo.dbupgrade;

import org.codehaus.mojo.dbupgrade.DBUpgradeConfiguration;
import org.codehaus.mojo.dbupgrade.DBUpgradeException;
import org.codehaus.mojo.dbupgrade.DBUpgradeLifecycle;
import org.codehaus.mojo.dbupgrade.DefaultDBUpgradeLifecyle;

import junit.framework.TestCase;

/**
 * @author dan.tran
 *
 */
public class DefaultDBUpgradeExecutorTest
    extends TestCase
{
    private DBUpgradeConfiguration config;

    protected void setUp()
    {
        config = new DBUpgradeConfiguration();
        config.setUsername( "sa" );
        config.setPassword( "" );
        config.setDriver( "org.hsqldb.jdbcDriver" );
        config.setDialect( "hsqldb" );
        config.setUrl( "jdbc:hsqldb:mem:target/testdb" );
        config.setPackageName( "org.codehaus.mojo.dbupgrade.test1" );
        config.setVersionTableName( "version" );
        config.setVersionColumnName( "version" );
    }

    /**
     * test 2 upgrade versions using both SQL and java
     * @throws Exception
     */
    public void testGoodDBUpgradeExecutorTest()
        throws Exception
    {
        DBUpgradeLifecycle upgrader = new DefaultDBUpgradeLifecyle();
        upgrader.upgrade( this.config );

        //do it one more time
        upgrader.upgrade( this.config );
    }

    public void testMissingDBUpgraderTest()
        throws Exception
    {
        config.setVersionResourceName( "missing-version.properties" );
        DBUpgradeLifecycle upgrader = new DefaultDBUpgradeLifecyle();
        try
        {
            upgrader.upgrade( this.config );
        }
        catch ( RuntimeException e )
        {
            assertTrue( e.getMessage().startsWith( "Unable to find a DBUpgrader capable of upgrading" )  );
        }
    }

    public void testDowngradeDBUpgraderTest()
        throws Exception
    {
        config.setVersionResourceName( "downgrade-version.properties" );
        DBUpgradeLifecycle upgrader = new DefaultDBUpgradeLifecyle();
        try
        {
            upgrader.upgrade( this.config );
        }
        catch ( DBUpgradeException e )
        {
            System.out.println( "Expected exception: " + e.getMessage() );
        }
    }



}

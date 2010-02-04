package org.codehaus.mojo.dbupdate2;

import org.codehaus.mojo.dbupdate2.DBUpgradeConfiguration;
import org.codehaus.mojo.dbupdate2.DBUpgradeLifecycle;
import org.codehaus.mojo.dbupdate2.FileDBUpgradeLifecycle;
import org.codehaus.mojo.dbupgrade.DBUpgradeException;

import junit.framework.TestCase;


/**
 * @author dan.tran
 *
 */
public class FileDBUpgradeExecutorTest
    extends TestCase
{
    private DBUpgradeConfiguration config;
    
    DBUpgradeLifecycle upgrader;

    protected void setUp()
        throws Exception
    {
        config = new DBUpgradeConfiguration();
        config.setUsername( "sa" );
        config.setPassword( "" );
        config.setDriver( "org.hsqldb.jdbcDriver" );
        config.setUrl( "jdbc:hsqldb:mem:target/testdb2s" );
        config.setVersionTableName( "version" );
        config.setVersionColumnName( "version" );
        
        upgrader = new FileDBUpgradeLifecycle( config );
        
    }

    /**
     * test 2 upgrade versions using both SQL and java
     * @throws Exception
     */
    public void testGoodDBUpgradeExecutorTest()
        throws Exception
    {
        upgrader.upgrade();

        //do it one more time
        upgrader.upgrade();
    }

    public void testMissingDBUpgraderTest()
        throws Exception
    {
        try
        {
            upgrader.upgrade();
        }
        catch ( RuntimeException e )
        {
            assertTrue( e.getMessage().startsWith( "Unable to find a DBUpgrader capable of upgrading" )  );
        }
    }

    public void testDowngradeDBUpgraderTest()
        throws Exception
    {
        try
        {
            upgrader.upgrade();
        }
        catch ( DBUpgradeException e )
        {
            System.out.println( "Expected exception: " + e.getMessage() );
        }
    }



}

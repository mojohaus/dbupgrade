package org.codehaus.mojo.dbupgrade.file;

import java.io.File;

import junit.framework.TestCase;

import org.codehaus.mojo.dbupgrade.DBUpgradeException;


/**
 * @author dan.tran
 *
 */
public class FileDBUpgradeExecutorTest
    extends TestCase
{
    private DBUpgradeConfiguration config;
    
    private DBUpgradeLifecycle upgrader;
    
    private File dataDirectory = new File( "src/test/resources/org/codehaus/mojo/dbupgrade/file" );

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
        config.setWorkingDirectory( dataDirectory );
        upgrader = new FileDBUpgradeLifecycle( config );
        
    }

    /**
     * test 2 upgrade versions using both SQL and java
     * @throws Exception
     */
    public void testGoodDBUpgradeExecutorTest()
        throws Exception
    {
        config.setUpgradeFile( new File( dataDirectory, "version-1.lst"  ) );
        upgrader.upgrade();

        //do it one more time
        //upgrader.upgrade();
    }

    public void testMissingDBUpgraderTest()
        throws Exception
    {
        config.setUpgradeFile( new File( dataDirectory, "version-1.lst"  ) );
        upgrader.upgrade();
        /*
        try
        {
            upgrader.upgrade();
        }
        catch ( RuntimeException e )
        {
            assertTrue( e.getMessage().startsWith( "Unable to find a DBUpgrader capable of upgrading" )  );
        }
        */
    }

    public void testDowngradeDBUpgraderTest()
        throws Exception
    {
        config.setUpgradeFile( new File( dataDirectory, "version-1.lst"  ) );
        upgrader.upgrade();
        /*
        try
        {
            upgrader.upgrade();
        }
        catch ( DBUpgradeException e )
        {
            System.out.println( "Expected exception: " + e.getMessage() );
        }
        */
    }



}

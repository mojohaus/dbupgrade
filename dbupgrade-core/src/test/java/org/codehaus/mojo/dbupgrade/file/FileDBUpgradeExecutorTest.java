package org.codehaus.mojo.dbupgrade.file;

import java.io.File;

import junit.framework.TestCase;


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
        assertEquals( 2, upgrader.upgrade() );

        //do it one more time
        upgrader = new FileDBUpgradeLifecycle( config );
        assertEquals( 0, upgrader.upgrade() );
    }

    public void testMissingDBUpgraderTest()
        throws Exception
    {
        try
        {
            config.setUpgradeFile( new File( dataDirectory, "version-bogus.lst"  ) );
            upgrader.upgrade();
            fail( "Exception expected." );
        }
        catch ( Exception e )
        {
        }
    }

}

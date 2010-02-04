package org.codehaus.mojo.dbupgrade.file;

import java.io.File;

import org.codehaus.mojo.dbupgrade.DBUpgradeLifecycle;

import junit.framework.TestCase;

/*
 * Copyright 2000-2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

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

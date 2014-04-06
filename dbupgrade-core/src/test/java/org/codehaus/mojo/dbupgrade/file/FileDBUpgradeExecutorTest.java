package org.codehaus.mojo.dbupgrade.file;

import java.io.File;

import junit.framework.TestCase;

import org.codehaus.mojo.dbupgrade.DBUpgradeException;
import org.codehaus.mojo.dbupgrade.DBUpgradeLifecycle;
import org.codehaus.mojo.dbupgrade.sqlexec.DefaultSQLExec;

/*
 * Copyright 2000-2010 The Apache Software Foundation
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

public class FileDBUpgradeExecutorTest
    extends TestCase
{
    private FileListDBUpgradeConfiguration config;

    private DBUpgradeLifecycle upgrader;

    private File dataDirectory = new File( "src/test/resources/org/codehaus/mojo/dbupgrade/file" );

    protected void setUp()
        throws Exception
    {
        config = new FileListDBUpgradeConfiguration();
        config.setUsername( "sa" );
        config.setPassword( "" );
        config.setDriver( "org.hsqldb.jdbcDriver" );
        config.setUrl( "jdbc:hsqldb:mem:target/testdb2s" );
        config.setVersionTableName( "upgradeinfo" );
        config.setVersionColumnName( "upgradeversion" );
        config.setScriptDirectory( dataDirectory );
        upgrader = new FileDBUpgradeLifecycle( config );

    }

    protected void tearDown()
        throws Exception
    {
        DefaultSQLExec sqlExec = new DefaultSQLExec( config );
        sqlExec.execute( "delete from " + config.getVersionTableName() );
        sqlExec.commit();
    }

    /**
     * Sets the DB version to the supplied one. Can be used by tests to set the DB to a particular
     * version before executing tests. Note that this function assumes that the version table is
     * created and a row exists for the version. But, as setUp() initializes FileDBUpgradeLifecycle,
     * the version table and rows are already created.
     *
     * @param version the version to set in the DB
     * @throws Exception
     */
    protected void setDBVersion( String version )
        throws Exception
    {
        DefaultSQLExec sqlExec = new DefaultSQLExec( config );
        sqlExec.execute( "update " + this.config.getVersionTableName() + " set "
                + this.config.getVersionColumnName() + " ='" + version + "'" );
        sqlExec.commit();
    }

    /**
     * test 2 upgrade versions using both SQL and java
     * @throws Exception
     */
    public void testGoodDBUpgradeExecutorTest()
        throws Exception
    {
        //version 1
        config.setUpgradeFile( new File( dataDirectory, "version-1.lst" ) );
        assertEquals( 2, upgrader.upgrade() );

        //version 1.1
        upgrader = new FileDBUpgradeLifecycle( config );
        config.setUpgradeFile( new File( dataDirectory, "version-1.1.lst" ) );
        assertEquals( 1, upgrader.upgrade() );

        //version 1.1 again
        upgrader = new FileDBUpgradeLifecycle( config );
        config.setUpgradeFile( new File( dataDirectory, "version-1.1.lst" ) );
        assertEquals( 0, upgrader.upgrade() );

        //version 2
        upgrader = new FileDBUpgradeLifecycle( config );
        config.setUpgradeFile( new File( dataDirectory, "version-2.lst" ) );
        assertEquals( 2, upgrader.upgrade() );

        //version 2
        upgrader = new FileDBUpgradeLifecycle( config );
        config.setUpgradeFile( new File( dataDirectory, "version-2.lst" ) );
        assertEquals( 0, upgrader.upgrade() );

    }

    public void testMissingDBUpgraderFileTest()
        throws Exception
    {
        try
        {
            config.setUpgradeFile( new File( dataDirectory, "version-bogus.lst" ) );
            upgrader.upgrade();
            fail( "Exception expected." );
        }
        catch ( DBUpgradeException e )
        {
        }
    }

    public void testMissingDBUpgraderScriptFileTest()
        throws Exception
    {
        //version 3
        config.setUpgradeFile( new File( dataDirectory, "bad.lst" ) );
        try
        {
            upgrader.upgrade();
            fail( "Missing SQL script not detected." );
        }
        catch ( DBUpgradeException e )
        {

        }
    }

    public void testBadList()
        throws Exception
    {
        //Set DB Version to a version not present in version-3.lst
        setDBVersion( "version2/file-2.sql" );

        config.setUpgradeFile( new File( dataDirectory, "version-3.lst" ) );
        upgrader = new FileDBUpgradeLifecycle( config );
        try
        {
            upgrader.upgrade();
            fail( "Exception expected." );
        }
        catch ( DBUpgradeException e )
        {

        }
    }

    public void testDBUpgradeFromScriptDirExecution()
        throws Exception
    {
        //From Empty initial version
        config.setUpgradeFile( null );
        assertEquals( 6, upgrader.upgrade() );

        upgrader = new FileDBUpgradeLifecycle( config );
        config.setUpgradeFile( null );
        assertEquals( 0, upgrader.upgrade() );

        //From version1/file-2.sql version
        setDBVersion( "version1/file-2.sql" );

        upgrader = new FileDBUpgradeLifecycle( config );
        config.setUpgradeFile( null );
        assertEquals( 4, upgrader.upgrade() );

        upgrader = new FileDBUpgradeLifecycle( config );
        config.setUpgradeFile( null );
        assertEquals( 0, upgrader.upgrade() );

        //From version2/file-1.sql version
        setDBVersion( "version2/file-1.sql" );

        upgrader = new FileDBUpgradeLifecycle( config );
        config.setUpgradeFile(null);
        assertEquals( 2, upgrader.upgrade() );

        upgrader = new FileDBUpgradeLifecycle( config );
        config.setUpgradeFile( null );
        assertEquals( 0, upgrader.upgrade() );
    }

    public void testDBUpgradeFromScriptDirMissingVersion()
        throws Exception
    {

        setDBVersion( "bougus-dir/bogus-file.sql" );
        upgrader = new FileDBUpgradeLifecycle( config );

        try
        {
            upgrader.upgrade();
            fail( "Exception Expected" );
        }
        catch ( DBUpgradeException e )
        {
        }

        setDBVersion( "version2/bogus-file.sql" );
        upgrader = new FileDBUpgradeLifecycle( config );

        try
        {
            upgrader.upgrade();
            fail( "Exception Expected" );
        }
        catch ( DBUpgradeException e )
        {
        }

        setDBVersion( "version2/file-1.sql/bogus-file.sql" );
        upgrader = new FileDBUpgradeLifecycle( config );

        try
        {
            upgrader.upgrade();
            fail( "Exception Expected" );
        }
        catch ( DBUpgradeException e )
        {
        }
    }

}

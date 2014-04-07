package org.codehaus.mojo.dbupgrade.generic;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.mojo.dbupgrade.DBUpgradeException;
import org.codehaus.mojo.dbupgrade.DBUpgradeLifecycle;
import org.codehaus.mojo.dbupgrade.sqlexec.DefaultSQLExec;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;


public class GenericDBUpgradeExecutorTest
{
    private GenericDBUpgradeConfiguration config;

    @Before
    public void setUp()
    {
        config = new GenericDBUpgradeConfiguration();
        config.setUsername( "sa" );
        config.setPassword( "" );
        config.setDriver( "org.hsqldb.jdbcDriver" );
        config.setDialect( "hsqldb" );
        config.setUrl( "jdbc:hsqldb:mem:target/testdb" );
        config.setPackageName( "org.codehaus.mojo.dbupgrade.generic.test1" );
        config.setVersionTableName( "version" );
        config.setVersionColumnName( "version" );
        //show that we can start the db version using negative number, normally it starts as 0
        config.setInitialVersion( -2 );
    }

    @After
    public void tearDown()
        throws Exception
    {
        DefaultSQLExec sqlExec = new DefaultSQLExec( config );
        sqlExec.execute( "delete from " + config.getVersionTableName() );
        sqlExec.commit();
    }

    /**
     * test 2 upgrade versions using both SQL and java
     * @throws Exception
     */
    public void testGoodDBUpgradeExecutorTest()
        throws Exception
    {
        DBUpgradeLifecycle upgrader = new GenericDBUpgradeLifecycle( config );
        Assert.assertEquals( 4, upgrader.upgrade() );

        //test whether the component can reconnect after a shutdown
        upgrader.close();
        Assert.assertEquals( 0, upgrader.upgrade() );

        //do it one more time
        Assert.assertEquals( 0, upgrader.upgrade() );
    }

    public void testMissingDBUpgraderTest()
        throws Exception
    {
        config.setVersionResourceName( "missing-version.properties" );
        DBUpgradeLifecycle upgrader = new GenericDBUpgradeLifecycle( config );
        try
        {
            upgrader.upgrade();
        }
        catch ( RuntimeException e )
        {
            Assert.assertTrue( e.getMessage().startsWith( "Unable to find a DBUpgrader capable of upgrading" ) );
        }
    }

    public void testDowngradeDBUpgraderTest()
        throws Exception
    {
        config.setVersionResourceName( "downgrade-version.properties" );
        DBUpgradeLifecycle upgrader = new GenericDBUpgradeLifecycle( config );
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

package org.codehaus.mojo.dbupgrade.sqlexec;

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

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for simple SQLExec taken from sqlexec-maven-plugin 1.3
 */
public class SQLExecTest
{
    private DefaultSQLExec sqlexec;

    private SQLExecConfig config;

    private Properties p;

    @Before
    public void setUp()
        throws Exception
    {
        p = new Properties();
        p.load( getClass().getResourceAsStream( "/test.properties" ) );

        config = new SQLExecConfig();
        config.setDriver( p.getProperty( "driver" ) );
        config.setUsername( p.getProperty( "user" ) );
        config.setPassword( p.getProperty( "password" ) );
        config.setUrl( p.getProperty( "url" ) );
        config.setDriverProperties( p.getProperty( "driverProperties" ) );

        sqlexec = new DefaultSQLExec( config );

        // populate parameters

    }

    @After
    public void tearDown()
        throws Exception
    {
        if ( sqlexec != null )
        {
            sqlexec.close();
        }
    }

    /**
     * No error when there is no input
     */
    @Test
    public void testNoCommandMojo()
        throws SQLException
    {
        sqlexec.execute( "" );

        Assert.assertEquals( 0, sqlexec.getSuccessfulStatements() );
    }

    @Test
    public void testCreateDropCommandMojo()
        throws SQLException
    {
        String randomTable = "T" + System.currentTimeMillis();
        String command = "create table " + randomTable + " ( PERSON_ID integer, FIRSTNAME varchar, LASTNAME varchar)";
        sqlexec.execute( command );
        Assert.assertEquals( 1, sqlexec.getSuccessfulStatements() );
        sqlexec.execute( "drop table " + randomTable );
        Assert.assertEquals( 1, sqlexec.getSuccessfulStatements() );

    }

    @Test
    public void testFileSetMojo()
        throws SQLException
    {
        FileSet ds = new FileSet();
        ds.setBasedir( "src/test" );
        ds.setIncludes( new String[] { "**/create*.sql" } );
        ds.scan();
        assert ( ds.getIncludedFiles().length == 1 );

        sqlexec.execute( ds );

        Assert.assertEquals( 3, sqlexec.getSuccessfulStatements() );

        sqlexec.execute( new File( "src/test/data/drop-test-tables.sql" ) );

    }

    @Test
    public void testFileArrayMojo()
        throws SQLException
    {
        File[] srcFiles = new File[3];
        srcFiles[0] = new File( "src/test/data/create-test-tables.sql" );
        srcFiles[1] = new File( "src/test/data/query-test-tables.sql" );
        srcFiles[2] = new File( "src/test/data/drop-test-tables.sql" );

        // sqlexec.setSrcFiles( srcFiles );
        sqlexec.execute( srcFiles );

        Assert.assertEquals( 9, sqlexec.getSuccessfulStatements() );
    }

    /**
     * Ensure srcFiles always execute first
     */
    @Test
    public void testAllMojo()
        throws SQLException
    {
        String command = "create table PERSON2 ( PERSON_ID integer, FIRSTNAME varchar, LASTNAME varchar)";
        // sqlexec.addText( command );

        File[] srcFiles = new File[1];
        srcFiles[0] = new File( "src/test/data/create-test-tables.sql" );
        // sqlexec.setSrcFiles( srcFiles );

        FileSet ds = new FileSet();
        ds.setBasedir( "src/test" );
        ds.setIncludes( new String[] { "**/drop*.sql" } );
        ds.scan();
        // sqlexec.setFileSet( ds );
        sqlexec.execute( command, srcFiles, ds );

        Assert.assertEquals( 7, sqlexec.getSuccessfulStatements() );
    }

    @Test
    public void testOrderFile()
        throws SQLException
    {
        FileSet ds = new FileSet();
        ds.setBasedir( "src/test" );
        ds.setIncludes( new String[] { "**/drop*.sql", "**/create*.sql" } );
        ds.scan();
        // sqlexec.setFileSet( ds );

        config.setOrderFile( SQLExecConfig.FILE_SORTING_ASC );
        sqlexec.execute( ds );

        Assert.assertEquals( 6, sqlexec.getSuccessfulStatements() );

        try
        {
            config.setOrderFile( SQLExecConfig.FILE_SORTING_DSC );
            sqlexec.execute( ds );
            Assert.fail( "Execution is not aborted on error." );
        }
        catch ( SQLException e )
        {
        }
    }

    @Test
    public void testOnErrorContinueMojo()
        throws SQLException
    {
        String command = "create table BOGUS"; // bad syntax
        config.setOnError( "continue" );
        sqlexec.execute( command );
        Assert.assertEquals( 0, sqlexec.getSuccessfulStatements() );
    }

    @Test
    public void testOnErrorAbortMojo()
        throws SQLException
    {
        String command = "create table BOGUS"; // bad syntax

        try
        {
            sqlexec.execute( command );
            Assert.fail( "Execution is not aborted on error." );

        }
        catch ( SQLException e )
        {

        }

        Assert.assertEquals( 0, sqlexec.getSuccessfulStatements() );
    }

    @Test
    public void testOnErrorAbortAfterMojo()
        throws SQLException
    {
        String commands = "create table BOGUS"; // bad syntax

        File[] srcFiles = new File[1];
        srcFiles[0] = new File( "src/test/data/invalid-syntax.sql" );

        Assert.assertTrue( srcFiles[0].exists() );

        // sqlexec.setSrcFiles( srcFiles );
        config.setOnError( "abortAfter" );

        try
        {
            sqlexec.execute( commands, srcFiles, null );
            Assert.fail( "Execution is not aborted on error." );
        }
        catch ( SQLException e )
        {
            // expected
        }

        Assert.assertEquals( 0, sqlexec.getSuccessfulStatements() );
        Assert.assertEquals( 2, sqlexec.getTotalStatements() );
    }

    @Test
    public void testBadDriver()
        throws SQLException
    {
        config.setDriver( "bad-driver" );
        try
        {
            sqlexec.execute( "" );
            Assert.fail( "Bad driver is not detected" );
        }
        catch ( RuntimeException e )
        {
        }
    }

    @Test
    public void testBadUrl()
        throws SQLException
    {
        config.setUrl( "bad-url" );
        try
        {
            sqlexec.execute( "" );
            Assert.fail( "Bad URL is not detected" );
        }
        catch ( RuntimeException e )
        {
        }
    }

    @Test
    public void testBadFile()
    {
        File[] srcFiles = new File[1];
        srcFiles[0] = new File( "a-every-bogus-file-that-does-not-exist" );

        try
        {
            sqlexec.execute( srcFiles );
            Assert.fail( "Bad files is not detected" );
        }
        catch ( SQLException e )
        {
        }
    }

    @Test
    public void testOnError()
    {
        config.setOnError( "AbOrT" );
        Assert.assertEquals( SQLExecConfig.ON_ERROR_ABORT, config.getOnError() );
        config.setOnError( "cOnTiNuE" );
        Assert.assertEquals( SQLExecConfig.ON_ERROR_CONTINUE, config.getOnError() );
        try
        {
            config.setOnError( "bad" );
            Assert.fail( IllegalArgumentException.class.getName() + " was not thrown." );
        }
        catch ( IllegalArgumentException e )
        {
            // expected
        }
        try
        {
            config.setOnError( null );
            Assert.fail( IllegalArgumentException.class.getName() + " was not thrown." );
        }
        catch ( IllegalArgumentException e )
        {
            // expected
        }
    }

    @Test
    public void testDriverProperties()
        throws SQLException
    {
        Properties driverProperties = this.config.getDriverPropertyMap();
        Assert.assertEquals( 2, driverProperties.size() );
        Assert.assertEquals( "value1", driverProperties.get( "key1" ) );
        Assert.assertEquals( "value2", driverProperties.get( "key2" ) );
    }

    @Test
    public void testBadDriverProperties()
        throws SQLException
    {
        try
        {
            config.setDriverProperties( "key1=value1,key2" );
            config.getDriverPropertyMap();
            Assert.fail( "Unable to detect bad driver properties" );
        }
        catch ( RuntimeException e )
        {

        }
    }

    @Test
    public void testKeepFormat()
        throws SQLException
    {
        // Normally a line starting in -- would be ignored, but with keepformat mode
        // on it will not.
        String command = "--create table PERSON ( PERSON_ID integer, FIRSTNAME varchar, LASTNAME varchar)";
        // sqlexec.addText( command );
        config.setKeepFormat( true );

        try
        {
            sqlexec.execute( command );
            Assert.fail( "-- at the start of the SQL command is ignored." );
        }
        catch ( SQLException e )
        {
        }

        Assert.assertEquals( 0, sqlexec.getSuccessfulStatements() );

    }

    @Test
    public void testBadDelimiter()
        throws Exception
    {
        String command =
            "create table SEPARATOR ( PERSON_ID integer, FIRSTNAME varchar, LASTNAME varchar):"
                + "create table SEPARATOR2 ( PERSON_ID integer, FIRSTNAME varchar, LASTNAME varchar)";

        // sqlexec.addText( command );
        config.setDelimiter( ":" );

        try
        {
            sqlexec.execute( command );
            Assert.fail( "Expected parser error." );
        }
        catch ( SQLException e )
        {
        }
    }

    @Test
    public void testGoodDelimiter()
        throws Exception
    {
        String command =
            "create table SEPARATOR ( PERSON_ID integer, FIRSTNAME varchar, LASTNAME varchar)\n:\n"
                + "create table SEPARATOR2 ( PERSON_ID integer, FIRSTNAME varchar, LASTNAME varchar)";

        // sqlexec.addText( command );
        config.setDelimiter( ":" );

        sqlexec.execute( command );

        Assert.assertEquals( 2, sqlexec.getSuccessfulStatements() );
    }

    @Test
    public void testBadDelimiterType()
        throws Exception
    {
        String command =
            "create table BADDELIMTYPE ( PERSON_ID integer, FIRSTNAME varchar, LASTNAME varchar)" + "\n:"
                + "create table BADDELIMTYPE2 ( PERSON_ID integer, FIRSTNAME varchar, LASTNAME varchar)";

        // sqlexec.addText( command );
        config.setDelimiter( ":" );
        config.setDelimiterType( DefaultSQLExec.DelimiterType.ROW );

        try
        {
            sqlexec.execute( command );
            Assert.fail( "Expected parser error." );
        }
        catch ( SQLException e )
        {
        }
    }

    @Test
    public void testGoodDelimiterType()
        throws Exception
    {
        String command =
            "create table GOODDELIMTYPE ( PERSON_ID integer, FIRSTNAME varchar, LASTNAME varchar)" + "\n:  \n"
                + "create table GOODDELIMTYPE2 ( PERSON_ID integer, FIRSTNAME varchar, LASTNAME varchar)";

        // sqlexec.addText( command );
        config.setDelimiter( ":" );
        config.setDelimiterType( DefaultSQLExec.DelimiterType.ROW );

        sqlexec.execute( command );
        Assert.assertEquals( 2, sqlexec.getSuccessfulStatements() );
    }

}

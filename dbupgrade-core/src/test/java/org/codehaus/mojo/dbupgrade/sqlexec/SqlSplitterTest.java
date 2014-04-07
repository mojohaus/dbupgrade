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


import org.junit.Assert;
import org.junit.Test;

public class SqlSplitterTest
{

    @Test
    public void testContainsSqlString()
        throws Exception
    {
        containsNot( "" );
        containsNot( " " );
        containsNot( "  \t  " );

        contains( ";", 1 );
        contains( "SELECT * from myTable;", 22 );

        contains( "SELECT * from myTable; -- with sl comment", 22 );
        contains( "SELECT * from myTable; /* with part comment */", 22 );

        contains( "SELECT * from myTable /* with part comment inside*/  ; ", 54 );

        contains( "SELECT * from myTable /* with ; semicolon*/  ; ", 46 );

        contains( "INSERT INTO testTable (thevalue) VALUES ('value  !'); -- comment at the end", 53 );

        // a " inside a ' quoted text
        contains( "INSERT INTO testTable (thevalue) VALUES ('value \"  !');", 55 );

        // a ' inside a " quoted text
        contains( "INSERT INTO testTable (thevalue) VALUES (\"value '  !\");", 55 );

        contains( "INSERT INTO testTable (thevalue) VALUES (\"value --  \");", 55 );
        contains( "INSERT INTO testTable (thevalue) VALUES ('value --  ');", 55 );

        containsNot( "SELECT * from myTable where value = ';' AND -- semicolon is quoted!" );

        contains( "INSERT INTO testTable (thevalue) VALUES (' text '' other ');", 60 );

    }

    @Test
    public void testMsSQLStrings()
        throws Exception
    {
        String del = "GO";

        containsNot( "SELECT COUNT(*) FROM Logs", del );
        containsNot( "SELECT * FROM TPersons", del );
        contains( "GO", del, 2 );
    }

    private void contains( String sql, int expectedIndex )
        throws Exception
    {
        contains( sql, ";", expectedIndex );
    }

    private void containsNot( String sql )
        throws Exception
    {
        containsNot( sql, ";" );
    }

    private void contains( String sql, String delimiter, int expectedIndex )
        throws Exception
    {
        Assert.assertEquals( sql, SqlSplitter.containsSqlEnd( sql, delimiter ), expectedIndex );
    }

    private void containsNot( String sql, String delimiter )
        throws Exception
    {
        Assert.assertTrue( sql, SqlSplitter.containsSqlEnd( sql, delimiter ) == SqlSplitter.NO_END );
    }
}

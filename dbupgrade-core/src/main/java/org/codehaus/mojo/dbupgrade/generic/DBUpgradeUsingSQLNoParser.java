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

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.codehaus.mojo.dbupgrade.DBUpgradeException;
import org.codehaus.mojo.dbupgrade.sqlexec.SQLExec;
import org.codehaus.mojo.dbupgrade.sqlexec.SQLUtils;

/**
 * Submit the resource to jdbc in one shot. Use this to execute SQL function/store procedure
 * @author dtran
 *
 */
public class DBUpgradeUsingSQLNoParser
    extends AbstractDBUpgrade
{
    private String sqlResouceName;

    public DBUpgradeUsingSQLNoParser( String sqlResourceName )
    {
        this.sqlResouceName = sqlResourceName;
    }

    public void upgradeDB( SQLExec sqlexec, String dialect )
        throws DBUpgradeException
    {
        InputStream is = null;
        Statement statement = null;
        String sql = null;

        try
        {
            is = DBUpgradeUsingSQL.class.getClassLoader().getResourceAsStream( this.sqlResouceName );
            sql = IOUtils.toString( is );

            statement = sqlexec.getConnection().createStatement();
            statement.setEscapeProcessing( false );

            if ( statement.execute( sql ) )
            {
                //we expect a false return since the execution has no result set
                throw new DBUpgradeException( "Unable execute SQL Statement:" + sql );
            }

        }
        catch ( IOException e )
        {
            throw new DBUpgradeException( "Unable load SQL Statement from resource:" + sqlResouceName, e );
        }
        catch ( SQLException e )
        {
            throw new DBUpgradeException( "Unable execute SQL Statement:" + sql, e );
        }
        finally
        {
            SQLUtils.closeQuietly( statement );
        }

    }

    public String toString()
    {
        return "DBUpgradeUsingSQLNoParser:" + sqlResouceName;
    }

}

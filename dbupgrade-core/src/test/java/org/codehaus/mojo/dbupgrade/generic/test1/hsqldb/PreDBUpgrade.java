package org.codehaus.mojo.dbupgrade.generic.test1.hsqldb;

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
import org.codehaus.mojo.dbupgrade.generic.AbstractDBUpgrade;
import org.codehaus.mojo.dbupgrade.sqlexec.SQLExec;

public class PreDBUpgrade
    extends AbstractDBUpgrade
{
    public void upgradeDB( SQLExec sqlexec, String dialect )
        throws DBUpgradeException
    {
        try
        {
            this.executeSQL( sqlexec, "create table version ( version integer ) " );
            this.executeSQL( sqlexec, "insert into version values ( '-2' )" );
        }
        catch ( Exception e )
        {
            // should ignore if we can not create table
        }
    }

}

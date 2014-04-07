package org.codehaus.mojo.dbupgrade;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

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

public abstract class AbstractDBUpgradeMojo
    extends AbstractMojo
{

    /**
     * Internal Maven's project
     * @since 1.0
     */
    @Parameter( defaultValue = "${project}", readonly = true )
    protected MavenProject project;

    /**
     * Retrieve all messages in the stack trace
     * @param t
     */
    protected static String getExceptionMessages( Throwable t )
    {
        StringBuffer buffer = new StringBuffer();

        while ( t != null )
        {
            buffer.append( t.getMessage() ).append( " " );
            t = t.getCause();
        }

        return buffer.toString();
    }

}

package org.codehaus.mojo.dbupgrade;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

public abstract class AbstractDBUpgradeMojo
    extends AbstractMojo
{

    /**
     * Internal Maven's project
     * 
     * @parameter expression="${project}"
     * @readonly
     * @since 1.0
     * 
     */
    protected MavenProject project;

}

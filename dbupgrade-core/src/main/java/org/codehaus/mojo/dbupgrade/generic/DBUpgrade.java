/**
 * 
 */
package org.codehaus.mojo.dbupgrade.generic;

import org.codehaus.mojo.dbupgrade.DBUpgradeException;
import org.codehaus.mojo.dbupgrade.sqlexec.SQLExec;


/**
 * @author dtran
 *
 */
public interface DBUpgrade
{
    String ROLE = DBUpgrade.class.getName();
    
    void upgradeDB( String dialect ) throws DBUpgradeException;
    void setSqlexec( SQLExec sqlExec );
}

package org.codehaus.mojo.dbupgrade.file;

import org.codehaus.mojo.dbupgrade.DBUpgradeException;


public interface DBUpgradeLifecycle 
{
	String ROLE = DBUpgradeLifecycle.class.getName();
	
	/**
	 * Upgrade database
	 * @return number of upgrader it executes
	 * @throws DBUpgradeException
	 */
	int upgrade() throws DBUpgradeException;
	  
}

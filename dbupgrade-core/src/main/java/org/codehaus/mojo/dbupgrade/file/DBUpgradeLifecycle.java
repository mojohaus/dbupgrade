package org.codehaus.mojo.dbupgrade.file;

import org.codehaus.mojo.dbupgrade.DBUpgradeException;


public interface DBUpgradeLifecycle 
{
	String ROLE = DBUpgradeLifecycle.class.getName();
	
	void upgrade() throws DBUpgradeException;
	  
}

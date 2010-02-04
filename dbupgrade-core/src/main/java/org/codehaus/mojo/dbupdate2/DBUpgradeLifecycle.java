package org.codehaus.mojo.dbupdate2;

import org.codehaus.mojo.dbupgrade.DBUpgradeException;


public interface DBUpgradeLifecycle 
{
	String ROLE = DBUpgradeLifecycle.class.getName();
	
	void upgrade() throws DBUpgradeException;
	  
}

package org.codehaus.mojo.dbupgrade.generic;

import org.codehaus.mojo.dbupgrade.DBUpgradeException;


public interface DBUpgradeLifecycle 
{
	String ROLE = DBUpgradeLifecycle.class.getName();
	
	void upgrade( DBUpgradeConfiguration configuration ) throws DBUpgradeException;
	  
}

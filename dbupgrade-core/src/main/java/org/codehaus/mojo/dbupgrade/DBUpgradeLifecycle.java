package org.codehaus.mojo.dbupgrade;


public interface DBUpgradeLifecycle 
{
	String ROLE = DBUpgradeLifecycle.class.getName();
	
	void upgrade( DBUpgradeConfiguration configuration ) throws DBUpgradeException;
	  
}

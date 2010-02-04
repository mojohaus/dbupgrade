package org.codehaus.mojo.dbupgrade.sqlexec;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;

public interface SQLExec
{
    enum DelimiterType {
        NORMAL,

        ROW
    }

    
    Connection getConnection()
        throws SQLException;

    void rollback()
        throws SQLException;

    void roolbackQuietly();

    void close();

    void execute( String sqlCommand )
        throws SQLException;

    void execute( File[] srcFiles )
        throws SQLException;

    void execute( FileSet fileset )
        throws SQLException;

    void execute( File sqlFile )
        throws SQLException;
    
    void execute( String sqlCommand, File[] srcFiles, FileSet fileset )
        throws SQLException;

    public void execute( InputStream istream )
        throws SQLException;

    public void execute( Reader reader )
        throws SQLException;

}

/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.tools.ant.taskdefs.optional.net;
import com.oroinc.net.ftp.FTPClient;
import com.oroinc.net.ftp.FTPFile;
import com.oroinc.net.ftp.FTPReply;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;

/**
 * Basic FTP client that performs the following actions:
 * <ul>
 *   <li> <strong>send</strong> - send files to a remote server. This is the
 *   default action.</li>
 *   <li> <strong>get</strong> - retrive files from a remote server.</li>
 *   <li> <strong>del</strong> - delete files from a remote server.</li>
 *   <li> <strong>list</strong> - create a file listing.</li>
 * </ul>
 * <strong>Note:</strong> Some FTP servers - notably the Solaris server - seem
 * to hold data ports open after a "retr" operation, allowing them to timeout
 * instead of shutting them down cleanly. This happens in active or passive
 * mode, and the ports will remain open even after ending the FTP session. FTP
 * "send" operations seem to close ports immediately. This behavior may cause
 * problems on some systems when downloading large sets of files.
 *
 * @author Roger Vaughn <a href="mailto:rvaughn@seaconinc.com">
 *      rvaughn@seaconinc.com</a>
 * @author Glenn McAllister <a href="mailto:glennm@ca.ibm.com">glennm@ca.ibm.com
 *      </a>
 * @author <a href="mailto:umagesh@apache.org">Magesh Umasankar</a>
 */
public class FTP
     extends Task
{
    protected final static int SEND_FILES = 0;
    protected final static int GET_FILES = 1;
    protected final static int DEL_FILES = 2;
    protected final static int LIST_FILES = 3;
    protected final static int MK_DIR = 4;

    protected final static String[] ACTION_STRS = {
        "sending",
        "getting",
        "deleting",
        "listing",
        "making directory"
        };

    protected final static String[] COMPLETED_ACTION_STRS = {
        "sent",
        "retrieved",
        "deleted",
        "listed",
        "created directory"
        };
    private boolean binary = true;
    private boolean passive = false;
    private boolean verbose = false;
    private boolean newerOnly = false;
    private int action = SEND_FILES;
    private Vector filesets = new Vector();
    private Vector dirCache = new Vector();
    private int transferred = 0;
    private String remoteFileSep = "/";
    private int port = 21;
    private boolean skipFailedTransfers = false;
    private int skipped = 0;
    private boolean ignoreNoncriticalErrors = false;
    private File listing;
    private String password;

    private String remotedir;
    private String server;
    private String userid;

    /**
     * Sets the FTP action to be taken. Currently accepts "put", "get", "del",
     * "mkdir" and "list".
     *
     * @param action The new Action value
     * @exception BuildException Description of Exception
     */
    public void setAction( Action action )
        throws BuildException
    {
        this.action = action.getAction();
    }

    /**
     * Specifies whether to use binary-mode or text-mode transfers. Set to true
     * to send binary mode. Binary mode is enabled by default.
     *
     * @param binary The new Binary value
     */
    public void setBinary( boolean binary )
    {
        this.binary = binary;
    }

    /**
     * A synonym for setNewer. Set to true to transmit only new or changed
     * files.
     *
     * @param depends The new Depends value
     */
    public void setDepends( boolean depends )
    {
        this.newerOnly = depends;
    }

    /**
     * set the flag to skip errors on dir creation (and maybe later other server
     * specific errors)
     *
     * @param ignoreNoncriticalErrors The new IgnoreNoncriticalErrors value
     */
    public void setIgnoreNoncriticalErrors( boolean ignoreNoncriticalErrors )
    {
        this.ignoreNoncriticalErrors = ignoreNoncriticalErrors;
    }

    /**
     * The output file for the "list" action. This attribute is ignored for any
     * other actions.
     *
     * @param listing The new Listing value
     * @exception BuildException Description of Exception
     */
    public void setListing( File listing )
        throws BuildException
    {
        this.listing = listing;
    }

    /**
     * Set to true to transmit only files that are new or changed from their
     * remote counterparts. The default is to transmit all files.
     *
     * @param newer The new Newer value
     */
    public void setNewer( boolean newer )
    {
        this.newerOnly = newer;
    }

    /**
     * Specifies whether to use passive mode. Set to true if you are behind a
     * firewall and cannot connect without it. Passive mode is disabled by
     * default.
     *
     * @param passive The new Passive value
     */
    public void setPassive( boolean passive )
    {
        this.passive = passive;
    }

    /**
     * Sets the login password for the given user id.
     *
     * @param password The new Password value
     */
    public void setPassword( String password )
    {
        this.password = password;
    }

    /**
     * Sets the FTP port used by the remote server.
     *
     * @param port The new Port value
     */
    public void setPort( int port )
    {
        this.port = port;
    }

    /**
     * Sets the remote directory where files will be placed. This may be a
     * relative or absolute path, and must be in the path syntax expected by the
     * remote server. No correction of path syntax will be performed.
     *
     * @param dir The new Remotedir value
     */
    public void setRemotedir( String dir )
    {
        this.remotedir = dir;
    }

    /**
     * Sets the remote file separator character. This normally defaults to the
     * Unix standard forward slash, but can be manually overridden using this
     * call if the remote server requires some other separator. Only the first
     * character of the string is used.
     *
     * @param separator The new Separator value
     */
    public void setSeparator( String separator )
    {
        remoteFileSep = separator;
    }

    /**
     * Sets the FTP server to send files to.
     *
     * @param server The new Server value
     */
    public void setServer( String server )
    {
        this.server = server;
    }


    /**
     * set the failed transfer flag
     *
     * @param skipFailedTransfers The new SkipFailedTransfers value
     */
    public void setSkipFailedTransfers( boolean skipFailedTransfers )
    {
        this.skipFailedTransfers = skipFailedTransfers;
    }

    /**
     * Sets the login user id to use on the specified server.
     *
     * @param userid The new Userid value
     */
    public void setUserid( String userid )
    {
        this.userid = userid;
    }

    /**
     * Set to true to receive notification about each file as it is transferred.
     *
     * @param verbose The new Verbose value
     */
    public void setVerbose( boolean verbose )
    {
        this.verbose = verbose;
    }

    /**
     * Adds a set of files (nested fileset attribute).
     *
     * @param set The feature to be added to the Fileset attribute
     */
    public void addFileset( FileSet set )
    {
        filesets.addElement( set );
    }

    /**
     * Runs the task.
     *
     * @exception BuildException Description of Exception
     */
    public void execute()
        throws BuildException
    {
        checkConfiguration();

        FTPClient ftp = null;

        try
        {
            log( "Opening FTP connection to " + server, Project.MSG_VERBOSE );

            ftp = new FTPClient();

            ftp.connect( server, port );
            if( !FTPReply.isPositiveCompletion( ftp.getReplyCode() ) )
            {
                throw new BuildException( "FTP connection failed: " + ftp.getReplyString() );
            }

            log( "connected", Project.MSG_VERBOSE );
            log( "logging in to FTP server", Project.MSG_VERBOSE );

            if( !ftp.login( userid, password ) )
            {
                throw new BuildException( "Could not login to FTP server" );
            }

            log( "login succeeded", Project.MSG_VERBOSE );

            if( binary )
            {
                ftp.setFileType( com.oroinc.net.ftp.FTP.IMAGE_FILE_TYPE );
                if( !FTPReply.isPositiveCompletion( ftp.getReplyCode() ) )
                {
                    throw new BuildException(
                        "could not set transfer type: " +
                        ftp.getReplyString() );
                }
            }

            if( passive )
            {
                log( "entering passive mode", Project.MSG_VERBOSE );
                ftp.enterLocalPassiveMode();
                if( !FTPReply.isPositiveCompletion( ftp.getReplyCode() ) )
                {
                    throw new BuildException(
                        "could not enter into passive mode: " +
                        ftp.getReplyString() );
                }
            }

            // If the action is MK_DIR, then the specified remote directory is the
            // directory to create.

            if( action == MK_DIR )
            {

                makeRemoteDir( ftp, remotedir );

            }
            else
            {
                if( remotedir != null )
                {
                    log( "changing the remote directory", Project.MSG_VERBOSE );
                    ftp.changeWorkingDirectory( remotedir );
                    if( !FTPReply.isPositiveCompletion( ftp.getReplyCode() ) )
                    {
                        throw new BuildException(
                            "could not change remote directory: " +
                            ftp.getReplyString() );
                    }
                }
                log( ACTION_STRS[action] + " files" );
                transferFiles( ftp );
            }

        }
        catch( IOException ex )
        {
            throw new BuildException( "error during FTP transfer: " + ex );
        }
        finally
        {
            if( ftp != null && ftp.isConnected() )
            {
                try
                {
                    log( "disconnecting", Project.MSG_VERBOSE );
                    ftp.logout();
                    ftp.disconnect();
                }
                catch( IOException ex )
                {
                    // ignore it
                }
            }
        }
    }

    /**
     * Retrieve a single file to the remote host. <code>filename</code> may
     * contain a relative path specification. The file will then be retreived
     * using the entire relative path spec - no attempt is made to change
     * directories. It is anticipated that this may eventually cause problems
     * with some FTP servers, but it simplifies the coding.
     *
     * @param ftp Description of Parameter
     * @param dir Description of Parameter
     * @param filename Description of Parameter
     * @exception IOException Description of Exception
     * @exception BuildException Description of Exception
     */
    protected void getFile( FTPClient ftp, String dir, String filename )
        throws IOException, BuildException
    {
        OutputStream outstream = null;
        try
        {
            File file = resolveFile( new File( dir, filename ).getPath() );

            if( newerOnly && isUpToDate( ftp, file, resolveFile( filename ) ) )
                return;

            if( verbose )
            {
                log( "transferring " + filename + " to " + file.getAbsolutePath() );
            }

            File pdir = new File( file.getParent() );// stay 1.1 compatible
            if( !pdir.exists() )
            {
                pdir.mkdirs();
            }
            outstream = new BufferedOutputStream( new FileOutputStream( file ) );
            ftp.retrieveFile( resolveFile( filename ), outstream );

            if( !FTPReply.isPositiveCompletion( ftp.getReplyCode() ) )
            {
                String s = "could not get file: " + ftp.getReplyString();
                if( skipFailedTransfers == true )
                {
                    log( s, Project.MSG_WARN );
                    skipped++;
                }
                else
                {
                    throw new BuildException( s );
                }

            }
            else
            {
                log( "File " + file.getAbsolutePath() + " copied from " + server,
                    Project.MSG_VERBOSE );
                transferred++;
            }
        }
        finally
        {
            if( outstream != null )
            {
                try
                {
                    outstream.close();
                }
                catch( IOException ex )
                {
                    // ignore it
                }
            }
        }
    }

    /**
     * Checks to see if the remote file is current as compared with the local
     * file. Returns true if the remote file is up to date.
     *
     * @param ftp Description of Parameter
     * @param localFile Description of Parameter
     * @param remoteFile Description of Parameter
     * @return The UpToDate value
     * @exception IOException Description of Exception
     * @exception BuildException Description of Exception
     */
    protected boolean isUpToDate( FTPClient ftp, File localFile, String remoteFile )
        throws IOException, BuildException
    {
        log( "checking date for " + remoteFile, Project.MSG_VERBOSE );

        FTPFile[] files = ftp.listFiles( remoteFile );

        // For Microsoft's Ftp-Service an Array with length 0 is
        // returned if configured to return listings in "MS-DOS"-Format
        if( files == null || files.length == 0 )
        {
            // If we are sending files, then assume out of date.
            // If we are getting files, then throw an error

            if( action == SEND_FILES )
            {
                log( "Could not date test remote file: " + remoteFile
                     + "assuming out of date.", Project.MSG_VERBOSE );
                return false;
            }
            else
            {
                throw new BuildException( "could not date test remote file: " +
                    ftp.getReplyString() );
            }
        }

        long remoteTimestamp = files[0].getTimestamp().getTime().getTime();
        long localTimestamp = localFile.lastModified();
        if( this.action == SEND_FILES )
        {
            return remoteTimestamp > localTimestamp;
        }
        else
        {
            return localTimestamp > remoteTimestamp;
        }
    }

    /**
     * Checks to see that all required parameters are set.
     *
     * @exception BuildException Description of Exception
     */
    protected void checkConfiguration()
        throws BuildException
    {
        if( server == null )
        {
            throw new BuildException( "server attribute must be set!" );
        }
        if( userid == null )
        {
            throw new BuildException( "userid attribute must be set!" );
        }
        if( password == null )
        {
            throw new BuildException( "password attribute must be set!" );
        }

        if( ( action == LIST_FILES ) && ( listing == null ) )
        {
            throw new BuildException( "listing attribute must be set for list action!" );
        }

        if( action == MK_DIR && remotedir == null )
        {
            throw new BuildException( "remotedir attribute must be set for mkdir action!" );
        }
    }

    /**
     * Creates all parent directories specified in a complete relative pathname.
     * Attempts to create existing directories will not cause errors.
     *
     * @param ftp Description of Parameter
     * @param filename Description of Parameter
     * @exception IOException Description of Exception
     * @exception BuildException Description of Exception
     */
    protected void createParents( FTPClient ftp, String filename )
        throws IOException, BuildException
    {
        Vector parents = new Vector();
        File dir = new File( filename );
        String dirname;

        while( ( dirname = dir.getParent() ) != null )
        {
            dir = new File( dirname );
            parents.addElement( dir );
        }

        for( int i = parents.size() - 1; i >= 0; i-- )
        {
            dir = ( File )parents.elementAt( i );
            if( !dirCache.contains( dir ) )
            {
                log( "creating remote directory " + resolveFile( dir.getPath() ),
                    Project.MSG_VERBOSE );
                ftp.makeDirectory( resolveFile( dir.getPath() ) );
                // Both codes 550 and 553 can be produced by FTP Servers
                //  to indicate that an attempt to create a directory has
                //  failed because the directory already exists.
                int result = ftp.getReplyCode();
                if( !FTPReply.isPositiveCompletion( result ) &&
                    ( result != 550 ) && ( result != 553 ) &&
                    !ignoreNoncriticalErrors )
                {
                    throw new BuildException(
                        "could not create directory: " +
                        ftp.getReplyString() );
                }
                dirCache.addElement( dir );
            }
        }
    }

    /**
     * Delete a file from the remote host.
     *
     * @param ftp Description of Parameter
     * @param filename Description of Parameter
     * @exception IOException Description of Exception
     * @exception BuildException Description of Exception
     */
    protected void delFile( FTPClient ftp, String filename )
        throws IOException, BuildException
    {
        if( verbose )
        {
            log( "deleting " + filename );
        }

        if( !ftp.deleteFile( resolveFile( filename ) ) )
        {
            String s = "could not delete file: " + ftp.getReplyString();
            if( skipFailedTransfers == true )
            {
                log( s, Project.MSG_WARN );
                skipped++;
            }
            else
            {
                throw new BuildException( s );
            }
        }
        else
        {
            log( "File " + filename + " deleted from " + server, Project.MSG_VERBOSE );
            transferred++;
        }
    }

    /**
     * List information about a single file from the remote host. <code>filename</code>
     * may contain a relative path specification. The file listing will then be
     * retrieved using the entire relative path spec - no attempt is made to
     * change directories. It is anticipated that this may eventually cause
     * problems with some FTP servers, but it simplifies the coding.
     *
     * @param ftp Description of Parameter
     * @param bw Description of Parameter
     * @param filename Description of Parameter
     * @exception IOException Description of Exception
     * @exception BuildException Description of Exception
     */
    protected void listFile( FTPClient ftp, BufferedWriter bw, String filename )
        throws IOException, BuildException
    {
        if( verbose )
        {
            log( "listing " + filename );
        }

        FTPFile ftpfile = ftp.listFiles( resolveFile( filename ) )[0];
        bw.write( ftpfile.toString() );
        bw.newLine();

        transferred++;
    }

    /**
     * Create the specified directory on the remote host.
     *
     * @param ftp The FTP client connection
     * @param dir The directory to create (format must be correct for host type)
     * @exception IOException Description of Exception
     * @exception BuildException Description of Exception
     */
    protected void makeRemoteDir( FTPClient ftp, String dir )
        throws IOException, BuildException
    {
        if( verbose )
        {
            log( "creating directory: " + dir );
        }

        if( !ftp.makeDirectory( dir ) )
        {
            // codes 521, 550 and 553 can be produced by FTP Servers
            //  to indicate that an attempt to create a directory has
            //  failed because the directory already exists.

            int rc = ftp.getReplyCode();
            if( !( ignoreNoncriticalErrors && ( rc == 550 || rc == 553 || rc == 521 ) ) )
            {
                throw new BuildException( "could not create directory: " +
                    ftp.getReplyString() );
            }

            if( verbose )
            {
                log( "directory already exists" );
            }
        }
        else
        {
            if( verbose )
            {
                log( "directory created OK" );
            }
        }
    }

    /**
     * Correct a file path to correspond to the remote host requirements. This
     * implementation currently assumes that the remote end can handle
     * Unix-style paths with forward-slash separators. This can be overridden
     * with the <code>separator</code> task parameter. No attempt is made to
     * determine what syntax is appropriate for the remote host.
     *
     * @param file Description of Parameter
     * @return Description of the Returned Value
     */
    protected String resolveFile( String file )
    {
        return file.replace( System.getProperty( "file.separator" ).charAt( 0 ),
            remoteFileSep.charAt( 0 ) );
    }

    /**
     * Sends a single file to the remote host. <code>filename</code> may contain
     * a relative path specification. When this is the case, <code>sendFile</code>
     * will attempt to create any necessary parent directories before sending
     * the file. The file will then be sent using the entire relative path spec
     * - no attempt is made to change directories. It is anticipated that this
     * may eventually cause problems with some FTP servers, but it simplifies
     * the coding.
     *
     * @param ftp Description of Parameter
     * @param dir Description of Parameter
     * @param filename Description of Parameter
     * @exception IOException Description of Exception
     * @exception BuildException Description of Exception
     */
    protected void sendFile( FTPClient ftp, String dir, String filename )
        throws IOException, BuildException
    {
        InputStream instream = null;
        try
        {
            File file = resolveFile( new File( dir, filename ).getPath() );

            if( newerOnly && isUpToDate( ftp, file, resolveFile( filename ) ) )
                return;

            if( verbose )
            {
                log( "transferring " + file.getAbsolutePath() );
            }

            instream = new BufferedInputStream( new FileInputStream( file ) );

            createParents( ftp, filename );

            ftp.storeFile( resolveFile( filename ), instream );
            boolean success = FTPReply.isPositiveCompletion( ftp.getReplyCode() );
            if( !success )
            {
                String s = "could not put file: " + ftp.getReplyString();
                if( skipFailedTransfers == true )
                {
                    log( s, Project.MSG_WARN );
                    skipped++;
                }
                else
                {
                    throw new BuildException( s );
                }

            }
            else
            {

                log( "File " + file.getAbsolutePath() +
                    " copied to " + server,
                    Project.MSG_VERBOSE );
                transferred++;
            }
        }
        finally
        {
            if( instream != null )
            {
                try
                {
                    instream.close();
                }
                catch( IOException ex )
                {
                    // ignore it
                }
            }
        }
    }

    /**
     * For each file in the fileset, do the appropriate action: send, get,
     * delete, or list.
     *
     * @param ftp Description of Parameter
     * @param fs Description of Parameter
     * @return Description of the Returned Value
     * @exception IOException Description of Exception
     * @exception BuildException Description of Exception
     */
    protected int transferFiles( FTPClient ftp, FileSet fs )
        throws IOException, BuildException
    {
        FileScanner ds;

        if( action == SEND_FILES )
        {
            ds = fs.getDirectoryScanner( project );
        }
        else
        {
            ds = new FTPDirectoryScanner( ftp );
            fs.setupDirectoryScanner( ds, project );
            ds.scan();
        }

        String[] dsfiles = ds.getIncludedFiles();
        String dir = null;
        if( ( ds.getBasedir() == null ) && ( ( action == SEND_FILES ) || ( action == GET_FILES ) ) )
        {
            throw new BuildException( "the dir attribute must be set for send and get actions" );
        }
        else
        {
            if( ( action == SEND_FILES ) || ( action == GET_FILES ) )
            {
                dir = ds.getBasedir().getAbsolutePath();
            }
        }

        // If we are doing a listing, we need the output stream created now.
        BufferedWriter bw = null;
        if( action == LIST_FILES )
        {
            File pd = new File( listing.getParent() );
            if( !pd.exists() )
            {
                pd.mkdirs();
            }
            bw = new BufferedWriter( new FileWriter( listing ) );
        }

        for( int i = 0; i < dsfiles.length; i++ )
        {
            switch ( action )
            {
            case SEND_FILES:
            {
                sendFile( ftp, dir, dsfiles[i] );
                break;
            }

            case GET_FILES:
            {
                getFile( ftp, dir, dsfiles[i] );
                break;
            }

            case DEL_FILES:
            {
                delFile( ftp, dsfiles[i] );
                break;
            }

            case LIST_FILES:
            {
                listFile( ftp, bw, dsfiles[i] );
                break;
            }

            default:
            {
                throw new BuildException( "unknown ftp action " + action );
            }
            }
        }

        if( action == LIST_FILES )
        {
            bw.close();
        }

        return dsfiles.length;
    }

    /**
     * Sends all files specified by the configured filesets to the remote
     * server.
     *
     * @param ftp Description of Parameter
     * @exception IOException Description of Exception
     * @exception BuildException Description of Exception
     */
    protected void transferFiles( FTPClient ftp )
        throws IOException, BuildException
    {
        transferred = 0;
        skipped = 0;

        if( filesets.size() == 0 )
        {
            throw new BuildException( "at least one fileset must be specified." );
        }
        else
        {
            // get files from filesets
            for( int i = 0; i < filesets.size(); i++ )
            {
                FileSet fs = ( FileSet )filesets.elementAt( i );
                if( fs != null )
                {
                    transferFiles( ftp, fs );
                }
            }
        }

        log( transferred + " files " + COMPLETED_ACTION_STRS[action] );
        if( skipped != 0 )
        {
            log( skipped + " files were not successfully " + COMPLETED_ACTION_STRS[action] );
        }
    }

    public static class Action extends EnumeratedAttribute
    {

        private final static String[] validActions = {
            "send", "put", "recv", "get", "del", "delete", "list", "mkdir"
            };

        public int getAction()
        {
            String actionL = getValue().toLowerCase( Locale.US );
            if( actionL.equals( "send" ) ||
                actionL.equals( "put" ) )
            {
                return SEND_FILES;
            }
            else if( actionL.equals( "recv" ) ||
                actionL.equals( "get" ) )
            {
                return GET_FILES;
            }
            else if( actionL.equals( "del" ) ||
                actionL.equals( "delete" ) )
            {
                return DEL_FILES;
            }
            else if( actionL.equals( "list" ) )
            {
                return LIST_FILES;
            }
            else if( actionL.equals( "mkdir" ) )
            {
                return MK_DIR;
            }
            return SEND_FILES;
        }

        public String[] getValues()
        {
            return validActions;
        }
    }

    protected class FTPDirectoryScanner extends DirectoryScanner
    {
        protected FTPClient ftp = null;

        public FTPDirectoryScanner( FTPClient ftp )
        {
            super();
            this.ftp = ftp;
        }

        public void scan()
        {
            if( includes == null )
            {
                // No includes supplied, so set it to 'matches all'
                includes = new String[1];
                includes[0] = "**";
            }
            if( excludes == null )
            {
                excludes = new String[0];
            }

            filesIncluded = new Vector();
            filesNotIncluded = new Vector();
            filesExcluded = new Vector();
            dirsIncluded = new Vector();
            dirsNotIncluded = new Vector();
            dirsExcluded = new Vector();

            try
            {
                String cwd = ftp.printWorkingDirectory();
                scandir( ".", "", true );// always start from the current ftp working dir
                ftp.changeWorkingDirectory( cwd );
            }
            catch( IOException e )
            {
                throw new BuildException( "Unable to scan FTP server: ", e );
            }
        }

        protected void scandir( String dir, String vpath, boolean fast )
        {
            try
            {
                if( !ftp.changeWorkingDirectory( dir ) )
                {
                    return;
                }

                FTPFile[] newfiles = ftp.listFiles();
                if( newfiles == null )
                {
                    ftp.changeToParentDirectory();
                    return;
                }

                for( int i = 0; i < newfiles.length; i++ )
                {
                    FTPFile file = newfiles[i];
                    if( !file.getName().equals( "." ) && !file.getName().equals( ".." ) )
                    {
                        if( file.isDirectory() )
                        {
                            String name = file.getName();
                            if( isIncluded( name ) )
                            {
                                if( !isExcluded( name ) )
                                {
                                    dirsIncluded.addElement( name );
                                    if( fast )
                                    {
                                        scandir( name, vpath + name + File.separator, fast );
                                    }
                                }
                                else
                                {
                                    dirsExcluded.addElement( name );
                                }
                            }
                            else
                            {
                                dirsNotIncluded.addElement( name );
                                if( fast && couldHoldIncluded( name ) )
                                {
                                    scandir( name, vpath + name + File.separator, fast );
                                }
                            }
                            if( !fast )
                            {
                                scandir( name, vpath + name + File.separator, fast );
                            }
                        }
                        else
                        {
                            if( file.isFile() )
                            {
                                String name = vpath + file.getName();
                                if( isIncluded( name ) )
                                {
                                    if( !isExcluded( name ) )
                                    {
                                        filesIncluded.addElement( name );
                                    }
                                    else
                                    {
                                        filesExcluded.addElement( name );
                                    }
                                }
                                else
                                {
                                    filesNotIncluded.addElement( name );
                                }
                            }
                        }
                    }
                }
                ftp.changeToParentDirectory();
            }
            catch( IOException e )
            {
                throw new BuildException( "Error while communicating with FTP server: ", e );
            }
        }
    }
}

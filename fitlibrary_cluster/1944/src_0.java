/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.differences;

import java.io.File;

import fitlibrary.log.Logging;
import fitlibrary.utility.StringUtility;

public class FolderRunnerLocalFile implements LocalFile {
    protected File file;
    private static File CONTEXT = new File(".");

    public FolderRunnerLocalFile(File file) {
        this.file = file;
    }
    public FolderRunnerLocalFile(String localFileNameInitial) {
    	String localFileName = localFileNameInitial;
        if (!localFileName.startsWith("files/") && !localFileName.startsWith("files\\"))
            localFileName = "files/"+localFileName;
        Logging.log(this,"FolderRunnerLocalFile(): "+localFileName);
        this.file = new File(CONTEXT,localFileName);
    }
    public LocalFile withSuffix(String suffix) {
        String name = file.getPath();
        int last = name.lastIndexOf(".");
        if (last >= 0)
            name = name.substring(0,last+1)+suffix;
        Logging.log(this,"withSuffix(): "+name);
        return new FolderRunnerLocalFile(name);
    }
    public File getFile() {
        Logging.log(this,"getFile(): "+file.getAbsolutePath());
        return file;
    }
    public void mkdirs() {
        File diry = file.getParentFile();
        Logging.log(this,"mkdirs(): "+diry.getAbsolutePath());
        if (!diry.exists())
            diry.mkdirs();
    }
    public String htmlImageLink() {  
        return "<img src=\"file:///"+escape(file.getPath())+"\">";
    }
    public String htmlLink() { 
        return "<a href=\"file:///"+escape(file.getPath())+"\">"+file.getName()+"</a>";
    }
    protected String escape(String path) {
    	return StringUtility.replaceString(path,"\\","/").replaceAll(" ", "%20");
    }
    @Override
	public boolean equals(Object object) {
        if (!(object instanceof FolderRunnerLocalFile))
            return false;
        String absolutePath = ((FolderRunnerLocalFile)object).file.getPath();
        String otherAbsolutePath = file.getPath();
        boolean equals = absolutePath.equals(otherAbsolutePath);
        Logging.log(this,"equals(): "+equals+" with: '"+absolutePath+"' and '"+otherAbsolutePath);
        return equals;
    }
    @Override
	public int hashCode() {
        return file.hashCode();
    }
    @Override
	public String toString() {
        return "FolderRunnerLocalFile["+file.getName()+"]";
    }
    public static void setContext(File context) {
        CONTEXT  = context;
    }
}

package com.filepassapp.chenze.filepass;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;


public class FileTool 
{
	public static final int LOADER_BROWSE = 0;
	
	public static final String FILE_LIST_KEY = "FileListKey";
	public static final String PATH_KEY = "PATH";
 
    public  static String address;
    private static String domain;
    private static String username;
    private static String password;
    

	static {	
		address = "";
		domain = "";
		username = "";
		password = "";
	}
	
	static public String getAddress()
	{
		return address;
	}
	
	static public String getDomain()
	{
		return domain;
	}
	
	static public String getUsername()
	{
		return username;
	}
	
	static public String getPassword()
	{
		return password;
	}
	
	static public void setServerConnection(String address, String domain, String username, String password)
	{
	
		FileTool.address = address;
		FileTool.domain = domain;
		FileTool.username = username;
		FileTool.password = password;
	}
	
  	
	static public ArrayList<FileDescription> readFolder(String path)
	{	
		FileDescription fileDescription;
		ArrayList<FileDescription> fileList = null;
		
	  	try 
	  	{
			String url = "smb://" + address + '/' + path;
			
			NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, username, password);
			
			SmbFile file = new SmbFile(url, auth);		
			SmbFile[] fileArray = file.listFiles();

			fileList = new ArrayList<FileDescription>();
			for(SmbFile item :  fileArray)
			{	                	
				fileDescription = new FileDescription(); 	
			
				fileDescription.fileName = item.getName();
				fileDescription.isFolder = item.isDirectory();
				fileDescription.size = item.length();
				
				fileList.add(fileDescription);	
			}
			 
			return fileList;
        
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	
	static public File copyToPhone(String strPCPath)
	{
	    SmbFile smbFileToDownload = null;      
	    
	    try 
	    {
	        File localFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS+"/Temporalfolder");

	        // create sdcard path if not exist.
	        if (!localFilePath.isDirectory()) 
	        {
	            localFilePath.mkdir();
	        }
	        try 
	        {         

				String url = "smb://" + address + '/' + strPCPath;				
				NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, username, password);
	            smbFileToDownload = new SmbFile(url , auth);
	            String smbFileName = smbFileToDownload.getName();
                InputStream inputStream = smbFileToDownload.getInputStream();

                File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"/Temporalfolder/"+smbFileName);
                
                OutputStream out = new FileOutputStream(localFile);
                
                byte buf[] = new byte[16 * 1024 * 1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) 
                {
                    out.write(buf, 0, len);
                }
                out.flush();
                out.close();
                inputStream.close();
                return localFile;
	        }
	        catch (Exception e) 
	        {
	            e.printStackTrace();
		        return null;
	        }
	    } 
	    catch (Exception e) 
	    {
	        e.printStackTrace();
	        return null;
	    }   

	}

}

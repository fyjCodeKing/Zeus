package com.filepassapp.chenze.filepass;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class FileListActivity extends Activity implements LoaderManager.LoaderCallbacks<ArrayList<FileDescription>>
{
	private ListView listView;
	private com.filepassapp.chenze.filepass.FileListAdapter listAdapter;
	private ArrayList<com.filepassapp.chenze.filepass.FileDescription> fileList ;
	private LoaderManager lm;
	private String path;
	private FileListActivity fileListActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		fileListActivity = this;
    	loadPreferences();
    	setContentView(R.layout.filemanager);
    	    	
      	if (savedInstanceState != null) 
      	{

      		fileList = savedInstanceState.getParcelableArrayList(com.filepassapp.chenze.filepass.FileTool.FILE_LIST_KEY);
      		path = savedInstanceState.getString(com.filepassapp.chenze.filepass.FileTool.PATH_KEY,"");
      	}
      	else
      	{	
      		Intent intent = getIntent();
      		path = intent.getStringExtra(com.filepassapp.chenze.filepass.FileTool.PATH_KEY);
      		if (path==null)	path = "";
        	lm = getLoaderManager(); 
		    Bundle pathString = new Bundle();
		    pathString.putString(FileTool.PATH_KEY, path);
		    lm.restartLoader(FileTool.LOADER_BROWSE, pathString, fileListActivity);

      	}
		
      	listView=(ListView)findViewById(R.id.list);  
    	listAdapter = new FileListAdapter(this,fileList);
    	listView.setAdapter(listAdapter);
    	listView.setOnItemClickListener(new OnItemClickListener() {
    		@Override
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
        	{
				com.filepassapp.chenze.filepass.FileDescription fileDescription = (com.filepassapp.chenze.filepass.FileDescription) parent.getItemAtPosition(position);

    			if (fileDescription.isFolder)
    			{
       				Intent intent;				
  
    			    intent = new Intent (view.getContext(), FileListActivity.class);
    		        intent.putExtra(com.filepassapp.chenze.filepass.FileTool.PATH_KEY, path + fileDescription.fileName);
    	        	startActivity(intent);	   				
    			}
    			else
    			{   	
    				AsyncFileAccess copyAsyncTask = new AsyncFileAccess();
    				copyAsyncTask.execute(path + fileDescription.fileName, com.filepassapp.chenze.filepass.FileTool.getAddress(), com.filepassapp.chenze.filepass.FileTool.getDomain(), com.filepassapp.chenze.filepass.FileTool.getUsername(), com.filepassapp.chenze.filepass.FileTool.getPassword());
    			}

        	}
    	});  


     
        if (fileList!=null) 
        {
       		showResultSet(fileList);
        }    	
	}

	private void loadPreferences()
	{
	    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	   	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		com.filepassapp.chenze.filepass.FileTool.setServerConnection( sharedPref.getString("shared_resource_address", ""),
    			sharedPref.getString("domain_name", ""), 
    			sharedPref.getString("user_name", ""), 
    			sharedPref.getString("password", ""));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
 	    	Intent intent = new Intent (this, com.filepassapp.chenze.filepass.SettingsActivity.class);
        	startActivity(intent);    
			return true;
		}
		return super.onOptionsItemSelected(item);		
	}
	
    @Override
    public void onSaveInstanceState(Bundle savedState) 
    {
	   	super.onSaveInstanceState(savedState);
        savedState.putParcelableArrayList(com.filepassapp.chenze.filepass.FileTool.FILE_LIST_KEY, fileList);
        savedState.putString(com.filepassapp.chenze.filepass.FileTool.PATH_KEY, path);
	}   
    
	void showResultSet(ArrayList<com.filepassapp.chenze.filepass.FileDescription> fileListLoaded)
	{
		if (fileListLoaded == null) 
			fileList = null;
		else
		{
			fileList = new ArrayList<com.filepassapp.chenze.filepass.FileDescription>();
			for (com.filepassapp.chenze.filepass.FileDescription item : fileListLoaded)
			{
				fileList.add(item);
			}
		}
		if (listAdapter!=null) 
			listAdapter.showResultSet(fileList);
	}

	@Override
	public Loader<ArrayList<com.filepassapp.chenze.filepass.FileDescription>> onCreateLoader(int id, Bundle filter)
	{
		return new com.filepassapp.chenze.filepass.FileListLoader(this,id,filter);
	}
	
	@Override
	public void onLoadFinished(Loader<ArrayList<com.filepassapp.chenze.filepass.FileDescription>> loader, ArrayList<com.filepassapp.chenze.filepass.FileDescription> fileList)
	{
	    if (fileList==null)
	    {			    
        	Toast.makeText(this, "Shared folder not found", Toast.LENGTH_SHORT).show();
	    }
	    else
	    	showResultSet(fileList);
	}
	
	@Override 
	public void onLoaderReset(Loader<ArrayList<com.filepassapp.chenze.filepass.FileDescription>> loader)
	{

	}
	
	public class AsyncFileAccess extends AsyncTask<String, Integer, File> 
	{
		ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(FileListActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setMessage("Loading");
			dialog.setIndeterminate(false);
			dialog.setCancelable(false);   
			dialog.setMax(100);
			dialog.setProgress(100);
			dialog.show();
	    }
		
	    @Override 
	    protected File doInBackground(String... strPCPath /* path, address, domain, username, password */) {
	    	
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

					String url = "smb://" + strPCPath[1] + '/' + strPCPath[0];				
					NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(strPCPath[2], strPCPath[3], strPCPath[4]);
		            smbFileToDownload = new SmbFile(url , auth);
		            String smbFileName = smbFileToDownload.getName();
	                InputStream inputStream = smbFileToDownload.getInputStream();

	                File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"/Temporalfolder/"+smbFileName);
	                long fileLength = smbFileToDownload.length();
	                
	                OutputStream out = new FileOutputStream(localFile);
	                
	                byte buf[] = new byte[16 * 1024];
	                int len;
	                int cicles = 0;
	                while ((len = inputStream.read(buf)) > 0) 
	                {
	                    out.write(buf, 0, len);
	                    cicles++;
	                    int percentage = (100*cicles)/((int)(fileLength / (16*1024)) + 1);
	                    publishProgress(percentage);
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
	    
	    @Override
	    protected void onProgressUpdate(Integer... values) {
	        int progress = values[0].intValue();
	        dialog.setProgress(progress);
	    }
	    
	   
	    @Override protected void onPostExecute(File file) {
	    	dialog.dismiss();
	    	Uri uri = Uri.fromFile(file);
	        
			Intent intent = new Intent(Intent.ACTION_VIEW);
			// Check what kind of file you are trying to open, by comparing the url with extensions.
			// When the if condition is matched, plugin sets the correct intent (mime) type, 
			// so Android knew what application to use to open the file
			if (file.toString().contains(".doc") || file.toString().contains(".docx")) {
			    // Word document
			    intent.setDataAndType(uri, "application/msword");
			} else if(file.toString().toLowerCase().contains(".pdf")) {
			    // PDF file
			    intent.setDataAndType(uri, "application/pdf");
			} else if(file.toString().toLowerCase().contains(".xls") || file.toString().toLowerCase().contains(".xlsx")) {
			    // Excel file
			    intent.setDataAndType(uri, "application/vnd.ms-excel");
			} else if(file.toString().toLowerCase().contains(".zip") || file.toString().toLowerCase().contains(".rar"))  {
			    // ZIP Files
			    intent.setDataAndType(uri, "application/zip");
			} else if(file.toString().toLowerCase().contains(".rtf")) {
			    // RTF file
			    intent.setDataAndType(uri, "application/rtf");
			} else if(file.toString().toLowerCase().contains(".gif")) {
			    // GIF file
			    intent.setDataAndType(uri, "image/gif");
			} else if(file.toString().toLowerCase().contains(".jpg") || file.toString().toLowerCase().contains(".jpeg") || file.toString().toLowerCase().contains(".png")) {
			    // JPG file
			    intent.setDataAndType(uri, "image/jpeg");
			} else if(file.toString().toLowerCase().contains(".txt")) {
			    // Text file
			    intent.setDataAndType(uri, "text/plain");
			} else {
			    //if you want you can also define the intent type for any other file
			    
			    //additionally use else clause below, to manage other unknown extensions
			    //in this case, Android will show all applications installed on the device
			    //so you can choose which application to use
			    intent.setDataAndType(uri, "*/*");
			}
	        
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	        startActivity(intent);
	    }

	    @Override
	    protected void onCancelled() {
	    	 dialog.dismiss();
	    }


	}

}

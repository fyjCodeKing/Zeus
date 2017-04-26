package com.filepassapp.chenze.filepass;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Locale;



public class FileListLoader extends AsyncTaskLoader<ArrayList<FileDescription>> {
	private ArrayList<FileDescription> fileList;
	private String path;
	private int loaderMode;

	
	public FileListLoader(Context ctx,int id, Bundle filter)
	{
		super(ctx);
		path = filter.getString(FileTool.PATH_KEY,"").toUpperCase(Locale.US);	
		loaderMode = id;
	
	}
	
	@Override
	public ArrayList<FileDescription> loadInBackground()
	{	
		fileList = FileTool.readFolder(path);
		return fileList;

 	}
	
	
	@Override
	public void deliverResult(ArrayList<FileDescription> arrayList)
	{
		fileList = arrayList;
		
		if (isStarted())
		{
			super.deliverResult(arrayList);
		}
	}
	
	@Override
	protected void onStartLoading()
	{

		if (fileList != null)
		{
			deliverResult(fileList);
		}
		else
		{
			forceLoad();
		}
		
	}
	
	@Override
	protected void onStopLoading()
	{
		cancelLoad();
	}
	
	@Override
	protected void onReset()
	{
		if (fileList!=null)
	    {
			fileList.clear();
			fileList = null;
    	}
		onStopLoading();
	}
	
	@Override
	public void onCanceled(ArrayList<FileDescription> arrayList)
	{
		super.onCanceled(arrayList);
	}
	
}

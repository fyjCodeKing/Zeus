package com.filepassapp.chenze.filepass;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FileListAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private ArrayList<com.filepassapp.chenze.filepass.FileDescription> mFileList;
	
	public FileListAdapter(Activity activity,ArrayList<com.filepassapp.chenze.filepass.FileDescription> fileList)
	{
		if(fileList==null){
			mFileList = new ArrayList<>();
		}
		else
		mFileList = fileList;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount()
	{
		if (mFileList == null)
			return 0;
		else
			return mFileList.size();
	}
	
	@Override
	public Object getItem(int position)
	{
		if (mFileList == null)
			return 0;
		else			
			return mFileList.get(position);
	}
	
	@Override
	public long getItemId(int position)
	{
			return position;
	}
	
	public ArrayList<com.filepassapp.chenze.filepass.FileDescription>getProductList()
	{
		return mFileList;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{	
		final com.filepassapp.chenze.filepass.FileDescription file;
			
	    if (mFileList == null)
	    	return null;
		View localView = convertView;
		if (localView==null)
		{
			localView = inflater.inflate(R.layout.file_row, parent, false);
		}
		
		TextView filename = (TextView)localView.findViewById(R.id.filename_list);	
		TextView size = (TextView)localView.findViewById(R.id.size_list);	
		
		file = mFileList.get(position);		
		
		filename.setText(file.fileName);
		if (!(file.isFolder))
			size.setText(Long.toString(file.size/1024)+" KB");
		else
			size.setText("");
		
		return localView;
	}
	

	public  void showResultSet(  ArrayList<com.filepassapp.chenze.filepass.FileDescription> fileList)
	{
		mFileList = fileList;
	    notifyDataSetChanged();		
	}

	
}

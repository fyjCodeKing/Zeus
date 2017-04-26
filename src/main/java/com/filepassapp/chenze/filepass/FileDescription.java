package com.filepassapp.chenze.filepass;

import android.os.Parcel;
import android.os.Parcelable;


public class FileDescription implements Parcelable{

	public String fileName;
	public boolean isFolder;
	public long size;
	
	FileDescription (String fileName, boolean[] boolArray, long size)
	{
        this.fileName = fileName;
        this.isFolder = boolArray[0];;
        this.size = size;
        		

	}
	
	public FileDescription() {
        this.fileName = "";
        this.isFolder = false;
        this.size = 0;
	}

	@Override
    public int describeContents() {
        return 0;
    }
	
    @Override
    public void writeToParcel(Parcel parcel, int arg1) 
    {
    	boolean[] boolArray={isFolder};
    	
        parcel.writeString(fileName);
        parcel.writeBooleanArray(boolArray);
        parcel.writeLong(size);
    }
     
    public static final Creator<FileDescription> CREATOR = new Creator<FileDescription>() {
    	
    	boolean[] boolArray;
        
    	@Override
        public FileDescription createFromParcel(Parcel parcel) 
        {        	
            String fileName = parcel.readString();
            parcel.readBooleanArray(boolArray);
            long size = parcel.readLong();
                
            return new FileDescription(fileName,boolArray,size);
        }
 
        @Override
        public FileDescription[] newArray(int size) 
        {
            return new FileDescription[size];
        }
         
    };

}

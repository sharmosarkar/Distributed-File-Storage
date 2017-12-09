package com.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import com.dropbox.core.DbxApiException;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.google.gson.Gson;
import com.models.DeviceData;
import com.models.FileData;
import com.models.FileSplit;
import com.models.FileUploadData;
import com.models.ResponseData;

public class DropBoxIntegration
{
	public ResponseData uploadFileToDrive(FileUploadData fileUploadData) throws FileNotFoundException, DbxApiException, DbxException
	{		
		List<FileSplit> fs = fileUploadData.getSplitDataList();

		System.out.println("Files " + fileUploadData.getFileName() + " is split as: ");
		for (FileSplit f : fs)
		{
			System.out.print(f.getFileName() + " - " + f.getSplitSize() + " - " + f.getDeviceInfo().getDeviceId()
					+ " - " + f.getDeviceInfo().getDeviceType());
			System.out.println();
		}
		
		//InputStream is = new FileInputStream(fileUploadData.getLocalFilePath() + fileUploadData.getFileName());
		long splitOffset=0;
		for(FileSplit f : fs)
		{
			DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
			DbxClientV2 client = new DbxClientV2(config, f.getDeviceInfo().getToken());

			//FullAccount account = client.users().getCurrentAccount();
			System.out.println("Uploading " + f.getFileName() + " to drop box");

			try
			{
				/*byte[] buf = new byte[(	int) f.getSplitSize()];
				int bytesRead = is.read(buf);
				System.out.println("Bytes read: " + bytesRead);
				InputStream temp = new ByteArrayInputStream(buf);*/
				
				InputStream is = new FileInputStream(fileUploadData.getLocalFilePath() + fileUploadData.getFileName());
				is.mark(f.getSplitSize());
				is.skip(splitOffset);
				
				FileMetadata metadata = client.files().uploadBuilder("/" + f.getFileName())
						.uploadAndFinish(is);
				
				splitOffset=splitOffset+f.getSplitSize();
			}
			catch (Exception e)
			{
				System.out.println("Exception occured while uploading " + f.getFileName() + " : " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		// handle error
		return new ResponseData(0, "Files uploaded successfully");
	}

	public int deleteFilesFromDrive(FileData fdd)
	{
		// TODO Auto-generated method stub		
		int filesDeleted = 0;
		
		List<FileSplit> fs = fdd.getSplitDataList();
		
		for(FileSplit f : fs)
		{
			DeviceData d = f.getDeviceInfo();
			
			DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
			DbxClientV2 client = new DbxClientV2(config, d.getToken());

			try
			{
				Metadata metadata = client.files().delete("/" + f.getFileName());

				filesDeleted++;
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				System.out.println("Error in deleting file " + f.getFileName() + " from drop box");
				e.printStackTrace();
			}
		}

		return filesDeleted;
		
	}

	public int getFreeSpaceInCloud(String token)
	{
		// TODO Auto-generated method stub
		
		long freeSpace = 0;
		
		DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
		DbxClientV2 client = new DbxClientV2(config, token);
		
		try
		{		
			String totalSpace = client.users().getSpaceUsage().getAllocation().toString();
			long usedSpace = client.users().getSpaceUsage().getUsed();
			
			Gson gson = new Gson();
			Properties data = gson.fromJson(totalSpace, Properties.class);
			totalSpace = data.getProperty("allocated");
			
			freeSpace = new Long(totalSpace) - usedSpace; 						
		}
		catch(Exception e)
		{
			System.out.println("Error occured while getting free space from Drop Box");
			e.printStackTrace();
			return -1;
		}
				
		return (int) freeSpace;
	}
	
	
	
}

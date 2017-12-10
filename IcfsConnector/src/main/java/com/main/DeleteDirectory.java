package com.main;

import java.util.List;
import java.util.Map;

import com.dao.FileOperationsDAO;
import com.models.DeviceData;
import com.models.FileArgs;
import com.models.FileData;
import com.models.ResponseData;
import com.mysql.jdbc.StringUtils;

public class DeleteDirectory
{

	public ResponseData deleteDirectory(FileArgs args)
	{
		// validate args
		if (validateArgsForDeleteDirectory(args))
		{
			// handle error
			System.out.println("Incorrect arguments received");
			System.exit(0);
		}

		// fetch split files and fstats to delete		
		Map<Integer, FileData> map = FileOperationsDAO.getFilesToDelete(args);

		//test
		System.out.println("Printing delete list");
		for(Map.Entry<Integer, FileData> entry : map.entrySet())
		{
			System.out.println(entry.getKey());
			System.out.println(entry.getValue());
		}

		//System.exit(0);

		if(map != null && map.isEmpty())
		{
			// directory not found
			return new ResponseData(0, "Directory not found");
		}

		// delete from cloud
		DropBoxIntegration dbi = new DropBoxIntegration();

		for(Map.Entry<Integer, FileData> entry : map.entrySet())
		{
			if(entry.getValue() != null && !entry.getValue().isDirectory())
				dbi.deleteFilesFromDrive(entry.getValue());
		}

		// delete from db and update free space

		if(FileOperationsDAO.deleteFilesFromDB(map))
		{
			return new ResponseData(1, "Failed to delete data from db");
		}

		List<DeviceData> devices = FileOperationsDAO.getDeviceData(true);

		if(new FileOperationUtils().updateFreeSpace(devices))
		{
			return new ResponseData(1, "Failed to update cloud metadata");
		}

		return new ResponseData(0, "Delete successful");
	}

	private boolean validateArgsForDeleteDirectory(FileArgs args)
	{
		// TODO Auto-generated method stub
		boolean errorFlag = false;
		String message = "";

		if (StringUtils.isNullOrEmpty(args.getFileName()))
		{
			errorFlag = true;
			message = "File name is empty";
		}
		else if (StringUtils.isNullOrEmpty(args.getFilePath()))
		{
			errorFlag = true;
			message = "File path is empty";
		}

		if (!errorFlag)
			System.out.println(message); // for debugging only

		return errorFlag;
	}

}

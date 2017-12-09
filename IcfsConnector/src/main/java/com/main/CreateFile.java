package com.main;

import java.util.ArrayList;
import java.util.List;

import com.dao.FileOperationsDAO;
import com.models.DeviceData;
import com.models.FileArgs;
import com.models.FileUploadData;
import com.models.FstatResponse;
import com.models.ResponseData;
import com.mysql.jdbc.StringUtils;

public class CreateFile
{
	public List<FstatResponse> createFile(FileArgs args)
	{
		FileOperationUtils fou = new FileOperationUtils();
		List<FstatResponse> resp = new ArrayList<FstatResponse>();
		
		// validate received args

		if (validateArgsForCreateFile(args))
		{
			// handle error
			System.out.println("Incorrect arguments received");
			//return new ResponseData(1, "Incorrect arguments received");
			return resp;
		}

		// check if received inp is for a file and not dir

		if (args.getProtection().contains("d"))
		{
			// handle error
			System.out.println("For create op input should be a file and not a directory");
			//return new ResponseData(1, "For create op input should be a file and not a directory");
			return resp;
		}

		// check if already exists

		if (FileOperationsDAO.fileAlreadyExists(args.getFileName(), args.getFilePath()))
		{
			// handle error
			System.out.println("File already present");
			//return new ResponseData(1, "File already present.");
			return resp;
		}

		// check if space available

		List<DeviceData> devices = FileOperationsDAO.getDeviceData(false);

		// device null check
		if (devices == null || (devices != null && devices.isEmpty()))
		{
			// handle error
			System.out.println("Unable to fetch device information");
			//return new ResponseData(1, "Unable to fetch device information.");
			return resp;
		}

		if (FileOperationUtils.getTotalFreeSpace(devices) < new Long(args.getFileSize()))
		{
			// handle error
			System.out.println("No space available");
			//return new ResponseData(1, "Not enough space to store this file.");
			return resp;
		}

		// upload
		FileUploadData fileUploadData = fou.getUploadDetails(args, devices);

		DropBoxIntegration dbi = new DropBoxIntegration();

		try
		{
			ResponseData resp1 = dbi.uploadFileToDrive(fileUploadData);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// update tables
		// fstat, permissions and destination
		FileOperationsDAO.updateFileTablesForCreate(args, fileUploadData); // make methods non-static

		// update cloud meta
		//FileOperationsDAO.updateCloudMetadata(fileUploadData); // use db api instead of calc
		
		boolean metadataUpdateStatus = new FileOperationUtils().updateFreeSpace(devices);
		System.out.println("Cloud meta updated? " + metadataUpdateStatus);

		resp = FileOperationsDAO.getFstatResponse(-1, fileUploadData.getFileName(),
				fileUploadData.getFilePath());

		return resp;
	}

	private static boolean validateArgsForCreateFile(FileArgs args)
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
		else if (StringUtils.isNullOrEmpty(args.getLocalFilePath()))
		{
			errorFlag = true;
			message = "File local path is empty";
		}
		else if (args.getFileSize() == -1)
		{
			errorFlag = true;
			message = "File size is empty";
		}
		else if (StringUtils.isNullOrEmpty(args.getProtection()))
		{
			errorFlag = true;
			message = "File protection is empty";
		}
		else if (StringUtils.isNullOrEmpty(args.getOwner()))
		{
			errorFlag = true;
			message = "File owner is empty";
		}

		if (!errorFlag)
			System.out.println(message); // for debugging only

		return errorFlag;
	}

}

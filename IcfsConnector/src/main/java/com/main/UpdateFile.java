package com.main;

import java.util.ArrayList;
import java.util.List;

import com.dao.FileOperationsDAO;
import com.models.DeviceData;
import com.models.FileArgs;
import com.models.FileData;
import com.models.FileSplit;
import com.models.FileUploadData;
import com.models.FstatResponse;
import com.models.ResponseData;
import com.mysql.jdbc.StringUtils;

public class UpdateFile
{
	public List<FstatResponse> updateFile(FileArgs args)
	{
		FileData fileData = null;
		long currentFreeSpace = 0;
		List<DeviceData> devices = null;
		FileOperationUtils fou = new FileOperationUtils();

		List<FstatResponse> resp = new ArrayList<FstatResponse>();
		
		// validate args

		if (validateArgsForUpdateFile(args))
		{
			// handle error
			System.out.println("Incorrect arguments received");
			return resp;
		}

		// check if received inp is for a file and not dir

		if (args.getProtection().contains("d"))
		{
			// handle error
			System.out.println("For update op input should be a file and not a directory");
			//return new ResponseData(1, "For update op input should be a file and not a directory");
			return resp;
		}

		// check if file exists at all

		if (!FileOperationsDAO.fileAlreadyExists(args.getFileName(), args.getFilePath()))
		{
			// handle error
			System.out.println("File does not exist");
			//System.exit(0);
			return resp;
		}

		// get upload details

		devices = FileOperationsDAO.getDeviceData(true);
		fileData = FileOperationsDAO.getFileData(args.getFileName(), args.getFilePath());

		currentFreeSpace = FileOperationUtils.getTotalFreeSpace(devices);

		if ((currentFreeSpace + fileData.getFileSize()) < args.getFileSize()) // assuming whole file (all splits) will
																				// be deleted
		{
			System.out.println("Not enough free space available to store updated file");
			//System.exit(0);
			return resp;
		}

		// fn to determine if all the blocks are to be deleted - next phase

		// delete all old data - retain fstat

		ResponseData deleteResp = new DeleteFile().deleteFile(args, fileData, true);

		if (deleteResp.getResponseCode() == 1)
		{
			//return deleteResp;
			return resp;
		}

		// get upload details - create file

		// test
		for (DeviceData d : devices)
			System.out.println(d.getDeviceId() + " " + d.getFreeSpace());

		// update device data to save one db call
		for (FileSplit f : fileData.getSplitDataList())
		{
			DeviceData d = devices.stream().filter(p -> p.getDeviceId() == f.getDeviceInfo().getDeviceId()).findFirst()
					.get();

			if (d != null)
			{
				d.setFreeSpace(d.getFreeSpace() + f.getSplitSize());
				f.setDeviceInfo(d);
			}
		}

		// test
		for (DeviceData d : devices)
			System.out.println(d.getDeviceId() + " " + d.getFreeSpace());

		FileUploadData fud = fou.getUploadDetails(args, devices);
		fud.setInode(fileData.getInode());

		DropBoxIntegration dbi = new DropBoxIntegration();

		try
		{
			ResponseData resp1 = dbi.uploadFileToDrive(fud);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// update tables
		// fstat, permissions and destination
		FileOperationsDAO.updateFileTablesForUpdate(args, fud); // make methods non-static

		// update cloud meta
		//FileOperationsDAO.updateCloudMetadata(fud); // use db api instead of calc
		
		boolean metadataUpdateStatus = new FileOperationUtils().updateFreeSpace(devices);
		System.out.println("Cloud meta updated? " + metadataUpdateStatus);

		resp = FileOperationsDAO.getFstatResponse(fileData.getInode(), fileData.getFileName(),
				fileData.getFilePath());
		
		//return new ResponseData(0, "File updated successfully");
		return resp;
	}

	private static boolean validateArgsForUpdateFile(FileArgs args)
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
		else if (args.getInode() == -1)
		{
			errorFlag = true;
			message = "File inode is empty";
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

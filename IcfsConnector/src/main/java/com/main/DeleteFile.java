package com.main;

import java.util.ArrayList;
import java.util.List;

import com.dao.FileOperationsDAO;
import com.models.DeviceData;
import com.models.FileArgs;
import com.models.FileData;
import com.models.FileSplit;
import com.models.ResponseData;
import com.mysql.jdbc.StringUtils;

public class DeleteFile
{
	// user permission to delete file to be checked by fuse
	// retainFstat - flag for need to retain fstat data on delete for update
	public ResponseData deleteFile(FileArgs args, FileData fd, boolean retainFstat)
	{
		int noOfFilesDeleted = 0;
		FileData fdd = null;
		DropBoxIntegration dbi = new DropBoxIntegration();

		// validate args
		if (validateArgsForDeleteFile(args))
		{
			// handle error
			System.out.println("Incorrect arguments received");
			System.exit(0);
		}

		// get files names to be deleted - if split

		// cases:
		// file not present - resultset empty - return delete success
		// file not split - 1 result
		// file split - multiple rows
		// is_directory - 1 result () - no action on cloud - delete everyting within
		// folder - recursive call or write separate function

		if(fd == null)
			fdd = FileOperationsDAO.getFileData(args.getFileName(), args.getFilePath());
		else
			fdd = fd;

		// delete from cloud - if present

		if (null == fdd)
		{
			return new ResponseData(1, "Delete failed");
		}
		else if (null != fdd && fdd.getInode() == -1) // no such file present
		{
			return new ResponseData(0, "No such file exists");
		}
		else if (null != fdd && null != fdd.getSplitDataList() && fdd.getSplitDataList().isEmpty())
		{
			if (fdd.isDirectory()) // should be a directory
			{
				// handle separately
				// what to do if directory has data in it
			}
			else // cannot be
			{
				return new ResponseData(1, "Delete failed");
			}
		}
		else
		{
			noOfFilesDeleted = dbi.deleteFilesFromDrive(fdd);
			System.out.println("noOfFilesDeleted : " + noOfFilesDeleted);
		}

		// update db

		boolean deleteStatus = FileOperationsDAO.deleteFileData(fdd, retainFstat); // false if no error

		boolean metadataUpdateStatus = false;
		
		// update meta test
		for (FileSplit f : fdd.getSplitDataList())
		{
			DeviceData d = f.getDeviceInfo();
			d.setFreeSpace(d.getFreeSpace() + f.getSplitSize());
		}
		
		//FileOperationsDAO.updateCloudMetadata(fdd);
		// update meta test - end

				
		// update meta prod
		List<DeviceData> ds = new ArrayList<DeviceData>();

		for (FileSplit f : fdd.getSplitDataList())
		{
			DeviceData d = f.getDeviceInfo();
			ds.add(d);
		}

		metadataUpdateStatus = new FileOperationUtils().updateFreeSpace(ds); // false if no error
		
		System.out.println("Cloud meta updated? " + metadataUpdateStatus);

		if (deleteStatus || metadataUpdateStatus)
		{
			System.out.println("Failed to update db after delete");
			return new ResponseData(1, "Failed to update db after delete");
		}

		return new ResponseData(0, "Delete successful");
	}

	private static boolean validateArgsForDeleteFile(FileArgs args)
	{
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

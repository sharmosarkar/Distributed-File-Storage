package com.main;

import java.util.ArrayList;
import java.util.List;

import com.dao.FileOperationsDAO;
import com.models.FileArgs;
import com.models.FstatResponse;
import com.mysql.jdbc.StringUtils;

public class UpdateDirectory
{

	public List<FstatResponse> updateDirectory(FileArgs args)
	{
		// TODO Auto-generated method stub

		List<FstatResponse> resp = new ArrayList<FstatResponse>();
		
		// validate args

		if (validateArgsForUpdateDirectory(args))
		{
			// handle error
			System.out.println("Incorrect arguments received");
			//System.exit(0);
			return resp;
		}

		// check if received inp is for a dir and not a file

		if (!args.getProtection().contains("d"))
		{
			// handle error
			System.out.println("For updatedir op input should be a directory and not a file.");
			//return new ResponseData(1, "For updatedir op input should be a directory and not a file.");
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

		// insert into fstat

		if (FileOperationsDAO.updateFileTablesForUpdateDirectory(args))
		{
			//return new ResponseData(1, "Failed to update fstat for directory: " + args.getFileName());
			return resp;
		}

		//return new ResponseData(0, "Directory updated successfully: " + args.getFileName());
		
		resp = FileOperationsDAO.getFstatResponse(args.getInode(), args.getFileName(),
				args.getFilePath());
		
		return resp;

	}

	private boolean validateArgsForUpdateDirectory(FileArgs args)
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

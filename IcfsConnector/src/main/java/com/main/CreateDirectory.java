package com.main;

import java.util.ArrayList;
import java.util.List;

import com.dao.FileOperationsDAO;
import com.models.FileArgs;
import com.models.FstatResponse;
import com.mysql.jdbc.StringUtils;

public class CreateDirectory
{

	public List<FstatResponse> createDirectory(FileArgs fileArgs)
	{
		// validate received args
		List<FstatResponse> resp = new ArrayList<FstatResponse>();

		if (validateArgsForCreateDirectory(fileArgs))
		{
			// handle error
			System.out.println("Incorrect arguments received");
			//return new ResponseData(1, "Incorrect arguments received");
			return resp;
		}

		// check if received inp is for a dir and not a file

		if (fileArgs.isDirectory() == false)
		{
			// handle error
			System.out.println("For createdir op input should be a directory and not a file.");
			//return new ResponseData(1, "For createdir op input should be a directory and not a file.");
			return resp;
		}

		// check if already exists

		if (FileOperationsDAO.fileAlreadyExists(fileArgs.getFileName(), fileArgs.getFilePath()))
		{
			// handle error
			System.out.println("Directory already present");
			//return new ResponseData(1, "Directory already present.");
			return resp;
		}

		// insert into fstat

		if (FileOperationsDAO.updateFileTablesForCreateDirectory(fileArgs))
		{
			//return new ResponseData(1, "Failed to update fstat for directory: " + fileArgs.getFileName());
			return resp;
		}

		//return new ResponseData(0, "Directory created successfully: " + fileArgs.getFileName());

		resp = FileOperationsDAO.getFstatResponse(-1, fileArgs.getFileName(),
				fileArgs.getFilePath());

		return resp;
	}

	private boolean validateArgsForCreateDirectory(FileArgs args)
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
		else if (args.getProtection() == 0)
		{
			errorFlag = true;
			message = "File protection is empty";
		}
		else if (StringUtils.isNullOrEmpty(args.getOwner()))
		{
			errorFlag = true;
			message = "File owner is empty";
		}
		else if (StringUtils.isNullOrEmpty(args.getGroup()))
		{
			errorFlag = true;
			message = "File group is empty";
		}

		if (!errorFlag)
			System.out.println(message); // for debugging only

		return errorFlag;
	}

}

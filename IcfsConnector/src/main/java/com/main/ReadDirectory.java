package com.main;

import java.util.ArrayList;
import java.util.List;

import com.dao.FileOperationsDAO;
import com.models.FileArgs;
import com.models.FstatResponse;
import com.mysql.jdbc.StringUtils;

public class ReadDirectory
{
	public List<FstatResponse> readDirectory(FileArgs args)
	{
				
		/*FileData fd = FileOperationsDAO.getFileData(args.getFileName(), args.getLocalFilePath());
		if (fd.isDirectory())
		{
			List<String> contents = FileOperationsDAO.getDirectoryContents(args.getLocalFilePath());
			return new ResponseData(0, contents.stream().reduce("", (a, b) -> a + "," + b));
		}
		return new ResponseData(1, "failed while reading directory contents");*/

		List<FstatResponse> resp = new ArrayList<FstatResponse>();
		
		if (validateArgsForReadDirectory(args))
		{
			// handle error
			System.out.println("Incorrect arguments received");
			//return new ResponseData(1, "Incorrect arguments received");
			return resp;
		}
		
		resp = FileOperationsDAO.readDirectory(args);
		
		return resp;
	}

	private boolean validateArgsForReadDirectory(FileArgs args)
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

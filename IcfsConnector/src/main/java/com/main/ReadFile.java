package com.main;

import java.io.*;
import java.util.Collections;
import java.util.List;

import com.dao.FileOperationsDAO;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.models.*;
import com.mysql.jdbc.StringUtils;
import com.utils.FileSplitSortBySequence;

public class ReadFile
{
	public List<FstatResponse> readFile(FileArgs args)
	{
		OutputStream downloadFile = null;
		List<FstatResponse> response = null;

		if (validateArgsForReadFile(args))
		{
			// handle error
			System.out.println("Incorrect arguments received");
			return null;
		}

		FileData fd = FileOperationsDAO.getFileData(args.getFileName(), args.getFilePath());

		if (fd == null)
		{
			System.out.println("File not found");
			return null;
		}

		System.out.println("File to be downloaded at: " + args.getLocalFilePath() + args.getFileName());

		try
		{
			List<FileSplit> fsList = fd.getSplitDataList();

			if (fsList.size() > 1)
			{
				Collections.sort(fsList, new FileSplitSortBySequence());
			}

			System.out.println("Files to be downloaded from cloud: ");
			for (FileSplit f : fsList)
			{
				System.out.println(f);
			}

			downloadFile = new FileOutputStream(args.getLocalFilePath() + args.getFileName());

			for (FileSplit f : fsList)
			{
				DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
				DbxClientV2 client = new DbxClientV2(config, f.getDeviceInfo().getToken());

				try
				{

					FileMetadata metadata = client.files().downloadBuilder("/" + f.getFileName())
							.download(downloadFile);

				} catch (Exception e)
				{
					System.out.println("Exception occured while downloading file: " + e.getMessage());
					e.printStackTrace();
					return null;
				}
			}

		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			System.out.println("File not found");
			e.printStackTrace();
			return null;
		} finally
		{
			try
			{
				downloadFile.close();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		response = FileOperationsDAO.getFstatResponse(-1, args.getFileName(),
				args.getFilePath());

		return response;

	}

	private boolean validateArgsForReadFile(FileArgs args)
	{
		// TODO Auto-generated method stub
		boolean errorFlag = false;
		String message = "";

		if (StringUtils.isNullOrEmpty(args.getFileName()))
		{
			errorFlag = true;
			message = "File name is empty";
		} else if (StringUtils.isNullOrEmpty(args.getFilePath()))
		{
			errorFlag = true;
			message = "File path is empty";
		} else if (StringUtils.isNullOrEmpty(args.getLocalFilePath()))
		{
			errorFlag = true;
			message = "Local file path is empty";
		}

		if (!errorFlag)
			System.out.println(message); // for debugging only

		return errorFlag;
	}
}

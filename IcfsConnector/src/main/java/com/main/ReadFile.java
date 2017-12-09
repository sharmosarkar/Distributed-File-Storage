package com.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.dao.FileOperationsDAO;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.models.FileArgs;
import com.models.FileData;
import com.models.FileSplit;
import com.models.ResponseData;

public class ReadFile
{
	public ResponseData readFile(FileArgs args)
	{
		FileData fd = FileOperationsDAO.getFileData(args.getFileName(), args.getLocalFilePath());
		// Todo : Replace this by dump part.
		File file = new File("destination.file");

		try
		{
			for (FileSplit f : fd.getSplitDataList())
			{
				DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
				DbxClientV2 client = new DbxClientV2(config, f.getDeviceInfo().getToken());

				OutputStream out = new FileOutputStream(file, true);
				FileMetadata metadata = client.files().downloadBuilder("").download(out);
				out.close();
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseData(1, "failed while downloading for read");
		}
		return new ResponseData(0, "downloaded successfully");
	}
}

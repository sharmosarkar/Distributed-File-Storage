package com.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dao.FileOperationsDAO;
import com.models.DeviceData;
import com.models.FileArgs;
import com.models.FileSplit;
import com.models.FileUploadData;
import com.utils.DeviceSpaceSort;

public class FileOperationUtils
{
	public boolean updateFreeSpace(List<DeviceData> devices)
	{
		int currentfreeSpace = 0;
		Map<Integer, Integer> updatedFreeSpace = new HashMap<Integer, Integer>();
		DropBoxIntegration dbi = new DropBoxIntegration();
		
		for(DeviceData d : devices)
		{
			currentfreeSpace = dbi.getFreeSpaceInCloud(d.getToken());
			
			if(currentfreeSpace != d.getFreeSpace())
				updatedFreeSpace.put(d.getDeviceId(), currentfreeSpace);
		}
						
		for(Map.Entry<Integer, Integer> entry : updatedFreeSpace.entrySet())
		{
			System.out.println(entry.getKey() + " - " + entry.getValue());
		}
		
		
		return FileOperationsDAO.updateFreeSpace(updatedFreeSpace);
	}
	
	public static long getTotalFreeSpace(List<DeviceData> devices)
	{
		// TODO Auto-generated method stub
		
		
		long totalFreeSpace = 0;
		for (DeviceData d : devices)
		{
			System.out.println(d.getFreeSpace());
			totalFreeSpace += d.getFreeSpace();
		}
		System.out.println(totalFreeSpace);
		
		return totalFreeSpace;
	}
	
	public FileUploadData getUploadDetails(FileArgs args, List<DeviceData> deviceData)
	{
		// TODO Auto-generated method stub
		FileUploadData fud = new FileUploadData();
		List<FileSplit> splitList = new ArrayList<FileSplit>();

		Collections.sort(deviceData, new DeviceSpaceSort());

		int remainingSize = args.getFileSize();

		if (deviceData.get(0).getFreeSpace() > remainingSize) // null check for device data
		{
			// no need to split
			FileSplit fs = new FileSplit();
			
			// update free space and insert
			DeviceData d =  deviceData.get(0);
			d.setFreeSpace(d.getFreeSpace() - remainingSize);
			deviceData.get(0).setFreeSpace(d.getFreeSpace());
			
			fs.setDeviceInfo(d); 			
			fs.setFileName(args.getFileName());
			fs.setSplitSize(remainingSize);
			fs.setSequence(1);
			splitList.add(fs);
		}
		else
		{
			// split file
			
			int splitCounter = 1;

			for (int i = 0; i < deviceData.size(); i++)
			{
				FileSplit temp = new FileSplit();

				if (deviceData.get(i).getFreeSpace() > remainingSize)
				{
					temp.setSplitSize(remainingSize);
					temp.setFileName(args.getFileName() + splitCounter);
					temp.setSequence(splitCounter);
					splitCounter++;
					
					// update free space and insert
					DeviceData d =  deviceData.get(i);
					d.setFreeSpace(d.getFreeSpace() - remainingSize);					
					temp.setDeviceInfo(d);

					splitList.add(temp);

					deviceData.get(i).setFreeSpace(d.getFreeSpace());
					remainingSize = 0;
				}

				else
				{
					temp.setSplitSize(deviceData.get(i).getFreeSpace());
					temp.setFileName(args.getFileName() + splitCounter);
					temp.setSequence(splitCounter);
					splitCounter++;
					
					remainingSize = remainingSize - deviceData.get(i).getFreeSpace();
					
					// update free space and insert
					DeviceData d =  deviceData.get(i);
					d.setFreeSpace(0);
					temp.setDeviceInfo(d);

					splitList.add(temp);					
				}

				if (remainingSize <= 0)
					break;

			}

		}

		fud.setFileName(args.getFileName());
		fud.setFilePath(args.getFilePath());
		fud.setFileSize(args.getFileSize());
		fud.setNoOfSplits(splitList.size());
		fud.setSplitDataList(splitList);
		fud.setLocalFilePath(args.getLocalFilePath());
		
		
		return fud;
	}
}

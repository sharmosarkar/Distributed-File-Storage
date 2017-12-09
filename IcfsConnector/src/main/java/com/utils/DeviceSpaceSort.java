package com.utils;

import java.util.Comparator;

import com.models.DeviceData;

public class DeviceSpaceSort implements Comparator<DeviceData>
{

	@Override
	public int compare(DeviceData arg0, DeviceData arg1)
	{
		return new Integer(arg1.getFreeSpace()).compareTo(new Integer(arg0.getFreeSpace()));
	}
	
}

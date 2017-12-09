package com.models;

public class FileSplit
{
	private DeviceData deviceInfo;
	private String fileName; // filename after split
	private int splitSize; // size of file after split
	private int sequence;
	
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("{ " + " " + this.fileName + " " + this.splitSize + " " + this.sequence + " ");
		
		if(null != this.deviceInfo)
		{
			sb.append(this.deviceInfo.getDeviceId() + " " + this.deviceInfo.getFreeSpace());
		}
		sb.append(" } \n");
		
		return sb.toString();
	}
	
	public int getSequence()
	{
		return sequence;
	}
	public void setSequence(int sequence)
	{
		this.sequence = sequence;
	}
	public DeviceData getDeviceInfo()
	{
		return deviceInfo;
	}
	public void setDeviceInfo(DeviceData deviceInfo)
	{
		this.deviceInfo = deviceInfo;
	}
	public String getFileName()
	{
		return fileName;
	}
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
	public int getSplitSize()
	{
		return splitSize;
	}
	public void setSplitSize(int splitSize)
	{
		this.splitSize = splitSize;
	}
	
	
}

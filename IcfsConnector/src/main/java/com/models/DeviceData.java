package com.models;

public class DeviceData
{
	private int deviceId;
	private String token;
	private String deviceType;
	private int freeSpace;
	public int getDeviceId()
	{
		return deviceId;
	}
	public void setDeviceId(int deviceId)
	{
		this.deviceId = deviceId;
	}
	public String getToken()
	{
		return token;
	}
	public void setToken(String token)
	{
		this.token = token;
	}
	public String getDeviceType()
	{
		return deviceType;
	}
	public void setDeviceType(String deviceType)
	{
		this.deviceType = deviceType;
	}
	public int getFreeSpace()
	{
		return freeSpace;
	}
	public void setFreeSpace(int freeSpace)
	{
		this.freeSpace = freeSpace;
	}
	
	
}

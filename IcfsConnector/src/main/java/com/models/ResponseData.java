package com.models;

public class ResponseData
{
	int responseCode; // 0 - success, 1 - failure
	String responseMessage;
	
	public ResponseData(int responseCode, String responseMessage)
	{
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
	}
	
	public int getResponseCode()
	{
		return responseCode;
	}
	public void setResponseCode(int responseCode)
	{
		this.responseCode = responseCode;
	}
	public String getResponseMessage()
	{
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage)
	{
		this.responseMessage = responseMessage;
	}
	
	
}

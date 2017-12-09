package com.models;

import java.util.List;

public class FileUploadData
{
	private String fileName;
	private String filePath;
	private String localFilePath;
	private int fileSize;
	private int noOfSplits;
	private List<FileSplit> splitDataList;
	private int inode;
	
	
	public int getInode()
	{
		return inode;
	}
	public void setInode(int inode)
	{
		this.inode = inode;
	}
	public String getLocalFilePath()
	{
		return localFilePath;
	}
	public void setLocalFilePath(String localFilePath)
	{
		this.localFilePath = localFilePath;
	}
	public String getFileName()
	{
		return fileName;
	}
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
	public String getFilePath()
	{
		return filePath;
	}
	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}
	public int getFileSize()
	{
		return fileSize;
	}
	public void setFileSize(int fileSize)
	{
		this.fileSize = fileSize;
	}
	public int getNoOfSplits()
	{
		return noOfSplits;
	}
	public void setNoOfSplits(int noOfSplits)
	{
		this.noOfSplits = noOfSplits;
	}
	public List<FileSplit> getSplitDataList()
	{
		return splitDataList;
	}
	public void setSplitDataList(List<FileSplit> splitDataList)
	{
		this.splitDataList = splitDataList;
	}
	
}

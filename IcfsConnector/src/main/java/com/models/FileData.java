package com.models;

import java.util.List;

public class FileData
{
	private String fileName;
	private String filePath;
	private int fileSize;
	private int noOfSplits;
	private boolean isDirectory;
	private int inode;
	private List<FileSplit> splitDataList;

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[ " + " " + this.fileName + " " + this.filePath + " " + this.fileSize + " " + this.noOfSplits + " "
				+ this.isDirectory + " " + this.inode + " \n");
		
		for(FileSplit f : this.splitDataList)
		{					
			sb.append(f.toString());
		}
		sb.append(" ]");
		
		return sb.toString();
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

	public boolean isDirectory()
	{
		return isDirectory;
	}

	public void setDirectory(boolean isDirectory)
	{
		this.isDirectory = isDirectory;
	}

	public int getInode()
	{
		return inode;
	}

	public void setInode(int inode)
	{
		this.inode = inode;
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

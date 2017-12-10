package com.models;

public class FstatResponse
{
	private int st_dev;
	private int st_ino;
	private int st_mode;
	private int st_nlink;
	private int st_uid;
	private int st_gid;
	private int st_rdev;
	private int st_size;
	private int st_blksize;
	private int st_blocks;
	private long st_atime;
	private long st_mtime;
	private long st_ctime;
	private int st_isdir;


	public int getSt_dev()
	{
		return st_dev;
	}
	public void setSt_dev(int st_dev)
	{
		this.st_dev = st_dev;
	}
	public int getSt_ino()
	{
		return st_ino;
	}
	public void setSt_ino(int st_ino)
	{
		this.st_ino = st_ino;
	}
	public int getSt_mode()
	{
		return st_mode;
	}
	public void setSt_mode(int st_mode)
	{
		this.st_mode = st_mode;
	}
	public int getSt_nlink()
	{
		return st_nlink;
	}
	public void setSt_nlink(int st_nlink)
	{
		this.st_nlink = st_nlink;
	}
	public int getSt_uid()
	{
		return st_uid;
	}
	public void setSt_uid(int st_uid)
	{
		this.st_uid = st_uid;
	}
	public int getSt_gid()
	{
		return st_gid;
	}
	public void setSt_gid(int st_gid)
	{
		this.st_gid = st_gid;
	}
	public int getSt_rdev()
	{
		return st_rdev;
	}
	public void setSt_rdev(int st_rdev)
	{
		this.st_rdev = st_rdev;
	}
	public int getSt_size()
	{
		return st_size;
	}
	public void setSt_size(int st_size)
	{
		this.st_size = st_size;
	}
	public int getSt_blksize()
	{
		return st_blksize;
	}
	public void setSt_blksize(int st_blksize)
	{
		this.st_blksize = st_blksize;
	}
	public int getSt_blocks()
	{
		return st_blocks;
	}
	public void setSt_blocks(int st_blocks)
	{
		this.st_blocks = st_blocks;
	}
	public long getSt_atime()
	{
		return st_atime;
	}
	public void setSt_atime(long st_atime)
	{
		this.st_atime = st_atime;
	}
	public long getSt_mtime()
	{
		return st_mtime;
	}
	public void setSt_mtime(long st_mtime)
	{
		this.st_mtime = st_mtime;
	}
	public long getSt_ctime()
	{
		return st_ctime;
	}
	public void setSt_ctime(long st_ctime)
	{
		this.st_ctime = st_ctime;
	}
	public void setSt_isdir(int st_isdir)
	{
		this.st_isdir = st_isdir;
	}
	public int getSt_isdir(){ return st_isdir;}
	
	
}
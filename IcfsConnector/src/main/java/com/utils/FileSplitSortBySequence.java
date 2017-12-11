package com.utils;

import com.models.FileSplit;

import java.util.Comparator;


public class FileSplitSortBySequence implements Comparator<FileSplit>
{

	@Override
	public int compare(FileSplit arg0, FileSplit arg1)
	{
		// TODO Auto-generated method stub
		return new Integer(arg0.getSequence()).compareTo(new Integer(arg1.getSequence()));
	}

}

package com.utils;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class CommonUtils
{
	public static String getStringFromInputStream(InputStream is)
	{
		StringBuilder sb = new StringBuilder();
		String line;
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null)
				sb.append(line);
		}
		catch (Exception e)
		{
			System.out.println("Exception occured: " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (Exception ex)
				{
					System.out.println("Exception occured: " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
	
	public static String getCurrentTime()
	{
		java.util.Date curDate = new java.util.Date();
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		return sd.format(curDate);
	}
	
	public static Properties loadDatabaseProperties()
	{
		Properties prop = new Properties();
		InputStream is = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		try
		{
			//is = new FileInputStream("dbConfig.properties");
            is = loader.getResourceAsStream("dbConfig.properties");
			prop.load(is);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if( is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}		
		
		return prop;
	}

	public static long getEpochFromStringDate(String string)
	{
		// TODO Auto-generated method stub
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		long epoch = 0;
		
		try
		{
			date = sd.parse(string);
			epoch = date.getTime();
		}
		catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return epoch;
	}
	
}

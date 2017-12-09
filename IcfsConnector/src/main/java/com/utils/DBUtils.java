package com.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtils
{
	private static Connection con = null;
	private static Properties props = CommonUtils.loadDatabaseProperties();

	public static Connection getDbConnection()
	{
		/*
		 * String url = "jdbc:mysql://localhost:3306/test?useSSL=false"; String user =
		 * "root"; String password = "root123";
		 */

		String url = props.getProperty("db.url");
		String user = props.getProperty("db.username");
		String password = props.getProperty("db.password");
		
		try
		{
			con = DriverManager.getConnection(url, user, password);
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			System.out.println("Exception occured in creating DB connection: " + e.getMessage());
			e.printStackTrace();
			return null;
		}

		return con;
	}

	public static boolean closeDbConnection(Connection con, PreparedStatement pst, ResultSet rs)
	{
		try
		{
			if (con != null)
			{
				con.close();
			}

			if (pst != null)
			{
				pst.close();
			}

			if (rs != null)
			{
				rs.close();
			}

		}
		catch (Exception e)
		{
			// TODO: handle exception
			System.out.println("Exception occured while closing DB connection: " + e.getMessage());
			e.printStackTrace();
			return false;
		}

		return true;
	}

}

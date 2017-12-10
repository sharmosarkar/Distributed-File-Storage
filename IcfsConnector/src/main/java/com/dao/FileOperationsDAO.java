package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.models.DeviceData;
import com.models.FileArgs;
import com.models.FileData;
import com.models.FileSplit;
import com.models.FileUploadData;
import com.models.FstatResponse;
import com.mysql.jdbc.Statement;
import com.utils.CommonUtils;
import com.utils.DBUtils;

public class FileOperationsDAO
{
    Connection connObj = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    String query = null;
    public FileOperationsDAO(){
        connObj = DBUtils.getDbConnection();
    }

	public static boolean fileAlreadyExists(String fileName, String filePath)
	{
		// returns true if the file exists

		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		boolean flag = false;
		try
		{
			con = DBUtils.getDbConnection();
			pst = con.prepareStatement("select count(*) from fstat where file_name= ? and file_path=?");
			pst.setString(1, fileName);
			pst.setString(2, filePath);

			rs = pst.executeQuery();
			rs.next();
			if (rs.getInt(1) != 0)
			{
				flag = true;
			}

		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!DBUtils.closeDbConnection(con, pst, rs))
			System.out.println("Error occured");

		return flag;
	}

	public static List<DeviceData> getDeviceData(boolean includeDevicesWithNoSpace)
	{
		List<DeviceData> lst = new ArrayList<DeviceData>();

		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		try
		{
			con = DBUtils.getDbConnection();

			if (includeDevicesWithNoSpace)
				pst = con.prepareStatement("select * from cloud_metadata;");
			else
				pst = con.prepareStatement("select * from cloud_metadata where free_space != 0;");

			rs = pst.executeQuery();

			while (rs.next())
			{
				DeviceData dd = new DeviceData();
				dd.setDeviceId(rs.getInt("device_id"));
				dd.setToken(rs.getString("access_token"));
				dd.setDeviceType("device_type");
				dd.setFreeSpace(rs.getInt("free_space"));

				lst.add(dd);
			}

		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!DBUtils.closeDbConnection(con, pst, rs))
			System.out.println("Error occured");

		return lst;
	}

	public static void updateFileTablesForCreate(FileArgs args, FileUploadData fud)
	{
		// TODO Auto-generated method stub
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		String insertFstatQuery = "insert into fstat (file_name, file_path, file_size, protection, owner_id, group_id, "
				+ "hard_links,last_user_access,last_access_time, modified_time, creation_time, is_directory) "
				+ "values (?, ?, ?, (select protection_id from permissions where protection_bits = ?), ?, ?, ?, ?, ?, ?, ?, ?)";

		String insertDestCloudQuery = "insert into destination_cloud (device_id, inode, file_name, file_path, file_size, sequence) values (?, ?, ?, ?, ?, ?)";

		try
		{
			con = DBUtils.getDbConnection();
			con.setAutoCommit(false);

			pst = con.prepareStatement(insertFstatQuery, Statement.RETURN_GENERATED_KEYS);

			pst.setString(1, args.getFileName());
			pst.setString(2, args.getFilePath());
			pst.setInt(3, args.getFileSize());
			pst.setString(4, args.getProtection());
			pst.setInt(5, new Integer(args.getOwner()));
			pst.setInt(6, 0);
			pst.setInt(7, 0);
			pst.setInt(8, new Integer(args.getOwner()));
			pst.setString(9, CommonUtils.getCurrentTime());
			pst.setString(10, CommonUtils.getCurrentTime());
			pst.setString(11, CommonUtils.getCurrentTime());
			pst.setBoolean(12, args.getProtection().contains("d"));

			System.out.println("Rows inserted/updated in fstat: " + pst.executeUpdate());

			rs = pst.getGeneratedKeys();

			int inode = 0;
			if (rs.next())
			{
				inode = rs.getInt(1);
				System.out.println("Generated inode key = " + inode);
			}

			for (FileSplit fs : fud.getSplitDataList())
			{
				pst = con.prepareStatement(insertDestCloudQuery);

				pst.setInt(1, fs.getDeviceInfo().getDeviceId());
				pst.setInt(2, inode); // add sequence for inode. No need to query table again
				pst.setString(3, fs.getFileName());
				pst.setString(4, fud.getFilePath());
				pst.setInt(5, fs.getSplitSize());
				pst.setInt(6, fs.getSequence());

				pst.executeUpdate();
			}

			con.commit();

		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			System.out.println("Insert in db failed - Rolling back");
			try
			{
				con.rollback();
			}
			catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}

		if (!DBUtils.closeDbConnection(con, pst, null))
			System.out.println("Error occured");

	}

	public static void updateFileTablesForUpdate(FileArgs args, FileUploadData fud)
	{
		// TODO Auto-generated method stub
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		String updateFstatQuery = "update fstat "
				+ "set file_size = ?, protection = (select protection_id from permissions where protection_bits = ? ), "
				+ "owner_id = ?, last_access_time = now(), modified_time = now() " + "where inode = ?;";

		String insertDestCloudQuery = "insert into destination_cloud (device_id, inode, file_name, file_path, file_size, sequence) values (?, ?, ?, ?, ?, ?)";

		try
		{
			con = DBUtils.getDbConnection();
			con.setAutoCommit(false);

			pst = con.prepareStatement(updateFstatQuery);

			pst.setInt(1, args.getFileSize());
			pst.setString(2, args.getProtection());
			pst.setInt(3, new Integer(args.getOwner()));
			pst.setInt(4, fud.getInode());

			System.out.println("Rows updated in fstat: " + pst.executeUpdate());

			for (FileSplit fs : fud.getSplitDataList())
			{
				pst = con.prepareStatement(insertDestCloudQuery);

				pst.setInt(1, fs.getDeviceInfo().getDeviceId());
				pst.setInt(2, fud.getInode());
				pst.setString(3, fs.getFileName());
				pst.setString(4, fud.getFilePath());
				pst.setInt(5, fs.getSplitSize());
				pst.setInt(6, fs.getSequence());

				pst.executeUpdate();
			}

			con.commit();

		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			System.out.println("Insert in db failed - Rolling back");
			try
			{
				con.rollback();
			}
			catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}

		if (!DBUtils.closeDbConnection(con, pst, null))
			System.out.println("Error occured");

	}

	public static void updateCloudMetadata(Object updateData)
	{
		// Note - Function for test only - Free space to be updated using
		// FileOperationUtils.updateFreeSpace fn in prod

		List<FileSplit> fs = null;

		if (updateData instanceof FileUploadData)
			fs = ((FileUploadData) updateData).getSplitDataList();
		else
			fs = ((FileData) updateData).getSplitDataList();

		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		try
		{
			con = DBUtils.getDbConnection();

			for (FileSplit f : fs)
			{
				pst = con.prepareStatement("update cloud_metadata set free_space = ? where device_id = ?;");
				pst.setInt(1, f.getDeviceInfo().getFreeSpace());
				pst.setInt(2, f.getDeviceInfo().getDeviceId());
				System.out.println(pst);
				int rowsUpdated = pst.executeUpdate();

				System.out.println("rowsUpdated: " + rowsUpdated);
			}

		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!DBUtils.closeDbConnection(con, pst, rs))
			System.out.println("Error occured");

	}

	public static FileData getFileData(String fileName, String filePath)
	{
		// TODO Auto-generated method stub
		FileData fdd = new FileData();
		List<FileSplit> splitList = new ArrayList<FileSplit>();
		int splitCounter = 1;

		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		String getDeleteData = "select f.inode,f.file_name, f.file_path, f.file_size, f.is_directory, d.file_name as 'cloud_file_name', "
				+ " d.file_size as 'cloud_file_size', d.sequence, c.*" + " from "
				+ "(fstat f inner join destination_cloud d right outer join cloud_metadata c "
				+ " on f.inode = d.inode and d.device_id = c.device_id)"
				+ " where f.file_name = ? and f.file_path = ? order by d.sequence";

		try
		{
			con = DBUtils.getDbConnection();

			pst = con.prepareStatement(getDeleteData);
			pst.setString(1, fileName);
			pst.setString(2, filePath);

			rs = pst.executeQuery();

			fdd.setInode(-1);

			while (rs.next())
			{
				if (splitCounter == 1)
				{
					fdd.setInode(rs.getInt("inode"));
					fdd.setFileName(rs.getString("file_name"));
					fdd.setFilePath(rs.getString("file_path"));
					fdd.setFileSize(rs.getInt("file_size"));
					fdd.setDirectory(rs.getBoolean("is_directory"));

					if (fdd.isDirectory())
						break;
				}

				FileSplit f = new FileSplit();
				DeviceData d = new DeviceData();

				f.setFileName(rs.getString("cloud_file_name"));
				f.setSplitSize(rs.getInt("cloud_file_size"));
				f.setSequence(rs.getInt("sequence"));

				d.setDeviceId(rs.getInt("device_id"));
				d.setDeviceType(rs.getString("device_type"));
				d.setFreeSpace(rs.getInt("free_space"));
				d.setToken(rs.getString("access_token"));

				f.setDeviceInfo(d);

				splitList.add(f);

				splitCounter++;
			}

			fdd.setNoOfSplits(splitList.size());
			fdd.setSplitDataList(splitList);

		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!DBUtils.closeDbConnection(con, pst, rs))
			System.out.println("Error occured");

		return fdd;
	}

	public static boolean deleteFileData(FileData fdd, boolean retainFstat)
	{
		// TODO Auto-generated method stub
		boolean errorFlag = false;

		List<FileSplit> fs = fdd.getSplitDataList();

		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		try
		{
			con = DBUtils.getDbConnection();

			con.setAutoCommit(false);

			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < fs.size(); i++)
				sb.append("? ,");

			String deleteFromDestQuery = "delete from destination_cloud where inode = ? and file_name in ( "
					+ sb.deleteCharAt(sb.length() - 1).toString() + " )";

			pst = con.prepareStatement(deleteFromDestQuery);

			pst.setInt(1, fdd.getInode());

			int c = 2;
			for (int i = 0; i < fs.size(); i++)
				pst.setString(c++, fs.get(i).getFileName());

			System.out.println("deleteFromDestQuery: " + pst);

			if (0 == pst.executeUpdate())
			{
				throw new Exception("Unable to delete data from destination cloud");
			}

			if (!retainFstat)
			{
				// delete fstat
				pst = con.prepareStatement("delete from fstat where inode = ?");
				pst.setInt(1, fdd.getInode());

				if (0 == pst.executeUpdate())
				{
					throw new Exception("Unable to delete data from fstat");
				}

			}

			con.commit();

		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			errorFlag = true;

			try
			{
				con.rollback();
			}
			catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		if (!DBUtils.closeDbConnection(con, pst, rs))
			System.out.println("Error occured");

		return errorFlag;
	}

	public static boolean updateFreeSpace(Map<Integer, Integer> updatedFreeSpace)
	{
		// TODO Auto-generated method stub
		boolean errorFlag = false;

		if (updatedFreeSpace.isEmpty())
			return true; // nothing to update

		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		try
		{
			con = DBUtils.getDbConnection();

			con.setAutoCommit(false);

			pst = con.prepareStatement("update cloud_metadata set free_space = ? where device_id = ?;");

			Iterator<Map.Entry<Integer, Integer>> itr = updatedFreeSpace.entrySet().iterator();

			while (itr.hasNext())
			{
				Map.Entry<Integer, Integer> entry = itr.next();
				
				pst.setInt(1, entry.getValue());				
				pst.setInt(2, entry.getKey());
				System.out.println(pst);
				pst.addBatch(); // executing in batch to avoid multiple db connections
			}

			pst.executeBatch();

			con.commit();

		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			errorFlag = true;

			try
			{
				con.rollback();
			}
			catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		if (!DBUtils.closeDbConnection(con, pst, rs))
			System.out.println("Error occured");

		return errorFlag;
	}

	public static boolean updateFileTablesForCreateDirectory(FileArgs args)
	{
		// TODO Auto-generated method stub
		boolean errorFlag = false;

		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		try
		{
			con = DBUtils.getDbConnection();

			pst = con.prepareStatement(
					"insert into fstat (file_name, file_path, file_size, protection, owner_id, group_id, "
							+ "hard_links,last_user_access,last_access_time, modified_time, creation_time, is_directory) "
							+ "values (?, ?, ?, (select protection_id from permissions where protection_bits = ?), ?, ?, ?, ?, ?, ?, ?, ?)");

			pst.setString(1, args.getFileName());
			pst.setString(2, args.getFilePath());
			pst.setInt(3, 0);
			pst.setString(4, args.getProtection());
			pst.setInt(5, new Integer(args.getOwner()));
			pst.setInt(6, 0);
			pst.setInt(7, 0);
			pst.setInt(8, new Integer(args.getOwner()));
			pst.setString(9, CommonUtils.getCurrentTime());
			pst.setString(10, CommonUtils.getCurrentTime());
			pst.setString(11, CommonUtils.getCurrentTime());
			pst.setBoolean(12, true);

			if (0 == pst.executeUpdate())
			{
				errorFlag = true;
			}

		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			errorFlag = true;

			try
			{
				con.rollback();
			}
			catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		if (!DBUtils.closeDbConnection(con, pst, rs))
			System.out.println("Error occured");

		return errorFlag;

	}

	public static boolean updateFileTablesForUpdateDirectory(FileArgs args)
	{
		// TODO Auto-generated method stub
		boolean errorFlag = false;

		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		try
		{
			con = DBUtils.getDbConnection();

			pst = con.prepareStatement("update fstat "
					+ "set protection = (select protection_id from permissions where protection_bits = ? ), "
					+ "owner_id = ?, last_access_time = now(), modified_time = now() " + "where inode = ?;");

			pst.setString(1, args.getProtection());
			pst.setString(2, args.getOwner());
			pst.setInt(3, args.getInode());

			if (0 == pst.executeUpdate())
			{
				errorFlag = true;
			}

		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			errorFlag = true;

			try
			{
				con.rollback();
			}
			catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		if (!DBUtils.closeDbConnection(con, pst, rs))
			System.out.println("Error occured");

		return errorFlag;

	}

	public static Map<Integer, FileData> getFilesToDelete(FileArgs args)
	{
		// TODO Auto-generated method stub
		boolean errorFlag = false;
		Map<Integer, FileData> map = new HashMap<Integer, FileData>();

		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		String query = "select f.inode,f.file_name, f.file_path, f.file_size, "
				+ "f.is_directory, d.file_name as 'cloud_file_name', d.file_size as 'cloud_file_size', "
				+ "d.sequence, c.* " + "from (fstat f inner join destination_cloud d right outer join "
				+ "cloud_metadata c on f.inode = d.inode and d.device_id = c.device_id) "
				+ "where f.file_path like ? order by f.inode;";
		// + "where f.file_path like '/home/test/large/%' order by d.sequence;";

		try
		{
			con = DBUtils.getDbConnection();

			pst = con.prepareStatement(query);

			String notes = args.getFilePath() + args.getFileName() + "/";
			notes = notes.replace("!", "!!").replace("%", "!%").replace("_", "!_").replace("[", "![");

			// pst.setString(1, (args.getFilePath() + args.getFileName() + "/%"));
			pst.setString(1, notes + "%");

			System.out.println(pst);

			rs = pst.executeQuery();

			List<FileSplit> splitList = new ArrayList<FileSplit>();
			int currentInode = 0;
			int splitCounter = 0;

			FileData fdd = null;

			while (rs.next())
			{
				System.out.println("test");
				System.out.println(rs.isLast());
				currentInode = rs.getInt("inode");

				FileSplit f = new FileSplit();
				DeviceData d = new DeviceData();

				f.setFileName(rs.getString("cloud_file_name"));
				f.setSplitSize(rs.getInt("cloud_file_size"));
				f.setSequence(rs.getInt("sequence"));

				d.setDeviceId(rs.getInt("device_id"));
				d.setDeviceType(rs.getString("device_type"));
				d.setFreeSpace(rs.getInt("free_space"));
				d.setToken(rs.getString("access_token"));

				f.setDeviceInfo(d);

				splitList.add(f);

				if (rs.isLast() || (!rs.isLast() && !checkIfNextIsSameInode(rs, currentInode)))
				{

					fdd = new FileData();

					// set file data fields
					fdd.setInode(rs.getInt("inode"));
					fdd.setFileName(rs.getString("file_name"));
					fdd.setFilePath(rs.getString("file_path"));
					fdd.setFileSize(rs.getInt("file_size"));
					fdd.setDirectory(rs.getBoolean("is_directory"));
					fdd.setNoOfSplits(splitCounter);

					// add file split to it
					List<FileSplit> temp = new ArrayList<FileSplit>();
					temp.addAll(splitList);
					fdd.setSplitDataList(temp);
					splitList.clear();

					// add to map
					map.put(new Integer(currentInode), fdd);

					splitCounter = 1;

				}
			}

		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			errorFlag = true;

			try
			{
				con.rollback();
			}
			catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		if (!DBUtils.closeDbConnection(con, pst, rs))
			System.out.println("Error occured");

		return map;

		// return errorFlag;

	}

	private static boolean checkIfNextIsSameInode(ResultSet rs, int currentInode)
	{
		// TODO Auto-generated method stub
		try
		{
			if (!rs.isLast())
			{
				rs.next();
				if (rs.getInt("inode") == currentInode)
				{
					rs.previous();
					return true;
				}
			}

			return false;
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public static boolean deleteFilesFromDB(Map<Integer, FileData> map)
	{
		// TODO Auto-generated method stub
		boolean errorFlag = false;

		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		try
		{
			con = DBUtils.getDbConnection();
			con.setAutoCommit(false);

			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < map.entrySet().size(); i++)
				sb.append("? ,");

			String deleteFromDestCloud = "delete from destination_cloud where inode in ( "
					+ sb.deleteCharAt(sb.length() - 1).toString() + " );";
			String deleteFromFstat = "delete from fstat where inode in ( " + sb.deleteCharAt(sb.length() - 1).toString()
					+ " );";

			pst = con.prepareStatement(deleteFromDestCloud);

			int ctr = 1;
			for (Map.Entry<Integer, FileData> entry : map.entrySet())
			{
				pst.setInt(ctr++, entry.getKey().intValue());
			}

			System.out.println(pst);
			System.out.println("Rows deleted from dest cloud: " + pst.executeUpdate());

			// if no of rows deleted is less, throw exception

			pst = con.prepareStatement(deleteFromFstat);

			ctr = 1;
			for (Map.Entry<Integer, FileData> entry : map.entrySet())
			{
				pst.setInt(ctr++, entry.getKey().intValue());
			}

			System.out.println(pst);
			System.out.println("Rows deleted from fstat: " + pst.executeUpdate());

			con.commit();

		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			errorFlag = true;

			try
			{
				con.rollback();
			}
			catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		if (!DBUtils.closeDbConnection(con, pst, rs))
			System.out.println("Error occured");

		return errorFlag;

	}

	// Sanket's changes
	public static List<String> getDirectoryContents(String directoryPath)
	{
		String query = "select * from fstat f where f.file_path=? and f.is_Directory=0";
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		List<String> inodeDetailsOfMemebrs = new ArrayList<String>();
		try
		{
			con = DBUtils.getDbConnection();

			pst = con.prepareStatement(query);
			pst.setString(1, directoryPath);

			rs = pst.executeQuery();

			while (rs.next())
			{
				inodeDetailsOfMemebrs.add(rs.getInt("inode") + rs.getString("file_name") + rs.getString("file_path")
						+ rs.getInt("file_size") + rs.getBoolean("is_directory"));

			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (!DBUtils.closeDbConnection(con, pst, rs))
			System.out.println("Error occured");
		
		return inodeDetailsOfMemebrs; 
	}

	public static List<FstatResponse> getFstatResponse(int inode, String fileName, String filePath)
	{
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		sb.append("select * from fstat where file_name = ? and file_path = ? ");
		List<FstatResponse> resp = new ArrayList<FstatResponse>(); 
		 		
		if(inode != -1)
			sb.append("and inode = ?");
		
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		
		try
		{
			con = DBUtils.getDbConnection();

			pst = con.prepareStatement(sb.toString());
			
			pst.setString(1, fileName);
			pst.setString(2, filePath);
			
			if(inode != -1)
				pst.setInt(3, inode);

			System.out.println(pst);
			
			rs = pst.executeQuery();

			while (rs.next())
			{
				FstatResponse temp = new FstatResponse();
				temp.setSt_dev(0); // ?
				temp.setSt_ino(rs.getInt("inode"));
				temp.setSt_mode(rs.getInt("protection"));
				temp.setSt_nlink(rs.getInt("hard_links"));
				temp.setSt_uid(rs.getInt("owner_id"));
				temp.setSt_gid(rs.getInt("group_id"));
				temp.setSt_rdev(0); // ?
				temp.setSt_size(rs.getInt("file_size"));
				temp.setSt_blksize(0);
				temp.setSt_blocks(0);
				temp.setSt_atime(CommonUtils.getEpochFromStringDate(rs.getString("last_access_time")));
				temp.setSt_mtime(CommonUtils.getEpochFromStringDate(rs.getString("modified_time")));
				temp.setSt_ctime(CommonUtils.getEpochFromStringDate(rs.getString("creation_time")));
								
				resp.add(temp);
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		if (!DBUtils.closeDbConnection(con, pst, rs))
			System.out.println("Error occured");
		
		return resp;
	}

	public static List<FstatResponse> readDirectory(FileArgs args)
	{
		// TODO Auto-generated method stub
		String fileName = args.getFileName();
		String filePath = args.getFilePath();
		List<FstatResponse> resp = new ArrayList<FstatResponse>();
		
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		
		String query = "select * from fstat where file_path like ?;";
		
		String notes = args.getFilePath() + args.getFileName() + "/";
		notes = notes.replace("!", "!!").replace("%", "!%").replace("_", "!_").replace("[", "![");		
		
		try
		{
			con = DBUtils.getDbConnection();

			pst = con.prepareStatement(query);
			
			// pst.setString(1, (args.getFilePath() + args.getFileName() + "/%"));
			pst.setString(1, notes + "%");						

			System.out.println(pst);
			
			rs = pst.executeQuery();

			while (rs.next())
			{
				FstatResponse temp = new FstatResponse();
				temp.setSt_dev(0); // ?
				temp.setSt_ino(rs.getInt("inode"));
				temp.setSt_mode(rs.getInt("protection"));
				temp.setSt_nlink(rs.getInt("hard_links"));
				temp.setSt_uid(rs.getInt("owner_id"));
				temp.setSt_gid(rs.getInt("group_id"));
				temp.setSt_rdev(0); // ?
				temp.setSt_size(rs.getInt("file_size"));
				temp.setSt_blksize(0);
				temp.setSt_blocks(0);
				temp.setSt_atime(CommonUtils.getEpochFromStringDate(rs.getString("last_access_time")));
				temp.setSt_mtime(CommonUtils.getEpochFromStringDate(rs.getString("modified_time")));
				temp.setSt_ctime(CommonUtils.getEpochFromStringDate(rs.getString("creation_time")));
								
				resp.add(temp);
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		if (!DBUtils.closeDbConnection(con, pst, rs))
			System.out.println("Error occured");
		
		return resp;
	}

	public String getParent(String child){
        String parent = "";
        query = "select file_path from fstat where file_name = ?";
        try {
            pst = connObj.prepareStatement(query);
            pst.setString(1, child);
            rs = pst.executeQuery();
            String parentPath = "";
            while (rs.next()) {
//                System.out.println(rs.getString("file_path"));
                parentPath = rs.getString("file_path");
            }
            if(parentPath.equals("")){
                parent ="";
            }
            else if (parentPath.equals("/")){
                parent = parentPath;
            }
            else{
                String split[] = parentPath.split("/");
                parent = split[split.length-1];
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        System.out.println(parent);
        return  parent;
    }


	public List<String> getDirEnt(String folderPath){
	    List<String> dirEnt = new ArrayList<>();
	    String split[] = folderPath.split("/");
        String currFolder;
	    if (split.length == 0){
            currFolder = "/";
        }
	    else{
	        currFolder = split[split.length-1];
        }
	    dirEnt.add(currFolder);
        dirEnt.add(getParent(currFolder));
        query = "select file_name from fstat where file_path = ?";
        try {

            pst = connObj.prepareStatement(query);
            pst.setString(1, folderPath);
            rs = pst.executeQuery();
            while (rs.next())
            {
//                System.out.println(rs.getString("file_name"));
                dirEnt.add(rs.getString("file_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dirEnt;
	}


    public FstatResponse getFstat(FileArgs args) {
        String fileName = args.getFileName();
        String filePath = args.getFilePath();
        FstatResponse temp = new FstatResponse();
        query = "select * from fstat where file_path = ? and file_name = ?";
        try {
            pst = connObj.prepareStatement(query);
            pst.setString(1, filePath);
            pst.setString(2, fileName);
            rs = pst.executeQuery();
            while (rs.next())
            {
                temp.setSt_dev(0); // ?
                temp.setSt_ino(rs.getInt("inode"));
                temp.setSt_mode(rs.getInt("protection"));
                temp.setSt_nlink(rs.getInt("hard_links"));
                temp.setSt_uid(rs.getInt("owner_id"));
                temp.setSt_gid(rs.getInt("group_id"));
                temp.setSt_rdev(0); // ?
                temp.setSt_size(rs.getInt("file_size"));
                temp.setSt_blksize(0);
                temp.setSt_blocks(0);
                temp.setSt_isdir(rs.getInt("is_directory"));
                temp.setSt_atime(CommonUtils.getEpochFromStringDate(rs.getString("last_access_time")));
                temp.setSt_mtime(CommonUtils.getEpochFromStringDate(rs.getString("modified_time")));
                temp.setSt_ctime(CommonUtils.getEpochFromStringDate(rs.getString("creation_time")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return temp;
    }



}

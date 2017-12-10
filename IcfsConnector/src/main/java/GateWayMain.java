import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.dao.FileOperationsDAO;
import com.google.gson.Gson;
import com.main.*;
import com.models.FileArgs;
import com.models.FstatResponse;
import com.models.ResponseData;

import py4j.GatewayServer;

public class GateWayMain
{
    Operations opObj = new Operations();

	public String createFile(String fileName, String filePath, String localFilePath, int fileSize, String protection, String owner)
	{
		CreateFile cf = new CreateFile();
		FileArgs args = new FileArgs();
		args.setFileName(fileName);
		args.setFilePath(filePath);
		args.setLocalFilePath(localFilePath);
		args.setFileSize(fileSize);
		args.setProtection(protection);
		args.setOwner(owner);
		
		List<FstatResponse> r = cf.createFile(args);					
		return new Gson().toJson(r);		
	}
	
	public String updateFile(String fileName, String filePath, String localFilePath, int fileSize, String protection, String owner, int inode)
	{
		UpdateFile uf = new UpdateFile();
		FileArgs args = new FileArgs();
		args.setFileName(fileName);
		args.setFilePath(filePath);
		args.setLocalFilePath(localFilePath);
		args.setFileSize(fileSize);
		args.setProtection(protection);
		args.setOwner(owner);
		args.setInode(inode);
		
		List<FstatResponse> r = uf.updateFile(args);					
		return new Gson().toJson(r);		
	}
	
	public String deleteFile(String fileName, String filePath)
	{
		DeleteFile df = new DeleteFile();
		FileArgs args = new FileArgs();
		args.setFileName(fileName);
		args.setFilePath(filePath);
		
		ResponseData r = df.deleteFile(args, null, false);					
		return new Gson().toJson(r);		
	}
	
	public String createDir(String fileName, String filePath, String protection, String owner)
	{
		CreateDirectory cf = new CreateDirectory();
		FileArgs args = new FileArgs();
		args.setFileName(fileName);
		args.setFilePath(filePath);		
		args.setProtection(protection);
		args.setOwner(owner);
		
		List<FstatResponse> r = cf.createDirectory(args);					
		return new Gson().toJson(r);		
	}

//	public String readDir(String fileName, String filePath)
//	{
//		ReadDirectory rf = new ReadDirectory();
//		FileArgs args = new FileArgs();
//		args.setFileName(fileName);
//		args.setFilePath(filePath);
//
//		List<FstatResponse> r = rf.readDirectory(args);
//		return new Gson().toJson(r);
//	}
	
	public String updateDir(String fileName, String filePath, String protection, String owner, int inode)
	{
		UpdateDirectory uf = new UpdateDirectory();
		FileArgs args = new FileArgs();
		args.setFileName(fileName);
		args.setFilePath(filePath);
		args.setFileSize(0);
		args.setProtection(protection);
		args.setOwner(owner);
		args.setInode(inode);
		
		List<FstatResponse> r = uf.updateDirectory(args);			
		return new Gson().toJson(r);		
	}
	
	public String deleteDir(String fileName, String filePath)
	{
		DeleteDirectory df = new DeleteDirectory();
		FileArgs args = new FileArgs();
		args.setFileName(fileName);
		args.setFilePath(filePath);
		
		ResponseData r = df.deleteDirectory(args);					
		return new Gson().toJson(r);		
	}

	public List<String> readDir(String folderPath){
	    List<String> dirEnt = new ArrayList<String>();

		dirEnt = opObj.readDir(folderPath);
	    return dirEnt;
    }

    public String getFstat(String fileName, String filePath){
        FileArgs args = new FileArgs();
        args.setFileName(fileName);
		args.setFilePath(filePath);
        FstatResponse r = opObj.getFstat(args);
        return new Gson().toJson(r);
    }
	
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		GateWayMain app = new GateWayMain();
		GatewayServer server = null;
		try
		{
			InetAddress host = InetAddress.getLocalHost();
			server = new GatewayServer(app, 25333, 0, host, null, 0, 0, null);
			System.out.println("GatewayServer for " + app.getClass().getName() + " started on " + host.toString());
		}
		catch (UnknownHostException e)
		{
			System.out.println("exception occurred while constructing GatewayServer().");
			e.printStackTrace();
		}
		server.start();

//        String sss = app.getFstat("home", "/tmp/nipesh/");
//        System.out.println(sss);
//        List<String> dirEnt = new ArrayList<String>();
//        dirEnt = app.readDir("/tmp/nipesh/");
//        for (String s: dirEnt) {
//            System.out.println(s);
//        }

	}

}

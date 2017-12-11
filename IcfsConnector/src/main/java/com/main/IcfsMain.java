package com.main;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.models.FileArgs;
import com.models.FstatResponse;
import com.models.ResponseData;

public class IcfsMain
{
	private static Logger log = LogManager.getLogger(IcfsMain.class);

	public static void main(String[] args)
	{

		ResponseData resp = null;
		List<FstatResponse> response = null;
		String requestType = "";
		FileArgs fileArgs = populateArgs(args);
		requestType = fileArgs.getOperation();

		switch (requestType)
		{
			case "create":
				// file and directory
				response = new CreateFile().createFile(fileArgs);
				//System.out.println(new Gson().toJson(response));
				System.out.println(response.isEmpty() ? "" : (new Gson().toJson(response.get(0))));
				//System.out.println("Create resp: " + resp.getResponseCode() + " - " + resp.getResponseMessage());
				break;

			case "read":
				response = new ReadFile().readFile(fileArgs);
				System.out.println("Read file resp : " + (response.isEmpty() ? "" : (new Gson().toJson(response.get(0)))));
				break;

			case "update":
				//resp = new UpdateFile().updateFile(fileArgs);
				response = new UpdateFile().updateFile(fileArgs);
				//System.out.println("Update resp: " + resp.getResponseCode() + " - " + resp.getResponseMessage());
				//System.out.println(new Gson().toJson(response));
				System.out.println(response.isEmpty() ? "" : (new Gson().toJson(response.get(0))));
				break;

			case "delete":
				resp = new DeleteFile().deleteFile(fileArgs, null, false);
				System.out.println("Delete resp: " + resp.getResponseCode() + " - " + resp.getResponseMessage());
				break;

			case "createdir":
				response = new CreateDirectory().createDirectory(fileArgs);
				//System.out.println("Create Dir resp: " + resp.getResponseCode() + " - " + resp.getResponseMessage());
				//System.out.println(new Gson().toJson(response));
				System.out.println(response.isEmpty() ? "" : (new Gson().toJson(response.get(0))));
				break;

			case "readdir":
				response = new ReadDirectory().readDirectory(fileArgs);
				//System.out.println("Create Dir resp: " + resp.getResponseCode() + " - " + resp.getResponseMessage());
				System.out.println(new Gson().toJson(response));
				break;

			case "updatedir": // chmod and chown
				response = new UpdateDirectory().updateDirectory(fileArgs);
				//System.out.println("Update dir resp: " + resp.getResponseCode() + " - " + resp.getResponseMessage());
				//System.out.println(new Gson().toJson(response));
				System.out.println(response.isEmpty() ? "" : (new Gson().toJson(response.get(0))));
				break;

			case "deletedir":
				resp = new DeleteDirectory().deleteDirectory(fileArgs);
				//System.out.println("Delete directory resp: " + resp.getResponseCode() + " - " + resp.getResponseMessage());
				break;

			default:
				System.out.println("Incorrect operation type received");
				break;
		}

	}

	private static FileArgs populateArgs(String[] args)
	{
		// Note - Put ints (size and inode) as -1 if arg not received

		// TODO Auto-generated method stub
		FileArgs arg = new FileArgs();

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		Options options = IcfsMain.loadArgOptions();

		try
		{
			cmd = parser.parse(options, args);
		}
		catch (ParseException e)
		{
			System.out.println("Incorrect arguments: " + e.getMessage());
			formatter.printHelp("utility-name", options);

			System.exit(1);
			return null;
		}

		arg = IcfsMain.loadArgs(cmd);

		return arg;
	}

	private static FileArgs loadArgs(CommandLine cmd)
	{
		// TODO Auto-generated method stub
		FileArgs args = new FileArgs();

		if (null != cmd.getOptionValue("op"))
			args.setOperation(cmd.getOptionValue("op"));
		if (null != cmd.getOptionValue("filename"))
			args.setFileName(cmd.getOptionValue("filename"));
		if (null != cmd.getOptionValue("filepath"))
			args.setFilePath(cmd.getOptionValue("filepath"));
		if (null != cmd.getOptionValue("localfilepath"))
			args.setLocalFilePath(cmd.getOptionValue("localfilepath"));
		if (null != cmd.getOptionValue("protection"))
			args.setProtection(new Integer(cmd.getOptionValue("protection")).intValue());
		if (null != cmd.getOptionValue("owner"))
			args.setOwner(cmd.getOptionValue("owner"));
		if (null != cmd.getOptionValue("group"))
			args.setGroup(cmd.getOptionValue("group"));
		if (null != cmd.getOptionValue("currentuser"))
			args.setCurrentUser(cmd.getOptionValue("currentuser"));
		if (null != cmd.getOptionValue("inode"))
			args.setInode(new Integer(cmd.getOptionValue("inode")).intValue());
		else
			args.setInode(-1);
		if (null != cmd.getOptionValue("filesize"))
			args.setFileSize(new Integer(cmd.getOptionValue("filesize")).intValue());
		else
			args.setFileSize(-1);
		if (null != cmd.getOptionValue("isDirectory"))
			args.setDirectory(new Boolean(cmd.getOptionValue("isDirectory")).booleanValue());

		return args;
	}

	private static Options loadArgOptions()
	{
		Options options = new Options();

		Option operation = new Option("op", true, "operation to be performed");
		operation.setRequired(true);
		options.addOption(operation);

		Option fileName = new Option("filename", true, "name of the file");
		fileName.setRequired(false);
		options.addOption(fileName);

		Option filePath = new Option("filepath", true, "path of the file");
		filePath.setRequired(false);
		options.addOption(filePath);

		Option filesize = new Option("filesize", true, "filesize");
		filesize.setRequired(false);
		options.addOption(filesize);

		Option localPath = new Option("localfilepath", true, "local path of the file");
		localPath.setRequired(false);
		options.addOption(localPath);

		Option protection = new Option("protection", true, "protection");
		protection.setRequired(false);
		options.addOption(protection);

		Option inode = new Option("inode", true, "inode");
		inode.setRequired(false);
		options.addOption(inode);

		Option owner = new Option("owner", true, "owner");
		owner.setRequired(false);
		options.addOption(owner);

		Option group = new Option("group", true, "group");
		group.setRequired(false);
		options.addOption(group);

		Option currentuser = new Option("currentuser", true, "currentuser");
		currentuser.setRequired(false);
		options.addOption(currentuser);

		Option isDirectory = new Option("isDirectory", true, "isDirectory");
		isDirectory.setRequired(false);
		options.addOption(isDirectory);


		return options;
	}
}

// -op create -filename test.txt -filepath /home/ -filesize 388 -localfilepath D:\\CS5600_Workspace\\TestData\\ -protection -rwxrwxrwx -owner 2
// -op update -filename test.txt -filepath /home/ -filesize 400 -localfilepath D:\\CS5600_Workspace\\TestData\\ -protection drwxrwxrwx -owner 1 -inode 24
// -op delete -filename test.txt -filepath /home/
// -op deletedir -filename large -filepath /home/test/
// -op readdir -filename home -filepath /
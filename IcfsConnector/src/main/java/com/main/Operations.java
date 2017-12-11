package com.main;

import com.dao.FileOperationsDAO;
import com.models.FileArgs;
import com.models.FstatResponse;
import com.utils.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Operations {

    FileOperationsDAO obj = new FileOperationsDAO();

    public List<String> readDir(String folderPath){

        List<String> dirEnt = new ArrayList<String>();
        dirEnt = obj.getDirEnt(folderPath);
        return dirEnt;
    }

    public FstatResponse getFstat(FileArgs args) {
        return obj.getFstat(args);
    }

    public int getInode(String fileName, String filePath, String owner, String group ){
        return obj.getInode( fileName,  filePath,  owner,  group );
    }

    public boolean deleteDir(String filename, String filepath){
        return obj.deleteDir(filename, filepath);
    }
}

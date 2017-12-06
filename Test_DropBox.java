import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import com.dropbox.core.v2.users.FullAccount;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;

public class Test_DropBox {

    private String DROP_BOX_ACCESS_TOKEN = "Fu8JUVLZcVAAAAAAAAACc6eiK3w9618gKRlXOfsvfVT-bWQ_L1HNcn9mfMsktv1B";
    private long CHUNKED_UPLOAD_CHUNK_SIZE = 100;

    // TODO :: just a mock up supplement to a database
    HashMap<String, String> dataBase = new HashMap<>();
    private void populateDatabase(){
        dataBase.put("output.txt_1", "19b3c594096d3398dd5337531d8937ca99e1831c8dfa43005e2dae8f6729f2b8");
        dataBase.put("output.txt_2", "07ddaac62c380cd37645aa9b003f9536ed6319f7d133634a0080f58ec6465ec6");
        dataBase.put("output.txt_3", "4333e742f5e91c7d2fda548a63b93bb5d4ca468690de39f2e35a9435e49e511e");
        dataBase.put("output.txt_4", "b02ba3fa4bfbab3737d4ace1701195de7f5c1e1331f4bcfbf1a9c73567309393");
    }
    private boolean hasHashCodeChanged(String filename, String path, String hashCode){
        System.out.println("hasHashCodeChanged filename :: "+filename);
        if (dataBase.containsKey(filename))
            return hashCode.compareTo(dataBase.get(filename)) != 0;
        return true;
    }
    private void updateDatabase(String filename, String hashCode){
        dataBase.put(filename, hashCode);
    }
    private void lookupDataBase(){
        for (String key: dataBase.keySet()){
            System.out.println(key+"    :   "+dataBase.get(key));
        }
    }
    // todo :: end of database mockup code

    private DbxClientV2 getDropBoxClient ()
            throws DbxException {
        DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
        DbxClientV2 client = new DbxClientV2(config, DROP_BOX_ACCESS_TOKEN);

        FullAccount account = client.users().getCurrentAccount();
        System.out.println(account.getName().getDisplayName());

        return client;
    }



    private void listDropBoxFiles(DbxClientV2 client)
            throws ListFolderErrorException, DbxException
    {
        // list files in drop box
        ListFolderResult result = client.files().listFolder("");
        while (true)
        {
            for (Metadata metadata : result.getEntries())
            {
                System.out.println(metadata.getPathLower());
            }

            if (!result.getHasMore())
            {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }
    }


    private FileMetadata downloadFile(DbxClientV2 client, String fileName, String path, String downloadPath)
            throws IOException, DbxException {
        FileOutputStream downloadFile = new FileOutputStream(fileName);
        FileMetadata metadata = client.files().downloadBuilder(path+fileName).download(downloadFile);
        downloadFile.close();
        return metadata;
    }


    private String calculateFileHashCode(String fileName, String path, InputStream in, long bytedToSkip, long bytesToRead)
            throws NoSuchAlgorithmException, IOException {
        System.out.println("HashCode Bytes to Read = " + (int)bytesToRead);
        String fileWithPath = path+fileName;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        if (in == null){
            InputStream fis = new FileInputStream(fileWithPath);
            byte[] dataBytes = new byte[1024];
            int nread = 0;
            while ((nread = fis.read(dataBytes,(int) bytedToSkip, (int)bytesToRead)) != -1) {
                md.update(dataBytes, 0, nread);
            }
        }
        else{
            byte[] dataBytes = IOUtils.toByteArray(in);
            md.update(dataBytes, (int)bytedToSkip, (int)bytesToRead);
            in.close();
        }

        byte[] mdbytes = md.digest();
        //convert the byte to hex format
        StringBuffer sb = new StringBuffer("");
        for (byte mdbyte : mdbytes) {
            sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }



    private void uploadFile(DbxClientV2 client, String fileName, String filePath, long fileSize)
            throws DbxException, NoSuchAlgorithmException {
        File localFile = new File(fileName);
        System.out.println("size :: "+fileSize);
        long uploaded = 0L;
        int counter = 1;
        String devicePath, deviceFilename;
        while (uploaded < fileSize){
            devicePath = "/";
            System.out.println("uploaded :: "+uploaded);
            long uploadFileSize = (CHUNKED_UPLOAD_CHUNK_SIZE <= (fileSize-uploaded))?
                    CHUNKED_UPLOAD_CHUNK_SIZE : (fileSize-uploaded);

            deviceFilename = fileName + "_" + counter;
            devicePath = devicePath + deviceFilename;
            System.out.println(devicePath);
            try (InputStream in = new FileInputStream(localFile)) {

                // hashcode
                String hashCode = calculateFileHashCode("","",
                        new FileInputStream(localFile),uploaded,uploadFileSize);
                System.out.println("hashcode :: "+hashCode);

                // skip uploaded bytes
                in.skip(uploaded);
                if (hasHashCodeChanged(deviceFilename, filePath, hashCode)) {
                    try{
                        FileMetadata metadata = client.files().uploadBuilder(devicePath)
                                .withMode(WriteMode.ADD)
                                .withClientModified(new Date(localFile.lastModified()))
                                .uploadAndFinish(in, uploadFileSize);
                        System.out.println(metadata.toStringMultiline());
                    }
                    catch (UploadErrorException e){
                        System.out.println(e);
                        deleteFile(client, deviceFilename, "/");
                        FileMetadata metadata = client.files().uploadBuilder(devicePath)
                                .withMode(WriteMode.ADD)
                                .withClientModified(new Date(localFile.lastModified()))
                                .uploadAndFinish(in, uploadFileSize);
                        System.out.println(metadata.toStringMultiline());
                    }

                    // update the database
                    updateDatabase(deviceFilename, hashCode);
                }

                in.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            uploaded+=CHUNKED_UPLOAD_CHUNK_SIZE;
            counter+=1;
        }

    }


    private void deleteFile(DbxClientV2 client, String filename, String path) throws DbxException {

        String deviceFilePath = "/"+filename;
        DeleteResult metadata = client.files().deleteV2(deviceFilePath);

    }


    private void getFileDifference(DbxClientV2 client, String fileName, String path){

        File localFile = new File(fileName);
        try (InputStream in = new FileInputStream(localFile)) {
            
        } catch (IOException e){
            System.out.println(e);
        }

    }


    public static void main(String args[])
            throws DbxException, IOException, NoSuchAlgorithmException {
        //9cee9c0c92c46ddc8c2d0d34c739fb38d3b38a05
        Test_DropBox obj = new Test_DropBox();
        DbxClientV2 client = obj.getDropBoxClient();
//        obj.listDropBoxFiles(client);
        String fileName = "output.txt";
        String path = "\\";
        String downloadPath = "";
        long size = (new File(downloadPath+fileName)).length();
//        FileMetadata metadata = obj.downloadFile(client, fileName, path, downloadPath);
//        System.out.println("Downloaded File :: "+path+fileName);
//        System.out.println("MetaData :: "+metadata);
        /*

        /output.txt_1 :: 19b3c594096d3398dd5337531d8937ca99e1831c8dfa43005e2dae8f6729f2b8
        /output.txt_2 :: 07ddaac62c380cd37645aa9b003f9536ed6319f7d133634a0080f58ec6465ec6
        /output.txt_3 :: 4333e742f5e91c7d2fda548a63b93bb5d4ca468690de39f2e35a9435e49e511e
        /output.txt_4 :: a64c0c0e5bc9f5285d9145c63490e425a6384f9a2c3da0bf07d88f002c7218fc

         */
//        String hashCode = obj.calculateFileHashCode(fileName, downloadPath, null, 0L, size);
//        System.out.println("Digest(in hex format):: " + hashCode);
        // TODO :: supplement for database mockup
//        obj.populateDatabase();
//        obj.uploadFile(client, fileName, path, size);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            System.out.print("\n\nEnter Action format <action> <filename> : ");
            String input = br.readLine();
            String[] input_split = input.split(" ");
            if (input_split[0].compareTo("DOWNLOAD") == 0){

            }
            if (input_split[0].compareTo("U") == 0){
                String filename = input_split[1];
                size = (new File(downloadPath+fileName)).length();
                obj.uploadFile(client, fileName, path, size);
            }
            if (input_split[0].compareTo("QUIT") == 0){
                System.exit(0);
            }
            if (input_split[0].compareTo("CHECK_DATABASE") == 0){
                obj.lookupDataBase();
            }

        }


    }
}

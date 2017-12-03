
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Test_Database {

    private String pythonScriptPath = "Z:/Python/2017/Distributed_File_Storage/AWS_Conn.py";
    // this is just the Python path (I used full path because
    // I dont have python path set in the ENVIRONMENT VARIABLES
    private String pythonPath = "C:/Python27/python";

    void callPythonScript(String[] cmd) throws IOException {
        // create runtime to execute external command
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec(cmd);

        // retrieve output from python script
        BufferedReader bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = "";
        while((line = bfr.readLine()) != null) {
            // display each output line form python script
            System.out.println(line);
        }
    }

    void read_file_fstat() throws IOException {
        /*
            OUTPUT HAS THE FOLLOWING DATA
            'SELECT f.inode, f.file_name, f.path, f.protection, f.owner_id, f.group_id, f.size, ' \
            'f.last_access_time, f.creation_time, f.modification_time, f.last_access_user, ' \
            'd.device_file_name, d.device_sequence, d.device_size, d.device_path, d.device_id, ' \
            'p.protection_bits ' \
            'FROM DistributedFileSystem.Fstat f INNER JOIN DistributedFileSystem.Destination d ' \
            'INNER JOIN DistributedFileSystem.Permission p '
         */
        // set up the command and parameter
        // cmd array is of length 6 because
        // 0 -- for the interpreter path
        // 1 -- python script path
        // 2 to 5  -- arguments needed by the python script
        String[] cmd = new String[6];
        cmd[0] = pythonPath;
        cmd[1] = pythonScriptPath;
        // set up the arguments for the python script
        // we need 4 arguments for the script to run the read_file_fstat
        // hence we need to have store the 4 arguments needed
        // the actual way to run the python code is
        // $python AWS_Conn.py <function> <file_name> <file_path> <owner_id>
        // in this case <function> is read_file_fstat
        cmd[2] = "read_file_fstat";
        cmd[3] = "output.txt";
        cmd[4] = "/"; // the path is \ (which represents the root)
        cmd[5] = "1000";

        callPythonScript(cmd);
    }


    void store_file_fstat() throws IOException {
        // set up the command and parameter
        // cmd array is of length 4 because
        // 0 -- for the interpreter path
        // 1 -- python script path
        // 2 to 3  -- arguments needed by the python script
        String[] cmd = new String[13];
        cmd[0] = pythonPath;
        cmd[1] = pythonScriptPath;
        // set up the arguments for the python script
        // for storing the file fstat we need 2 arguments
        // $python AWS_Conn.py <function> <file_name> <path> <protection> <owner_id> <group_id> <acc_tym> <mod_tym> <cre_tym>
        cmd[2] = "store_file_fstat";
        cmd[3] = "doc.pdf"; //file_name
        cmd[4] =  "/"; //path
        cmd[5] = "777"; //protection
        cmd[6] = "1000"; //owner_id
        cmd[7] = "1000"; //group_id
        cmd[8] = "2017-12-03T04:59:59.000Z"; //last_access_time
        cmd[9] = "2017-12-03T03:59:59.000Z"; //modification_time
        cmd[10] = "2017-12-03T01:59:59.000Z"; //creation_time
        cmd[11] = "662090"; //size
        cmd[12] = "1000"; // current user (to be used as last_access_user)

        callPythonScript(cmd);

    }

    public static void main(String args[]) throws IOException {
        Test_Database obj = new Test_Database();
//        obj.read_file_fstat();
        obj.store_file_fstat();

    }
}

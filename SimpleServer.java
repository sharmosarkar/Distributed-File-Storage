//https://www.py4j.org/getting_started.html

import java.net.InetAddress;
import java.net.UnknownHostException;
import py4j.GatewayServer;
import java.util.HashMap;

public class SimpleServer {

	public int addition( int first, int second ) {
		return first+second;
	}

	public HashMap<String, Integer> getStat() {
        HashMap<String, Integer> stat = new HashMap<>();
        stat.put("st_mode", 16877);
        stat.put("st_ino", 3936067);
        stat.put("st_dev", 2049);
        stat.put("st_nlink", 3);
        stat.put("st_uid", 0);
        stat.put("st_gid", 0);
        stat.put("st_size", 4096);
        stat.put("st_atime", 1512636558);
        stat.put("st_mtime", 1512636510);
        stat.put("st_ctime", 1512636510);

        return stat;
    }

	public static void main( String[] args) {
		SimpleServer app = new SimpleServer();
		GatewayServer server = null;
		try {
			InetAddress host = InetAddress.getLocalHost();
			server = new GatewayServer(app, 25333, 0, host, null, 0, 0, null );
			System.out.println( "GatewayServer for " + app.getClass().getName() + " started on " + host.toString() );
		}
		catch (UnknownHostException e) {
			System.out.println( "exception occurred while constructing GatewayServer()." ); 
			e.printStackTrace();
		}
		server.start();
	}
	
}

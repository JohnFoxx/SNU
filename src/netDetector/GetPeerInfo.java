package netDetector;
import java.net.InetAddress;
import java.net.UnknownHostException;

import controller.Logger;


public class GetPeerInfo {
	
	static PeerInfo get()
	{
		String l_tmp_hostname 							= null;
		String l_tmp_username 							= null;

		
		/*
		 * Get hostname
		 */
		try {
			l_tmp_hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			Logger.exception(e.toString());
			l_tmp_hostname = "<Not Found>";
		}
		
		/*
		 * Get username
		 */
		
		l_tmp_username = System.getProperty("user.name");
		
		
		return new PeerInfo(l_tmp_hostname,l_tmp_username);
	}
}

package netDetector;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Broadcast {
	
	public static ArrayList<InetAddress> getBroadcastAddresses() throws SocketException 
	{
		ArrayList<InetAddress> broadcastAddressList = new ArrayList<InetAddress>();
		ArrayList<NetworkInterface> networkInterfaceList = new ArrayList<NetworkInterface>();
		
			Enumeration<NetworkInterface> netInterfaceEnum = NetworkInterface.getNetworkInterfaces();

			while ( netInterfaceEnum.hasMoreElements() ) 
			{
				NetworkInterface networkInterface = netInterfaceEnum.nextElement();

				if (!networkInterface.isLoopback()) {
					networkInterfaceList.add(networkInterface);
				}
				
			}

			// get all broadcast addresses from all interfaces
			for (NetworkInterface netInterface : networkInterfaceList) {

				List<InterfaceAddress> ifaddrList = netInterface.getInterfaceAddresses();

				for (InterfaceAddress interfaceAddr : ifaddrList) 
				{
					System.out.println(interfaceAddr);
					InetAddress broadcastAddress = interfaceAddr.getBroadcast();
					if (broadcastAddress == null) 
					{
						continue;
					} 
					else 
					{

						if (!broadcastAddressList.contains(broadcastAddress)) 
						{
							broadcastAddressList.add(broadcastAddress);
						} 
					}
				}
			}
		
		return broadcastAddressList;
	}

}

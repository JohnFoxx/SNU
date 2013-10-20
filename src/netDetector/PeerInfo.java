package netDetector;

public class PeerInfo implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1878600247457397631L;
	
	public String PI_hostname = null;
	public String PI_username = null;
	

	public PeerInfo( 	String a_hostname, 
						String a_username)
	{
		this.PI_hostname = a_hostname;
		this.PI_username = a_username;
	}
	
	public boolean equals(Object arg0 )
	{
		if( arg0 == null)
			return false;
		
		PeerInfo l_peerinfo = (PeerInfo)arg0;
		
		return ( this.PI_hostname.equals(l_peerinfo.PI_hostname) &&
				 this.PI_username.equals(l_peerinfo.PI_username));
	}

	public void display()
	{

		System.out.println("HOSTNAME : \t" + this.PI_hostname);
		System.out.println("USERNAME : \t" + this.PI_username);
		System.out.println("SERIAL : \t " + this.hashCode());
	}
}

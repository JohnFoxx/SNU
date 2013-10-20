package netDetector;

public class Peer {
	
	public PeerInfo	 	infos = null;
	public String		addr  = null;
	
	/*Not tested in equal*/
	public long			timeout = 0;
	
	public boolean equals( Object arg0 )
	{
		if(arg0 == null)
			return false;
		
		Peer l_peer = (Peer)arg0;
		
		return (	this.infos.equals(l_peer.infos) && 
					this.addr.equals(l_peer.addr));
	}
	
	public String toString()
	{
		return "<html><strong>" + this.infos.PI_username + "</strong>@<i>" + this.infos.PI_hostname + "</i> (" + this.addr + ")</html>";
	}
	
	public void update()
	{
		this.timeout = System.currentTimeMillis();
	}
}

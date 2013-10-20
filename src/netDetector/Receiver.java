package netDetector;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import controller.Logger;


public class Receiver extends Thread{

	private DatagramSocket 		Receiver_socket 		= null;
	private long				Receiver_timeout		= 0;
	
	
	// theses variables can be used by concurrent thread
	private volatile ArrayList<Peer> 	Receiver_peers			= null;
	private volatile boolean			Receiver_enabled		= true;
	
	public Receiver(int a_port, long a_timeout ) throws SocketException
	{
		this.Receiver_socket =  new DatagramSocket(a_port);
		//this.Receiver_socket.setSoTimeout(2000);
		
		this.Receiver_timeout = a_timeout;
		this.Receiver_peers = new ArrayList<Peer>();
	}
	
	private Peer Recv() throws IOException, ClassNotFoundException
	{
		DatagramPacket			l_content 		= null;
		byte[]					l_byte_data		= null;
		ByteArrayInputStream 	l_byte_stream 	= null;
		ObjectInputStream 		l_object_stream = null;
		Peer	 				l_tmp_peer		= new Peer();
		
		l_byte_data = new byte[1024];
		l_content = new DatagramPacket(l_byte_data, l_byte_data.length);
		
		Receiver_socket.receive(l_content);
		
		l_byte_stream = new ByteArrayInputStream(l_byte_data);
		l_object_stream = new ObjectInputStream(l_byte_stream);
		
		l_tmp_peer.infos = (PeerInfo) l_object_stream.readObject();
		l_tmp_peer.addr  = l_content.getAddress().getHostAddress();
		l_tmp_peer.timeout = System.currentTimeMillis();
		
		return l_tmp_peer;
	}
	
	
	public /*synchronized*/ void run()
	{
		Peer l_tmp_peer = null;
		int  l_tmp_error_count = 0;
		this.Receiver_enabled = true;
		
		while( this.Receiver_enabled )
		{
			try {
				
				l_tmp_peer = Recv();

				while( this.Receiver_peers.contains(l_tmp_peer))
					this.Receiver_peers.remove(l_tmp_peer);
				
				this.Receiver_peers.add( l_tmp_peer );
				
				l_tmp_error_count = 0;
				
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				
				if( this.Receiver_enabled )
				{
					Logger.exception(e.toString());
					/* IO Error, just wait a bit, network can be down */
					try {
						Thread.sleep( this.Receiver_timeout*10 );
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						Logger.exception(e1.toString());
					}
				}
				else
				{
					/* NOTHING TO DO SOCKET WAS INTENTIONNALLY CLOSED */
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				Logger.exception(e.toString());
				
				l_tmp_error_count++;
			} 
			
			if( l_tmp_error_count > 3)
			{
				/*
				 * If error count is higher than 3, stop all
				 * It means something bad occur 3 times consecutively or more
				 */
				exit();
			}
		}
	}
	
	public void exit()
	{
		this.Receiver_enabled = false;
		this.Receiver_socket.close();
	}
	
	
	public boolean is_running()
	{
		return this.Receiver_enabled;
	}
	
	public void setTimeout( long a_timeout )
	{
		this.Receiver_timeout = a_timeout;
	}
	
	/*
	 * Update content according to timeout
	 */
	public synchronized ArrayList<Peer> getPeers()
	{
		Peer l_tmp_peer = null;
		int l_tmp_peer_index = 0;
		
		for( 	l_tmp_peer_index = this.Receiver_peers.size() - 1 ; 
				l_tmp_peer_index >= 0 ; 
				l_tmp_peer_index-- )
		{
			l_tmp_peer  = this.Receiver_peers.get( l_tmp_peer_index );
			if( (System.currentTimeMillis() - l_tmp_peer.timeout ) > this.Receiver_timeout )
			{
				this.Receiver_peers.remove(l_tmp_peer_index);
			}
		}
		
		return this.Receiver_peers;
	}
	

}

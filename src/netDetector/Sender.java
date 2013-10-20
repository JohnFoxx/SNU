package netDetector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;

import controller.Logger;

public class Sender extends Thread{

	private DatagramSocket 		Sender_socket 			= null;
	private DatagramPacket  	Sender_packet			= null;
	private PeerInfo			Sender_info				= null;
	private int 				Sender_port 			= 0;
	private volatile boolean	Sender_enabled			= true;
	private long				Sender_time_wait		= 0;
	
	private ArrayList<InetAddress>	Sender_broadcast_addressses = null;
	
	
	public Sender( int a_port, long a_period) throws IOException 
	{
		Sender_socket = new DatagramSocket();
		Sender_socket.setBroadcast(true);
		
		this.Sender_port = a_port;
		this.Sender_time_wait = a_period;
		this.Sender_info = GetPeerInfo.get();
		this.Sender_packet = createPacketFromInfo( this.Sender_info );
		
		// Get all broadcast addresses, in cas of error, set internet broadcast address
		try 
		{
			this.Sender_broadcast_addressses = Broadcast.getBroadcastAddresses();
		} 
		catch (SocketException e) 
		{
			// TODO Auto-generated catch block
			this.Sender_broadcast_addressses = new ArrayList<InetAddress>();
			this.Sender_broadcast_addressses.add(InetAddress.getByName("255.255.255.255"));
		}
		
	}
	
	private DatagramPacket createPacketFromInfo( PeerInfo info ) throws IOException
	{
        ByteArrayOutputStream 		l_byte_stream 		= null;
        ObjectOutputStream 			l_object_stream 	= null;
        DatagramPacket				l_content 			= null;
        
        l_byte_stream =         new ByteArrayOutputStream();
        l_object_stream = 		new ObjectOutputStream(l_byte_stream);
        
        l_object_stream.writeObject( this.Sender_info);
        l_object_stream.flush();

        l_content = new DatagramPacket(l_byte_stream.toByteArray(), l_byte_stream.size());
        l_content.setPort(this.Sender_port);
        
		l_object_stream.close();
        
        return l_content;
	}
	
	private void Send() throws IOException
	{
		PeerInfo l_info = GetPeerInfo.get();
		
		if( !this.Sender_info.equals( l_info ) )
		{
			this.Sender_info = l_info;
			this.Sender_packet = createPacketFromInfo( this.Sender_info );
		}
		
		// Send to all broadcast addresses
		for( InetAddress l_tmp_broadcast : this.Sender_broadcast_addressses)
		{
			this.Sender_packet.setAddress( l_tmp_broadcast );
			Sender_socket.send( this.Sender_packet );
		}
		
	}
	
	public void run()
	{
		this.Sender_enabled = true;
		int l_tmp_error_count = 0;
		
		while( this.Sender_enabled )
		{
			try {
				Send();
				Thread.sleep( this.Sender_time_wait );
				l_tmp_error_count = 0;
			}
			catch (SocketException e)
			{
				if(this.Sender_enabled)
				{
					l_tmp_error_count++;
					Logger.exception(e.toString());
				}
				else
				{
					/*ELSE in means that socket was intentionnally closed*/
				}
			}
			catch (IOException e) {
				// TODO Auto-generated catch block

				
				if( this.Sender_enabled )
				{
					Logger.exception(e.toString());
					/* IO Error, just wait a bit, network can be down */
					try {
						Thread.sleep( this.Sender_time_wait*10 );
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						Logger.exception(e1.toString());
					}
				}
				else
				{
					/* NOTHING TO DO SOCKET WAS INTENTIONNALLY CLOSED */
				}
				
			} catch (InterruptedException e) {
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
	
	public void setPeriod( long a_period )
	{
		this.Sender_time_wait = a_period;
	}
	
	public void setPort ( int a_port )
	{
		this.Sender_port = a_port;
		this.Sender_packet.setPort( this.Sender_port );
	}
	
	public boolean is_running()
	{
		return this.Sender_enabled;
	}
	
	public void exit() 
	{
		this.Sender_enabled = false;
		this.Sender_socket.close();
	}
}

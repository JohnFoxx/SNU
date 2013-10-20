package fileLoad;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Downloader {

	private static final String ALLOWED = "OK";
	private static final String REJECTED = "NO";
	
	private Socket do_socket;
	private String do_dir;
	private String do_filename;
	private long   do_fileSize;

	
	public Downloader()
	{
		do_socket = null;
		do_dir = null;
		do_fileSize = 0;
		do_filename = null;
	}
	
	public Downloader( Socket a_socket, String a_dir)
	{
		do_socket = a_socket;
		do_dir = a_dir;
		do_fileSize = 0;
		do_filename = null;
	}
	
	public void ReSet( Socket a_socket, String a_dir)
	{
		do_socket = a_socket;
		do_dir = a_dir;
		do_fileSize = 0;
		do_filename = null;
	}
	
	public boolean getHeader() throws IOException
	{
		Scanner l_readStream;
		
		if( ( do_socket == null ) ||
				( do_dir == null) )
			return false;
		
		l_readStream = new Scanner( do_socket.getInputStream() );
		
		do_filename = l_readStream.nextLine();// l_header[0];
		do_fileSize = Long.parseLong(l_readStream.nextLine());
		
		return ( ( !do_filename.isEmpty() ) && ( do_fileSize > 0 ) );
	}
	

	
	public String getFilenameFromHeader()
	{
		return do_filename;
	}
	
	public long getFileSizeFromHeader()
	{
		return do_fileSize;
	}
	
	public void accept() throws IOException
	{
		sendAnswer(ALLOWED);
	}
	
	public void refuse() throws IOException
	{
		sendAnswer(REJECTED);
	}
	
	private void sendAnswer( String a_answer ) throws IOException
	{
		PrintStream l_printStream = new PrintStream( do_socket.getOutputStream() );
		
		l_printStream.println(a_answer);
	}
	
	public boolean getData() throws IOException
	{
		DataInputStream   l_in 					= null;
		FileOutputStream  l_out					= null;
		
		long l_sizeReceived						= 0;
		byte []l_buffer							= new byte[1024];
		int l_read								= 0;

		/*
		 * Check if filename and/or socket are not null
		 * Check if file size is not equal to 0
		 */
		if( 	( do_filename == null ) || 
				( do_socket == null ) 	|| 
				( do_fileSize == 0 ) 	||
				( do_dir == null)        )
			return false;
		
		l_in = new DataInputStream(do_socket.getInputStream());
		l_out = new FileOutputStream(do_dir + File.separator + do_filename);
		
		/*
		 * Send bytes till end of file
		 */
		while( 	((l_read = l_in.read(l_buffer)) != -1) && 
				(l_sizeReceived < do_fileSize))
		{
			l_out.write(l_buffer,0,l_read);
			l_sizeReceived += l_read;
		}
		
		/*
		 * Close streams
		 */
		l_in.close();
		l_out.close();
		
		/*
		 * if size_sent is equal to file size, then upload was successfully terminated.
		 */
		return (l_sizeReceived == do_fileSize );
	}
}

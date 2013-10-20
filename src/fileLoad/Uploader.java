package fileLoad;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;


public class Uploader {
	
	private static final String ALLOWED = "OK";
	
	private Socket up_socket;
	private File   up_filename;
	private long   up_fileSize;
	private long   up_sizeSent;
	
	private Progress	up_progress;
	
	public Uploader()
	{
		up_socket = null;
		up_filename = null;
		up_fileSize = 0;
		up_sizeSent = 0;
		
		up_progress = null;
	}
	
	public Uploader( Socket a_socket, String a_filename)
	{
		up_socket = a_socket;
		up_filename = new File(a_filename);
		up_fileSize = 0;
		up_sizeSent = 0;
		
		up_progress = null;
	}
	
	public void ReSet( Socket a_socket, String a_filename )
	{
		up_socket = a_socket;
		up_filename = new File(a_filename);
		up_fileSize = 0;
		up_sizeSent = 0;
		
		
		up_progress = null;
	}
	
	public void enableProgress( Progress progress)
	{
		this.up_progress = progress;
	}
	
	public boolean SendHeader() throws IOException
	{
		File l_fileAttribute;
		PrintStream l_printStream;
		Scanner l_readStream;
		
		String  l_basename;
		long	l_fileSize;
		
		/*
		 * Check if socket and/or filename are not null
		 */
		if( ( up_socket == null   ) || 
			( up_filename == null )  )
			return false;
		
		/*
		 * Get file attrbutes
		 */
		l_fileAttribute = up_filename;
		if( ( ! l_fileAttribute.exists() ) || 
			( ! l_fileAttribute.isFile() ) || 
			( ! l_fileAttribute.canRead() ) )
		{
			return false;
		}
		
		l_basename = l_fileAttribute.getName();
		l_fileSize = l_fileAttribute.length();
		
		/*
		 * Get in/out streams
		 */
		
		l_printStream = new PrintStream( up_socket.getOutputStream() );
		l_readStream = new Scanner( up_socket.getInputStream() );
		

		
		/*
		 * Send Basename and file size
		 */
		l_printStream.println(l_basename);
		l_printStream.println(Long.toString(l_fileSize));
		
		/*
		 * Retrieve server's response
		 */
		up_fileSize = l_fileSize;
		return l_readStream.nextLine().equals(ALLOWED);

	}
	
	public boolean SendData() throws IOException
	{
		DataOutputStream l_out 					= null;
		FileInputStream  l_in					= null;
		
		byte []l_buffer							= new byte[1024];
		int l_read								= 0;
		
		/*
		 * Check if filename and/or socket are not null
		 * Check if file size is not equal to 0
		 */
		if( 	( up_filename == null ) || 
				( up_socket == null ) 	|| 
				( up_fileSize == 0 ) 	)
			return false;
		
		/*
		 * Retrieve file read stream and network out stream.
		 */
		l_out = new DataOutputStream(up_socket.getOutputStream());
		l_in = new FileInputStream(up_filename);
		
		/*
		 * Reset size sent.
		 */
		up_sizeSent = 0;
		
		/*
		 * Reset progress, if it's enabled
		 */
		if(up_progress != null)
			up_progress.startProgress((Object) up_filename.getName(), (long) up_fileSize);
		
		/*
		 * Send bytes till end of file
		 */
		while( (l_read = l_in.read(l_buffer)) != -1 )
		{
			l_out.write(l_buffer,0,l_read);
			up_sizeSent += l_read;
			if(up_progress != null)
				up_progress.setProgress(up_sizeSent);
		}
		
		/*
		 * Close streams
		 */
		l_in.close();
		l_out.close();
		
		/*
		 * if size_sent is equal to file size, then upload was successfully terminated.
		 */
		return (up_sizeSent == up_fileSize );
	}
	
	public Progress getProgress()
	{
		return this.up_progress;
	}
	
}

package controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
	

	private static PrintWriter				logger_destination = null;
	
	private static final String ERROR_HEADER = "[ERROR]\t";
	private static final String INFO_HEADER = "[INFO]\t";
	private static final String WARNING_HEADER = "[WARNING]\t";
	
	public static void init( String logger_file ) throws FileNotFoundException
	{
		logger_destination = new PrintWriter( logger_file );
	}
	
	public static void close() throws IOException
	{
		logger_destination.close();
	}
	
	public static void error( String message )
	{
		System.err.println( ERROR_HEADER + message );
		if( logger_destination != null )
		{
			logger_destination.println( ERROR_HEADER + message );
			logger_destination.flush();
		}
	}
	
	public static void notify( String message )
	{
		System.out.println( INFO_HEADER + message );
		if( logger_destination != null )
		{
			logger_destination.println( INFO_HEADER + message );
			logger_destination.flush();
		}
	}
	
	public static void warn( String message )
	{
		System.out.println( WARNING_HEADER + message );
		if( logger_destination != null )
		{
			logger_destination.println( WARNING_HEADER + message );
			logger_destination.flush();
		}
	}
	
	public static void exception( String exception )
	{
		String l_disp_bloc = new String("\r\n");
		l_disp_bloc += "-----------------------------------------------------------------------------------------------\r\n";
		l_disp_bloc += exception;
		l_disp_bloc += "-----------------------------------------------------------------------------------------------\r\n";
		
		System.err.println( l_disp_bloc );
		if( logger_destination != null )
		{
			logger_destination.println( l_disp_bloc );
			logger_destination.flush();
		}
	}
	
}

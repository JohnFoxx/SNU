package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Scanner;


public class Configuration {

	private FileReader configuration_file;
	private String	   configuration_sep;

	public Configuration( String configuration_file,String separator ) throws FileNotFoundException
	{
		File l_conf_file = new File( configuration_file );
		this.configuration_sep = separator;
		
		if(l_conf_file.exists() && l_conf_file.isFile() && l_conf_file.canRead())
			this.configuration_file = new FileReader( l_conf_file );
		else
			this.configuration_file = null;
	}
	
	public HashMap<String,String> getConfigurationData()
	{
		HashMap<String,String> 	l_tmp_map;
		Scanner		  			l_reader;
		Scanner					l_line_scanner;
		String			   		l_tmp_key;
		String			   		l_tmp_val;
		
		if( this.configuration_file == null)
			return null;
		
		l_tmp_map = new HashMap<String,String>();
		l_reader = new Scanner( this.configuration_file );
		
		while( l_reader.hasNext())
		{
			l_tmp_key = null;
			l_tmp_val = null;
			
			l_line_scanner = new Scanner( l_reader.nextLine());
			l_line_scanner.useDelimiter(this.configuration_sep);
			
			if(l_line_scanner.hasNext())
				l_tmp_key = l_line_scanner.next().trim();
			if(l_line_scanner.hasNext())
				l_tmp_val = l_line_scanner.next().trim();
			
			if( l_tmp_key != null && l_tmp_val != null)
			{
				l_tmp_val.replaceAll("[^\\w\\s]", "");
				Logger.notify("CONFIGURATION get " + l_tmp_key + " is " + l_tmp_val + ";");
				l_tmp_map.put( l_tmp_key, l_tmp_val );
			}
		}
		
		if( l_tmp_map.size() < 0 )
			return null;
		
		return l_tmp_map;
	}
}

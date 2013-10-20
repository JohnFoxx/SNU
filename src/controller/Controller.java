package controller;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import fileLoad.Downloader;
import fileLoad.Uploader;

import mainWindow.MainWindow;
import mainWindow.TrayInteract;
import mainWindow.UserInteract;
import netDetector.Peer;
import netDetector.Receiver;
import netDetector.Sender;

public class Controller {
	
	// ----------------- // Instances
	
	private ServerSocket	ctrl_srvSock	= null;
	private Receiver		ctrl_receiver 	= null;
	private Sender			ctrl_sender		= null;
	private Uploader		ctrl_uploader   = null;
	private Downloader		ctrl_downloader = null;
	
	private MainWindow		ctrl_mainwindow = null;
	private TrayInteract	ctrl_trayinter  = null;
	
	
	// ------------------ // Thread activation
	
	private Thread					ctrl_fileTransfertServer  = null;
	private volatile boolean		ctrl_fileServerActivation = true;
	
	// ------------------ // Constants
	
	private Configuration	ctrl_configuration      = null;
	private int 			ctrl_fileTransfertPort 	= 9999; // TCP port to listen and send	 			// "filetransfertport" 		key
	private int				ctrl_netDetectorPort 	= 9999; // UDP port to listen and send				// "netdetectorport" 		key
	private long			ctrl_netDetectorTimeout = 2000; // Timeout to declare host disconnected		// "netdetectortimeout" 	key
	private long			ctrl_netDetectorPeriod  = 2000; // Period for between 2 udp packet send		// "netdetectorperiod"  	key
	private long 			ctrl_tcpTransfertTimeout= 60000; // 1 min timeout for TCP socket			// "filetransferttimeout" 	key
	
	private String			ctrl_fileBoxDirectory 	= System.getProperty("user.home") + "/SNUFileBox";
	
	public Controller() throws IOException
	{
		super();
		load();
	}
	
	public Controller( 	String conf_file )  throws IOException
	{
		super();
		this.ctrl_configuration = new Configuration( conf_file,"=" );
		load();
	}
	
	private void load() throws IOException
	{
		CheckFileBox();
		if( this.ctrl_configuration != null)
			getConstants();
		instanciate();
	}
	
	private void CheckFileBox() throws IOException
	{
		File l_filebox = new File( this.ctrl_fileBoxDirectory );
		
		if( (!l_filebox.exists()) || 
			(!l_filebox.isDirectory()) )
		{
			l_filebox.mkdir();
		}
	}
	
	private void getConstants()
	{
		HashMap<String,String> l_data = this.ctrl_configuration.getConfigurationData();
		
		if (l_data == null)
			return;
		
		if(l_data.containsKey("filetransfertport"))
			this.ctrl_fileTransfertPort = Integer.parseInt( l_data.get("filetransfertport"));
		
		if(l_data.containsKey("netdetectorport"))
			this.ctrl_netDetectorPort = Integer.parseInt( l_data.get("netdetectorport"));
		
		if(l_data.containsKey("netdetectortimeout"))
			this.ctrl_netDetectorTimeout = Long.parseLong( l_data.get("netdetectortimeout"));
		
		if(l_data.containsKey("netdetectorperiod"))
			this.ctrl_netDetectorPeriod = Long.parseLong( l_data.get("netdetectorperiod"));
		
		if(l_data.containsKey("filetransferttimeout"))
			this.ctrl_tcpTransfertTimeout = Long.parseLong( l_data.get("filetransferttimeout"));
	}
	
	private void instanciate () throws IOException
	{
		Logger.notify("Creating new services...");
		
		ctrl_srvSock = new ServerSocket( ctrl_fileTransfertPort );
		ctrl_srvSock.setReuseAddress(true);
		ctrl_receiver = new Receiver( ctrl_netDetectorPort, ctrl_netDetectorTimeout);
		ctrl_sender = new Sender( ctrl_netDetectorPort, ctrl_netDetectorPeriod );
		
		ctrl_uploader = new Uploader();
		ctrl_downloader = new Downloader();
		
		/*
		 * Create Main window (give it controller)
		 */
		ctrl_mainwindow = new MainWindow( this );
		ctrl_trayinter = new TrayInteract( this, ctrl_mainwindow );
	}
	
	
	
	
	private void fileTransfertServer()
	{
		
		Socket		l_newClient = null;
		
		this.ctrl_fileServerActivation = true;
		
		while( this.ctrl_fileServerActivation )
		{
			try 
			{
				l_newClient = this.ctrl_srvSock.accept();
				l_newClient.setSoTimeout( (int) this.ctrl_tcpTransfertTimeout );
				
				ctrl_downloader.ReSet( l_newClient, 
						this.ctrl_fileBoxDirectory );
				
				if( ctrl_downloader.getHeader() ) // a timeout can occur
				{
					//ASK TO USER !!
					String ll_clientName = addrToName( l_newClient.getInetAddress() );
					String ll_fileName   = ctrl_downloader.getFilenameFromHeader();
					String ll_fileSize   = Long.toString( ctrl_downloader.getFileSizeFromHeader());
					
					Logger.notify("New File Transfert Request from " + ll_clientName + " (" + ll_fileName + "," + ll_fileSize + " Bytes )");
					
					if( UserInteract.askuser(  	"<html><strong>" + ll_clientName + "</strong> wants to send you a file : </html>\n\n" + 
												"<html>Name\t : <strong><i>\"" + ll_fileName + "\"</i></strong>.</html>\n" +
												"<html>Size\t : <strong>" + ll_fileSize + "</strong> octets.</html>\n\n" +
												"<html><center>Will you accept this request ?</center></html>","New request !") )
					{
						ctrl_downloader.accept();
						ctrl_trayinter.InformationMessage("Download of " + ll_fileName + " started.");
						
						if( ctrl_downloader.getData() ) // a timeout can occur
						{
							// SUCCESS DOWNLOAD
							Logger.notify("Download is ok !");
							ctrl_trayinter.InformationMessage("Download of " + ll_fileName + " successful !");
						}
						else
						{
							// FAILURE DOWNLOAD
							Logger.error("Download failed...");
							ctrl_trayinter.ErrorMessage("Download of " + ll_fileName + " failed...");
						}
					}
					else
					{
						ctrl_downloader.refuse();
					}
				}
				else
				{
					Logger.error("Cannot read the header...");
				}
				
				l_newClient.close();
			} 
			catch ( SocketTimeoutException e)
			{
				//Logger.exception(e.toString());;
				// No answer from client
				Logger.error("Client timed out...");
				
			}
			catch (SocketException e)
			{
				if( this.ctrl_fileServerActivation)
				{
					Logger.exception(e.toString());;
					Logger.error("Socket error...");
				}
				else
				{
					/* Nothing to do : server was intentionally closed */
				}
			}
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				Logger.exception(e.toString());;
				Logger.error("I/O Fatal error...");
			}
			finally
			{
				// Closing the socket is not compulsory, thanks to garbage collector
			}

		}
	}
	
	private String addrToName( InetAddress a_client )
	{
		for( Peer l_tmp_peer : this.ctrl_receiver.getPeers() )
		{
			if( l_tmp_peer.addr.equals( a_client.getHostAddress()))
			{
				return l_tmp_peer.infos.PI_username;
			}
		}
		
		return a_client.getHostAddress();
	}
	
	public void launch()
	{
		Logger.notify("Starting all services...");
		
		ctrl_fileTransfertServer = new Thread (
				new Runnable() 
				{
					public void run()
					{
						fileTransfertServer();
					}
				} 
		);
		
		
		this.ctrl_receiver.start();
		this.ctrl_fileTransfertServer.start();
		this.ctrl_sender.start();
	}
	
	public void turnOff() throws InterruptedException
	{
		Logger.notify("Turning off all services...");
		
		this.ctrl_fileServerActivation = false;	// Quit FileServer
		this.ctrl_receiver.exit();				// Quit Receiver
		this.ctrl_sender.exit();				// Quit Sender
		this.ctrl_mainwindow.exit();			// Quit MainWindow
		
		// ---------- // Wait for the end...
		
		try 
		{
			this.ctrl_srvSock.close();			// Close server
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			Logger.exception(e.toString());;
		}
		
		
		// Then wait for all processes to end
		this.ctrl_fileTransfertServer.join();
		this.ctrl_receiver.join();
		this.ctrl_sender.join();
	}
	
	public void restartAll() 
	{
		try 
		{
			turnOff();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Logger.exception(e.toString());;
			 Logger.warn("Error occurs while turning off services...");
		}
		
		/* In order to reset all properly, it's can be cool to reload constants, so re - instanciate all*/
		
		try 
		{
			load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Logger.exception(e.toString());;
			Logger.error("Fatal Errors occurs while instanciating services..");
			return;
		}
		
		launch();
	}
	
	public void sendAFile( Peer a_recipient, File a_file ) throws UnknownHostException, IOException
	{
		Socket l_cliSock = new Socket( a_recipient.addr, this.ctrl_fileTransfertPort);
		ctrl_uploader.ReSet( l_cliSock, a_file.getAbsolutePath() );
		ctrl_uploader.enableProgress( this.ctrl_mainwindow.getFileProgressBar());
		
		
		Logger.notify("Starting an upload request to " + a_recipient.infos.PI_username + " for file : " + a_file);
		try 
		{
			if ( ctrl_uploader.SendHeader() ) // a timeout can occur
			{
				Logger.notify("Remote host accepted file transfert.");
				// Now start transfert of file

				if ( ctrl_uploader.SendData() ) // a timeout can occur
				{
					// SUCCESS
					UserInteract.info(" Upload was successfully performed !");
					Logger.notify("File "+ a_file.getName() +" was successfully sent !");
				}
				else
				{
					// ERROR
					Logger.error("Upload of "+ a_file.getName() +" failed...");
					ctrl_uploader.getProgress().interruptProgress();
				}
			}
			else
			{
				// CAN'T CONTACT CLIENT
				Logger.error("Remote host refused file transfert.");
				UserInteract.error("Remote host has rejected the file transfert request...");
			}
		} 
		catch ( SocketTimeoutException e)
		{
			Logger.exception(e.toString());;
			// No answer from server
			ctrl_uploader.getProgress().interruptProgress();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			Logger.exception(e.toString());;
			ctrl_uploader.getProgress().interruptProgress();
		}
		finally
		{
			l_cliSock.close();
		}	
	}
	
	public ArrayList<Peer> getUpdatedPeers()
	{
		return this.ctrl_receiver.getPeers();
	}
	
	public static void main(String[] args) throws IOException, InterruptedException
	{

		Controller appli = null;
		//System.out.println(System.getProperty("os.version")); System.exit(0);
		
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
		}
		
		Logger.init("trace.log");
		
		if( args.length > 0)
			appli = new Controller( args[0] ); // launch with a config file
		else
			appli = new Controller();
		
		appli.launch();

		
	}
}

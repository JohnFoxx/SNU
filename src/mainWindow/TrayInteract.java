package mainWindow;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import controller.Controller;
import controller.Logger;

public class TrayInteract {
	
    private TrayIcon 		ti_trayIcon 	= null;
    private SystemTray 		ti_tray 		= null;
    private ImageIcon		ti_image        = null;
    private PopupMenu 		ti_popup 		= null;
    private MenuItem 		ti_aboutItem 	= null;
    private MenuItem 		ti_displayItem 	= null;
    private MenuItem 		ti_restartItem 	= null;
    private MenuItem		ti_exitItem		= null;
    
    private Controller		ti_controller 	= null;
    private MainWindow		ti_main			= null;
    
    public TrayInteract( Controller ctrl, MainWindow main )
    {
        if (!SystemTray.isSupported()) 
        {
            // Tray not supported !
        	Logger.error("FATAL : Tray system not supported !");
            System.exit(0);
        }
        
        this.ti_image = new ImageIcon("ressources/network.png");
        if( this.ti_image == null)
        {
        	// Cannot find tray image !
        	Logger.error("FATAL : Cannot retrieve Tray Icon !");
        	System.exit(0);
        }
        
    	this.ti_controller 			= ctrl;
    	this.ti_main 				= main;
    	this.ti_trayIcon 			= new TrayIcon( this.ti_image.getImage() );
    	this.ti_tray 				= SystemTray.getSystemTray();
    	
    	this.ti_popup				= new PopupMenu();
    	this.ti_aboutItem 			= new MenuItem("About");
    	this.ti_displayItem			= new MenuItem("Display");
    	this.ti_restartItem         = new MenuItem("Restart");
    	this.ti_exitItem            = new MenuItem("Exit");
    	
    	this.ti_popup.add( this.ti_aboutItem );
    	this.ti_popup.add( this.ti_displayItem );
    	this.ti_popup.add( this.ti_restartItem );
    	this.ti_popup.addSeparator();
    	this.ti_popup.add( this.ti_exitItem );
    	
    	this.ti_trayIcon.setPopupMenu( this.ti_popup );
    	try
    	{
			this.ti_tray.add( this.ti_trayIcon );
		} 
    	catch (AWTException e) 
    	{
			// TODO Auto-generated catch block
    		Logger.exception(e.toString());
			System.exit(0);
		}
    	
    	this.ti_trayIcon.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
            	SwingUtilities.invokeLater( new Runnable() {
            		
            		public void run()
            		{
            			ti_main.showWindow();
            		}
            		
            	});
            	
            }
        });
    	
        this.ti_aboutItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                UserInteract.about();
            }
        });
        
        this.ti_displayItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
            	ti_main.showWindow();
            	
            }
        });
        
        this.ti_restartItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            	SwingUtilities.invokeLater( new Runnable() {
            		
            		public void run()
            		{
            			// RESTART METHODS
            			ti_tray.remove( ti_trayIcon );
            			ti_controller.restartAll();
            		}
            		
            	});
            }
        });
        
        this.ti_exitItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            	SwingUtilities.invokeLater( new Runnable() {
            		
            		public void run()
            		{
            			//EXIT METHODS
            			try 
            			{
            				ti_tray.remove( ti_trayIcon );
							ti_controller.turnOff();
							System.exit(0);
						} 
            			catch (InterruptedException e) 
            			{
							// TODO Auto-generated catch block
            				Logger.exception(e.toString());
						}
            		}
            		
            	});
            }
        });
    }
    
    public void InformationMessage( String message )
    {
    	final String l_message = message;
    	
    	SwingUtilities.invokeLater( new Runnable() {
    		
    		public void run()
    		{
    			ti_trayIcon.displayMessage("SNU", l_message, TrayIcon.MessageType.INFO);
    		}
    		
    	});
    }
    
    public void ErrorMessage ( String message )
    {
    	final String l_message = message;
    	
    	SwingUtilities.invokeLater( new Runnable() {
    		
    		public void run()
    		{
    			ti_trayIcon.displayMessage("SNU", l_message, TrayIcon.MessageType.WARNING);
    		}
    		
    	});
    }

}

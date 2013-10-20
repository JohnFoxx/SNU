package mainWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import controller.Controller;
import controller.Logger;
import netDetector.Peer;

public class MainWindow extends JFrame {

	/**
	 * 
	 */
	private static final long 	serialVersionUID 	= -3775040083699917169L;
	private final int 			widthFrame 			= 400;
	private final int 			heightFrame			= 300;
	
	private int							posXFrame	= 0;
	private int							posYFrame   = 0;
	
	private JLabel						updateIcon  = null;
	private JList	   					peerList    = null;
	private FileProgressBar				progressBar = null;
	private JButton						hideButton  = null;
	private ImageIcon					iconButton  = null;
	private ImageIcon					iconUpdate  = null;
	
	private JPanel						northPanel  = null;
	private JPanel						southPanel  = null;
	private JPanel						centerPanel = null;
	
	private DefaultListModel	 		peerListModel = null;
	private volatile boolean			peerListModelAutoRefresh = true; 
	
	private boolean 					alreadyUploading = false;
	
	private Controller controller = null;
	
	public MainWindow( Controller controller)
	{
		super();
		
		this.controller = controller;

		initComponents();
		autoRefresh();
		
	}
	
	public void exit()
	{
		this.peerListModelAutoRefresh = false;
		this.dispose();
	}
	
	private void computePosScreen()
	{
		Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		this.posXFrame = bounds.x + bounds.width - this.widthFrame;
		this.posYFrame = bounds.y + bounds.height - this.heightFrame;
	}
	
	private void initComponents()
	{
		this.setUndecorated(true);
		this.setSize(this.widthFrame,this.heightFrame);
		
		peerListModel = new DefaultListModel();
		peerList = new JList(peerListModel);
		peerList.setBackground(new Color(0xd6d9df));
		
		progressBar = new FileProgressBar();
		
		hideButton  = new JButton();
		iconButton = new ImageIcon( new ImageIcon("ressources/hide.png").getImage().getScaledInstance(30, 20, Image.SCALE_DEFAULT) );
		hideButton.setIcon( iconButton );
		hideButton.setToolTipText("Hide");
		
		updateIcon = new JLabel();
		iconUpdate = new ImageIcon( new ImageIcon("ressources/updating.gif").getImage().getScaledInstance(30, 30, Image.SCALE_REPLICATE) );
		updateIcon.setIcon(iconUpdate);
		updateIcon.setBorder(new EmptyBorder(6,0,0,30));
		
		northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
		northPanel.add(hideButton,BorderLayout.WEST);
		northPanel.add(updateIcon,BorderLayout.EAST);
		
		southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		southPanel.add(progressBar,BorderLayout.CENTER);
		
		centerPanel = new JPanel();
		centerPanel.setBorder(BorderFactory.createTitledBorder("Hosts detected on network :"));
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(peerList,BorderLayout.CENTER);
		
		this.getContentPane().setLayout(new BorderLayout());
		this.add(northPanel,BorderLayout.NORTH);
		this.add(centerPanel, BorderLayout.CENTER);
		this.add(southPanel,BorderLayout.SOUTH);
		this.setAlwaysOnTop(true);
		
		eventManager();
		computePosScreen();
		showWindow();
	}

	private void autoRefresh() 
	{
		// TODO Auto-generated method stub
		
		this.peerListModelAutoRefresh = true;
		
			new Thread( new Runnable()
			{
				@Override
				public void run() {
					// TODO Auto-generated method stub
					while( peerListModelAutoRefresh )
					{
						SwingUtilities.invokeLater( new Runnable() {
							
							public void run()
							{
								ArrayList<Peer> l_peerOriginalModel = controller.getUpdatedPeers();
								
								
								for (int l_i = peerListModel.size()-1 ; l_i >= 0 ; l_i--)
								{
									/*
									 * Remove Peers only if it's timeout
									 */
									if( !l_peerOriginalModel.contains( peerListModel.get(l_i) ) )
									{
										peerListModel.remove( l_i );
									}
								}
								
								for( Peer l_tmp_peer : controller.getUpdatedPeers())
								{
									if( !peerListModel.contains(l_tmp_peer))
									{
										peerListModel.addElement(l_tmp_peer);
									}
									else
									{
										
									}
								}
							}
							
						});
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							Logger.exception(e.toString());
						}
					}
				}
				
			}).start();
	}
	
	private void eventManager()
	{
		
		/*
		 * Add an action (upload file) to double click on selected item
		 */
		MouseListener mouseListener = new MouseAdapter() 
		{
		    public void mouseClicked(MouseEvent e) 
		    {
		        if (e.getClickCount() == 2) 
		        /* Double Click*/
		        {
		        	if( !alreadyUploading )
		        	{
				           final Peer l_selectedPeer = (Peer) peerList.getSelectedValue();
				           
				           if( l_selectedPeer == null)
				        	   return;
				           
				           new Thread( new Runnable()
				           {
				        		   public void run()
				        		   {
				        			   try 
						        	   {
				        				   File l_selectedFile = UserInteract.askfile();
				        		           if( l_selectedFile != null)
				        		           {
				        		        	   alreadyUploading = true;
				        		        	   controller.sendAFile(l_selectedPeer, l_selectedFile);
				        		        	   alreadyUploading = false;
				        		           }
						        	   } catch (UnknownHostException e1) {
										// TODO Auto-generated catch block
						        		   Logger.exception(e1.toString());
						        	   } catch (IOException e1) 
						        	   {
										// TODO Auto-generated catch block
						        		   Logger.exception(e1.toString());
						        	   }	
				        		   }
				        	   }).start();
				        	   
				    }
		        	else
		        	{
		        		UserInteract.error("You can only perform one download at a time...");
		        	}
		        }
		        
		       }
		};
		peerList.addMouseListener(mouseListener);
		
		/*
		 * Add an actionListener to hide button
		 */
		
		hideButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				// TODO Auto-generated method stub
				hideWindow();
			}	
		});
		
	}
	
	public FileProgressBar getFileProgressBar()
	{
		return this.progressBar;
	}
	
	public void hideWindow()
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run()
			{
				setVisible(false);
			}
		});
	}
	
	public void showWindow()
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run()
			{
				setLocation( posXFrame, posYFrame);
				setVisible(true);
			}
		});
	}
}

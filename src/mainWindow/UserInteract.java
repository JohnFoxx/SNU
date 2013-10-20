package mainWindow;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import controller.Logger;


public class UserInteract {

	public static final String ABOUT = 	"<html><strong>About</strong></html>\n\n" +
			"This Software was designed to make easier local network configuration and file transfert.\n" +
			"This Software is under free licence, you can dispose / modify it without any conditions.\n" +
			"If you have any remarks and/or suggestions, please contact me at : \n\n" +
			"<html><center><i><u>moise.roussel@mail.com</u></i></center></html>";
	
	public static boolean askuser( String a_request,String a_title )
	{
		final int[]  l_result = new int[1];
		final String l_request = a_request;
		final String l_title = a_title;
		
		l_result[0] = -1;
		
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
			
				public void run()
				{
					l_result[0] = JOptionPane.showConfirmDialog (null, l_request, l_title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				}
			});
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Logger.exception(e.toString());
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			Logger.exception(e.toString());
		}
		
		return ( l_result[0] == JOptionPane.YES_OPTION );
	}
	
	public static File askfile()
	{
		final JFileChooser l_fileChooser = new JFileChooser();
		final File[]	   l_fileSelected = new File[1];
		
		l_fileSelected[0] = null;
		
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				
				public void run()
				{
					l_fileChooser.setDialogTitle("Choose a File");
					int response = l_fileChooser.showOpenDialog(null);
					
					if( response != JFileChooser.APPROVE_OPTION)
					{
						l_fileSelected[0] = null;
					}
					else
					{
						l_fileSelected[0] = l_fileChooser.getSelectedFile();
					}
				}
			});
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Logger.exception(e.toString());
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			Logger.exception(e.toString());
		}
		
		return l_fileSelected[0];
	}
	
	public static void error ( String message )
	{
		final String l_message = message;
	
		SwingUtilities.invokeLater( new Runnable() {
				
				public void run()
				{
					JOptionPane.showMessageDialog(null, l_message,"Error",JOptionPane.ERROR_MESSAGE);
				}
			});

	}
	
	public static void info ( String message )
	{
		final String l_message = message;

		SwingUtilities.invokeLater( new Runnable() {
			
			public void run()
			{
				JOptionPane.showMessageDialog(null, l_message,"Info",JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}
	
	public static void about()
	{
		UserInteract.info(UserInteract.ABOUT);
	}
}

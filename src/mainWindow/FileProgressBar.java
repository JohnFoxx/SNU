package mainWindow;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import fileLoad.Progress;

public class FileProgressBar extends JPanel implements Progress{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8088183949467475391L;
	
	private JProgressBar	progressBar   = null;
	private JLabel		    progressLabel = null;

	@SuppressWarnings("unused")
	private String 		    filename 	  = null;
	private boolean 	    completed 	  = false;
	private boolean 	    interrupted   = false;
	
	private long 	        maximum       = 0;
	private long            current       = 0;
	
	public FileProgressBar()
	{
		super();
		this.progressBar = new JProgressBar();
		this.progressLabel = new JLabel();
		this.progressLabel.setBorder(new EmptyBorder(0,10,0,0));
		
		this.filename = "";
		this.progressBar.setMaximum(100);
		this.progressBar.setMinimum(0);
		this.progressLabel.setText("");
		
		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(0,4,6,6));
		this.add(progressBar,BorderLayout.CENTER);
		this.add(progressLabel,BorderLayout.EAST);
	}
	
	public void startProgress( Object subject, long maximum )
	{
		this.filename = subject.toString();
		this.completed = false;
		this.maximum = maximum;
		this.current = 0;
		
		SwingUtilities.invokeLater( new Runnable() {
			
			public void run()
			{		
				progressBar.setValue(0);	
				progressLabel.setText( getStringText() );
			}
		});
	}
	
	public void setProgress( long value )
	{
		this.current = value;
		
		final int percent = (int) (this.current*100/this.maximum);
		
		SwingUtilities.invokeLater( new Runnable() {
			
			public void run()
			{		
				progressBar.setValue( percent );
				progressLabel.setText( getStringText() );
				if(percent == progressBar.getMaximum())
				{
					completed = true;
				}
			}
		});
		
	}
	
	public boolean is_completed()
	{
		return this.completed;
	}
	
	public void interruptProgress()
	{
		this.interrupted = true;

		SwingUtilities.invokeLater( new Runnable() {
			
			public void run()
			{		
				progressBar.setValue( progressBar.getMinimum() );
				progressLabel.setText("<html></i>Interrupted</i></html>");
			}
		});
	}
	
	public boolean is_interrupted()
	{
		return this.interrupted;
	}
	
	private String getStringText()
	{
		return "<html><strong>" + this.progressBar.getValue() + "%</strong></html>";
		
	}
}

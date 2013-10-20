package fileLoad;

public interface Progress {

	public void startProgress( Object subject, long maximum );
	public void setProgress( long evolution );
	public void interruptProgress();
}

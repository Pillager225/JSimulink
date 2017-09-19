package edu.scu.engr.rsl.connections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;

public class DTconnection {
	protected InputStream iStream = null;
	protected OutputStream oStream = null;
	protected Source dtSource;
	protected ChannelMap srcChannels;
	protected Sink dtSink;
	protected Thread sinkThread = null, sourceThread = null;
	protected ChannelMap sinkChannels;
	protected String dthostname = "127.0.0.1:3333", subscriptionHandle = "*/*", sourceName = "dirt"; 
	protected boolean debug = false;
	protected static String endOfHelpText = "\tdthostname: The connection handle for Dataturbine (probably 127.0.0.1:3333)\n"
											+ "\tsourceName: The name you would like this program to have on Dataturbine\n" 
											+ "\tsubscriptionHandle: The source name and channel you are connecting to on Dataturbine (something like MatlabSource/MatlabChannel or MatlabSource/*)";
	
	public DTconnection(String dthostname, String sourceName, String subscriptionHandle) {
		this.dthostname = dthostname;
		this.sourceName = sourceName;
		this.subscriptionHandle = subscriptionHandle;
	}
	
	public DTconnection(String dthostname, String sourceName, String subscriptionHandle, boolean debug) {
		this.dthostname = dthostname;
		this.sourceName = sourceName;
		this.subscriptionHandle = subscriptionHandle;
		this.debug = debug;
	}
	
	// this should be called after the constructor is called
	// ostream and istream should be setup before this is called
	protected void start() {
		try {
			setupSource();
			setupSink();
		} catch (SAPIException e) {
			e.printStackTrace();
			System.exit(1);
		}
		setupThreads();
		startThreads();
		try {
			waitForThreads();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		exitGracefully();
	}
	
	private void setupSource() throws SAPIException {
		dtSource = new Source();
		srcChannels = new ChannelMap();
		srcChannels.Add(sourceName+"Channel"); 
		dtSource.OpenRBNBConnection(dthostname, sourceName+"Source"); 
	}
	
	private void setupSink() throws SAPIException {
		dtSink = new Sink();
		sinkChannels = new ChannelMap();
		sinkChannels.Add(subscriptionHandle); 
		dtSink.OpenRBNBConnection(dthostname, sourceName+"Sink"); 
		dtSink.Monitor(sinkChannels, 0);
	}
	
	private Thread sinkThread() {
		return new Thread() {
			@Override
			public void run() {
				try {
					while(!isInterrupted()) {
						ChannelMap m = dtSink.Fetch(0);
						if(m.NumberOfChannels() > 0) {
							byte[] data = m.GetData(0);
							if(debug) {
								System.out.print(data);
							}
							oStream.write(data);
						}
					}
				} catch (SAPIException | IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	// if a child class needs something special (like a async connection) then this should be overridden
	// see SerialToDT for an example
	protected Thread sourceThread() {
		return new Thread() {
			@Override
			public void run() {
				try {
					while(!isInterrupted()) {
						int readableBytes = iStream.available();
						if(readableBytes > 0) {
							byte[] data = new byte[readableBytes];
							iStream.read(data, 0, readableBytes);
							if(debug) {
								System.out.print(data);
							}
							srcChannels.PutDataAsInt8(0,  data);
							dtSource.Flush(srcChannels, true);
						}
					}
				} catch (SAPIException | IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	protected void setupThreads() {
		sinkThread = sinkThread();
		sourceThread = sourceThread();
	}
	
	protected void startThreads() {
		if(sinkThread != null) {
			sinkThread.start();
		}
		if(sourceThread != null) {
			sourceThread.start();
		}
	}
	
	protected void waitForThreads() throws InterruptedException {
		if(sinkThread != null) {
			sinkThread.join();
		}
		if(sourceThread != null) {
			sourceThread.join();
		}
	}
	
	protected void closeStreams() throws IOException {
		if(oStream != null) {
			oStream.close();
		}
		if(iStream != null) {
			iStream.close();
		}
	}
	
	protected void exitGracefully() {
		try {
			dtSink.CloseRBNBConnection();
			dtSource.CloseRBNBConnection();
			closeStreams();
		} catch (IOException e) {
			dtSink.CloseRBNBConnection();
			dtSource.CloseRBNBConnection();
			try {
				closeStreams();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			if(sinkThread != null) {
				sinkThread.interrupt();
			}
			if(sourceThread != null) {
				sourceThread.interrupt();
			}
			try {
				waitForThreads();
			} catch (InterruptedException e1) {
				System.exit(1);
			}
		}
	}
}

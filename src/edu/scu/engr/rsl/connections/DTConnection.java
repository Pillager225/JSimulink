package edu.scu.engr.rsl.connections;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;

/**
 * This class provides a connection to DataTurbine.
 * It supports subscribing to only one channel and publishing to a single channel.
 * 
 * @author Ryan Cooper (ryanloringcooper@gmail.com)
 * @date September 19th, 2017
 */
public class DTConnection {
	protected Source dtSource;
	protected ChannelMap srcChannels;
	protected Sink dtSink;
	protected ChannelMap sinkChannels;
	protected String dthostname = "127.0.0.1:3333", subscriptionHandle = "*/*", sourceName = "defaultSource"; 
	protected boolean debug = false;
	protected static String endOfHelpText = "\tdthostname: The connection handle for Dataturbine (probably 127.0.0.1:3333)\n"
			+ "\tsourceName: The name you would like this program to have on Dataturbine\n" 
			+ "\tsubscriptionHandle: The source name and channel you are connecting to on Dataturbine (something like MatlabSource/MatlabChannel or MatlabSource/*)";
	
	public DTConnection(String dthostname, String sourceName, String subscriptionHandle) {
		this.dthostname = dthostname;
		this.sourceName = sourceName;
		this.subscriptionHandle = subscriptionHandle;
		setupDTConnection();
	}

	public DTConnection(String dthostname, String sourceName, String subscriptionHandle, boolean debug) {
		this.dthostname = dthostname;
		this.sourceName = sourceName;
		this.subscriptionHandle = subscriptionHandle;
		this.debug = debug;
		setupDTConnection();
	}
	
	protected void setupDTConnection() {
		setupSource();
		setupSink();
	}
	
	protected void setupSource() {
		dtSource = new Source();
		srcChannels = new ChannelMap();
		try {
			srcChannels.Add(sourceName+"Channel");
			dtSource.OpenRBNBConnection(dthostname, sourceName+"Source"); 
		} catch (SAPIException e) {
			e.printStackTrace();
		} 
	}
	
	protected void setupSink() {
		dtSink = new Sink();
		sinkChannels = new ChannelMap();
		try {
			sinkChannels.Add(subscriptionHandle);
			dtSink.OpenRBNBConnection(dthostname, sourceName+"Sink"); 
			dtSink.Monitor(sinkChannels, 0);
		} catch (SAPIException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * This function is typically overridden and called in inherited classes.
	 */
	protected void exitGracefully() {
		dtSink.CloseRBNBConnection();
		dtSource.CloseRBNBConnection();
	}

	public void write(String data) throws SAPIException {
		write(data.getBytes());
	}
	
	public void write(byte[] data) throws SAPIException {
		if(debug) {
			System.out.print(data);
		}
		srcChannels.PutDataAsInt8(0,  data);
		dtSource.Flush(srcChannels, true);
	}
	
	public byte[] read() throws SAPIException {
		ChannelMap m = dtSink.Fetch(0);
		if(m.NumberOfChannels() > 0) {
			byte[] data = m.GetData(0);
			if(debug) {
				System.out.print(data);
			}
			return data;
		}
		return null;
	}
	
	/**
	 * This function provides a public interface for shutting down the
	 * connections to DataTurbine.
	 */
	public void closeConnections() {
		exitGracefully();
	}
}
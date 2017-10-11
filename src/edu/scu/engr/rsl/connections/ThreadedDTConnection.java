package edu.scu.engr.rsl.connections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.rbnb.sapi.SAPIException;

/**
 * This abstract class provides a threaded connection to DataTurbine.
 * It supports subscribing to only one channel and publishing to a single channel.
 * 
 * The abstract method should be implemented to setup the InputStream and OutputStream 
 * used in the threads.
 * 
 * This class is useful because it acts as a glue for communication methods to connect
 * to DataTurbine.
 * 
 * @author Ryan Cooper (ryanloringcooper@gmail.com)
 * @date September 19th, 2017
 */
public abstract class ThreadedDTConnection extends DTConnection {
	protected InputStream iStream = null;
	protected OutputStream oStream = null;
	protected Thread sinkThread = null, sourceThread = null;
	
	public ThreadedDTConnection(String dthostname, String sourceName, String subscriptionHandle) {
		super(dthostname, sourceName, subscriptionHandle);
	}
	
	public ThreadedDTConnection(String dthostname, String sourceName, String subscriptionHandle, boolean debug) {
		super(dthostname, sourceName, subscriptionHandle, debug);
	}
	
	/** 
	 * This is called in start and is required to be implemented in a child class.
	 * 
	 * @throws IOException
	 */
	protected abstract void setupStreams() throws IOException;
	
	/**
	 * Begins the communication over streams to and from DataTurbine
	 */
	protected void start() { 
		try {
			setupStreams();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		setupThreads();
		startThreads();
		try {
			waitForThreads();
		} catch (InterruptedException e) {
			// Ctrl-c was probably entered
		}
		exitGracefully();
	}
	
	protected Thread sinkThread() {
		return new Thread() {
			@Override
			public void run() {
				try {
					while(!isInterrupted()) {
						byte[] data = read();
						if(data != null) {
							oStream.write(data);
						}
					}
				} catch (SAPIException | IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	/** 
	 * If a child class needs something special (like a connection driven by callbacks)
	 * then this should be overridden. See SerialToDT for an example.
	 */
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
							write(data);
						}
					}
				} catch (SAPIException | IOException e) {
					/* If a SAPIException happens here, I figure DataTurbine is
					 * down and it won't be pleasant to see the same error over
					 * and over.
					 */
					e.printStackTrace();
				}
			}
		};
	}
	
	/**
	 * Since the threads could have been redefined in a child class, the threads are
	 * actually set here to allow for the redefinitions to take effect.
	 */
	protected void setupThreads() {
		sinkThread = sinkThread();
		sourceThread = sourceThread();
	}
	
	/**
	 * Will only start the threads if they are not null. This is important because
	 * if a child process wants to disable one of the threads because it is handling
	 * the source or sink differently, then it will set one of the threads to null.
	 * See SerialToDT for an example.
	 */
	protected void startThreads() {
		if(sinkThread != null) {
			sinkThread.start();
		}
		if(sourceThread != null) {
			sourceThread.start();
		}
	}
	
	/**
	 * Will wait for the threads to finish executing before moving on.
	 * 
	 * @throws InterruptedException
	 */
	protected void waitForThreads() throws InterruptedException {
		if(sinkThread != null) {
			sinkThread.join();
		}
		if(sourceThread != null) {
			sourceThread.join();
		}
	}
	
	/**
	 * If there is a valid stream open, close it.
	 * 
	 * @throws IOException
	 */
	protected void closeStreams() throws IOException {
		if(oStream != null) {
			oStream.close();
		}
		if(iStream != null) {
			iStream.close();
		}
	}
	
	/** 
	 * Will attempt to end the threads in a respectful manner.
	 */
	protected void interruptThreads() {
		if(sinkThread != null) {
			sinkThread.interrupt();
		}
		if(sourceThread != null) {
			sourceThread.interrupt();
		}
		try {
			waitForThreads();
		} catch (InterruptedException e1) {
			// somebody is getting a little excited with Ctrl-c
			System.exit(-1);
		}
	}
	
	/**
	 * This will clean up the threads and close the streams in addtion to doing what
	 * the parent class does.
	 * This is initially defined in DTConnection.
	 */
	@Override
	protected void exitGracefully() {
		interruptThreads();
		super.exitGracefully();
		try {
			closeStreams();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

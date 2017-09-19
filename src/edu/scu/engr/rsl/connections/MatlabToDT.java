package edu.scu.engr.rsl.connections;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MatlabToDT extends DTconnection {
	protected boolean oStreamSetup = false;

	public MatlabToDT(String dthostname, String sourceName, String subscriptionHandle) {
		super(dthostname, sourceName, subscriptionHandle);
	}

	public MatlabToDT(String dthostname, String sourceName, String subscriptionHandle, boolean debug) {
		super(dthostname, sourceName, subscriptionHandle, debug);
	}
	
	public void writeToDT(String data) {
		writeToDT(data.getBytes());
	}
	
	public void writeToDT(byte[] data) {
		iStream = new ByteArrayInputStream(data);
	}
	
	public int available() {
		return ((ByteArrayOutputStream)oStream).size();
	}
	
	public String read() {
		String retval = ((ByteArrayOutputStream)oStream).toString();
		try {
			oStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return retval;
	}
	
	public void start() {
		if(oStreamSetup) {
			super.start();
		} else {
			
		}
	}
}

package edu.scu.engr.rsl.connections;

import java.io.IOException;
import java.net.Socket;

/**
 * This class provides a TCP connection a connection to DataTurbine.
 * It supports subscribing to only one channel and publishing to a single channel.
 * 
 * This class acts as a TCP client.
 * 
 * @author Ryan Cooper (ryanloringcooper@gmail.com)
 * @date September 19th, 2017
 */
public class TCPToDT extends ThreadedDTConnection {
	protected Socket sock;
	// regex to determine if args contains an IPv4 address 
	static String ipaddrRegex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
	
	public TCPToDT(String ipaddr, int port, String dthostname, String sourceName, String subscriptionHandle) {
		super(dthostname, sourceName, subscriptionHandle);
		setupSock(ipaddr, port);
		start();
	}
	
	public TCPToDT(String ipaddr, int port, String dthostname, String sourceName, String subscriptionHandle, boolean debug) {
		super(dthostname, sourceName, subscriptionHandle, debug);
		setupSock(ipaddr, port);
		start();
	}
	
	protected void setupSock(String ipaddr, int port) {
		try {
			sock = new Socket(ipaddr, port);
		} 
		catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}
	
	@Override
	protected void setupStreams() throws IOException {
		oStream = sock.getOutputStream();
		iStream = sock.getInputStream();	
	}
	
	@Override
	protected void exitGracefully() {
		super.exitGracefully();
		try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This function provides a command line interface for using and testing this class
	 * 
	 * @param args The command line arguments describing the connection. See the help text for description about usage.
	 */
	public static void main(String[] args) {
		if(args.length == 5 && args[0].matches(ipaddrRegex)) {
			new TCPToDT(args[0], Integer.parseInt(args[1]), args[2], args[3], args[4]);
		} else {
			System.out.println("\nIncorrect arguments.");
			System.out.println("Usage:\n\tjava -jar TCPToDT.jar ipaddr port dthostname sourceName subscriptionHandle");
			System.out.println("\tipaddr: The IP address of the socket the program you are connecting to Dataturbine is on");
			System.out.println("\tport: The port number of the socket the program you are connecting to Dataturbine is on");
			System.out.println(endOfHelpText);
			System.exit(3);
		}
	}
}

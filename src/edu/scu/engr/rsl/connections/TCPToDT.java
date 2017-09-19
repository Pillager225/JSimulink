package edu.scu.engr.rsl.connections;

import java.io.IOException;
import java.net.Socket;

// This is a TCP client
public class TCPToDT extends DTconnection {
	// regex to determine if args contains an IPv4 address 
	static String ipaddrRegex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
	Socket sock;
	
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
	
	private void setupSock(String ipaddr, int port) {
		try {
			sock = new Socket(ipaddr, port);
			oStream = sock.getOutputStream();
			iStream = sock.getInputStream();
		} 
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void main(String[] args) {
		if(args.length == 5 && args[0].matches(ipaddrRegex)) {
			new TCPToDT(args[0], Integer.parseInt(args[1]), args[2], args[3], args[4]);
		} else {
			System.out.println("\nIncorrect usage\nUsage:\n\tjava -jar TCPToDT.jar ipaddr port dthostname sourceName subscriptionHandle");
			System.out.println("\tipaddr: The IP address of the socket the program you are connecting to Dataturbine is on");
			System.out.println("\tport: The port number of the socket the program you are connecting to Dataturbine is on");
			System.out.println(endOfHelpText);
		}
	}
	
}

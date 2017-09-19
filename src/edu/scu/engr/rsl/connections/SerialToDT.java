package edu.scu.engr.rsl.connections;

import java.io.IOException;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

import com.rbnb.sapi.SAPIException;

import edu.scu.engr.rsl.util.SerialConnectionException;
import edu.scu.engr.rsl.util.SerialParameters;

public class SerialToDT extends DTconnection implements SerialPortEventListener {
	private int databits = 8;
	private int stop = 1;
	private int parity = 0;
	private SerialPort serialPort;
	
	public SerialToDT(String port, int baud, String dthostname, String sourceName, String subscriptionHandle) {
		super(dthostname, sourceName, subscriptionHandle);
		setupSerialPort(port, baud);
		start();
	}
	
	public SerialToDT(String port, int baud, String dthostname, String sourceName, String subscriptionHandle, boolean debug) {
		super(dthostname, sourceName, subscriptionHandle, debug);
		setupSerialPort(port, baud);
		start();
	}
	
	private void setupSerialPort(String port, int baud) {
		System.loadLibrary("win32com");
		SerialParameters serialParameters = new SerialParameters(port, baud, SerialPort.FLOWCONTROL_NONE, SerialPort.FLOWCONTROL_NONE, databits, stop, parity);
		try {
			CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(serialParameters.getPortName());
	
			serialPort = (SerialPort) portId.open("CommHandler", 2000);
			System.out.println("Serial connection established!");
	
			oStream = serialPort.getOutputStream();
			iStream = serialPort.getInputStream();
			setConnectionParameters(serialParameters);
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			serialPort.notifyOnBreakInterrupt(true);
			serialPort.enableReceiveTimeout(30);
			serialPort.notifyOnOutputEmpty(true);

			// add code to get rts/cts signals - attempt to solve packet errors 
			serialPort.notifyOnCTS(true);
			serialPort.notifyOnDSR(true);
		} catch (SerialConnectionException | IOException | NoSuchPortException | PortInUseException | TooManyListenersException | UnsupportedCommOperationException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Sets the connection parameters to the setting in the parameters object. If
	 * set fails return the parameters object to origional settings and throw
	 * exception.
	 * 
	 * @throws SerialConnectionException
	 */
	private void setConnectionParameters(SerialParameters serialParams) throws SerialConnectionException {

		// Save state of parameters before trying a set.
		int oldBaudRate = serialPort.getBaudRate();
		int oldDatabits = serialPort.getDataBits();
		int oldStopbits = serialPort.getStopBits();
		int oldParity = serialPort.getParity();

		// Set connection parameters, if set fails return parameters object
		// to original state.
		try
		{
			serialPort.setSerialPortParams(serialParams.getBaudRate(),
					serialParams.getDatabits(),
					serialParams.getStopbits(),
					serialParams.getParity());
		}
		catch (UnsupportedCommOperationException e)
		{
			serialParams.setBaudRate(oldBaudRate);
			serialParams.setDatabits(oldDatabits);
			serialParams.setStopbits(oldStopbits);
			serialParams.setParity(oldParity);
			throw new SerialConnectionException("Unsupported parameter");
		}

		// Set flow control.
		try
		{
			serialPort.setFlowControlMode(serialParams.getFlowControlIn()
					| serialParams.getFlowControlOut());
		}
		catch (UnsupportedCommOperationException e) {
			throw new SerialConnectionException("Unsupported flow control");
		}
	}

	@Override
	protected Thread sourceThread() {
		return null;
	}
	
	/**
	 * Implements the <code>serialEvent</code> function of the <code>SerialPortListener</code> 
	 * interface. When data is available on the COM port, it is read until the 
	 * transmission is complete, packed up, and <code>Observer</code> are notified 
	 * of the receipt of data. 
	 * @param evt The <code>SerialPortEvent</code> provided by the underlying object 
	 * 		responsible for event notification. 
	 */
	public void serialEvent(SerialPortEvent evt) {
		// Determine type of event.
		switch (evt.getEventType()) {
			case SerialPortEvent.CTS:
				break;
			case SerialPortEvent.DSR:
				break;
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				break;
			case SerialPortEvent.DATA_AVAILABLE:
				try {
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
				} catch (SAPIException | IOException e) {
					e.printStackTrace();
				}
				break;
		}
	}
	
	public static void main(String[] args) {
		if(args.length != 5) {
			System.out.println("Incorrect arguments.");
			System.out.println("Usage:\n\tjava -jar PixhawkDTConnector.jar port baud dthostname sourceName subscriptionHandle");
			System.out.println(endOfHelpText);
			System.out.println("\nWARING: THERE IS NO PARAMETER CHECKING, SO IF THE ARGUMENTS ARE WRONG BAD THINGS MAY HAPPEN.");
			System.exit(-1);
		}
		new SerialToDT(args[0], Integer.parseInt(args[1]), args[2], args[3], args[4]);
	}
}
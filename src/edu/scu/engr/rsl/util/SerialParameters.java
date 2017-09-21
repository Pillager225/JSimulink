/*
 * Created on Dec 21, 2004 by Tom Van Buskirk
 * 
 * Modified and appended to code written by Sun Microsystems, Inc. 
 * See copyright below for legality.
 * 
 * @(#)SerialParameters.java 1.5 98/07/17 SMI Copyright (c) 1998 Sun
 * Microsystems, Inc. All Rights Reserved. Sun grants you ("Licensee") a
 * non-exclusive, royalty free, license to use, modify and redistribute this
 * software in source and binary code form, provided that i) this copyright
 * notice and license appear on all copies of the software; and ii) Licensee
 * does not utilize the software in a manner which is disparaging to Sun. This
 * software is provided "AS IS," without a warranty of any kind. ALL EXPRESS OR
 * IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGES. This software is not designed or intended for use in on-line
 * control of aircraft, air traffic, aircraft navigation or aircraft
 * communications; or in the design, construction, operation or maintenance of
 * any nuclear facility. Licensee represents and warrants that it will not use
 * or redistribute the Software for such purposes.
 */

package edu.scu.engr.rsl.util;

import javax.comm.SerialPort;

/**
 * A class that stores parameters for serial ports.
 */
public class SerialParameters
{

  private String portName;
  private int baudRate;
  private int flowControlIn;
  private int flowControlOut;
  private int databits;
  private int stopbits;
  private int parity;

  public SerialParameters()
  {
    this("", 9600, SerialPort.FLOWCONTROL_NONE,
        SerialPort.FLOWCONTROL_NONE, SerialPort.DATABITS_8,
        SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
  }

  /**
   * Parameterized constructor.
   * 
   * @param portName The name of the port.
   * @param baudRate The baud rate.
   * @param flowControlIn Type of flow control for receiving.
   * @param flowControlOut Type of flow control for sending.
   * @param databits The number of data bits.
   * @param stopbits The number of stop bits.
   * @param parity The type of parity.
   */
  public SerialParameters(String portName, int baudRate, int flowControlIn,
      int flowControlOut, int databits, int stopbits, int parity)
  {

    this.portName = portName;
    this.baudRate = baudRate;
    this.flowControlIn = flowControlIn;
    this.flowControlOut = flowControlOut;
    this.databits = databits;
    this.stopbits = stopbits;
    this.parity = parity;
  }

  /**
   * Sets port name.
   * 
   * @param portName New port name.
   */
  public void setPortName(String portName)
  {
    this.portName = portName;
  }

  /**
   * Gets port name.
   * 
   * @return Current port name.
   */
  public String getPortName()
  {
    return portName;
  }

  /**
   * Sets baud rate.
   * 
   * @param baudRate New baud rate.
   */
  public void setBaudRate(int baudRate)
  {
    this.baudRate = baudRate;
  }

  /**
   * Gets baud rate as an <code>int</code>.
   * 
   * @return Current baud rate.
   */
  public int getBaudRate()
  {
    return baudRate;
  }

  /**
   * Sets flow control for reading.
   * 
   * @param flowControlIn New flow control for reading type.
   */
  public void setFlowControlIn(int flowControlIn)
  {
    this.flowControlIn = flowControlIn;
  }

  /**
   * Gets flow control for reading as an <code>int</code>.
   * 
   * @return Current flow control type.
   */
  public int getFlowControlIn()
  {
    return flowControlIn;
  }

  /**
   * Sets flow control for writing.
   * 
   * @param flowControlOut New flow control for writing type.
   */
  public void setFlowControlOut(int flowControlOut)
  {
    this.flowControlOut = flowControlOut;
  }

  /**
   * Gets flow control for writing as an <code>int</code>.
   * 
   * @return Current flow control type.
   */
  public int getFlowControlOut()
  {
    return flowControlOut;
  }

  /**
   * Sets data bits.
   * 
   * @param databits New data bits setting.
   */
  public void setDatabits(int databits)
  {
    this.databits = databits;
  }

  /**
   * Gets data bits as an <code>int</code>.
   * 
   * @return Current data bits setting.
   */
  public int getDatabits()
  {
    return databits;
  }

  /**
   * Sets stop bits.
   * 
   * @param stopbits New stop bits setting.
   */
  public void setStopbits(int stopbits)
  {
    this.stopbits = stopbits;
  }

  /**
   * Gets stop bits setting as an <code>int</code>.
   * 
   * @return Current stop bits setting.
   */
  public int getStopbits()
  {
    return stopbits;
  }

  /**
   * Sets parity setting.
   * 
   * @param parity New parity setting.
   */
  public void setParity(int parity)
  {
    this.parity = parity;
  }

  /**
   * Gets parity setting as an <code>int</code>.
   * 
   * @return Current parity setting.
   */
  public int getParity()
  {
    return parity;
  }
}
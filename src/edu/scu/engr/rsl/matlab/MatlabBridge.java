package edu.scu.engr.rsl.matlab;

import com.mathworks.jmi.Matlab;

/**
 * The <code>MatlabBridge</code> class provides methods for using objects to 
 * use the Matlab computation resources if created on the native Matlab JVM. 
 * There are only a couple methods implemented by this class as Mathworks has 
 * not supplied a complete API. Code used in this class have been modified from 
 * that written by Kamin Whitehouse at UC Berkeley (see 
 * <a href>http://www.cs.virginia.edu/~whitehouse/matlab/JavaMatlab.html</a>). 
 * The main purpose of the <code>MatlabBridge</code> is to execute Matlab functions 
 * without the need for the data to be 
 * 
 * <code>Matlab</code> member functions of interest: 
 * <br>feval(String,Object[]) throws InterruptedException,MatlabException
 * <br>whenMatlabReady(Runnable)
 * <br>setEchoEval(boolean) - specify whether or not commands are printed to the console. 
 * 
 * @author Mike Rasay, Robotics Systems Laboratory, Santa Clara University
 * @version 2009-06-19
 * @since JDK1.6
 * @since Matlab 2009a
 * @see edu.scu.engr.rsl.matlab.MatlabController
 *
 */
public class MatlabBridge {
	
	/**
	 * The hook to Matlab resources. 
	 */
	private Matlab m_matlab;
	
	/**
	 * The <code>Object</code> that is returned with the execution of the function or expression.
	 */
	private Object m_outputObject;
	
	/**
	 * A flag denoting whether the output from the function execution is set. 
	 */
	private boolean m_isOutputSet; 
	
	/**
	 * A flag denoting whether the output, if any, from functions or expressions are printed to the console. 
	 * This flag is used by the <code>ExecutionProcess</code> Thread to determine 
	 * what <code>Matlab</code> function to use to execute functions or expressions. 
	 */
	private boolean m_showConsoleOutput;
	
	/**
	 * Creates a <code>MatlabBridge</code> and sets the 'eval' statement to be 
	 * echoed on the command-line. 
	 */
	public MatlabBridge() {
		m_matlab = new Matlab();
		m_outputObject = new String("N/A");
		m_isOutputSet = false;
		m_showConsoleOutput = true;
	}
	
	/**
	 * Creates a <code>MatlabBridge</code> and sets the echo of function/expression 
	 * execution to be printed to the console. 
	 * @param v_showConsoleOutput True is the echo is desired, false if otherwise. 
	 */
	public MatlabBridge(boolean v_showConsoleOutput) {
		m_matlab = new Matlab();
		m_outputObject = new String("N/A");
		m_isOutputSet = false;
		m_showConsoleOutput = v_showConsoleOutput;
	}
	
	/**
	 * Executes the specified expression. 
	 * @param v_expression The expression to be executed. 
	 */
	public void eval(final String v_expression) {
		class EvalThread extends Thread {
			public void run() {
				try { 
					synchronized(m_matlab) {
						executeExpression(v_expression);
					}
				}catch(InterruptedException ie) { ie.printStackTrace(); } 
			}
		}
		EvalThread evalThread = new EvalThread(); 
		evalThread.start();
	}

	/**
	 * Executes the specified function without locking up the Matlab resources. 
	 * While a return value may be expected from the function the member field, 
	 * <code>m_outputObject</code>, which can be accessed via the <code>getFunctionOutput</code> 
	 * method serves as the means for the using Objects to get the output. 
	 * @param v_function The function to be executed. 
	 * @param v_args The arguments as expected by the Matlab function.
	 */
	public void feval(final String v_function, final Object[] v_args) {
		class EvalThread extends Thread {
			public void run() {
				try {
					synchronized(m_matlab) {
						executeFunction(v_function, v_args);
					}
				}catch(InterruptedException ie) { ie.printStackTrace(); }
			}
		}
		//create an evaluation Thread
		EvalThread evalThread = new EvalThread();
		evalThread.start();
	}
	
	/**
	 * Returns the output, if any, from the execution of the most recent function. 
	 * This function attempts to lock the monitor of the member field which is set 
	 * with the call to execute a Matlab function. 
	 * @return The output Object from the last function call made by the <code>MatlabBridge</code> 
	 * 			Object. 
	 */
	public Object getFunctionOutput() {
		synchronized(m_outputObject) {
			if(m_isOutputSet) { return m_outputObject; }
			else {
				try { 
					m_outputObject.wait();
				}catch(InterruptedException ie) { ie.printStackTrace(); }
				return m_outputObject;
			}
		}
	}
	
	/**
	 * Returns whether Matlab is available for use. 
	 * @return true if Matlab is available. 
	 */
	public static boolean isMatlabAvailable() {
		return Matlab.isMatlabAvailable();
	}
	
	/**
	 * Sets the option to have display (disp) outputs be printed to the console.  
	 * @param v_showConsoleOutput True if function print statments and output are to 
	 * 							printed to the console. False, if otherwise. 
	 */
	public void showConsoleOutput(boolean v_showConsoleOutput) {
		m_showConsoleOutput = v_showConsoleOutput;
	}
	
	/**
	 * Models the built-in 'feval' Matlab function that is designated for executing 
	 * functions. 
	 * @param v_functionName The name of the function to be executed.
	 * @param v_args The arguments expected and/or used by the specified function.
	 * @throws InterruptedException If the execution of the function cannot be completed as expected.
	 */
	protected void executeFunction(String v_functionName,Object[] v_args) throws InterruptedException {
		m_isOutputSet = false;
		m_outputObject = "N/A";
		Matlab.whenMatlabReady(new ExecutionProcess(v_functionName,v_args,true));
	}
	
	
	
	/**
	 * Models the built-in 'eval' Matlab function that is designated for executing 
	 * Strings as an expression or statement. 
	 * @param v_expression The expression that is to be executed. 
	 * @throws InterruptedException If the execution of the expression cannot be completed as expected. 
	 */
	protected void executeExpression(String v_expression) throws InterruptedException {
		m_isOutputSet = false;
		m_outputObject = "N/A";
		Matlab.whenMatlabReady(new ExecutionProcess(v_expression,null,false));
	}
		
	/**
	 * The <code>ExecutionProcess</code> inner-class is designed to execute commands 
	 * requested by using objects. Due to the fact that Matlab is a single-threaded 
	 * mechanism, a separate thread must be used to execute functions and expressions. 
	 *
	 */
	protected class ExecutionProcess implements Runnable {
		
		/**
		 * The name of the function or the expression to be executed. 
		 */
		protected String 	m_command;
		
		/**
		 * The input arguments of the function or expression to be executed. 
		 */
		protected Object[] 	m_args;
		
		/**
		 * A flag signifying the use of the 'feval' or 'eval' Matlab function.
		 */
		protected boolean	m_execFunction;
		
		/**
		 * Creates a new <code>ExecutionProcess</code> Object with the specified 
		 * function/expression, input arguments, and flag noting whether to use 
		 * the Matlab 'feval' or 'eval' function. 
		 * @param v_cmd The function or expression to execute. 
		 * @param v_args The arguments associated with the function, null if to be ignored.
		 * @param v_execFunction A flag noting whether to use 'feval' or 'eval.' 
		 * 			True if a function execution, false if an expression. 
		 */
		public ExecutionProcess(String v_cmd,Object[] v_args,boolean v_execFunction) {
			m_command = v_cmd;
			m_args = v_args;
			m_execFunction = v_execFunction;
		}

		/**
		 * Implements the <code>Runnable</code> <code>run</code> method. The function 
		 * or expression is executed then the calling function requiring the output 
		 * Object is notified. 
		 */
		public void run() {
			synchronized(m_outputObject) {
				Object v_refObj = m_outputObject;	//use this reference to notify the calling function of completion
				if(m_execFunction) {
					try { 
						synchronized(m_matlab) {
							if(m_showConsoleOutput) { m_outputObject = Matlab.mtFevalConsoleOutput(m_command, m_args, 0); } 
							else { m_outputObject = Matlab.mtFeval(m_command, m_args,0); }
							m_isOutputSet = true;
						}
					} catch(Exception e) {}
					v_refObj.notifyAll();
				}
				else {
					try {
						synchronized(m_matlab) {
							if(m_showConsoleOutput) { m_matlab.evalConsoleOutput(m_command); }
							else { Matlab.mtEval(m_command); }
							v_refObj.notifyAll();
						}
					} catch(Exception e) {}
				}
			}
		}

	}
}

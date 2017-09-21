package edu.scu.engr.rsl.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedList;

public class Logger {
	public static String logFilePrefix = "java_logger";
	private static BufferedWriter w;
	private static DateTimeFormatter loggerFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"), fileNameFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private static LocalDateTime now = LocalDateTime.now();
	private static File logFile = new File(logFilePrefix+fileNameFormat.format(now)+".log");
	private static boolean fileOpened = false;
	private static LinkedList<Thread> threads = new LinkedList<Thread>();
	
	public static void setLogLocation(String logLocation) {
		logFile = new File(logLocation);
	}
	
	private static void setupFile() throws IOException {
		if(!logFile.exists()) {
			logFile.createNewFile();
		}
		FileWriter fw = new FileWriter(logFile, true);
		w = new BufferedWriter(fw);
	}
	
	public static void log(String s, int logLevel) {
		if(logLevel > 0) {
			System.out.println(s);
		} 
		if(logLevel > 2) {
			writeToFile(s);
		}
	}
	
	public static void log(Exception e, int logLevel) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		log(sw.toString(), logLevel);
	}
	
	private static void writeToFile(final String s) {
		if(!fileOpened) {
			try {
				setupFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fileOpened = true;
		}
		Thread t = new Thread() {
			LocalDateTime now = LocalDateTime.now();
			
			private synchronized void writeToFile() {
				try {
					w.write(loggerFormat.format(now)+"| " + s + "\n");
					w.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			@Override 
			public void run() {
				writeToFile();
			}
		};
		threads.add(t);
		t.start();
	}
	
	public static void endLogger() throws IOException, InterruptedException {
		Iterator<Thread> threadIter = threads.iterator();
		while(threadIter.hasNext()) {
			Thread t = threadIter.next();
			if(t.isAlive()) {
				t.interrupt();
				t.join(500);
			}
		}
		w.close();
	}
}
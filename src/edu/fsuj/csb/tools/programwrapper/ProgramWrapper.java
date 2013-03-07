package edu.fsuj.csb.tools.programwrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.TreeSet;

import edu.fsuj.csb.tools.xml.ObjectComparator;


/**
 * this class implements a wrapper for calls to external programs
 * @author Stephan Richter
 *
 */
public class ProgramWrapper {

	private String command; // the command to be executed
	private OutputHandlingThread outputStreamThread,errorStreamThread; // threads for I/O handling
	private StringBuffer sb; // StringBuffer to hold the program output
	private Process process; // process handler
	
	
	/*************************************************************************************************************/
	/**/
	/**
	 * this class is used to handle outputs of a programm executed by the program wrapper
	 * @author Stephan Richter
	 *
	 */
	private class OutputHandlingThread extends Thread{
	/**/		
	/**/   private TreeSet<OutputHandler> outputHandlers; // the set of registered handlers for output
	/**/   private BufferedReader reader; 
	/**/   private BufferedWriter writer;
	/**/		
	/**/   public OutputHandlingThread() {
	/**/	   outputHandlers=new TreeSet<OutputHandler>(ObjectComparator.get());	    
	/**/   }
	/**/		
	/**/   public void start(BufferedWriter bw, BufferedReader br){
	/**/		 reader=br;
	/**/     writer=bw;
	/**/		 start();
	/**/	 }
	/**/		
	/**/   public void run() {
	/**/     try {
	/**/	     String line;
	/**/       while ((line=reader.readLine()) != null) alert(line,writer);
	/**/       reader.close();
	/**/     } catch (IOException e) {
	/**/       e.printStackTrace();
	/**/     }
	/**/   }	
	/**/
	/**/   private void alert(String line,BufferedWriter processWriter) {
	/**/	   sb.append(line+'\n');
	/**/     for (Iterator<OutputHandler> it = outputHandlers.iterator(); it.hasNext();) it.next().alert(line,processWriter);
	/**/   }
	/**/
	/**/   public void addOutputHandler(OutputHandler oh) {
	/**/     outputHandlers.add(oh);
	/**/   }
	/**/ }
	/**/	
	/*************************************************************************************************************/
	
	
	/**
	 * creates a new ProgramWrapper instance
	 * @param command the command, that is to be executed
	 */
	public ProgramWrapper(String command) {
		this.command=command;
		sb=new StringBuffer();
		outputStreamThread=new OutputHandlingThread(); // create I/O threads
		errorStreamThread=new OutputHandlingThread();
	}
	
	/**
	 * adds instances of OutputHandlers to the handling threads, so those instances can act on output events
	 * @param oh the OutputHandler, that shall be registered
	 */
	public void addOutputHandler(OutputHandler oh){
		outputStreamThread.addOutputHandler(oh);
		errorStreamThread.addOutputHandler(oh);
	}

	/**
	 * start the program in a non-blocking manner
	 * @return the BufferedWriter which is used to pass arguments to the program
	 * @throws IOException 
	 */
	public BufferedWriter startAndContinue() throws IOException {
		//System.out.println("ProgramWrapper.startAndContinue()");
		process=Runtime.getRuntime().exec(command); // actually execute the program
		
	// open writers and readers:
		
		
		// used to pass parameters to the program
		OutputStreamWriter osw=new OutputStreamWriter(process.getOutputStream()); 
		BufferedWriter bw = new BufferedWriter(osw);
		
		// used to read program output
		outputStreamThread.start(bw,new BufferedReader(new InputStreamReader(process.getInputStream())));
		errorStreamThread.start(bw,new BufferedReader(new InputStreamReader(process.getErrorStream())));
		
		return bw;
	}

	/**
	 * start the program in a non-blocking manner and pass the parameters to it
	 * @param parameters a set of strings that shall be passed to the program as parameters
	 * @return the BufferedReader which is my be used to pass further arguments to the program
	 * @throws IOException
	 */
	public BufferedWriter startAndContinue(String [] parameters) throws IOException {
		//System.out.println("ProgramWrapper.startAndContinue("+parameters+")");
		
		BufferedWriter bw = startAndContinue();
		if (parameters!=null){
			for (int i=0; i<parameters.length; i++) {
				System.out.print("passing "+parameters[i]+" to "+command+"...");				
				bw.write(parameters[i]+'\n');
				bw.flush();
				System.out.println("done.");
			}
		}
		return bw;
	}

	/**
	 * start the program (passing given parameters to it) and wait for it to continue
	 * @param parameters a set of arguments, which will be passed to the programm
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void startAndWait(String [] parameters) throws IOException, InterruptedException {
		//System.out.println("ProgramWrapper.startAndWait("+parameters+")");		
		startAndContinue(parameters);
		process.waitFor();
		int l=0;
		sleep10();
		while (sb.length()>l) {
			l=sb.length();
			sleep10();
		}
	}

	private void sleep10() {
		try {
	    Thread.sleep(10);
    } catch (InterruptedException e) {}	  
  }

	/**
	 * start the program and wit for it to continue
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void startAndWait() throws IOException, InterruptedException {
		//System.out.println("ProgramWrapper.startAndWait()");
		startAndWait(null);
	}
	
	/**
	 * get all output the program ahs produced so far
	 * @return a string holding all the program's output
	 */
	public String getOutput() {
		return sb.toString();
	}
	
	/**
	 * changes the command, which shall be executed by the different start methods
	 * @param cmd the new command
	 */
	protected void setCommand(String cmd) {
		command=cmd;
  }

	/**
	 * @return the command, which shall be executed by the different start methods
	 */
	public String command(){
		return command;
	}
	
	/**
	 * a main method for testing purposes
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
	  ProgramWrapper pw=new ProgramWrapper("find /home/stud/bid03/srichter/workspace");
	  pw.startAndContinue();
	  Thread.sleep(20000);
	  System.out.println(pw.getOutput());
  }

	/**
	 * this method just calls startAndWait() and is just here for compatibility with older versions
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void startBlocking() throws IOException, InterruptedException {
		//System.out.println("ProgramWrapper.startBlocking()");
		startAndWait();
  }
}

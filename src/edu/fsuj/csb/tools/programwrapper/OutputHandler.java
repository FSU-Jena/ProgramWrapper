package edu.fsuj.csb.tools.programwrapper;

import java.io.BufferedWriter;

/**
 * 
 * inteface to be implemented by classes, that shall cope with the output of a program wrapper
 * @author Stephan Richter
 *
 */
public interface OutputHandler {

	public void alert(String message, BufferedWriter processWriter);
}

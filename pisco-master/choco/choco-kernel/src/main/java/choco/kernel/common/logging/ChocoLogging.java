/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco.kernel.common.logging;

import static java.util.logging.Logger.getLogger;
import gnu.trove.TObjectIntHashMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import java.util.logging.XMLFormatter;


/**
 * A final class which handles choco logging statements.
 * Most of choco classes propose a static final field LOGGER.
 * @author Arnaud Malapert</br> 
 * @since 16 avr. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public final class ChocoLogging {

	public final static String START_MESSAGE =
			"** CHOCO : Constraint Programming Solver\n"+
					"** CHOCO v2.1.6 (November, 2012), Copyleft (c) 1999-2012";
	
	public final static Formatter LIGHT_FORMATTER = new LightFormatter();
	
	private ChocoLogging() {
		super();
	}
	
	////////////////////////////////////////////////////////////////////
	///////////////////// Loggers  /////////////////////////////////////
	////////////////////////////////////////////////////////////////////

	public final static Logger[] CHOCO_LOGGERS = new Logger[] {
		getLogger("choco"),

		getLogger("choco.core"),
		getLogger("choco.core.engine"),
		getLogger("choco.core.search"),
		getLogger("choco.core.search.branching"),

		getLogger("choco.api"),
		getLogger("choco.api.main"),
		getLogger("choco.api.test"),
	};

	public static Logger getChocoLogger() {
		return CHOCO_LOGGERS[0];
	}

	protected static Logger getCoreLogger() {
		return CHOCO_LOGGERS[1];
	}

	public static Logger getEngineLogger() {
		return CHOCO_LOGGERS[2];
	}

	public static Logger getSearchLogger() {
		return CHOCO_LOGGERS[3];
	}

	public static Logger getBranchingLogger() {
		return CHOCO_LOGGERS[4];
	}

	protected static Logger getAPILogger() {
		return CHOCO_LOGGERS[5];
	}

	public static Logger getMainLogger() {
		return CHOCO_LOGGERS[6];
	}

	public static Logger getTestLogger() {
		return CHOCO_LOGGERS[7];
	}


	/**
	 * create a new user logger with valid name
	 * @param name
	 * @return the newly created logger
	 */
	public static Logger makeUserLogger(String name) {
		final Logger logger = Logger.getLogger(getMainLogger().getName()+"."+name);
		logger.setUseParentHandlers(true);
		return logger;
	}

	
	////////////////////////////////////////////////////////////////////
	///////////////////// Handlers /////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	

	private final static Handler DEFAULT_HANDLER = new StreamHandler(System.out, LIGHT_FORMATTER);
	
	private final static Handler ERROR_HANDLER = new StreamHandler(System.err, LIGHT_FORMATTER);

	private static final int FILE_SIZE = 8388608; // 1 Mo

	private final static String DEFAULT_LOGFILE= "choco.log";

	private final static String DEFAULT_XML_LOGFILE= "choco.log.xml"; 

	

	static {
		try {
			clearHandlers();
			DEFAULT_HANDLER.setLevel(Level.ALL);
			getChocoLogger().addHandler(DEFAULT_HANDLER);
			ERROR_HANDLER.setLevel(Level.SEVERE);
			getChocoLogger().addHandler(ERROR_HANDLER);
			setVerbosity(loadProperties());
			getChocoLogger().info(ChocoLogging.START_MESSAGE);
		} catch (AccessControlException e) {
			// Do nothing if this is an applet !
			// TODO: see how to make it work with an applet !
		}
	}

	/**
	 * set the default handler for all loggers
	 */
	public static void clearHandlers() {
		flushLogs();
		// clear Handlers
		for (Logger logger : CHOCO_LOGGERS) {
			logger.setUseParentHandlers(true);
			for (Handler h : logger.getHandlers()) {
				logger.removeHandler(h);
			}
		}
		getChocoLogger().setUseParentHandlers(false);
	}
	
	/**
	 * Load the properties file and return default value to logging verbosity, if defined.
	 * @return Default verbosity
	 */
	private static Verbosity loadProperties() {
		try {
			Properties properties = new Properties();
			InputStream is = ChocoLogging.class.getResourceAsStream("/verbosity.properties");
			properties.load(is);
			final String key = "verbosity.level";
			if (!properties.isEmpty()
					&& properties.containsKey(key)) {
				Integer verb = Integer.parseInt(properties.getProperty(key));
				if (verb >= 0 && verb <= 6) {
					return Verbosity.values()[verb];
				}
			}
		} catch (IOException ignored) {}
		return Verbosity.DEFAULT;
	}

		
	////////////////////////////////////////////////////////////////////
	///////////////////// Flush logs  //////////////////////////////////
	////////////////////////////////////////////////////////////////////
	
	public static void flushLog(Logger logger) {
		for (Handler h : logger.getHandlers()) {
			h.flush();
		}
	}


	/**
	 * flush pending logs
	 */
	public static void flushLogs() {
		for (Logger logger : CHOCO_LOGGERS) {
			flushLog(logger);
		}
	}

	////////////////////////////////////////////////////////////////////
	///////////////////// Set up log files  ////////////////////////////
	////////////////////////////////////////////////////////////////////
	
	private static String checkLogfile(String name, String ext, String defaultName) {
		if(name != null && ! name.isEmpty() ) {
			return name.endsWith(ext) ? name : name+ext;
		} 
		return defaultName;
	}

	/**
	 * Write all log messages into text files (five rolling files).
	 * @param logfile a filename pattern
	 */
	public static void recordLogs(String logfile) {
		try {
			String filename = checkLogfile(logfile, ".log", DEFAULT_LOGFILE);
			FileHandler handler = new FileHandler(filename, FILE_SIZE, 5,false);
			handler.setFormatter(LIGHT_FORMATTER);
			getChocoLogger().addHandler(handler);
			//Check branching logger (xml logging is activated)
			if(! getBranchingLogger().getUseParentHandlers()) {
				getBranchingLogger().addHandler(handler);
			}
		} catch (IOException e) {
			getChocoLogger().warning("Failed to initialize logger file handler.");
		}

	}

	/**
	 * Write log messages into an xml file (with the exception of branching messages).
	 * @param logfile the output xml file
	 */
	public static void recordXmlLogs(String logfile) {
		try {
			String filename = checkLogfile(logfile, ".xml", DEFAULT_XML_LOGFILE);
			FileHandler handler = new FileHandler(filename, false);
			handler.setFormatter(new XMLFormatter());
			//Exclude branching logger for this handler
			getBranchingLogger().setUseParentHandlers(false);
			for (Handler h : getChocoLogger().getHandlers()) {
				getBranchingLogger().addHandler(h);
			}
			//
			getChocoLogger().addHandler(handler);
			
		} catch (IOException e) {
			getChocoLogger().warning("Failed to initialize logger xml handler.");
		}

	}
	/**
	 * Write error log messages into a file (warning and severe message).
	 * @param logfile the output file
	 */
	public static void recordErrorLogs(String logfile) {
		try {
			String filename = checkLogfile(logfile, ".log", DEFAULT_LOGFILE);
			FileHandler handler = new FileHandler(filename, false);
			handler.setFormatter(new SimpleFormatter());
			handler.setLevel(Level.WARNING);
			getChocoLogger().addHandler(handler);
			//Check branching logger (xml logging is activated)
			if(! getBranchingLogger().getUseParentHandlers()) {
				getBranchingLogger().addHandler(handler);
			}
		} catch (IOException e) {
			getChocoLogger().warning("Failed to initialize logger error handler.");
		}
	}


	////////////////////////////////////////////////////////////////////
	///////////////////// Set up Verbosity  ////////////////////////////
	////////////////////////////////////////////////////////////////////
	
	/**
	 * Set verbosity to SILENT <i>(syntaxic sugar)</i>
	 */
	public static void toSilent(){
		setVerbosity(Verbosity.SILENT);
	}

	/**
	 * Set verbosity to QUIET <i>(syntaxic sugar)</i>
	 */
	public static void toQuiet(){
		setVerbosity(Verbosity.QUIET);
	}

	/**
	 * Set verbosity to DEFAULT <i>(syntaxic sugar)</i>
	 */
	public static void toDefault(){
		setVerbosity(Verbosity.DEFAULT);
	}

	/**
	 * Set verbosity to SILENT <i>(syntaxic sugar)</i>
	 */
	public static void toVerbose(){
		setVerbosity(Verbosity.VERBOSE);
	}

	/**
	 * Set verbosity to SOLUTION <i>(syntaxic sugar)</i>
	 */
	public static void toSolution(){
		setVerbosity(Verbosity.SOLUTION);
	}

	/**
	 * Set verbosity to SEARCH <i>(syntaxic sugar)</i>
	 */
	public static void toSearch(){
		setVerbosity(Verbosity.SEARCH);
	}


	public static void setLevel(final Level level, final Logger... loggers) {
		for (Logger logger : loggers) {
			logger.setLevel(level);
		}
	}
	/**
	 * set the choco verbosity level
	 * @param verbosity the new verbosity level
	 */
	public static void setVerbosity(Verbosity verbosity) {
		if(verbosity == Verbosity.OFF) setLevel(Level.OFF, CHOCO_LOGGERS);
		else if(verbosity == Verbosity.FINEST) setLevel(Level.FINEST, CHOCO_LOGGERS);
		else {
			setLevel(Level.FINEST, getChocoLogger(),getCoreLogger(), getAPILogger());
			switch(verbosity) {
			case SILENT: {
				setLevel(Level.SEVERE,	getEngineLogger(), getSearchLogger(), getBranchingLogger());
				setLevel(Level.WARNING, getMainLogger(), getTestLogger());
				break;
			}
			case QUIET: {
				setLevel(Level.WARNING,	getEngineLogger(), getSearchLogger(), getBranchingLogger());
				setLevel(Level.INFO, getMainLogger(), getTestLogger());
				break;
			}
			case DEFAULT: {
				setLevel(Level.WARNING, getEngineLogger(), getBranchingLogger());
				setLevel(Level.INFO, getMainLogger(), getSearchLogger(), getTestLogger());
				break;
			}
			case VERBOSE: {
				setLevel(Level.INFO, getEngineLogger(), getBranchingLogger());
				setLevel(Level.CONFIG, getMainLogger(), getTestLogger());
				setLevel(Level.FINE, getSearchLogger());
				break;
			}
			case SOLUTION: { 
				setLevel(Level.INFO, getBranchingLogger());
				setLevel(Level.CONFIG,getEngineLogger(), getTestLogger());
				setLevel(Level.FINER, getMainLogger(), getSearchLogger());
				break;
			}
			case SEARCH: {
				setLevel(Level.CONFIG, getBranchingLogger(), getTestLogger());
				setLevel(Level.FINER, getEngineLogger());
				setLevel(Level.FINEST, getMainLogger(), getSearchLogger());
				break;
			}
			case FINEST: {
				setLevel(Level.FINEST,CHOCO_LOGGERS);
				break;
			}
			default: {
				setVerbosity(Verbosity.VERBOSE);
			}

			}
		}
	}


	////////////////////////////////////////////////////////////////////
	///////////////////// Display Logger Tree  /////////////////////////
	////////////////////////////////////////////////////////////////////
	
	public final static String toDotty() {
		final StringBuilder b = new StringBuilder();
		final TObjectIntHashMap<Logger> indexMap = new TObjectIntHashMap<Logger>(CHOCO_LOGGERS.length);
		//Create a node for each logger
		for (int i = 0; i < CHOCO_LOGGERS.length; i++) {
			indexMap.put(CHOCO_LOGGERS[i], i);
			String name = CHOCO_LOGGERS[i].getName();
			final int idx = name.lastIndexOf('.');
			if( idx != -1) name = name.substring(idx + 1);
			b.append(i).append("[ shape=record, label=\"{");
			b.append(name).append("|").append(CHOCO_LOGGERS[i].getLevel());
			b.append("}\"]");
		}
		//Create arcs between a logger and its parent
		for (int i = 1; i < CHOCO_LOGGERS.length; i++) {
			b.append(indexMap.get(CHOCO_LOGGERS[i].getParent()));
			b.append(" -> ").append(i).append('\n');
		}
		return new String(b);
	}


	public static void main(String[] args) {
		System.out.println(toDotty());
	}

}

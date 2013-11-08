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

package db;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import choco.kernel.common.logging.ChocoLogging;

public final class OdbHsqldbBridge {

	private final static int BSIZE=4048;

	private final static byte[] BUFFER = new byte[BSIZE];

	public final static String DIRECTORY_DB_OO = "database/";
	
	public final static String DBNAME= "chocodb";
	
	public final static String ODB_PATTERN = "/chocodb.odb";
	
	public final static Logger LOGGER = ChocoLogging.getMainLogger();

	public final static String HSQLDB_OPTIONS = ";sql.enforce_strict_size=true";
	
	private OdbHsqldbBridge() {
		super();
	}

	public static InputStream getDefaultOdbPattern(Object o) {
		return o.getClass().getResourceAsStream(ODB_PATTERN);
	}
	
	public static void copy(InputStream inStream, OutputStream outStream) throws IOException {
		int nrBytesRead = 0;
		while ((nrBytesRead = inStream.read(BUFFER)) > 0) {
			outStream.write(BUFFER, 0, nrBytesRead);
		}
	}

	/**
	 * Uncompress the database contained in a odb pattern.
	 * @param odbStream the odb pattern
	 * @param databaseDir the extraction directory
	 * @param databaseName the name of the extracted database.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void uncompressDatabase(InputStream odbStream, File databaseDir, String databaseName) throws FileNotFoundException, IOException {
		final ZipInputStream inStream = new ZipInputStream(odbStream);
		ZipEntry entry;
		while( (entry = inStream.getNextEntry()) != null) {
			if(entry.getName().startsWith(DIRECTORY_DB_OO) ) {
				final FileOutputStream outStream = new FileOutputStream(new File(databaseDir, databaseName+ "." + entry.getName().substring(DIRECTORY_DB_OO.length())));
				copy(inStream, outStream);
				outStream.close();	
				LOGGER.log(Level.FINE, "hsqldb...[extract:{0}][OK]",entry);
			}
		}
		inStream.close();
		LOGGER.log(Level.INFO, "hsqldb...[extract:{0}][OK]", databaseDir);
	}



	protected static void compressDatabase(File databaseDir, String databaseName, ZipOutputStream odbStream) throws IOException  {
		final File[] dbFiles = databaseDir.listFiles(new DatabaseFilter(databaseName));
		for (File dbf : dbFiles) {
			final ZipEntry entry = new ZipEntry(DIRECTORY_DB_OO+dbf.getName().substring(databaseName.length()+1));
			odbStream.putNextEntry(entry);
			final FileInputStream inStream = new FileInputStream(dbf);
			copy(inStream, odbStream);
			inStream.close();
			LOGGER.log(Level.FINE, "hsqldb...[compress:{0}][OK]",entry);
		}

	}

	/**
	 * Transfer only the files associated with the database in odb format.
	 * @param inStream the source (pattern)
	 * @param outStream the destination
	 * @param buffer a reusable buffer
	 */
	protected static void transferOdbPattern(ZipInputStream inStream, ZipOutputStream outStream) throws IOException {
		ZipEntry entry;
		while( (entry = inStream.getNextEntry()) != null) {
			if( ! entry.getName().startsWith(DIRECTORY_DB_OO) ) {
				outStream.putNextEntry(entry);
				copy(inStream, outStream);
				LOGGER.log(Level.FINE, "hsqldb...[transfer:{0}][OK]",entry);
			}
		}
	}


	/**
	 * Export the database in directory in a new odbfile which is created using the pattern argument.
	 * @param odbStream pattern
	 * @param databaseDir exported database directory
	 * @param databaseName 
	 * @param odbOutput new odbfile
	 * @throws IOException
	 */
	public static void exportDatabase(InputStream odbStream, File databaseDir, String databaseName, File odbOutput) throws IOException  {
		final ZipOutputStream outStream = new ZipOutputStream(new FileOutputStream(odbOutput));
		//copy Db
		compressDatabase(databaseDir, databaseName, outStream);
		//insert odb files
		final ZipInputStream inStream = new ZipInputStream(odbStream);
		transferOdbPattern(inStream, outStream);
		inStream.close();
		outStream.close();
		LOGGER.log(Level.INFO, "hsqldb...[export:{0}][OK]", odbOutput);
	}
	
	

	static class DatabaseFilter implements FilenameFilter{

		private final String databasePrefix;


		private DatabaseFilter(String databaseName) {
			super();
			this.databasePrefix = databaseName + ".";
		}


		@Override
		public boolean accept(File dir, String name) {
			if(name.startsWith(databasePrefix)) {
				//if(name.endsWith(".log") || name.endsWith(".lck") || name.endsWith(".backup")) return false;
				if(name.endsWith(".log") || name.endsWith(".lck")) return false;
				else return true;
			}
			return false;
		}		 

	}

	//*****************************************************************//
	//*******************  URLS FACTORY ******************************//
	//***************************************************************//

	public final static String makeLocalhostURL(String dbName) {
		return makeNetworkURL("localhost", dbName);
	}


	public final static String makeEmbeddedURL(File dbDir, String dbName) {
		return "jdbc:hsqldb:file:" + dbDir.getAbsolutePath()+"/"+dbName+HSQLDB_OPTIONS;
	}


	/**
	 * The host name or adress should begin with /.
	 */
	public final static String makeNetworkURL(String host, String dbName) {
		return "jdbc:hsqldb:hsql://"+host+"/"+dbName+HSQLDB_OPTIONS;
	}


	/**
	 * The host name or adress should begin with /.
	 */
	public final static String makeNetworkURL(String host, int port, String dbName) {
		return "jdbc:hsqldb:hsql://"+host+":"+port+"/"+dbName+HSQLDB_OPTIONS;
	}


}

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

package parser.absconparseur.tools;

import choco.kernel.common.logging.ChocoLogging;
import org.w3c.dom.Document;
import parser.absconparseur.InstanceTokens;
import parser.absconparseur.XMLManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstanceShuffler {

    protected final static Logger LOGGER = ChocoLogging.getMainLogger();

	private final File file;

	private final int mode;

	private final int seed;

	public InstanceShuffler(File file, int seed, int mode) {
		this.file = file;
		this.mode = mode;
		if (mode < 1 || mode > 3)
			throw new IllegalArgumentException();
		this.seed = seed;
	}

	private PrintWriter buildPrintWriter() throws Exception {
		String s = file.getName();
		int position = s.lastIndexOf(".xml");
		String fileName = s.substring(0, position) + "_shf" + seed + ".xml";
		File file = new File(fileName);
		if (file.exists())
			LOGGER.log(Level.INFO,"{0} exists", file);
		return new PrintWriter(new FileOutputStream(file)); // absoluteFileName));
	}

	public void treat() throws Exception {
		Document document = XMLManager.load(new FileInputStream(file), null);
        document = DocumentShuffler.shuffle(document, seed,mode);
		XMLManager.save(document, buildPrintWriter(), XMLManager.class.getResourceAsStream(InstanceTokens.INSTANCE_STYLESHEET_2_0));
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 3 && LOGGER.isLoggable(Level.INFO)) {
			StringBuilder b = new StringBuilder(128);
			b.append("InstanceShuffler " + InstanceParser.VERSION);
			b.append("Usage: java ... InstanceShuffler <instanceFileName> <seed> <mode>\n");
			b.append("  <instanceFileName> must be the name of a file which contains the representation of a CSP instance in format XCSP 2.0");
			b.append("  <seed> must be an integer that is used to shuffle variables and constraints");
			b.append("  <mode> must be equel to 1 (only variables shuffled), 2 (only constraints shuffled) and 3 (both variables and constraints shuffled)\n");
			b.append("With this usage, InstanceShuffler shuffles the given instance and saves the result in a new file (by appending _shf<seed> to the prefix of the file name)\n");
			b.append("Exit code of instanceShuffler is as follows:");
			b.append("  0 : no problem occurs and the new shuffled instance has been saved");
			b.append("  2 : a problem occurs (file not found, ...)");
			LOGGER.log(Level.INFO, "{0}", b);
			System.exit(0);
		}
		try {
			File file = new File(args[0]);
			if (file.isDirectory()) {
				LOGGER.severe("PROBLEM \t you must give the name of a file (and not the name of a directory)");
				System.exit(2);
			}
			if (!file.exists()) {
				LOGGER.severe("PROBLEM \t the file has not been found");
				System.exit(2);
			}
			InstanceShuffler instanceShuffler = new InstanceShuffler(file, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			instanceShuffler.treat();
			System.exit(0);

		} catch (Throwable e) {
			LOGGER.log(Level.SEVERE, "PROBLEM", e);
			System.exit(2);
		}
	}
}

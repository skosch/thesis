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

package choco.kernel.visu;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import choco.kernel.common.IDotty;
import choco.kernel.common.logging.ChocoLogging;


public final class VisuFactory {

	protected final static Logger LOGGER = ChocoLogging.getMainLogger();

	/**
	 * empty constructor
	 */
	private VisuFactory() {}
	
	
	public static IVisuManager getGnuplotManager() {
		return GnuplotManager.getInstance();
	}
	
	public static IVisuManager getDotManager() {
		return DotManager.getInstance();
	}

	// DONE 31 oct. 2011 - Allow to change the file extension - created 31 oct. 2011 by Arnaud Malapert
	public static IVisuManager getTextManager() {
		return TextManager.getInstance();
	}
	
	public static IVisuManager getSolManager() {
		return new TextManager("sol");
	}
	
	public static void launchCommand(final boolean waitFor, final String... cmd) {
		// Win 95/98/ : pour lancer un .bat
		// cmd = "command.com /c c:\\fichier.bat";

		// Win NT(XP...) : pour lancer un .bat
		// cmd = "cmd /c c:\\fichier.bat";

		// Win 95/98/NT : pour lancer un .exe
		// cmd = "command.com /c c:\\windows\\notepad.exe";

		// Win 95/98/NT : pour lancer une commande dos
		// cmd = "cmd /c copy src.txt dest.txt";


		// UNIX : pour lancer un script en precisant le shell (sh,bash)
		// cmd = "/usr/bin/sh script.sh";

		// UNIX : pour lancer script
		// cmd = "/path_complet/tonscript";

		// EXEMPLES .EXE : NetMeeting
		//cmd = "C:\\Program Files\\NetMeeting\\conf.exe";
		// ainsi on peut lancer des programme tout a fait autonome
		// on peut soit faire p.waitfor() ou pas les deux cas fonctionnent correctement
		// je suppose aussi que c'est tout a fait vrai pour le cas d'UNIX (je ne l'ai pas tester

		try {
			final Runtime r = Runtime.getRuntime();
			final Process p = r.exec(cmd);
			if(waitFor) {
				p.waitFor();//si l'application doit attendre a ce que ce process fini
			}
		}catch(Exception e) {
			LOGGER.log(Level.SEVERE, "exec...["+Arrays.toString(cmd)+"][FAIL]", e);
		}
	}

}


class DotManager extends AbstractVisuManager {

	/** 
	 * The shared instance. 
	 */ 
	private final static DotManager SINGLOTON = new DotManager(); 
	/** 
	 * Private constructor. 
	 */ 
	private DotManager() { 
		super(); 
	} 
	/** 
	 * Returns this shared instance. 
	 * 
	 * @returns The shared instance 
	 */ 
	public final static DotManager getInstance() { 
		return SINGLOTON;
	}
	
	
	@Override
	protected String getFileExtension() {
		return "dot";
	}

	@Override
	protected boolean doExport(File file, Object chart, int width, int height)
	throws IOException {
		if (chart instanceof IDotty) {
			IDotty source = (IDotty) chart;
			final FileWriter fw=new FileWriter(file);
			fw.write("digraph g {\n\n");
			fw.write(source.toDotty());
			fw.write("\n}");
			fw.close();
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected boolean doShow(Object chart, int width, int height) {
		File file = export(null, null, chart, width, height);
		if(file != null) {
			VisuFactory.launchCommand(false, "/home/nono/bin/xdot", file.getAbsolutePath());
			return true;
		} else return false;
	}


}


class GnuplotManager extends AbstractVisuManager {

	/** 
	 * The shared instance. 
	 */
	private final static GnuplotManager SINGLOTON = new GnuplotManager();

	/** 
	 * Private constructor. 
	 */
	private GnuplotManager() {
		super();
	}

	/** 
	 * Returns this shared instance. 
	 * 
	 * @returns The shared instance 
	 */
	public final static GnuplotManager getInstance() {
		return SINGLOTON;
	}

	@Override
	protected String getFileExtension() {
		return "dat";
	}

	@Override
	protected boolean doExport(File file, Object chart, int width, int height)
	throws IOException {
		if (chart instanceof String) {
			String source = (String) chart;
			final FileWriter fw=new FileWriter(file);
			fw.write(source);
			fw.close();
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected boolean doShow(Object chart, int width, int height) {
		File file = export(null, null, chart, width, height);
		if(file != null) {
			VisuFactory.launchCommand(false, "/bin/sh", "-c", "echo \"set key off; plot \'"+file.getAbsolutePath()+"\' title \'\' with linespoints\" | gnuplot -persist");
			return true;
		} else return false;
	}
}

class TextManager extends AbstractVisuManager {

	/** 
	 * The shared instance. 
	 */
	private final static TextManager SINGLOTON = new TextManager("txt");

	private final String extension;
	/** 
	 * Protected constructor. 
	 */
	protected TextManager(String extension) {
		super();
		this.extension =extension;
	}

	/** 
	 * Returns this shared instance. 
	 * 
	 * @returns The shared instance 
	 */
	public final static TextManager getInstance() {
		return SINGLOTON;
	}

	@Override
	protected String getFileExtension() {
		return extension;
	}

	@Override
	protected boolean doExport(File file, Object chart, int width, int height)
	throws IOException {
		if (chart instanceof String) {
			String source = (String) chart;
			final FileWriter fw=new FileWriter(file);
			fw.write(source);
			fw.close();
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected boolean doShow(Object chart, int width, int height) {
		if (chart instanceof String) {
			LOGGER.info((String) chart);
			return true;
		} else return false;
	}
}



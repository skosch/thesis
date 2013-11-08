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

package cli;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import parser.instances.AbstractInstanceModel;
import parser.instances.XcspModel;
import parser.instances.xcsp.XcspSettings;

import java.io.File;

public class XcspCmd extends AbstractBenchmarkCmd {

	@Option(name="-singloton", usage="Performs Singloton Consistency step")
	protected Boolean singloton;

	@Option(name="-h", usage="Heuristics")
	protected Integer heuristic;


	public XcspCmd() {
		super(new XcspSettings());
	}

	XcspSettings getXcspSettings() {
		return (XcspSettings) settings;
	}
	
	@Override
	protected void checkData() throws CmdLineException {
		super.checkData();	
		//overrides properties
		final XcspSettings set = getXcspSettings();
		if( singloton != null){
            set.putBoolean(XcspSettings.SINGLETON_CONSISTENCY, singloton);
        }
		if( heuristic != null){
            set.putEnum(XcspSettings.HEURISTIC, XcspSettings.match(heuristic));
        }
	}


	@Override
	protected AbstractInstanceModel createInstance() {
		return new XcspModel( getXcspSettings());
	}


	@Override
	public boolean execute(File file) {
//		LOGGER.info(file.toString());
//		return true;
		instance.solveFile(file);
		return instance.getStatus().isValidWithCSP();
	}


	public static void main( String[] args )   {
		final XcspCmd cmd = new XcspCmd();
		if(args.length==0) {cmd.help();}
		else {cmd.doMain(args);}
	}
	
}

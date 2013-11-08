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
package samples.tutorials.to_sort.packing.parser;

import cli.AbstractBenchmarkCmd;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import parser.instances.AbstractInstanceModel;
import parser.instances.BasicSettings;
import parser.instances.checker.SCheckFactory;

import java.io.File;
import java.util.Random;

/**
 * An extension of <code>AbstractBenchmarkCmd</code> abstract class to load and solve BinPacking problems.
 *
 * Instances can be found <a href="http://www.wiwi.uni-jena.de/Entscheidung/binpp/index.htm">here</a>
 *
 * <br/>
 *
 * @author Charles Prud'homme - Arnaud Malapert
 * @since 8 juil. 2010
 */
public class BinPackingCmd extends AbstractBenchmarkCmd {

	/**
	 * the type of model
	 */
	@Option(name="-l",aliases={"--light"},usage="set the light model")
	protected boolean lightModel;
	
	
    public BinPackingCmd() {
        super(new BasicSettings());
    }

    @Override
    protected AbstractInstanceModel createInstance() {
        return new BinPackingModel(settings);
    }

    @Override
	protected void checkData() throws CmdLineException {
		super.checkData();
		seeder =  new Random(seed);
		//check for Boolean, if null then keep default setting (property file)
		if(lightModel) settings.putTrue(BasicSettings.LIGHT_MODEL);
		//load status checkers
		SCheckFactory.load("/bin-packing-tut/bin-packing-tut.properties");
    }

    @Override
    public boolean execute(File file) {
        instance.solveFile(file);
        return instance.getStatus().isValidWithOptimize();
    }

    public static void main(String[] args) {
        final BinPackingCmd cmd = new BinPackingCmd();
        if (args.length == 0) {
            cmd.help();
        } else {
            cmd.doMain(args);
        }
    }
}

package samples.tutorials.lns.rcpsp;
/*
 * Created by IntelliJ IDEA.
 * User: sofdem - sophie.demassey{at}mines-nantes.fr
 * Date: 11/01/11 - 14:45
 */

import choco.kernel.solver.Configuration;
import cli.AbstractBenchmarkCmd;
import org.kohsuke.args4j.CmdLineException;
import parser.instances.AbstractInstanceModel;
import samples.tutorials.lns.lns.LNSCPConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * launch the resolution of the RCPSP by CP or LNS/CP
 * @author Sophie Demassey
 */
public class RCPSPCmd extends AbstractBenchmarkCmd {

public RCPSPCmd(Configuration configuration)
{
	super(configuration);
}

@Override
protected AbstractInstanceModel createInstance()
{
	return new RCPSPModeler(settings);
}

/**
 * Any procedure associated with a file.
 * @return <code>true</code> if succeeds.
 */
@Override
public boolean execute(File file)
{
	instance.solveFile(file);
	return instance.getStatus().isValidWithCSP();
}

@Override
protected void checkData() throws CmdLineException
{
	if (!inputFile.exists() || !inputFile.canRead()) {
		inputFile = new File(RCPSPCmd.class.getResource("/lns-tut/bl2014").getPath());
	}
	super.checkData();
}

public static void main(String[] args)
{
	Configuration configuration = new LNSCPConfiguration();
	try {
		configuration.load(RCPSPCmd.class.getResourceAsStream("/lns-tut/lns-rcpsp.properties"));
	} catch (IOException e) {
		e.printStackTrace();
	}


	final RCPSPCmd cmd = new RCPSPCmd(configuration);
	if (args.length == 0) {
		cmd.help();
	} else {
		cmd.doMain(args);
	}
}

}

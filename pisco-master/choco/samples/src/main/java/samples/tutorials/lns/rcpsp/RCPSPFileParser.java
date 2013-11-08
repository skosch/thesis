package samples.tutorials.lns.rcpsp;

import parser.absconparseur.tools.UnsupportedConstraintException;
import parser.instances.InstanceFileParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * Parser for instance files from benchmark sets of the Resource-Constrained Project Scheduling Problem:
 * BL (Baptiste-Le Pape)
 * PSPLib (KSD)
 * @author Sophie Demassey
 * @since 11/01/11 - 14:49
 */
public class RCPSPFileParser implements InstanceFileParser {

File file;
RCPSPData data;

public RCPSPData getData()
{
	return data;
}

@Override
public File getInstanceFile()
{
	return file;
}

@Override
public void loadInstance(File file)
{
	this.file = file;
}

@Override
public void parse(boolean displayInstance) throws UnsupportedConstraintException
{
	try {
		Scanner sc = new Scanner(file);
		if (file.getName().startsWith("bl")) {
			parseBL(sc);
		} else if (file.getName().endsWith(".sm")) {
			parseKSD(sc);
		} else {
			throw new UnsupportedConstraintException("no parser for file " + file.getName());
		}
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
	if (displayInstance && LOGGER.isLoggable(Level.INFO)) {
		LOGGER.log(Level.INFO, data.toString());
	}
}

/**
 * parse the Baptiste-Le Pape instance files
 * @param sc the instance file scanner
 */
private void parseBL(Scanner sc)
{
	final int nDummyAct = 2;
	int nAct = sc.nextInt() - nDummyAct;
	int bidon = sc.nextInt();
	assert bidon == 9999;
	int nRes = sc.nextInt();

	data = new RCPSPData(nAct, nRes);

	for (int i = 0; i < nRes + 2; i++) {
		sc.next();
	}
	for (int i = 0; i < nAct; i++) {
		int act = sc.nextInt();
		assert act == i + nDummyAct;
		data.setDuration(i, sc.nextInt());
		for (int k = 0; k < nRes; k++) {
			data.setRequest(i, k, sc.nextInt());
		}
	}
	for (int i = 0; i < nRes + 2; i++) {
		sc.next();
	}

	bidon = sc.nextInt();
	assert bidon == 9999;
	for (int k = 0; k < nRes; k++) {
		data.setCapacity(k, sc.nextInt());
	}

	int act1 = sc.nextInt();
	while (act1 != 9999) {
		int act2 = sc.nextInt();
		if (act1 != 1 && act2 != nAct + nDummyAct) {
			data.setPrecedence(act1 - nDummyAct, act2 - nDummyAct);
		}
		act1 = sc.nextInt();
	}
}

/**
 * parse the PSPLib (Kolisch-Sprecher-Drexl) instance files
 * @param sc the instance file scanner
 */
private void parseKSD(Scanner sc)
{
	final int nDummyAct = 2;
	String line;
	do {
		line = sc.findInLine("sink\\s*\\):");
		if (line == null) sc.nextLine();
	} while (line == null);
	int nAct = sc.nextInt() - nDummyAct;
	sc.nextLine();
	line = sc.findInLine("horizon\\s*:");
	assert line != null;
	int horizon = sc.nextInt();
	sc.nextLine();
	line = sc.findInLine("RESOURCES");
	assert line != null;
	sc.nextLine();
	line = sc.findInLine("renewable\\s*:");
	assert line != null;
	int nRes = sc.nextInt();

	data = new RCPSPData(nAct, nRes);

	do {
		line = sc.findInLine("successors");
		sc.nextLine();
	} while (line == null);
	sc.nextLine();

	for (int act = 0; act < nAct; act++) {
		assert act == sc.nextInt() - nDummyAct;
		sc.next();
		for (int nSucc = sc.nextInt(); nSucc > 0; nSucc--) {
			int act2 = sc.nextInt() - nDummyAct;
			if (act2 < nAct) {
				data.setPrecedence(act, act2);
			}
		}
	}

	for (int i = 0; i < 7; i++) {
		sc.nextLine();
	}
	for (int act = 0; act < nAct; act++) {
		assert act == sc.nextInt() - nDummyAct;
		sc.next();
		data.setDuration(act, sc.nextInt());
		for (int k = 0; k < nRes; k++) {
			data.setRequest(act, k, sc.nextInt());
		}
	}

	for (int i = 0; i < 5; i++) {
		sc.nextLine();
	}
	for (int k = 0; k < nRes; k++) {
		data.setCapacity(k, sc.nextInt());
	}
}


@Override
public void cleanup()
{
	file = null;
	data = null;
}

}

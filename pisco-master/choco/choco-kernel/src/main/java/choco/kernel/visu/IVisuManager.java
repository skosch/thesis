package choco.kernel.visu;

import java.io.File;
import java.util.logging.Logger;

import choco.kernel.common.logging.ChocoLogging;

public interface IVisuManager {

	public final static Logger LOGGER = ChocoLogging.getMainLogger();

	int getDefaultWidth();
	void setDefaultWidth(int width);
	
	int getDefaultHeight();
	void setDefaultHeight(int height);
	
	File export(File dir, String name, Object chart, int width, int height);
	File export(File dir, String name, Object chart);
	
	void show(Object chart, int width, int height);
	void show(Object chart);

}

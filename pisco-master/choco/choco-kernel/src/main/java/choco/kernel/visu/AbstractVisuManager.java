package choco.kernel.visu;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public abstract class AbstractVisuManager implements IVisuManager {

	protected int defaultWidth;
	protected int defaultHeight;
	
	
	
	@Override
	public final int getDefaultWidth() {
		return defaultWidth;
	}

	@Override
	public final void setDefaultWidth(int defaultWidth) {
		this.defaultWidth = defaultWidth;
	}

	@Override
	public final int getDefaultHeight() {
		return defaultHeight;
	}

	@Override
	public final void setDefaultHeight(int defaultHeight) {
		this.defaultHeight = defaultHeight;
	}

	protected abstract String getFileExtension();

	protected abstract boolean doExport(File file, Object chart, int width, int height) throws IOException;

	protected abstract boolean doShow(Object chart, int width, int height);

	
	@Override
	public File export(File dir, String name, Object chart) {
		return export(dir, name, chart, getDefaultWidth(), getDefaultHeight());
	}

	@Override
	public File export(File dir, String name, Object chart, int width, int height) {
		try{ 
			final String ext = '.'+getFileExtension();
			if(name == null) name = "visu";
			File file;
			if(dir == null) {
				file = File.createTempFile(name, ext);
			} else {
				file = new File(dir, name+'.'+ getFileExtension());
				if( file.exists()) {
					file =  File.createTempFile(name+"-", '.'+ getFileExtension(), dir);
				} 	
			}
			if( doExport(file, chart, width, height) ) {
				LOGGER.log(Level.CONFIG, "visu...[export:{0}][OK]", file);
				return file;
			} else {
				LOGGER.log(Level.WARNING, "visu...[export:{0}][FAIL]", file);
				return null;
			}
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "visu...[export:{0}][FAIL]", e);
			return null;
		}
	}

	
	@Override
	public void show(Object chart) {
		show(chart, getDefaultWidth(), getDefaultHeight());
	}

	@Override
	public void show(Object chart, int width, int height) {
		if ( doShow(chart, width, height) ) {
			LOGGER.config("visu...[show][OK]");
		} else {
			LOGGER.warning("visu...[show][FAIL]");
		}


	}


}
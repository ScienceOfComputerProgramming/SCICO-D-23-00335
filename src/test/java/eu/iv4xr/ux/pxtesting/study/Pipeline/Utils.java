package eu.iv4xr.ux.pxtesting.study.Pipeline;

import java.io.File;

public class Utils {
	
	/**
	 * Get the name of the level that is currently set in the model-folder (well, we assume
	 * there is only one there).
	 */
	public static String autoGetLevelName(String modelFolder) {
		File[] files = new File(modelFolder).listFiles() ;
		for (File f : files) {
			String name = f.getName() ;
			if (name.endsWith(".csv")) {
				return name.substring(0, name.length() - 4) ;
			}
		}
		return null;
	}

}

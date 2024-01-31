package eu.iv4xr.ux.pxtesting;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Utils {
	
	public static void cleanTestCasesAndTraces(String dirName, String prefixName) throws IOException {
		List<File> files = org.apache.maven.shared.utils.io.FileUtils.getFiles(new File(dirName), "*.csv", "");
		files.addAll(org.apache.maven.shared.utils.io.FileUtils.getFiles(new File(dirName), "*.ser", "")) ; 
		files.addAll(org.apache.maven.shared.utils.io.FileUtils.getFiles(new File(dirName), "*.dot", "")) ; 
		files.addAll(org.apache.maven.shared.utils.io.FileUtils.getFiles(new File(dirName), "*.txt", "")) ; 

		for (File file : files) {
			
			String fname = file.getName() ;
			// dropping the extension 
			fname = fname.substring(0, fname.length() - 4) ;
			
			if (! fname.startsWith(prefixName))
				continue ;
			
			file.delete() ;
		}
	}

}

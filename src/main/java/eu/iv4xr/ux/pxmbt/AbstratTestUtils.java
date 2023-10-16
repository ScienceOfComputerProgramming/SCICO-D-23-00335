package eu.iv4xr.ux.pxmbt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import eu.fbk.iv4xr.mbt.utils.TestSerializationUtils;

public class AbstratTestUtils {
	
	/**
	 * Save a test-sequence in a file as a serialized object (.ser file),
	 * and also save its visualization as a .dot file, and as a text
	 * version (.txt file).
	 * @throws IOException 
	 */
	public static void save(String dir, 
			String basefileName, 
			AbstractTestSequence seq) throws IOException {

		String base = basefileName ;
		if (dir != null)
			base = dir + File.separator + basefileName;

		FileUtils.writeStringToFile(new File(base + ".dot"), seq.toDot(), Charset.defaultCharset());
		FileUtils.writeStringToFile(new File(base + ".txt"), seq.toString(), Charset.defaultCharset());
		TestSerializationUtils.saveTestSequence(seq, base + ".ser");
	}
	
	public static void save(String dir, 
			String basefileName, 
			List<AbstractTestSequence> testSuite) throws IOException {
		
		int k = 0 ;
		for (var seq : testSuite) {
			save(dir, basefileName + "_" + k, seq) ;
			k++ ;
		}
	}
	
	/**
	 * Read a serialized test-case from a file (.ser file).
	 */
	public AbstractTestSequence parseAbstractTestSeq(String dir, String fname) throws FileNotFoundException {
		if (dir != null) {
			fname = dir + File.separator + fname ;
		}
		AbstractTestSequence seq = TestSerializationUtils.loadTestSequence(fname);
		return seq ;
	}
	
	/**
	 * Parse a bunch of serialized test-cases from a directory.
	 * @throws IOException 
	 */
	public List<AbstractTestSequence> parseAbstrastTestSuite(String dir) throws IOException {
		List<AbstractTestSequence> suite = new LinkedList<>() ;
		List<File> files = org.apache.maven.shared.utils.io.FileUtils.getFiles(new File(dir), "*.ser", "");
		for (File file : files) {
			AbstractTestSequence test = TestSerializationUtils.loadTestSequence(file.getAbsolutePath());
			suite.add(test) ;
		}
		return suite;
	}
	
	
	

}

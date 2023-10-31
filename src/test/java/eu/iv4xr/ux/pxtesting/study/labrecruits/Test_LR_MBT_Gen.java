package eu.iv4xr.ux.pxtesting.study.labrecruits;

import org.junit.jupiter.api.Test;

import eu.iv4xr.ux.pxtesting.mbt.TestSuiteGenerator;

public class Test_LR_MBT_Gen {
	
	@Test
	public void test1() throws Exception {
		var gen = new TestSuiteGenerator("eu.iv4xr.ux.pxtesting.mbt.EFSMSimple0") ;
		//gen.aimedCoverage = TestSuiteGenerator.STATE_COV ;
		//gen.idFinalState = "SI9" ;
		gen.idFinalState = "b3" ;
		//gen.idFinalState = "SS4" ;
		// generate using MOSA:
		//gen.generateWithSBT(60,null) ;
		//gen.printStats();
		// generate using a model checker (the produced test suite will be
		// added to the one previously generated)
		gen.generateWithMC(false, true, false, 16);
		gen.printStats();
		// apply sampling to select a subset of 20 test cases:
		//gen.applySampling(8,20);
		//gen.printStats();
		// save the resulting test cases in files:
		gen.save("./tmp","LRSimple0tc");
	}

}

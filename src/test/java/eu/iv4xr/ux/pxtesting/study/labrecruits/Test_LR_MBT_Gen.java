package eu.iv4xr.ux.pxtesting.study.labrecruits;

import org.junit.jupiter.api.Test;

import eu.iv4xr.ux.pxtesting.mbt.TestSuiteGenerator;

/**
 * An example of generating test cases from a model. The model is 
 * a model of a simple 3-rooms level in the game Lab Recruits.
 * You can find the level definition in ./src/test/resources/level/threerooms.csv.
 * You can play with it by loading it from Lab Recruits.
 * 
 * <p>The EFSM model of this level is defined programmatically
 * in the class {@link eu.iv4xr.ux.pxtesting.mbt.EFSMSimple0}.
 */
public class Test_LR_MBT_Gen {
	
	/**
	 * This example uses an LTL model checker to generate
	 * generate (abstract) test cases.
	 * Some statistics will be printed, and the final set of test
	 * cases will be saved in the directory ./tmp
	 */
	@Test
	public void test1() throws Exception {
		var gen = new TestSuiteGenerator("eu.iv4xr.ux.pxtesting.mbt.EFSMSimple0") ;
		gen.aimedCoverage = TestSuiteGenerator.STATE_COV ;
		gen.idFinalState = "b3" ;
		// generate using MOSA:
		//gen.generateWithSBT(60,null) ;
		//gen.printStats();
		gen.generateWithMC(true, false, true, 16);
		gen.printStats();
		// save the resulting test cases in files: (actually, will only generate one)
		gen.save("./tmp","LRSimple0tc");
	}

}

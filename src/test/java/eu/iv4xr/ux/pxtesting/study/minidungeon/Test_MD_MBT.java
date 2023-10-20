package eu.iv4xr.ux.pxtesting.study.minidungeon;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import eu.iv4xr.ux.pxtesting.mbt.TestSuiteGenerator;

public class Test_MD_MBT {
	

	@Test
	public void test1() throws Exception {
		var gen = new TestSuiteGenerator("eu.iv4xr.ux.pxtesting.study.minidungeon.EFSM_MD_L10") ;
		
		//gen.aimedCoverage = TestSuiteGenerator.STATE_COV ;
		//gen.idFinalState = "SI9" ;
		gen.idFinalState = "SI4" ;
		
		//gen.idFinalState = "SS4" ;
		gen.generateWithSBT(120,null) ;
		gen.printStats();
		gen.generateWithMC(false, true, false, 80);
		gen.printStats();
		//var tc = gen.getTestSuite().get(0) ;
		//System.out.println(">>> " + tc) ;
		gen.applySampling(8,20);
		gen.printStats();
	}
	

}

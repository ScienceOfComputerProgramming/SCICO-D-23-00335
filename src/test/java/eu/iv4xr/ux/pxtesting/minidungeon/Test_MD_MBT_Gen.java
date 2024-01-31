package eu.iv4xr.ux.pxtesting.minidungeon;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.extensions.occ.Goal;
import eu.iv4xr.ux.pxtesting.PXTestAgentRunner;
import eu.iv4xr.ux.pxtesting.Utils;
import eu.iv4xr.ux.pxtesting.mbt.TestSuiteGenerator;
import eu.iv4xr.ux.pxtesting.study.minidungeon.EFSM_MD_L5;
import eu.iv4xr.ux.pxtesting.study.minidungeon.MD_FBK_EFSM_Utils;
import eu.iv4xr.ux.pxtesting.study.minidungeon.MiniDungeonEventsProducer;
import eu.iv4xr.ux.pxtesting.study.minidungeon.MiniDungeonPlayerCharacterization;
import nl.uu.cs.aplib.utils.Pair;

/**
 * An example of generating test cases from a model. The model is 
 * a model of 5-mazes MiniDungeon. The model can be found in
 * ./assets/MD_L5.json and the accompanying MD_L5.dot for visualization.
 * Note that these are not EFSM models, but they are internally 
 * converted into an EFSM.
 */
public class Test_MD_MBT_Gen {

	/**
	 * This example first uses a search-algorithm (MOSA) to generate
	 * test cases from the model MD_L5. It is configured to aim for
	 * transition-coverage. Additionally, all test-cases are required
	 * to end at the entity SI4 (the final shrine in MD_L5).
	 * 
	 * <p>Then an LTL model checker is invoked to generate test cases.
	 * The resulting suite is added to the suite we generated before
	 * with MOSA.
	 * 
	 * <p>Then an adaptive random sampling is used to select 20 test
	 * cases from the overal set of test cases generated before.
	 * 
	 * <p>Some statistics will be printed, and the final set of test
	 * cases will be saved in the directory ./tmp
	 */
	@Order(1)
	@Test
	public void test_generate_only() throws Exception {
		var gen = new TestSuiteGenerator("eu.iv4xr.ux.pxtesting.study.minidungeon.EFSM_MD_L5") ;
		//gen.aimedCoverage = TestSuiteGenerator.STATE_COV ;
		//gen.idFinalState = "SI9" ;
		gen.idFinalState = "SI4" ;
		//gen.idFinalState = "SS4" ;
		// generate using MOSA:
		gen.generateWithSBT(120,null) ;
		gen.printStats();
		int N_SBT = gen.getTestSuite().size() ;
		assertTrue(N_SBT > 0) ;
		// generate using a model checker (the produced test suite will be
		// added to the one previously generated)
		gen.generateWithMC(false, true, false, 80);
		gen.printStats();
		int N_MC = gen.getTestSuite().size() ;
		assertTrue(N_MC > 0) ;
		// apply sampling to select a subset of 20 test cases:
		gen.applySampling(8,20);
		gen.printStats();
		int N_min = gen.getTestSuite().size() ;
		assertTrue(N_min > 0 && N_min < N_MC) ;
		// save the resulting test cases in files:
		Utils.cleanTestCasesAndTraces("./tmp","tc") ;
		gen.save("./tmp","tc");
	}
	
	/**
	 * An example of loading a bunch of previously generated (and saved) tests, and then
	 * executing them.
	 */
	@SuppressWarnings("unchecked")
	@Order(2)    
	@Test
	public void test_load_and_exec() throws Exception {
		
		var gwmodel = (new EFSM_MD_L5()).loadGameWorldModel();

		Pair<Goal, Integer> mentalGoal_clanseShrine = new Pair<>(MiniDungeonPlayerCharacterization.shrineCleansed, 50);

		PXTestAgentRunner runner = new PXTestAgentRunner(dummy -> Test_MD_MBT_Exec.deployTestAgent(),
				new MiniDungeonPlayerCharacterization(), new MiniDungeonEventsProducer(),
				agent -> tc -> MD_FBK_EFSM_Utils.abstractTestSeqToGoalStructure(agent, tc, gwmodel), null,
				mentalGoal_clanseShrine);

		runner.run("./tmp", "./tmp", 8000, 0);
		assertTrue(runner.numberOfFail == 0) ;

	}

}

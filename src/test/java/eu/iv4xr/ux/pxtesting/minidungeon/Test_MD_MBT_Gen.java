package eu.iv4xr.ux.pxtesting.minidungeon;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import eu.iv4xr.framework.extensions.occ.Goal;
import eu.iv4xr.framework.mainConcepts.EmotiveTestAgent;
import eu.iv4xr.framework.mainConcepts.SyntheticEventsProducer;
import eu.iv4xr.ux.pxtesting.PXTestAgentRunner;
import eu.iv4xr.ux.pxtesting.mbt.TestSuiteGenerator;
import eu.iv4xr.ux.pxtesting.occ.XUserCharacterization;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.DungeonApp;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.MiniDungeon.MiniDungeonConfig;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MyAgentEnv;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MyAgentState;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
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
	@Test
	public void test1() throws Exception {
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
		gen.save("./tmp","tc");
	}
}

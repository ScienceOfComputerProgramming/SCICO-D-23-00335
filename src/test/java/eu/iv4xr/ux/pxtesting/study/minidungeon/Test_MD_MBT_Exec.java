package eu.iv4xr.ux.pxtesting.study.minidungeon;

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
 * An example of how abstract test cases generated from a model
 * can be executed on the game under test (GUT). Test cases generated 
 * from a model are abstract. They can't be directly executed
 * on the GUT. To execute a test case, it is first translated to
 * a goal-structure for a test agent. The latter executes the
 * goal-structure by translating it to primitive interactions
 * with the GUT. The translation from abstract test cases
 * to goal-structures for a test agent is GUT-specific. For the
 * game MiniDungeon (which is used in this demo), the translation
 * is implemented in the method {@link MD_FBK_EFSM_Utils#abstractTestSeqToGoalStructure(eu.iv4xr.framework.mainConcepts.TestAgent, AbstractTestSequence, eu.iv4xr.framework.extensions.ltl.gameworldmodel.GameWorldModel)}.
 * 
 */
public class Test_MD_MBT_Exec {
	
	/**
	 * A helper method to launch the game MiniDungeon.
	 */
	DungeonApp deployApp() throws Exception {
		MiniDungeonConfig config = new EFSM_MD_L5().getConfig_MD_L5() ;
		System.out.println(">>> Configuration:\n" + config);
		DungeonApp app = new DungeonApp(config);
		app.soundOn = false;
		DungeonApp.deploy(app);
		return app ;
	}
	
	/**
	 * A helper method to create a test agent, and to connect it to
	 * a running instance of MiniDungeon.
	 */
	EmotiveTestAgent deployTestAgent() {
		try {
			DungeonApp app = deployApp() ;
			var agent = new EmotiveTestAgent("Frodo","Frodo") ;
			
			agent. attachState(new MyAgentState())
		         . attachEnvironment(new MyAgentEnv(app))  ;
			
			return agent ;
		}
		catch(Exception e) {
			System.err.println(">>> FAIL to deploy the test-agent") ;
			e.printStackTrace();
			return null ;
		}
		
	}
	
	/**
	 * An example of generating test cases from a model, and directly
	 * executing them.
	 */
	@Test
	public void test_generate_and_exec() throws Exception {
		var gen = new TestSuiteGenerator("eu.iv4xr.ux.pxtesting.study.minidungeon.EFSM_MD_L5") ;
		gen.idFinalState = "SI4" ;
		// use model-checker to generate the test suite:
		gen.generateWithMC(false, true, false, 80);
		gen.printStats();
		gen.applySampling(6,10);
		gen.printStats();
		// the produced test suite:
		var suite = gen.getTestSuite() ;
		
		var gwmodel = (new EFSM_MD_L5()).loadGameWorldModel() ;
		
		Pair<Goal,Integer> mentalGoal_clanseShrine = new Pair<>(MiniDungeonPlayerCharacterization.shrineCleansed,50) ;
		
		// create a test-runner:
		PXTestAgentRunner runner = new PXTestAgentRunner(
				dummy -> deployTestAgent(),
				new MiniDungeonPlayerCharacterization(),
				new MiniDungeonEventsProducer(),
				agent -> tc -> MD_FBK_EFSM_Utils.abstractTestSeqToGoalStructure(agent, tc, gwmodel),
				null,
				mentalGoal_clanseShrine
				) ;
		
		// run the suite using the runner:
		runner.run_(suite, "./tmp", 8000, 0);
		
	}
	
	
	@Test
	public void test_load_and_exec() throws Exception {
		
		var gwmodel = (new EFSM_MD_L5()).loadGameWorldModel();

		Pair<Goal, Integer> mentalGoal_clanseShrine = new Pair<>(MiniDungeonPlayerCharacterization.shrineCleansed, 50);

		PXTestAgentRunner runner = new PXTestAgentRunner(dummy -> deployTestAgent(),
				new MiniDungeonPlayerCharacterization(), new MiniDungeonEventsProducer(),
				agent -> tc -> MD_FBK_EFSM_Utils.abstractTestSeqToGoalStructure(agent, tc, gwmodel), null,
				mentalGoal_clanseShrine);

		runner.run("./tmp/suite", "./tmp", 8000, 0);

	}

}

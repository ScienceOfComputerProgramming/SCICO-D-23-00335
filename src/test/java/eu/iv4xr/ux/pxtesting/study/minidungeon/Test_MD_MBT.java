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

public class Test_MD_MBT {
	

	//@Test
	public void test1() throws Exception {
		var gen = new TestSuiteGenerator("eu.iv4xr.ux.pxtesting.study.minidungeon.EFSM_MD_L5") ;
		
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
	
	DungeonApp deployApp() throws Exception {
		MiniDungeonConfig config = new EFSM_MD_L5().getConfig_MD_L5() ;
		System.out.println(">>> Configuration:\n" + config);
		DungeonApp app = new DungeonApp(config);
		app.soundOn = false;
		DungeonApp.deploy(app);
		return app ;
	}
	
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
	
	@Test
	public void test2() throws Exception {
		var gen = new TestSuiteGenerator("eu.iv4xr.ux.pxtesting.study.minidungeon.EFSM_MD_L5") ;
		gen.idFinalState = "SI4" ;
		gen.generateWithMC(false, true, false, 80);
		gen.printStats();
		gen.applySampling(6,10);
		gen.printStats();
		var suite = gen.getTestSuite() ;
		
		var gwmodel = (new EFSM_MD_L5()).loadGameWorldModel() ;
		
		Pair<Goal,Integer> mentalGoal_clanseShrine = new Pair<>(MiniDungeonPlayerCharacterization.shrineCleansed,50) ;
		
		PXTestAgentRunner runner = new PXTestAgentRunner(
				dummy -> deployTestAgent(),
				new MiniDungeonPlayerCharacterization(),
				new MiniDungeonEventsProducer(),
				agent -> tc -> MD_FBK_EFSM_Utils.abstractTestSeqToGoalStructure(agent, tc, gwmodel),
				null,
				mentalGoal_clanseShrine
				) ;
		
		List<Pair<String,AbstractTestSequence>> suite_ = new LinkedList<>();
		int k=0 ;
		for (var tc : suite) {
			suite_.add(new Pair<>("tc" + k, tc)) ;
			k++ ;
		}
		
		runner.run(suite_, "tmp", 1000, 0);
		
	}
	

}

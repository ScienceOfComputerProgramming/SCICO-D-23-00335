package eu.iv4xr.ux.pxtesting.labrecruits;

import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import agents.EventsProducer;
import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.fbk.iv4xr.mbt.execution.labrecruits.LabRecruitsTestSuiteExecutor;
import eu.iv4xr.framework.extensions.occ.Goal;
import eu.iv4xr.framework.mainConcepts.EmotiveTestAgent;
import eu.iv4xr.ux.pxtesting.PXTestAgentRunner;
import eu.iv4xr.ux.pxtesting.mbt.TestSuiteGenerator;
import eu.iv4xr.ux.pxtesting.study.labrecruits.PlayerThreeCharacterization;
import game.LabRecruitsTestServer;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;
import world.BeliefState;

public class Test_LR_MBT_Exec {
	
	private static LabRecruitsTestServer labRecruitsTestServer ;
	
	@BeforeAll
	public static void start() {
		// TestSettings.USE_SERVER_FOR_TEST = false ;
		// Uncomment this to make the game's graphic visible:
		TestSettings.USE_GRAPHICS = true;
		String labRecruitesExeRootDir = Paths.get(System.getProperty("user.dir"), "labrecruits").toString();
		labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir);
	}
	
	@AfterAll
	public static void close() {
		labRecruitsTestServer.close();
	}
	
	LabRecruitsEnvironment environment ;
	
	/**
	 * A helper method to create a test agent, and to connect it to
	 * a running instance of MiniDungeon.
	 */
	EmotiveTestAgent deployTestAgent() {
		var config = new LabRecruitsConfig(
				"threerooms",
				Paths.get(System.getProperty("user.dir"),"src","test","resources","levels").toString());
		config.light_intensity = 0.3f;
		environment = new LabRecruitsEnvironment(config);
		var agent = new LabRecruitsTestAgent("player") ;
		
		//TestSettings.youCanRepositionWindow();
			
		agent. attachState(new BeliefState())
		     . attachEnvironment(environment)  ;
				
		return agent ;
	}
	
	GoalStructure SEQ_(List<GoalStructure> gs) {
		GoalStructure[] gs_ = new GoalStructure[gs.size()] ;
		for (int k=0; k < gs_.length ; k++) {
			gs_[k] = gs.get(k) ;
		}
		//System.out.println(">>> #gs=" + gs.size()) ; 
		return SEQ(gs_) ;
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_generate_and_exec() throws Exception {
		
		var gen = new TestSuiteGenerator("eu.iv4xr.ux.pxtesting.mbt.EFSMSimple0") ;
		gen.aimedCoverage = TestSuiteGenerator.STATE_COV ;
		gen.idFinalState = "b3" ;
		// generate using MOSA:
		//gen.generateWithSBT(60,null) ;
		//gen.printStats();
		gen.generateWithMC(true, false, true, 16);
		gen.printStats();
		var suite = gen.getTestSuite() ;
		
		Pair<Goal,Integer> finish_level = new Pair<>(PlayerThreeCharacterization.questIsCompleted,50) ;
		EventsProducer eventsProducer = new EventsProducer() ;
		eventsProducer.idOfLevelEnd = "b3" ;
		
		// use the test case executor to convert
        LabRecruitsTestSuiteExecutor concretizer = new LabRecruitsTestSuiteExecutor();
           
		// create a test-runner:
		PXTestAgentRunner runner = new PXTestAgentRunner(
			dummy -> deployTestAgent(),
			new PlayerThreeCharacterization(),
			eventsProducer,
			agent -> tc -> SEQ_(concretizer.convertTestCaseToGoalStructure(agent, tc)),
			null,
			finish_level) ;
		
		runner.printRunDebug = true ;
		
		try {
			// run the suite using the runner:
			runner.run_(suite, "./tmp", 500, 50);
		}
		finally {
			System.out.println(">>>> closing...") ;
			environment.close() ;	
		}
	}
	
}

package eu.iv4xr.ux.pxtesting.study.labrecruits;

import static agents.TestSettings.USE_INSTRUMENT;
import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import agents.EventsProducer;
import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.extensions.occ.Goal;
import eu.iv4xr.framework.extensions.occ.Iv4xrOCCEngine;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import game.LabRecruitsTestServer;
import nl.uu.cs.aplib.utils.Pair;
import world.BeliefState;

public class Test_integrationLR {
	
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
	
	//@Disabled
	@Test
	public void test_integration() throws InterruptedException {
				
		var config = new LabRecruitsConfig(
				"buttons_doors_1",
				Paths.get(System.getProperty("user.dir"),"src","test","resources","levels").toString());
		config.light_intensity = 0.3f;
		var environment = new LabRecruitsEnvironment(config);

		TestSettings.youCanRepositionWindow();
   
		try {
			// create a test agent
			var testAgent = new LabRecruitsTestAgent("agent1") // matches the ID in the CSV file
					.attachState(new BeliefState())
					.attachEnvironment(environment);

			// define the testing-task:
			var testingTask = SEQ(GoalLib.entityInteracted("button1"), 
					GoalLib.entityStateRefreshed("door1"),
					GoalLib.entityInteracted("button3"), 
					GoalLib.entityStateRefreshed("door2"),
					GoalLib.entityInteracted("button4"),
					GoalLib.entityStateRefreshed("door1"),
					GoalLib.entityInCloseRange("door3"));

			testAgent.setGoal(testingTask);
			
			int i = 0;
			// keep updating the agent
			while (testingTask.getStatus().inProgress()) {
				System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position);
				Thread.sleep(50);
				i++;
				testAgent.update();
				if (i > 200) {
					break;
				}
			}
			// check that we have passed both tests above:
			// goal status should be success
			assertTrue(testingTask.getStatus().success());	
		}
		finally {
			environment.close() ;
		}
	}
	
	//@Disabled
	@Test
	public void test_threerooms_level() throws InterruptedException {
		var config = new LabRecruitsConfig(
				"threerooms",
				Paths.get(System.getProperty("user.dir"),"src","test","resources","levels").toString());
		config.light_intensity = 0.3f;
		var environment = new LabRecruitsEnvironment(config);

		TestSettings.youCanRepositionWindow();
   
		try {
			// create a test agent
			var testAgent = new LabRecruitsTestAgent("player") // matches the ID in the CSV file
					.attachState(new BeliefState())
					.attachEnvironment(environment);

			// define the testing-task:
			var testingTask = SEQ(GoalLib.entityInteracted("b0"), 
					GoalLib.entityStateRefreshed("door0"),
					GoalLib.entityInteracted("b2"), 
					GoalLib.entityStateRefreshed("door1"),
					GoalLib.entityInteracted("b3"));

			testAgent.setGoal(testingTask);
			
			int i = 0;
			// keep updating the agent
			while (testingTask.getStatus().inProgress()) {
				System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position);
				Thread.sleep(50);
				i++;
				testAgent.update();
				if (i > 200) {
					break;
				}
			}
			// check that we have passed both tests above:
			// goal status should be success
			assertTrue(testingTask.getStatus().success());	
		}
		finally {
			environment.close() ;
		}
		
	}
	
	//@Disabled
	@Test
	public void test_emotiveagent_integration() throws InterruptedException {
		var config = new LabRecruitsConfig(
				"threerooms",
				Paths.get(System.getProperty("user.dir"),"src","test","resources","levels").toString());
		config.light_intensity = 0.3f;
		var environment = new LabRecruitsEnvironment(config);

		TestSettings.youCanRepositionWindow();
   
		try {
			// create a test agent
			var testAgent = new LabRecruitsTestAgent("player") 
					.attachState(new BeliefState())
					.attachEnvironment(environment);

			// define the testing-task:
			var testingTask = SEQ(GoalLib.entityInteracted("b0"), 
					GoalLib.entityStateRefreshed("door0"),
					GoalLib.entityInteracted("b2"), 
					GoalLib.entityStateRefreshed("door1"),
					GoalLib.entityInteracted("b3"));
			
			EventsProducer eventsProducer = new EventsProducer() ;
			eventsProducer.idOfLevelEnd = "b3" ;
			
			testAgent.setTestDataCollector(new TestDataCollector()) ;
			testAgent.attachSyntheticEventsProducer(eventsProducer) ;
			
			var currentOcc = new Iv4xrOCCEngine(testAgent.getId()) 
					 . attachToEmotiveTestAgent(testAgent) 
					 . withUserModel(new PlayerThreeCharacterization()) ;
			
			currentOcc.addGoal(PlayerThreeCharacterization.questIsCompleted, 50) ;
			currentOcc.addInitialEmotions(); 
			
			testAgent.setGoal(testingTask);
			
			int i = 0;
			// keep updating the agent
			while (testingTask.getStatus().inProgress()) {
				System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position);
				Thread.sleep(50);
				i++;
				testAgent.update();
				if (i > 200) {
					break;
				}
			}
			// check that we have passed both tests above:
			// goal status should be success
			assertTrue(testingTask.getStatus().success());	
		}
		finally {
			environment.close() ;
		}
	}

}

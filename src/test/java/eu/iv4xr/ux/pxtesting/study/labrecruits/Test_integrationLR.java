package eu.iv4xr.ux.pxtesting.study.labrecruits;

import static agents.TestSettings.USE_INSTRUMENT;
import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import game.LabRecruitsTestServer;
import world.BeliefState;

public class Test_integrationLR {
	
	private static LabRecruitsTestServer labRecruitsTestServer ;
	
	@BeforeAll
	public static void start() {
		// TestSettings.USE_SERVER_FOR_TEST = false ;
		// Uncomment this to make the game's graphic visible:
		TestSettings.USE_GRAPHICS = true;

		// System.out.println(">>> " + System.getProperty("user.dir")) ;
		// System.out.println(">>> " +
		// Paths.get(System.getProperty("user.dir"),"labrecruits")) ;

		String labRecruitesExeRootDir = Paths.get(System.getProperty("user.dir"), "labrecruits").toString();

		labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir);
	}
	
	@AfterAll
	public static void close() {
		labRecruitsTestServer.close();
	}
	
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
					.attachState(new BeliefState()).attachEnvironment(environment);

			// define the testing-task:
			var testingTask = SEQ(GoalLib.entityInteracted("button1"), 
					GoalLib.entityStateRefreshed("door1"),
					GoalLib.entityInteracted("button3"), 
					GoalLib.entityStateRefreshed("door2"),
					GoalLib.entityInteracted("button4"),
					// GoalLib.entityIsInRange("button3").lift(),
					// GoalLib.entityIsInRange("door1").lift(),
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

}

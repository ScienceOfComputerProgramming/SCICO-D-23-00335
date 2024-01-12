package eu.iv4xr.ux.pxtesting.study.Pipeline;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import agents.EventsProducer;
import agents.LabRecruitsTestAgent;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.fbk.iv4xr.mbt.MBTProperties;
import eu.fbk.iv4xr.mbt.MBTProperties.LR_random_mode;
import eu.fbk.iv4xr.mbt.efsm.EFSM;
import eu.fbk.iv4xr.mbt.efsm.EFSMFactory;
import eu.fbk.iv4xr.mbt.efsm.EFSMOperation;
import eu.fbk.iv4xr.mbt.efsm.EFSMState;
import eu.fbk.iv4xr.mbt.efsm.EFSMTransition;
import eu.fbk.iv4xr.mbt.efsm.exp.Assign;
import eu.fbk.iv4xr.mbt.efsm.exp.Var;
import eu.fbk.iv4xr.mbt.efsm.exp.integer.IntSum;
import eu.fbk.iv4xr.mbt.efsm.labRecruits.LabRecruitsRandomEFSM;
import eu.fbk.iv4xr.mbt.efsm.labRecruits.LabRecruitsRandomEFSM.StateType;
import eu.fbk.iv4xr.mbt.efsm.exp.Const;
import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import eu.fbk.iv4xr.mbt.testcase.MBTChromosome;
import eu.fbk.iv4xr.mbt.testsuite.SuiteChromosome;
import eu.fbk.iv4xr.mbt.testcase.Path;
import eu.fbk.iv4xr.mbt.utils.TestSerializationUtils;
import eu.iv4xr.framework.extensions.occ.Emotion;
import eu.iv4xr.framework.extensions.occ.EmotionAppraisalSystem;
import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;
import eu.iv4xr.framework.extensions.occ.Event.Tick;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.spatial.Vec3;
import eu.iv4xr.ux.pxtesting.mbt.Distance;
import eu.iv4xr.ux.pxtesting.study.labrecruits.CSVlevelImport;
import eu.iv4xr.ux.pxtesting.study.labrecruits.LREvent;
import eu.iv4xr.ux.pxtesting.study.labrecruits.PlayerOneCharacterization;
import eu.iv4xr.ux.pxtesting.study.labrecruits.levelsize;
import eu.iv4xr.ux.pxtesting.study.labrecruits.PlayerOneCharacterization.EmotionBeliefBase;
import game.LabRecruitsTestServer;
import game.Platform;
import eu.fbk.iv4xr.mbt.execution.labrecruits.LabRecruitsTestSuiteExecutor;

import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.GoalStructure.PrimitiveGoal;
import nl.uu.cs.aplib.multiAgentSupport.Message;

import static eu.iv4xr.ux.pxtesting.study.labrecruits.CSVExport.exportToCSV;
import static eu.iv4xr.ux.pxtesting.study.labrecruits.PlayerOneCharacterization.gotAsMuchPointsAsPossible;
import static eu.iv4xr.ux.pxtesting.study.labrecruits.PlayerOneCharacterization.questIsCompleted;
import static nl.uu.cs.aplib.AplibEDSL.* ;
import world.BeliefState;
import java.util.*;
/**
 *  load all test cases from the disk as a test suite - transform a test suite into a goal structure.

 * Using this along with the appraisal system we could record emotional states of different numbers of generated test cases.  
 * 
 * @author sansari
 */
 
// to run this, first you need to create the level model by running MC/SBtest_Generation. 
public class RunOCC {

	protected EFSM efsm_copy;
	
	boolean withGraphics = false ;
	
	@Test
    public void runGeneratedTests() throws IOException {

	    String rootFolder = new File(System.getProperty("user.dir")).getParent();
        String testFolder = rootFolder + File.separator + "Combinedtest";
        String modelFolder = testFolder + File.separator + "Model";
        String traceFolder = rootFolder + File.separator + "traces";
        String labRecruitesExeRootDir = rootFolder + File.separator + "iv4xrDemo";
        
		//get goal state			
        model_test_IOoperations set=new model_test_IOoperations();
        efsm_copy=set.loadModel(modelFolder);
        EFSMState goalstate = null;
		for(var state : efsm_copy.getStates()) {
			EFSMState state_ = (EFSMState) state ;
			if(LabRecruitsRandomEFSM.getStateType(state_) == StateType.GoalFlag) {
				goalstate=state_ ;
			}
		} ;
		
        // load tests from file
        Map<String,MBTChromosome> loadedSolution = set.parseTests(testFolder);
		SuiteChromosome loadedSuitechromosome = new SuiteChromosome(); 
        loadedSolution.values().forEach(c->loadedSuitechromosome.addTest(c));
        
        
        //List<AbstractTestSequence> absTestsuite=new ArrayList<AbstractTestSequence>();
        //loadedSolution.getTestChromosomes().forEach(c->absTestsuite.add((AbstractTestSequence)c.getTestcase()));
        //List<AbstractTestSequence> absTestsuite_new=eu.iv4xr.ux.pxtestingPipeline.MCtest_Generation.AdaptiveRandomSampling(absTestsuite, 40);
		//model_test_IOoperations io=new model_test_IOoperations();
        //set.writeTests(absTestsuite_new, save, "test");
       // loadedSolution=null;
       //absTestsuite_new.forEach(c->loadedSolution.addTest((MBTChromosome)c.getPath()));
		
        // choose a distance metric from Jaccard, Jaro- Winkler or Leveneshtien.
        Distance dis=new Distance("jaro-winkler");
		double totaldistance= dis.totDistance(loadedSuitechromosome);
		System.out.println("MC-testsuite size is: "+ loadedSuitechromosome.size());
		System.out.println("total Distance: "+totaldistance);
		
		var starttime = System.currentTimeMillis() ;
		List<String> notcoveredtr=new ArrayList();
        //EFSMState goalstate = null;
		int notcovered=0;
		  for(var tr : efsm_copy.getTransitons()) {
			  if(loadedSolution.containsValue(tr.toString()))
	        	{
	        		continue;
	        	}
	        	else
	        	{
	        		notcoveredtr.add(tr.toString());
	        		notcovered++;
	        	}
			}
	        System.out.println("# not covered transitions: "+notcovered+ "from : "+ efsm_copy.getTransitons().size());
	        File txtFile = new File( testFolder + File.separator + "SBTnotcovered_transitions" + ".txt");
			try {
			
				FileUtils.writeStringToFile(txtFile, notcoveredtr.toString(), Charset.defaultCharset());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
        // open the server
        LabRecruitsTestServer testServer = new LabRecruitsTestServer(withGraphics,
                Platform.PathToLabRecruitsExecutable(labRecruitesExeRootDir));
        // set the configuration of the server 
        // level file name is hard coded in writeModel but can be changed
        //String levelname = "flag_MAW2WF_f0_z72_34" ;
        String levelname = Utils.autoGetLevelName(modelFolder) ;
        
        LabRecruitsConfig lrCfg = new LabRecruitsConfig(levelname, modelFolder);
        levelsize lrsize= new levelsize(CSVlevelImport.ImportFromCSV(levelname, modelFolder));    
        
        // convert test cases in loadedSolution to goal structure: use the test case executor to convert
        LabRecruitsTestSuiteExecutor lrExecutor = new LabRecruitsTestSuiteExecutor(rootFolder, testFolder, modelFolder, null);
        		
        // iterate over test case and convert to goals
        String results = "" ;
        for(Entry<String, MBTChromosome> test: loadedSolution.entrySet()) {

        	 AbstractTestSequence testcase = (AbstractTestSequence) test.getValue().getTestcase();
        	 
            // the system returns a list of goals that can be sequentialized with SEQ
            LabRecruitsEnvironment labRecruitsEnvironment = new LabRecruitsEnvironment(lrCfg);
            var dataCollector = new TestDataCollector();
            // run the testing task:
            LabRecruitsTestAgent testAgent = new LabRecruitsTestAgent("Agent1") ; 
            testAgent.attachState(new BeliefState())
                     .setTestDataCollector(dataCollector)
                     .attachEnvironment(labRecruitsEnvironment)
                     .attachSyntheticEventsProducer(new EventsProducer());
            testAgent.getSyntheticEventsProducer().idOfLevelEnd="gf0";
            List<GoalStructure> goals = lrExecutor.convertTestCaseToGoalStructure(testAgent, testcase);
            // Add instrumentation here:
            goals = set.instrumentTestCase(testAgent,testcase,goals) ;
            GoalStructure g =SEQ(goals.toArray(new GoalStructure[goals.size()]));
            testAgent.setGoal(g) ;
            System.out.println(">> Testing task: " + test.getKey() + ", #=" + goals.size()) ;
            System.out.println(">> " + testcase ) ;    
            
            // add an event-producer to the test agent so that it produce events for
            // emotion appraisals:
            //EventsProducer eventsProducer = new EventsProducer().attachTestAgent(testAgent);
            	
            // Create an emotion appraiser, and hook it to the agent:
            EmotionAppraisalSystem eas = new EmotionAppraisalSystem(testAgent.getId());
            eas.attachEmotionBeliefBase(new EmotionBeliefBase().attachFunctionalState(testAgent.getState()))
                    .withUserModel(new PlayerOneCharacterization()).addGoal(questIsCompleted, 50)
                    .addGoal(gotAsMuchPointsAsPossible, 50).addInitialEmotions();       
            labRecruitsEnvironment.startSimulation();
            // goal not achieved yet
            assertFalse(testAgent.success());
            
            //collect data
            EmotionData emodata=new EmotionData();
            int t=0 ;
            while (g.getStatus().inProgress()) {
            	 Vec3 position = testAgent.getState().worldmodel.position;
                 System.out.println("*** " + t + ", " + testAgent.getState().id + " @" + position);
                 
                 
                 if (testAgent.getSyntheticEventsProducer().currentEvents.isEmpty()) {
                     eas.update(new Tick(), t);
                 }
                 else {
                     for (Message m : testAgent.getSyntheticEventsProducer().currentEvents) {
                         eas.update( new LREvent(m.getMsgName()), t);
                         
                     }
                 }

                 if (position != null) {
                    
                     Float score = (float) testAgent.getState().worldmodel().score;
                     Float healthlost = (float) testAgent.getState().worldmodel().healthLost;
                     Float health = (float) testAgent.getState().worldmodel().health;
                     System.out.println("*** score=" + score);
                     emodata.recordNewrow(score,healthlost,health,eas,position,t);
                     if(eas.newEmotions.stream().anyMatch(c->c.etype==Emotion.EmotionType.Disappointment)||eas.newEmotions.stream().anyMatch(c->c.etype==Emotion.EmotionType.Satisfaction))
                     {
                    	 break;
                     }
                 }
                if (t>900) {
                    break ;
                }
                try {
                    Thread.sleep(200);
                    testAgent.update();
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                t++ ;
             }          
            //save triggered emotions in the efsm model.
            //exportToCSV(emodata.csvData_goalQuestIsCompleted, "data_goalQuestCompleted_"+ test.getKey()+".csv");
            exportToCSV(emodata.csvData_goalQuestIsCompleted, traceFolder + File.separator + "data_goalQuestCompleted_"+ test.getKey()+".csv");
            //exportToCSV(emodata.csvData_goalGetMuchPoints, "data_goalGetMuchPoints_"+i+".csv");

            
            // run the python script called "mkgraph.py" for drawing graphs according to the saved .csv  
			
//			  String path=new File(new
//			  File(System.getProperty("user.dir")).getAbsolutePath(),"mkgraph.py").
//			  getAbsolutePath(); ProcessBuilder builder = new ProcessBuilder();
//			  ProcessBuilder pb = new ProcessBuilder();
//			  
			  //sending the csvfile number, width and heights of the level as parameters.
//			  builder.command("python",
//			  path,""+ test.getKey(),""+lrsize.getheight(),""+lrsize.getwidth()); Process
//			  p=builder.start(); BufferedReader bfr = new BufferedReader(new
//			  InputStreamReader(p.getInputStream()));
//			  
//			  System.out.println(".........start   visualization process........."); String
//			  line = ""; while ((line = bfr.readLine()) != null){
//			  System.out.println("Python Output: " + line); }
//			  
//			  //g.printGoalStructureStatus();
//			  labRecruitsEnvironment.close() ; results +=
//			  ">> tc-" + test.getKey() + ", Duration: "+ t + ", goalstatus: " + g.getStatus() +
//			  ", #fail: " + testAgent.getTestDataCollector().getNumberOfFailVerdictsSeen()
//			  + ", #success: " +
//			  testAgent.getTestDataCollector().getNumberOfPassVerdictsSeen() + "\n" ;
			 
        }
                
		float duration = ((float) (System.currentTimeMillis() - starttime)) / 1000f ; 
		System.out.println("Emotion system execution time with "+loadedSolution.size() + "is : "+ duration);  
        System.out.println("" + results) ;
        testServer.close();
        
	}

}

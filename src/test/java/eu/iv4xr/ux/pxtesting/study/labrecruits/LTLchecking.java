package eu.iv4xr.ux.pxtesting.study.labrecruits;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.experimental.theories.DataPoint;
import org.junit.jupiter.api.AfterAll;
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
import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.ltl.SATVerdict;
import eu.iv4xr.framework.extensions.occ.Emotion;
import eu.iv4xr.framework.extensions.occ.EmotionAppraisalSystem;
import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;
import eu.iv4xr.framework.extensions.occ.Event.Tick;
import eu.iv4xr.framework.mainConcepts.IEmotion;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.spatial.Vec3;
import eu.iv4xr.ux.pxtesting.study.labrecruits.Datapoint;
import eu.iv4xr.ux.pxtesting.study.labrecruits.PlayerOneCharacterization.EmotionBeliefBase;
import game.LabRecruitsTestServer;
import game.Platform;
import eu.fbk.iv4xr.mbt.execution.labrecruits.LabRecruitsTestSuiteExecutor;

import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.GoalStructure.PrimitiveGoal;
import nl.uu.cs.aplib.multiAgentSupport.Message;

import static eu.iv4xr.framework.extensions.ltl.LTL.always;
import static eu.iv4xr.framework.extensions.ltl.LTL.eventually;
import static eu.iv4xr.framework.extensions.ltl.LTL.ltlNot;
import static eu.iv4xr.framework.extensions.ltl.LTL.now;
import static eu.iv4xr.ux.pxtesting.study.labrecruits.CSVExport.importFromCSV;
import static eu.iv4xr.ux.pxtesting.study.labrecruits.PlayerOneCharacterization.gotAsMuchPointsAsPossible;
import static eu.iv4xr.ux.pxtesting.study.labrecruits.PlayerOneCharacterization.questIsCompleted;
import static nl.uu.cs.aplib.AplibEDSL.* ;
import world.BeliefState;

/**
 * 
 * @author sansari
 */

public class LTLchecking{

   public static Map<String, List<Datapoint>> data;

   @BeforeAll
   static void start() throws IOException {

			 String rootFolder = new File(System.getProperty("user.dir")).getParent();
		     String emotionFolder = rootFolder + File.separator + "occ4pxtesting-New version";
		      data = new HashMap<String, List<Datapoint>>();

		       try {
				List<File> files = org.apache.maven.shared.utils.io.FileUtils.getFiles(new File(emotionFolder), "*.csv", "");
				for (File file : files) {
					System.out.print("data_goalQuestCompleted_"+(files.indexOf(file)+1)+".csv");
			         List<Datapoint> dp= importFromCSV("data_goalQuestCompleted_"+files.indexOf(file)+".csv");
			         data.put("data_goalQuestCompleted_"+files.indexOf(file), dp);
				         
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
   
   
	@Test
	 public void Test_health() throws IOException {

		
		for(var entry : data.entrySet())
		{
			assertEquals(SATVerdict.SAT,  (always(( Datapoint d) -> d.health>0).sat(entry.getValue())));
		}

	}
	
	@Test
	 public void Test_thres() throws IOException {
		
		
		var phi2=now((Datapoint dp)->dp.emo.stream().anyMatch(e->e.etype==EmotionType.Hope&& e.intensity>400))
				.until((Datapoint dp)-> dp.emo.stream().anyMatch(e-> e.etype==EmotionType.Joy&& e.intensity>0));
		
		
		String solution=ltlSat(data,phi2);
		//Assert not null
		
	}
	
    private String ltlSat(Map<String, List<Datapoint>> data, LTL<Datapoint> ltl) throws IOException {

	    String rootFolder = new File(System.getProperty("user.dir")).getParent();
        String testFolder = rootFolder + File.separator + "MCtest";
        List<File> files= org.apache.maven.shared.utils.io.FileUtils.getFiles(new File(testFolder),"*.txt","");
		String example=null;
		String testcase=null;
		for(var entry : data.entrySet())
		{
			
			if(ltl.sat(entry.getValue())==SATVerdict.SAT)
			{
				example=entry.getKey();
				break;
			}
		}
		
		
		var suffix=example.substring(example.lastIndexOf("_"));	
		for(var file : files ) 
		{
			if(file.getName().endsWith(suffix+".txt")) {
				
				 testcase=org.apache.maven.shared.utils.io.FileUtils.fileRead(file);
				System.out.println(testcase);
			}
		}
		return testcase;
	}

  

}

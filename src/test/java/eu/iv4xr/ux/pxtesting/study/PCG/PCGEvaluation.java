package eu.iv4xr.ux.pxtesting.study.PCG;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

import com.opencsv.CSVReader;

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
import eu.iv4xr.ux.pxtesting.study.PCG.PCGCharacterization;
import eu.iv4xr.ux.pxtesting.study.PCG.PCGEvent;
import eu.iv4xr.ux.pxtesting.study.labrecruits.CSVlevelImport;
import eu.iv4xr.ux.pxtesting.study.labrecruits.EmotionData;
import eu.iv4xr.ux.pxtesting.study.labrecruits.LREvent;
import eu.iv4xr.ux.pxtesting.study.labrecruits.PlayerOneCharacterization;
import eu.iv4xr.ux.pxtesting.study.labrecruits.levelsize;
import eu.iv4xr.ux.pxtesting.study.labrecruits.model_test_IOoperations;
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
public class PCGEvaluation {

	@Test
    public void emotionevaluate() throws IOException {

	    String rootFolder = new File(System.getProperty("user.dir")).getParent();
        String PCGFolder = rootFolder + File.separator + "PCG";         
        File folder = new File(PCGFolder);
        File listOfFiles[] = folder.listFiles();
        List<List<PCGEvent>> level_events = new ArrayList<List<PCGEvent>>();
        for (File file : listOfFiles) {
            if (file.isFile()) {
            	List<PCGEvent> pcgevent_list=CSV_Reader(file.getName(),PCGFolder);
            	level_events.add(pcgevent_list);
            }
        }
        // Create an emotion appraiser, and hook it to the agent:
        EmotionAppraisalSystem eas = new EmotionAppraisalSystem("Agent1");
        eas.attachEmotionBeliefBase(new EmotionBeliefBase())
                .withUserModel(new PCGCharacterization()).addGoal(questIsCompleted, 50)
                .addInitialEmotions();       
        
        //collect data
        EmotionData emodata=new EmotionData();
        
        int i=0 ;

       for( List<PCGEvent> list: level_events)
       {
    	   
    	   for(PCGEvent e:list)
    	   {
               if (e.name=="No event") {
                   eas.update(new Tick(), e.time);
               }
               else {
                       eas.update(e , e.time); 
                     }

               emodata.recordNewrow(eas,e.time);
                 if(eas.newEmotions.stream().anyMatch(c->c.etype==Emotion.EmotionType.Disappointment)||eas.newEmotions.stream().anyMatch(c->c.etype==Emotion.EmotionType.Satisfaction))
                   {
                  	 break;
                   }
           }
       
  		//save triggered emotions in the efsm model.
          exportToCSV(emodata.csvData_goalQuestIsCompleted, "data_goalQuestCompleted_"+ i +".csv");
          
          // run the python script called "mkgraph.py" for drawing graphs according to the saved .csv  
  		
  		
  		  i++;
  		 
      }
       String path=new File(new
    	  		  File(System.getProperty("user.dir")).getAbsolutePath(),"mkgraph.py").getAbsolutePath(); 
    	  		
    	  		  ProcessBuilder builder = new ProcessBuilder();
    	  		  
    	  		  //sending the csvfile number, width and heights of the level as parameters.
    	  		  builder.command("python",
    	  		  path,""+ i,""+ 53,""+32); 
    	  		  Process p=builder.start(); 
    	  		  BufferedReader bfr = new BufferedReader(new  InputStreamReader(p.getInputStream()));
    	  		  
    	  		  System.out.println(".........start visualization process........."); String
    	  		  line = ""; while ((line = bfr.readLine()) != null){
    	  		  System.out.println("Python Output: " + line); } 	  		  
    }
       
private List<PCGEvent>  CSV_Reader(String Filename, String Path) throws IOException{
		
			List<PCGEvent> eventlist=new ArrayList();
			String filepath= Path+ File.separator +Filename;
		    CSVReader reader = new CSVReader(new FileReader(filepath));
		      String [] nextLine;
		      reader.readNext();
		      while ((nextLine = reader.readNext()) != null ) {
		    	  PCGEvent e=new PCGEvent(nextLine[3]);
		    	  e.health=(Integer.parseInt(nextLine[4]));
		    	  eventlist.add(e);
		      }
			return eventlist;
	}
	
}       


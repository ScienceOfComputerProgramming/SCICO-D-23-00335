package eu.iv4xr.ux.pxtesting.study.labrecruits;

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
import java.util.Scanner;
import java.util.function.Function;



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
import eu.fbk.iv4xr.mbt.efsm.exp.Const;
import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import eu.fbk.iv4xr.mbt.testcase.MBTChromosome;
import eu.fbk.iv4xr.mbt.testsuite.SuiteChromosome;
import eu.fbk.iv4xr.mbt.testcase.Path;
import eu.fbk.iv4xr.mbt.utils.TestSerializationUtils;
import eu.iv4xr.framework.extensions.occ.Emotion;
import eu.iv4xr.framework.extensions.occ.EmotionAppraisalSystem;
import eu.iv4xr.framework.extensions.occ.Event;
import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;
import eu.iv4xr.framework.extensions.occ.Event.Tick;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.ObservationEvent.TimeStampedObservationEvent;
import eu.iv4xr.framework.spatial.Vec3;
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

/**
 *  load all test cases from the disk as a test suite - transform a test suite into a goal structure.
 * Using this along with the appraisal system we could record emotional states of different numbers of generated test cases.  
 * @author sansari
 * 
 */
public class EmotionData {
	
 public List<String[]> csvData_goalQuestIsCompleted = new LinkedList<>();
 List<String[]> csvData_goalGetMuchPoints = new LinkedList<>();
 
 	public  EmotionData() {
         // some lists for collecting experiment data:
        String[] csvRow = { "t", "x", "y", "hope", "joy", "satisfaction", "fear", "distress","disappointment","score", "losthealth" , "remainedhealth"};
            csvData_goalQuestIsCompleted.add(csvRow);
            csvData_goalGetMuchPoints.add(csvRow);
 	}
	
	public  void recordNewrow( EmotionAppraisalSystem eas, int t)
	{
		 
        Function<Emotion, Float> normalizeIntensity = e -> e != null ? (float) e.intensity / 800f : 0f;
        
          float hope_completingQuest = normalizeIntensity
                  .apply(eas.getEmotion(questIsCompleted.name, EmotionType.Hope));
          float joy_completingQuest = normalizeIntensity
                  .apply(eas.getEmotion(questIsCompleted.name, EmotionType.Joy));
          float satisfaction_completingQuest = normalizeIntensity
                  .apply(eas.getEmotion(questIsCompleted.name, EmotionType.Satisfaction));
          float fear_completingQuest = normalizeIntensity
                  .apply(eas.getEmotion(questIsCompleted.name, EmotionType.Fear));
          float distress_completingQuest = normalizeIntensity
                  .apply(eas.getEmotion(questIsCompleted.name, EmotionType.Distress));
          float disapp_completingQuest = normalizeIntensity
                  .apply(eas.getEmotion(questIsCompleted.name, EmotionType.Disappointment));
          
          String[] csvRow1={ "" + t, "" + "", "" + "", "" + hope_completingQuest,
              "" + joy_completingQuest, "" + satisfaction_completingQuest, "" + fear_completingQuest, "" +distress_completingQuest,""+disapp_completingQuest, "" +"", "" +"",""+ ""};
         // String[] csvRow1 = { "" + t, ""  + hope_completingQuest,
             //     "" + joy_completingQuest, "" + satisfaction_completingQuest, "" + fear_completingQuest, "" +distress_completingQuest,""+disapp_completingQuest};

         // String[] csvRow2 = { "" + t, "" + p_.x, "" + p_.z, "" + hope_getMuchPoints, "" + joy_getMuchPoints,
             //     "" + satisfaction_getMuchPoints, "" + fear_getMuchPoints };

          csvData_goalQuestIsCompleted.add(csvRow1);
         // csvData_goalGetMuchPoints.add(csvRow2);
	}
	public  void recordNewrow(Float score, Float losthealth, float remainedhealth, EmotionAppraisalSystem eas, Vec3 position, int t)
	{
		 Vec3 p_ = position.copy();
        Function<Emotion, Float> normalizeIntensity = e -> e != null ? (float) e.intensity / 800f : 0f;
        
          float hope_completingQuest = normalizeIntensity
                  .apply(eas.getEmotion(questIsCompleted.name, EmotionType.Hope));
          float joy_completingQuest = normalizeIntensity
                  .apply(eas.getEmotion(questIsCompleted.name, EmotionType.Joy));
          float satisfaction_completingQuest = normalizeIntensity
                  .apply(eas.getEmotion(questIsCompleted.name, EmotionType.Satisfaction));
          float fear_completingQuest = normalizeIntensity
                  .apply(eas.getEmotion(questIsCompleted.name, EmotionType.Fear));
          float distress_completingQuest = normalizeIntensity
                  .apply(eas.getEmotion(questIsCompleted.name, EmotionType.Distress));
          float disapp_completingQuest = normalizeIntensity
                  .apply(eas.getEmotion(questIsCompleted.name, EmotionType.Disappointment));
          
          float hope_getMuchPoints = normalizeIntensity
                  .apply(eas.getEmotion(gotAsMuchPointsAsPossible.name, EmotionType.Hope));
          float joy_getMuchPoints = normalizeIntensity
                  .apply(eas.getEmotion(gotAsMuchPointsAsPossible.name, EmotionType.Joy));
          float satisfaction_getMuchPoints = normalizeIntensity
                  .apply(eas.getEmotion(gotAsMuchPointsAsPossible.name, EmotionType.Satisfaction));
          float fear_getMuchPoints = normalizeIntensity
                  .apply(eas.getEmotion(gotAsMuchPointsAsPossible.name, EmotionType.Fear));

          String[] csvRow1 = { "" + t, "" + p_.x, "" + p_.z, "" + hope_completingQuest,
                  "" + joy_completingQuest, "" + satisfaction_completingQuest, "" + fear_completingQuest, "" +distress_completingQuest,""+disapp_completingQuest, "" +score, "" +losthealth,""+ remainedhealth};

          String[] csvRow2 = { "" + t, "" + p_.x, "" + p_.z, "" + hope_getMuchPoints, "" + joy_getMuchPoints,
                  "" + satisfaction_getMuchPoints, "" + fear_getMuchPoints };

          csvData_goalQuestIsCompleted.add(csvRow1);
          csvData_goalGetMuchPoints.add(csvRow2);
	}


	
}

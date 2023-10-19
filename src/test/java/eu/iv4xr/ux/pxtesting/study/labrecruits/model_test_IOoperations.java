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
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.evosuite.shaded.org.apache.commons.lang3.SerializationUtils;
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
public class model_test_IOoperations {


	/**
	 * Save EFSM model
	 * 
	 * @param modelFolderName folder where to save the model
	 */
	public void writeModel(String modelFolderName) {
		File modelFolder = new File(modelFolderName);
		modelFolder.mkdirs();

		String modelFileName = modelFolderName + File.separator + "EFSM_model.ser";
		String levelFileName = modelFolderName + File.separator + "LabRecruits_level.csv";
		String modelDotFileName = modelFolderName + File.separator + "EFSM_model.dot";

		File csvFile = new File(levelFileName);
		File dotFile = new File(modelDotFileName);

		EFSM efsm = EFSMFactory.getInstance().getEFSM();
		try {

			FileUtils.writeByteArrayToFile(new File(modelFileName), 
					// old MBT 1.1.0b code, upgrading it; it should return a byte-code
					// serialization of the current EFSM:
					// EFSMFactory.getInstance().getOriginalEFSM()
					SerializationUtils.serialize(EFSMFactory.getInstance().getEFSM())
					);
			FileUtils.writeStringToFile(dotFile, efsm.getDotString(), Charset.defaultCharset());
			// if csv is available
			if (efsm.getEFSMString() != "") {
				FileUtils.writeStringToFile(csvFile, efsm.getEFSMString(), Charset.defaultCharset());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected EFSM loadModel(String modelFolderName) {
		File modelFolder = new File(modelFolderName);
		modelFolder.mkdirs();
		String modelFileName = modelFolderName + File.separator + "EFSM_model.ser";
		try {
			return TestSerializationUtils.loadEFSM(modelFileName);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}	
	/**
	 * Save generated test
	 * 
	 * @param solution   solution generated
	 * @param testFolder folder where to save the tests (make sure tests folder
	 *                   exists)
	 */
	protected void writeTests(SuiteChromosome solution, String testFolder) {
		// make sure tests folder exists
		File testsFolder = new File(testFolder);
		testsFolder.mkdirs();
		int count = 1;
		for (MBTChromosome testCase : solution.getTestChromosomes()) {
			String dotFileName = testFolder + File.separator + "testt_" + count + ".dot";
			String txtFileName = testFolder + File.separator + "testt_" + count + ".txt";
			String serFileName = testFolder + File.separator + "testt_" + count + ".ser";
			File dotFile = new File(dotFileName);
			File txtFile = new File(txtFileName);
			try {
				FileUtils.writeStringToFile(dotFile, ((AbstractTestSequence) testCase.getTestcase()).toDot(),
						Charset.defaultCharset());
				FileUtils.writeStringToFile(txtFile, testCase.getTestcase().toString(), Charset.defaultCharset());
				TestSerializationUtils.saveTestSequence((AbstractTestSequence) testCase.getTestcase(), serFileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			count++;
		}

	}
	protected void writeTests(List<AbstractTestSequence> solution, String testFolder,String testtype) {
		// make sure tests folder exists
		File testsFolder = new File(testFolder);
		testsFolder.mkdirs();
		int count = 1;
		for (AbstractTestSequence testCase : solution) {
			String dotFileName = testFolder + File.separator + testtype+"_" + count + ".dot";
			String txtFileName = testFolder + File.separator + testtype +"_"+ count + ".txt";
			String serFileName = testFolder + File.separator + testtype +"_"+ count + ".ser";
			File dotFile = new File(dotFileName);
			File txtFile = new File(txtFileName);
			try {
				FileUtils.writeStringToFile(dotFile, testCase.toDot(),
						Charset.defaultCharset());
				FileUtils.writeStringToFile(txtFile, testCase.toString(), Charset.defaultCharset());
				TestSerializationUtils.saveTestSequence(testCase, serFileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			count++;
		}

	}
	/**
	 * Load serialized tests into a SuiteChromosome object
	 * @param testsDir
	 * @return
	 */
	public Map<String,MBTChromosome> parseTests(String testsDir) {
		//SuiteChromosome suite = new SuiteChromosome();
		Map<String,MBTChromosome> suite=new HashMap<String,MBTChromosome>();
		try {
			List<File> files = org.apache.maven.shared.utils.io.FileUtils.getFiles(new File(testsDir), "*.ser", "");
			for (File file : files) {
				AbstractTestSequence test = TestSerializationUtils.loadTestSequence(file.getAbsolutePath());
				MBTChromosome chromosome = new MBTChromosome();
				chromosome.setTestcase(test);
				//suite.addTest(chromosome);
				suite.put(file.getName().replaceFirst(".ser", ""), chromosome);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return suite;
	}

	GoalStructure transitionMarker(TestAgent agent, EFSMTransition tr, String marker) {
		String tr_id = "" + tr.getId() + "_" + tr.toString() ;
		return goal("Start of MBT transition " + tr_id)
				.toSolve(doesNotMatter -> true) // will be immediately solved
				.withTactic(action("bla")
					.do1((BeliefState S) -> {
					   agent.getTestDataCollector().registerEvent(
							   agent.getId(), 
							   new TimeStampedObservationEvent(
									   "MBT-transition-" + marker,
									   tr_id + ":" + S.worldmodel.timestamp 
									   )) ;
					   
					   return true ;
				      })
					.lift())
				.lift() ;
	}
	
	List<GoalStructure> instrumentTestCase(TestAgent agent, AbstractTestSequence tc, List<GoalStructure> tcgoals) {
		Path path = tc.getPath();
		List<EFSMTransition> transitions = path.getTransitions();
		List<GoalStructure> intrumentedTcGoals = new LinkedList<>() ;
		int k = 0 ;
		for(var tr : transitions) {
			var G = tcgoals.get(k) ;
			String trId = tr.getId() ;
			intrumentedTcGoals.add(SEQ(
					transitionMarker(agent,tr,"START"),
					G,
					transitionMarker(agent,tr,"END")
					)) ;
			k++ ;
		}
		return intrumentedTcGoals ;
	}
}

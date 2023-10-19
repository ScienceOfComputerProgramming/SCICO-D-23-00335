package eu.iv4xr.ux.pxtesting.study.labrecruits;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.fbk.iv4xr.mbt.MBTProperties;
import eu.fbk.iv4xr.mbt.MBTProperties.LR_random_mode;
import eu.fbk.iv4xr.mbt.MBTProperties.ModelCriterion;
import eu.fbk.iv4xr.mbt.efsm.EFSM;
import eu.fbk.iv4xr.mbt.efsm.EFSMFactory;
import eu.fbk.iv4xr.mbt.efsm.EFSMState;
import eu.fbk.iv4xr.mbt.efsm.EFSMTransition;
import eu.fbk.iv4xr.mbt.efsm.labRecruits.LabRecruitsRandomEFSM;
import eu.fbk.iv4xr.mbt.efsm.labRecruits.LabRecruitsRandomEFSM.StateType;
import eu.fbk.iv4xr.mbt.efsm.modelcheckingInterface.InterfaceToIv4xrModelCheker;
import eu.fbk.iv4xr.mbt.efsm.modelcheckingInterface.InterfaceToIv4xrModelCheker.EFSMTransitionWrapper;
import eu.fbk.iv4xr.mbt.strategy.GenerationStrategy;
import eu.fbk.iv4xr.mbt.strategy.SearchBasedStrategy;
import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import eu.fbk.iv4xr.mbt.testcase.MBTChromosome;
import eu.fbk.iv4xr.mbt.testsuite.SuiteChromosome;
import eu.fbk.iv4xr.mbt.utils.TestSerializationUtils;
import eu.iv4xr.framework.extensions.ltl.BasicModelChecker;
import eu.iv4xr.framework.extensions.ltl.BuchiModelChecker;
import eu.iv4xr.framework.extensions.ltl.IExplorableState;
import eu.iv4xr.framework.extensions.ltl.ITransition;
import eu.iv4xr.framework.extensions.ltl.BasicModelChecker.Path;
import eu.iv4xr.ux.pxtesting.mbt.Distance;
import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.JaroWinkler;
import nl.uu.cs.aplib.utils.Pair;
import eu.fbk.iv4xr.mbt.Main;
/**
 * In this test I'll show how to - use mbt to create a Lab Recruits level -
 * generate a test suite on it - serialize the level and the test suite on the
 * disk
 * @author sansari
 * @author prandi
 * 
 */
public class SBtest_Generation_newlevels {

	// use a logger to save output execution information
	protected static final Logger logger = LoggerFactory.getLogger(Main.class);	
	protected List <EFSMState> goalstates = new ArrayList<EFSMState>();
	protected List<AbstractTestSequence> absTestsuite;
	public String targetState;
	public int notcovered=159;
	protected EFSM loaded_efsm;

	public void setPropertiesMBT() {
		
//		MBTProperties.LR_generation_mode = LR_random_mode.N_BUTTONS_DEPENDENT;
//		MBTProperties.SEARCH_BUDGET = 300;
//		MBTProperties.MODELCRITERION = new ModelCriterion[] { ModelCriterion.TRANSITION_FIX_END_STATE }; 
//		MBTProperties.TEST_FACTORY =  MBTProperties.TestFactory.RANDOM_LENGTH_FIX_TARGET;
//
//		  targetState = "gf2"; 
//		  MBTProperties.STATE_TARGET =targetState; 
//		  MBTProperties.MAX_LENGTH=1000;
		  
			MBTProperties.LR_generation_mode = LR_random_mode.N_BUTTONS_DEPENDENT;
			MBTProperties.SEARCH_BUDGET = 300;
			MBTProperties.SUT_EFSM = "labrecruits.random_default";
			// there are some predefined configuration to pass to MBTProperties.SUT_EFSM
			// "labrecruits.random_simple", "labrecruits.random_medium",
			// "labrecruits.random_large"

			
			MBTProperties.LR_mean_buttons = 0.5;
			MBTProperties.LR_n_buttons = 50;
			MBTProperties.LR_n_doors = 40;
			MBTProperties.LR_seed = 2142877334;
			
			MBTProperties.LR_n_goalFlags = 16 ;

			//MBTProperties.LR_n_fires=5;
			  // Set criterion
			   MBTProperties.MODELCRITERION = new ModelCriterion[] {
			  ModelCriterion.TRANSITION_FIX_END_STATE }; MBTProperties.TEST_FACTORY =
			  MBTProperties.TestFactory.RANDOM_LENGTH_FIX_TARGET;
			  
			  // Set Target state
			  targetState = "gf9"; 
			  MBTProperties.STATE_TARGET =targetState; MBTProperties.MAX_LENGTH=500;
	}

	/**
	 * Check if LabRecruits level exists
	 */
	public boolean existsLabRecruitLevel() {
		String efsmString = EFSMFactory.getInstance().getEFSM().getEFSMString();
		boolean out = !efsmString.equalsIgnoreCase("");
		return out;
	}
	
	/**
	 * Run test generation
	 */
	public List<MBTChromosome> runSBTestGeneration(Set<String> targetSet) {

		// initialize the test generator to use search based strategy
		SearchBasedStrategy sbStrategy = new SearchBasedStrategy<>();
		SuiteChromosome generatedTests = sbStrategy.generateTests(targetSet);
		List<MBTChromosome> testChromosomes = generatedTests.getTestChromosomes();
		return testChromosomes;
	}
	
	 @Test
	 public void runMultipleTests() throws Exception {
	     
	    	
	    	 while(notcovered>4)
	    	 {
	    		 notcovered=0;
	    		 SBTestGeneration_FBKtrancoverage();
	 	 	    String rootFolder = new File(System.getProperty("user.dir")).getParent();
		         String testFolder = rootFolder + File.separator + "SBTtest";
		 		String modelFolder = testFolder + File.separator + "Model";
		 		 model_test_IOoperations set=new model_test_IOoperations();
		 	        EFSM efsm = set.loadModel(modelFolder);
		 			List<String> notcoveredtr=new ArrayList();
		 	        EFSMState goalstate = null;
		 			 
		 	        for(var tr : efsm.getTransitons()) {
		 	        	
		 	        	if(absTestsuite.stream().anyMatch(c-> c.getPath().getTransitions().toString().contains(tr.toString())))
		 	        	{
		 	        		continue;
		 	        	}
		 	        	else
		 	        	{
		 	        		notcoveredtr.add(tr.toString());
		 	        		notcovered++;
		 	        	}
		 			}
		 	        System.out.println("# not covered transitions: "+notcovered+ "from : "+ efsm.getTransitons().size());
		 	        File txtFile = new File( testFolder + File.separator + "SBTnotcovered_transitions" + ".txt");
		 			try {
		 			
		 				FileUtils.writeStringToFile(txtFile, notcoveredtr.toString(), Charset.defaultCharset());
		 			} catch (IOException e) {
		 				// TODO Auto-generated catch block
		 				e.printStackTrace();
		 			}
	    	 }


	 	

	     
	 }
	
	public void SBTestGeneration_FBKtrancoverage() throws Exception{
		absTestsuite = new ArrayList<AbstractTestSequence>() ;
		setPropertiesMBT();
	    String rootFolder = new File(System.getProperty("user.dir")).getParent();
        String testFolder = rootFolder + File.separator + "MOSA levels"+
        								  File.separator+ "MOSA"+ 
        								  File.separator+"1649939356263";
        String modelFolder= testFolder + File.separator+ "Model2";

        model_test_IOoperations set=new model_test_IOoperations();
        set.writeModel(modelFolder);
        
			if (!existsLabRecruitLevel()) {
				fail();
			}
			
			// check that target state exists
			EFSM efsm = EFSMFactory.getInstance().getEFSM();
			assertTrue(efsm.getStates().contains(new EFSMState(targetState)));
			//assertTrue(loaded_efsm.getStates().contains(new EFSMState(targetState)));
			// Generate result
			GenerationStrategy generationStrategy = new SearchBasedStrategy<MBTChromosome>();
			SuiteChromosome solution = generationStrategy.generateTests();
			
			int count = 1;
			for (MBTChromosome testCase : solution.getTestChromosomes()) {
				AbstractTestSequence testSequence = (AbstractTestSequence)testCase.getTestcase();
				// check that the last state is target state
				EFSMTransition lastTranstion = testSequence.getPath().getTransitionAt(testSequence.getPath().getLength()-1);
				assertTrue(lastTranstion.getTgt().equals(new EFSMState(targetState)));
				

				if(!absTestsuite.contains(testSequence))
				{
					absTestsuite.add(testSequence);
				}
			
				  
				 
				count++;
			}

			// choose a distance metric from Jaccard, Jaro- Winkler or Leveneshtien.
			Distance dis=new Distance("jaro-winkler");
			double totaldistance= dis.totDistance(absTestsuite);
			System.out.println("Sub-testsuite size is: "+ absTestsuite.size());
			System.out.println("total Distance: "+totaldistance);
			

			  List<AbstractTestSequence> absTestsuite_Subset= eu.iv4xr.ux.pxtesting.study.labrecruits
					  .MCtest_Generation.RandomSampling(absTestsuite, 30 );

			// output folders
			//String selectedtestFolder = testFolder+File.separator + "selectedtest";

			// save generated tests
			File testFolderFile = new File(testFolder);
			if (!testFolderFile.exists()) {
				testFolderFile.mkdirs();
			}
			File modelFolderFile = new File(modelFolder);
			if (!modelFolderFile.exists()) {
				modelFolderFile.mkdirs();
			}
			model_test_IOoperations io=new model_test_IOoperations();
			io.writeTests(absTestsuite, testFolder, "SBTtest");
			//io.writeTests(absTestsuite_Subset, selectedtestFolder,"SBTtest");

			io.writeModel(modelFolder); 

	}
	//run a test for testgeneration using my written transition coverage
	//@Test
	public void runSBTGenerationTest() {
		
		// set the parameters for the generation
		setPropertiesMBT();
		
		if (!existsLabRecruitLevel()) {
			fail();
		}
	
		//get goal state	
		EFSM efsm=EFSMFactory.getInstance().getEFSM();
		for(var state : efsm.getStates()) {
			EFSMState state_ = (EFSMState) state ;
			if(LabRecruitsRandomEFSM.getStateType(state_) == StateType.GoalFlag) {
				goalstates.add(state_) ;
			}
		} ;
		assertTrue(goalstates != null) ;
		String goalid = goalstates.get(0).getId() ;

		
		List<AbstractTestSequence> absTestsuite = new ArrayList<AbstractTestSequence>() ;
	
		for(var efsmtr : efsm.getTransitons())
		{
			Set<String> targetSet = new HashSet<>();
			targetSet.add(goalid);
			targetSet.add(((EFSMTransition)efsmtr).getSrc().getId().toString());
			targetSet.add(((EFSMTransition)efsmtr).getTgt().getId().toString());
			System.out.println("Source"+((EFSMTransition)efsmtr).getSrc().getId().toString());
			System.out.println("Target"+((EFSMTransition)efsmtr).getTgt().getId().toString());
			
			var starttime = System.currentTimeMillis() ;
		
			List<MBTChromosome> testChromosomes =runSBTestGeneration(targetSet);
		
			float duration = ((float) (System.currentTimeMillis() - starttime)) / 1000f ; 
	
			System.out.println("\nGenerated "+testChromosomes.size()+" test cases");
			for(MBTChromosome chr : testChromosomes) {
				AbstractTestSequence testcase = (AbstractTestSequence) chr.getTestcase();
				if(!absTestsuite.contains(testcase))
				{
					absTestsuite.add(testcase);
				}
			}
			
		}
		
		// output folders
		String rootFolder = new File(System.getProperty("user.dir")).getParent();
		String testFolder = rootFolder + File.separator + "MBTtest";
		String modelFolder = testFolder + File.separator + "Model";
		
		// save generated tests
		File testFolderFile = new File(testFolder);
		if (!testFolderFile.exists()) {
			testFolderFile.mkdirs();
		}
		File modelFolderFile = new File(modelFolder);
		if (!modelFolderFile.exists()) {
			modelFolderFile.mkdirs();
		}
		model_test_IOoperations io=new model_test_IOoperations();
		io.writeTests(absTestsuite, testFolder, "SBTtest");
		io.writeModel(modelFolder);
	}
	
	//@AfterEach 
	public void test_transitioncoverage()
	{
		
	    String rootFolder = new File(System.getProperty("user.dir")).getParent();
        String testFolder = rootFolder + File.separator + "SBTtest";
		String modelFolder = testFolder + File.separator + "Model";
		 model_test_IOoperations set=new model_test_IOoperations();
	        EFSM efsm = set.loadModel(modelFolder);
			List<String> notcoveredtr=new ArrayList();
	        EFSMState goalstate = null;
			 notcovered=0;
	        for(var tr : efsm.getTransitons()) {
	        	
	        	if(absTestsuite.stream().anyMatch(c-> c.getPath().getTransitions().toString().contains(tr.toString())))
	        	{
	        		continue;
	        	}
	        	else
	        	{
	        		notcoveredtr.add(tr.toString());
	        		notcovered++;
	        	}
			}
	        System.out.println("# not covered transitions: "+notcovered+ "from : "+ efsm.getTransitons().size());
	        File txtFile = new File( testFolder + File.separator + "SBTnotcovered_transitions" + ".txt");
			try {
			
				FileUtils.writeStringToFile(txtFile, notcoveredtr.toString(), Charset.defaultCharset());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}

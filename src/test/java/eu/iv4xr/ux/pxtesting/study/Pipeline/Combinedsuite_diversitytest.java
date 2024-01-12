package eu.iv4xr.ux.pxtesting.study.Pipeline;

import static eu.iv4xr.framework.extensions.ltl.LTL.ltlAnd;
import static eu.iv4xr.framework.extensions.ltl.LTL.ltlNot;
import static eu.iv4xr.framework.extensions.ltl.LTL.next;
import static eu.iv4xr.framework.extensions.ltl.LTL.now;
import static eu.iv4xr.framework.extensions.ltl.LTL2Buchi.getBuchi;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
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
import eu.iv4xr.framework.extensions.ltl.Buchi;
import eu.iv4xr.framework.extensions.ltl.BuchiModelChecker;
import eu.iv4xr.framework.extensions.ltl.IExplorableState;
import eu.iv4xr.framework.extensions.ltl.ITransition;
import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.ltl.BasicModelChecker.Path;
import eu.iv4xr.ux.pxtesting.mbt.Distance;
import nl.uu.cs.aplib.utils.Pair;
import eu.fbk.iv4xr.mbt.Main;

/**
 * @author sansari
 */
public class Combinedsuite_diversitytest {


	List<DistancePoint> distancelist= new ArrayList<DistancePoint>();
	DistancePoint dp;
	public EFSM efsm;
	protected List <EFSMState> goalstates = new ArrayList<EFSMState>();
	public String targetState;
	

	@BeforeEach
	 public void start() {
		 
			// set the parameters for the generation
			setPropertiesMBT();
			
			if (!existsLabRecruitLevel()) {
				fail();
			}
			 efsm=EFSMFactory.getInstance().getEFSM();

	 }
	 
	 //@Test
	 public void runMultipleTests() throws Exception {
	     for (int i = 0; i < 10; i++) {
	    	
	    	 dp=new DistancePoint();
	    	 runMCGenerationTest();
	    	 SBTGentest();
	    	 Combinetest();
	    	 distancelist.add(dp);
	    	 
	     }
	     distancelist.forEach(c->System.out.println("mc-size: "+c.mc_size+ "mc_Winkler_distance: "+c.mc_Winkler_distance+
	    		"sbt_size: "+c.sbt_size+ "sbt_Winkler_distance: "+c.sbt_Winkler_distance+ "combineset_Winkler_distance : "+c.combineset_Winkler_distance));
	 }
	 
	public void setPropertiesMBT() {
		
		MBTProperties.LR_generation_mode = LR_random_mode.N_BUTTONS_DEPENDENT;
		MBTProperties.SEARCH_BUDGET = 60;
		MBTProperties.SUT_EFSM = "labrecruits.random_extreme";
		// there are some predefined configuration to pass to MBTProperties.SUT_EFSM
		// "labrecruits.random_simple", "labrecruits.random_medium",
		// "labrecruits.random_large"

		
		MBTProperties.LR_mean_buttons = 0.5;
		MBTProperties.LR_n_buttons = 40;
		MBTProperties.LR_n_doors = 28;
		MBTProperties.LR_seed = 3257439;
		MBTProperties.LR_n_rooms=8;
		MBTProperties.LR_n_goalFlags = 2 ;

		// Set criterion
				MBTProperties.MODELCRITERION = new ModelCriterion[] {
						//ModelCriterion.TRANSITION_FIX_END_STATE 
				};
				//MBTProperties.TEST_FACTORY = MBTProperties.TestFactory.RANDOM_LENGTH_FIX_TARGET;
				
				// Set Target state
				 targetState = "gf0";
				//MBTProperties.STATE_TARGET = targetState;
				MBTProperties.MAX_LENGTH=35;
				// Search budget in seconds
				MBTProperties.SEARCH_BUDGET = 250;
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
	
	
	public void SBTGentest() throws Exception{
		List<AbstractTestSequence> absTestsuite = new ArrayList<AbstractTestSequence>() ;
		/*
		 * setPropertiesMBT(); if (!existsLabRecruitLevel()) { fail(); } // Optionally
		 * set output folder // MBTProperties.OUTPUT_DIR = "outdir";
		 * 
		 * // check that target state exists EFSM efsm =
		 * EFSMFactory.getInstance().getEFSM();
		 */
			assertTrue(efsm.getStates().contains(new EFSMState(targetState)));
			// Generate result
			GenerationStrategy generationStrategy = new SearchBasedStrategy<MBTChromosome>();
			SuiteChromosome solution = generationStrategy.generateTests();
			
			String testFolder = new File(System.getProperty("user.dir")).getParent() + File.separator + "Combinetest";

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

			Distance dis=new Distance("jaro-winkler");
			double jarodistance= dis.totDistance(absTestsuite);
			System.out.println("testsuite size is: "+ absTestsuite.size());
			System.out.println(" Jaro-Winkler Distance: "+jarodistance);
			Distance levdis=new Distance("levenshtein");
			double levdistance= levdis.totDistance(absTestsuite);
			System.out.println(" Levenshtein Distance: "+levdistance);
			 dp.sbt_size=absTestsuite.size();
			 dp.sbt_Winkler_distance=jarodistance;
			 dp.sbt_Leveneshtein_distance=levdistance;
			Scanner in = new Scanner(System.in);
		    in.nextLine();

			
			// output folders
			String rootFolder = new File(System.getProperty("user.dir")).getParent();
			String modelFolder = testFolder + File.separator + "SBTModel";
			
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
			//io.writeTests(absTestsuite, testFolder);
			io.writeModel(modelFolder); 
		
			
			// make sure tests folder exists
			File testsFolder = new File(testFolder);
			testsFolder.mkdirs();
			 count = 1;
			for (AbstractTestSequence testCase : absTestsuite) {
				String dotFileName = testFolder + File.separator + "SBTtest_" + count + ".dot";
				String txtFileName = testFolder + File.separator + "SBTtest_" + count + ".txt";
				String serFileName = testFolder + File.separator + "SBTtest_" + count + ".ser";
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
	

	
	public void runMCGenerationTest() {
	

		//get goal state	
		for(var state : efsm.getStates()) {
			EFSMState state_ = (EFSMState) state ;
			if(LabRecruitsRandomEFSM.getStateType(state_) == StateType.GoalFlag) {
				goalstates.add(state_) ;
			}
		} ;
		
		assertTrue(goalstates != null) ;
		//String goalid = goalstates.get(0).getId() ;
		String goalid = "gf0" ;
		Predicate<IExplorableState> goal = state -> {
			InterfaceToIv4xrModelCheker.EFSMStateWrapper state_ = (InterfaceToIv4xrModelCheker.EFSMStateWrapper) state ;
			return state_.conf.getState().getId().equals(goalid) ;
		} ;
		
		// buchi model checking for full-- transition-- coverage along with the goal coverage.
		List<AbstractTestSequence> absTestsuite=TransitionCoverage(efsm,goal);
		
		
		
		// Measure Similarity btw test cases in a suite.
		
		//List<AbstractTestSequence> absTestsuite_Subset= eu.iv4xr.ux.pxtesting.MCtest_Generation.AdaptiveRandomSampling(absTestsuite, 33);
		//List<AbstractTestSequence> absTestsuite_Rand= eu.iv4xr.ux.pxtesting.MCtest_Generation.RandomSampling(absTestsuite, 33);

		Distance dis=new Distance("jaro-winkler");
		double jarodistance= dis.totDistance(absTestsuite);
		System.out.println("Original-testsuite size is: "+ absTestsuite.size());
		System.out.println(dis.mtr  + "  Distance: "+jarodistance);

		System.out.println("testsuite size is: "+ absTestsuite.size());
		System.out.println(" Jaro-Winkler Distance: "+jarodistance);
		Distance levdis=new Distance("levenshtein");
		double levdistance= levdis.totDistance(absTestsuite);
		System.out.println(" Levenshtein Distance: "+levdistance);
		 dp.mc_size=absTestsuite.size();
		 dp.mc_Winkler_distance=jarodistance;
		 dp.mc_Leveneshtein_distance=levdistance;
		//Scanner in = new Scanner(System.in);
	   // in.nextLine();
		
		
		// output folders
		String rootFolder = new File(System.getProperty("user.dir")).getParent();
		String testFolder = rootFolder + File.separator + "Combinetest";
		String modelFolder = testFolder + File.separator + "mcModel";
		
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
		//io.writeTests(absTestsuite, testFolder);
		io.writeModel(modelFolder);
		
		// make sure tests folder exists
				File testsFolder = new File(testFolder);
				testsFolder.mkdirs();
				 int count = 1;
				for (AbstractTestSequence testCase : absTestsuite) {
					String dotFileName = testFolder + File.separator + "MCtest_" + count + ".dot";
					String txtFileName = testFolder + File.separator + "MCtest_" + count + ".txt";
					String serFileName = testFolder + File.separator + "MCtest_" + count + ".ser";
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
	
	InterfaceToIv4xrModelCheker.EFSMStateWrapper cast(IExplorableState S) { return (InterfaceToIv4xrModelCheker.EFSMStateWrapper) S ; }

	
	private List<AbstractTestSequence> TransitionCoverage(EFSM efsm, Predicate<IExplorableState> goal) {
	    
		List<AbstractTestSequence> abstestsuite = new ArrayList<AbstractTestSequence>() ;
		
		for(var efsmtr : efsm.getTransitons())
		{
			
			Predicate<IExplorableState> tr_Src = state -> {
				InterfaceToIv4xrModelCheker.EFSMStateWrapper state_ = (InterfaceToIv4xrModelCheker.EFSMStateWrapper) state ;
				return state_.conf.getState().getId().equals(((EFSMTransition)efsmtr).getSrc().getId()) ;
			} ;
			
			Predicate<IExplorableState> tr_Tgt = state -> {
				InterfaceToIv4xrModelCheker.EFSMStateWrapper state_ = (InterfaceToIv4xrModelCheker.EFSMStateWrapper) state ;
				return state_.conf.getState().getId().equals(((EFSMTransition)efsmtr).getTgt().getId()) ;
			};
			
		   	//Buchi model checking	
			BuchiModelChecker bmc = new BuchiModelChecker(new InterfaceToIv4xrModelCheker(efsm)) ;
				
			// invoke the MC:
			var starttime = System.currentTimeMillis() ;
				
			float duration = ((float) (System.currentTimeMillis() - starttime)) / 1000f ; 
			
			
			LTL<IExplorableState> notgf0=ltlNot(now("gf0",S -> cast(S).conf.getState().getId().equals("gf0")));
			LTL<IExplorableState> f = notgf0.until(ltlAnd(now("n1",tr_Src),
					next(ltlAnd(now("n2",tr_Tgt),
				notgf0.until(now("gf0",S -> cast(S).conf.getState().getId().equals("gf0")))))));
			
		
			
			Buchi B = getBuchi(f) ;
			Path<Pair<IExplorableState,String>> path = bmc.find( B, 30) ;
			
			//Buchi
			//Path<Pair<IExplorableState,String>> path = bmc.find( eventuallyeventually(tr_Src,tr_Tgt,goal), 20) ;
			
			// print stats:
			System.out.println(">>> #nodes in efsm: " + efsm.getStates().size()) ;
			System.out.println(">>> #transitions in efsm: " + efsm.getTransitons().size()) ;
			System.out.println(">>> #concrete states and transitions:\n" + bmc.stats) ;
			System.out.println(">>> runtime(s): " + duration) ;
			if(path!=null) {
				System.out.println(">>> Solution length: " + path.path.size()) ;
			}
			System.out.println(">>> Solution: " + path) ;
				
			if(path!=null) {
				//transform transitions to AbstracttestSquence
				List<ITransition> theTransitions = path.path.stream()
						.map(step -> step.fst)
						.collect(Collectors.toList()) ;
				theTransitions.remove(0);
				List<EFSMTransition> efsmTransitions = new LinkedList<>() ;
				for(var tr : theTransitions) {
						EFSMTransitionWrapper tr_ = (EFSMTransitionWrapper) tr ;
						efsmTransitions.add(tr_.tr) ;	

				}	
					
				var path_ = new eu.fbk.iv4xr.mbt.testcase.Path(efsmTransitions) ;
				AbstractTestSequence absTestSeq = new AbstractTestSequence() ;
				absTestSeq.setPath(path_);
				if(!abstestsuite.contains(absTestSeq))
				{
					abstestsuite.add(absTestSeq);
				}
			}
		}
		return abstestsuite;
		
	}
	
	
	@Test
	public void Combinetest() throws IOException {
			
		String rootFolder = new File(System.getProperty("user.dir")).getParent();
	    String testFolder1 = rootFolder + File.separator + "Combinedtest";
	    String testFolder2 = rootFolder + File.separator + "MBTtest";

        model_test_IOoperations set=new model_test_IOoperations();

	    // load tests from file
	    SuiteChromosome loadedSolution = (SuiteChromosome) set.parseTests(testFolder1);
//	    SuiteChromosome loadedSolution2 = set.parseTests(testFolder2);
//	    for(var l: loadedSolution2.getTestChromosomes())
//	    {
//	    	loadedSolution.addTest(l);
//	    }
	    
	    Distance dis=new Distance("jaro-winkler");
		//int [][] ind= dis.pairs_distance(loadedSolution);
	    int ind= dis.furthestTestCase(loadedSolution);
		System.out.println("total size"+loadedSolution.size());
		
		
		String txtFileName1 = testFolder1 + File.separator + "test_" + "max" + ".txt";

		File txtFile = new File (txtFileName1);
		try {
			FileUtils.writeStringToFile(txtFile, loadedSolution.getTestChromosome(ind).toString(), Charset.defaultCharset());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i=1 ; i<=84; i++)
		{
			String txtFileName2 = testFolder1 + File.separator + "SBTtest_" +i+ ".txt";
			try{
				List<String> listF1 = Files.readAllLines(Paths.get(txtFileName2));
				List<String> listF2 = Files.readAllLines( Paths.get(txtFileName1));
			 System.out.println(listF1.containsAll(listF2));

			}catch(IOException ie) {
				ie.getMessage();
			}

    		

		}
		


		double jarodistance= dis.totDistance(loadedSolution);
		System.out.println("testsuite size is: "+ loadedSolution.size());
		System.out.println(" Jaro-Winkler Distance: "+jarodistance);
		Distance levdis=new Distance("levenshtein");
		double levdistance= levdis.totDistance(loadedSolution);
		System.out.println(" Levenshtein Distance: "+levdistance);
		 dp.combineset_Winkler_distance=jarodistance;
		 dp.combineset_Leveneshtein_distance=levdistance;
		Scanner in = new Scanner(System.in);
	    in.nextLine();
		
		
	}
}

class DistancePoint
{
	
	public int mc_size;
	public int sbt_size;
	public double mc_Winkler_distance;
	public double sbt_Winkler_distance;
	public double mc_Leveneshtein_distance;
	public double sbt_Leveneshtein_distance;
	public double combineset_Winkler_distance;
	public double combineset_Leveneshtein_distance;

	public DistancePoint()	{}

	public DistancePoint(int mc_size, int sbt_size,double mc_Winkler_distance, double sbt_Winkler_distance,
						double mc_Leveneshtein_distance, double sbt_Leveneshtein_distance, double combineset_Winkler_distance, double combineset_Leveneshtein_distance )
	{
		 this.mc_size=mc_size;
		 this.sbt_size=sbt_size;
		 this.mc_Winkler_distance=mc_Winkler_distance;
		 this.sbt_Winkler_distance=sbt_Winkler_distance;
		 this.mc_Leveneshtein_distance=mc_Leveneshtein_distance;
		 this.sbt_Leveneshtein_distance=sbt_Leveneshtein_distance;
		 this.combineset_Winkler_distance=combineset_Winkler_distance;
		 this.combineset_Leveneshtein_distance=combineset_Leveneshtein_distance;

	}
}

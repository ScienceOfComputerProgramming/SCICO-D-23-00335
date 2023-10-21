package eu.iv4xr.ux.pxtestingPipeline;

import static eu.iv4xr.framework.extensions.ltl.LTL.*;
import static eu.iv4xr.framework.extensions.ltl.LTL2Buchi.getBuchi;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.fbk.iv4xr.mbt.MBTProperties;
import eu.fbk.iv4xr.mbt.MBTProperties.LR_random_mode;
import eu.fbk.iv4xr.mbt.efsm.EFSM;
import eu.fbk.iv4xr.mbt.efsm.EFSMFactory;
import eu.fbk.iv4xr.mbt.efsm.EFSMPath;
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
import eu.iv4xr.framework.extensions.ltl.Buchi;
import eu.iv4xr.framework.extensions.ltl.BuchiModelChecker;
import eu.iv4xr.framework.extensions.ltl.IExplorableState;
import eu.iv4xr.framework.extensions.ltl.ITransition;
import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.ltl.BasicModelChecker.Path;
import eu.iv4xr.ux.pxtesting.mbt.Distance;
import eu.iv4xr.framework.extensions.ltl.*;
import nl.uu.cs.aplib.utils.Pair;
import eu.fbk.iv4xr.mbt.Main;
import static eu.iv4xr.framework.extensions.ltl.LTL.*;
import static org.junit.jupiter.api.Assertions.* ;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import static eu.iv4xr.framework.extensions.ltl.LTL.*;
import static org.junit.jupiter.api.Assertions.* ;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import info.debatty.java.stringsimilarity.*;
import java.util.HashMap;

/**
 * @author sansari
 */
public class MCtest_Generation {

	// use a logger to save output execution information
	protected static final Logger logger = LoggerFactory.getLogger(Main.class);	
	protected List <EFSMState> goalstates = new ArrayList<EFSMState>();
	protected List<AbstractTestSequence> absTestsuite=null;
	private BuchiModelChecker bmc;
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
		
	}

	/**
	 * Check if LabRecruits level exists
	 */
	public boolean existsLabRecruitLevel() {
		String efsmString = EFSMFactory.getInstance().getEFSM().getEFSMString();
		boolean out = !efsmString.equalsIgnoreCase("");
		return out;
	}
	
	//run a test for test generation using MC developed by Wishnu
	//Saba: right now it is written for one single goal but later on can be upgraded 
	//since I already included list of goalstates and absTestsuite --->just path_ needs to be replaced by list of path.
	@Test
	public void runMCGenerationTest() {
	
		// set the parameters for the generation
		setPropertiesMBT();
		
		if (!existsLabRecruitLevel()) {
			fail();
		}
		EFSM efsm=EFSMFactory.getInstance().getEFSM();

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
		
	    // buchi model checking for full-- state-- coverage along with the goal coverage.
		//List<AbstractTestSequence> absTestsuite=StateCoverage(efsm,goal);
		
		// buchi model checking for full-- transition-- coverage along with the goal coverage.
		var starttime = System.currentTimeMillis() ;
		 absTestsuite=TransitionCoverage(efsm,goal);
		float duration = ((float) (System.currentTimeMillis() - starttime)) / 1000f ; 
		// print stats:
		System.out.println(">>> #nodes in efsm: " + efsm.getStates().size()) ;
		System.out.println(">>> #transitions in efsm: " + efsm.getTransitons().size()) ;
		System.out.println(">>> runtime(s): " + duration) ;
		
		// Measure Similarity btw test cases in a suite.
		
		  List<AbstractTestSequence>  absTestsuite_Rand= RandomSampling(absTestsuite, 10	);
		  List<AbstractTestSequence> absTestsuite_Subset=  AdaptiveRandomSampling(absTestsuite, 10  );
		  
		  
			
			  Distance dis=new Distance("jaro-winkler"); double jarodistance=
			  dis.totDistance(absTestsuite_Rand);
			  System.out.println("Rand-testsuite size is: "+ absTestsuite_Rand.size());
			  System.out.println( " Rand Distance: "+jarodistance);
			  
			  jarodistance= dis.totDistance(absTestsuite_Subset);
			  System.out.println("Sub-testsuite size is: "+ absTestsuite_Subset.size());
			  System.out.println( "  Jaro Distance is "+jarodistance); jarodistance=
			  dis.totDistance(absTestsuite);
			  System.out.println("Original testsuite size is: "+ absTestsuite.size());
			  System.out.println(dis.mtr + "  Distance: "+jarodistance);
			 
		// output folders
		String rootFolder = new File(System.getProperty("user.dir")).getParent();
		String testFolder = rootFolder + File.separator + "MCtest";
		//String selectedtestFolder = rootFolder + File.separator + "MCtest"+File.separator + "selectedtest";

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
		io.writeTests(absTestsuite, testFolder, "MCtest");
		//io.writeTests(absTestsuite_Subset, selectedtestFolder,"MCtest");
		
		io.writeModel(modelFolder);
	}
	
	@AfterEach 
	public void test_transitioncoverage() throws IOException
	{
		List<String> notcoveredtr=new ArrayList();
	    String rootFolder = new File(System.getProperty("user.dir")).getParent();
        String testFolder = rootFolder + File.separator + "MCtest";
		String modelFolder = testFolder + File.separator + "Model";
		 model_test_IOoperations set=new model_test_IOoperations();
	        EFSM efsm = set.loadModel(modelFolder);
	        EFSMState goalstate = null;
			int notcovered=0;
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
			File txtFile = new File( testFolder + File.separator + "notcovered_transitions" + ".txt");
			try {
			
				FileUtils.writeStringToFile(txtFile, notcoveredtr.toString(), Charset.defaultCharset());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}
	
	
	InterfaceToIv4xrModelCheker.EFSMStateWrapper cast(IExplorableState S) { return (InterfaceToIv4xrModelCheker.EFSMStateWrapper) S ; }

	
	private List<AbstractTestSequence> TransitionCoverage(EFSM efsm, Predicate<IExplorableState> goal) {
	    
		List<AbstractTestSequence> abstestsuite = new ArrayList<AbstractTestSequence>() ;

		for(var efsmtr : efsm.getTransitons())
		{
			String notr= "gf0-{explore[EXPLORE];}->b8, gf0-{explore[EXPLORE];}->d4p, gf0-{explore[EXPLORE];}->d5m, gf0-{explore[EXPLORE];}->d6m";
		  if(!abstestsuite.stream().anyMatch(c-> c.getPath().getTransitions().toString().contains(efsmtr.toString())) & !notr.contains(efsmtr.toString()))
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
				
			
			
			LTL<IExplorableState> notgf0=ltlNot(now("gf0",S -> cast(S).conf.getState().getId().equals("gf0")));
			LTL<IExplorableState> f = notgf0.until(ltlAnd(now("n1",tr_Src),
					next(ltlAnd(now("n2",tr_Tgt),
				notgf0.until(now("gf0",S -> cast(S).conf.getState().getId().equals("gf0")))))));
			
			// invoke the MC:

			 bmc = new BuchiModelChecker(new InterfaceToIv4xrModelCheker(efsm)) ;

			Buchi B = getBuchi(f) ;
			Path<Pair<IExplorableState,String>> path = findShortest( B, 46) ;
			
			
			//Buchi
			//Path<Pair<IExplorableState,String>> path = bmc.find( eventuallyeventually(tr_Src,tr_Tgt,goal), 20) ;
			
			//System.out.println(">>> #concrete states and transitions:\n" + bmc.stats) ;
			System.out.println(">>> trnasition(s): " + efsmtr) ;

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
		}

		
		return abstestsuite;
		
	}

	public Path<Pair<IExplorableState,String>> findShortest(Buchi B , int maxDepth) {

	         if (maxDepth < 0)
	             throw new IllegalArgumentException() ;
	         int lowbound = 0 ;
	         int upbound = maxDepth+1 ;

	         Path<Pair<IExplorableState,String>> bestpath = null ;
	         while (upbound > lowbound) {
	             int mid = lowbound + (upbound - lowbound)/2 ;
	             Path<Pair<IExplorableState,String>> path = bmc.find(B,mid) ;
	             if (path != null) {
	                 upbound = mid ;
	                 bestpath = path ;
	             }
	             else {
	                 if(mid==lowbound) {
	                    upbound = mid ;
	                 }
	                 else {
	                     lowbound = mid ;
	                 }
	             }
	         }
	         return bestpath ;
	     }

	private List<AbstractTestSequence> StateCoverage(EFSM efsm, Predicate<IExplorableState> goal) {
		
		List<AbstractTestSequence> abstestsuite = new ArrayList<AbstractTestSequence>() ;
		for(var st : efsm.getStates())
		{
				

			Predicate<IExplorableState> state_cover = state -> {
				InterfaceToIv4xrModelCheker.EFSMStateWrapper state_ = (InterfaceToIv4xrModelCheker.EFSMStateWrapper) state ;
				return state_.conf.getState().getId().equals(((EFSMState)st).getId()) ;
			} ;
			

		    	//Buchi model checking	
				BuchiModelChecker bmc = new BuchiModelChecker(new InterfaceToIv4xrModelCheker(efsm)) ;
				
				// invoke the MC:
				var starttime = System.currentTimeMillis() ;
				
				float duration = ((float) (System.currentTimeMillis() - starttime)) / 1000f ; 
				
				
				//Buchi
				Path<Pair<IExplorableState,String>> path = bmc.find(eventuallyeventually(state_cover,goal), 19) ; //min depth to have all states covered in random.simple. 
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
	
	static List<AbstractTestSequence> RandomSampling(List<AbstractTestSequence> Testsuite, int size) {
		
		Random r = new Random();
		List<AbstractTestSequence> Testsuite_New= new ArrayList<AbstractTestSequence>();

		int[] unique = r.ints(0, Testsuite.size()-1).distinct().limit(size).toArray();
		for(var u: unique)
		{
			Testsuite_New.add(Testsuite.get(u));
		}	
		return Testsuite_New;
		
	}
	static List<AbstractTestSequence> AdaptiveRandomSampling(List<AbstractTestSequence> Testsuite, int size) {
		
		Random r = new Random();
		List<AbstractTestSequence> Testsuite_New= new ArrayList<AbstractTestSequence>();
		//HashMap<Integer,AbstractTestSequence> candidate_set=new HashMap<Integer,AbstractTestSequence>();

		int rand1=r.nextInt(Testsuite.size()-1);
		Testsuite_New.add(Testsuite.get(rand1));
		//candidate_set.put( rand1,Testsuite.get(rand1));
		while(Testsuite_New.size()!=size)
		{
			
			int[] unique = r.ints(0, Testsuite.size()-1).distinct().filter(c->!Testsuite_New.equals(c)).limit(30).toArray();
			
			int candidate=-1;
			HashMap<Integer,Double> dismap=new HashMap<Integer,Double>();
			for(var u : unique)
			{
				JaroWinkler jar=new JaroWinkler();
				double totaldistance=0;
				double mindistance=Integer.MAX_VALUE;;
				double maxdistance=-1;
				
				for (var l : Testsuite_New)
				{

					totaldistance+=jar.distance(Testsuite.get(u).toString(), l.toString());
					
					
					  double distance=jar.distance(Testsuite.get(u).toString(), l.toString());
					  if(distance< mindistance) mindistance=distance;
					 
					 
					  
					 
				}
				
				dismap.put(u, (double) totaldistance);

				
			}

			 
			
			//Testsuite_New.add(Testsuite.get(candidate));
			/*
			 * if(additional!=-1) { JaroWinkler jar=new JaroWinkler(); double
			 * totaldistance=0; for (var l : Testsuite_New) {
			 * totaldistance+=jar.distance(Testsuite.get(additional).toString(),
			 * l.toString()); }
			 * 
			 * dismap.put(additional, (double) totaldistance); }
			 */
			
			
			
			 double maxdistance=dismap.values().stream().max(Double::compare).get();
			  
				
				  if(maxdistance!=0) { List <Integer>
				  maxindex=dismap.entrySet().stream().filter(c->c.getValue()==maxdistance )
				  .map(Map.Entry::getKey).collect(Collectors.toList());
				  Testsuite_New.add(Testsuite.get(maxindex.get(0)));
				 }
			  //candidate_set.put( maxindex.get(0),Testsuite.get(maxindex.get(0)));
			 					
			
		}
        
		//List<AbstractTestSequence> list = new ArrayList<AbstractTestSequence>(candidate_set.values());

		return Testsuite_New;
		
	}
	
	public static int nextIntInRangeButExclude(int start, int end, int... excludes){
		
		Random r = new Random();
		int rangeLength = end - start - excludes.length;
	    int randomInt = r.nextInt(rangeLength) + start;

	    for(int i = 0; i < excludes.length; i++) {
	        if(excludes[i] > randomInt) {
	            return randomInt;
	        }

	        randomInt++;
	    }

	    return randomInt;
	}
	
	
	Buchi eventuallyeventually(Predicate<IExplorableState> p, Predicate<IExplorableState> q) {
		Buchi buchi = new Buchi() ;
		buchi.withStates("S0","S1","accept") 
		.withInitialState("S0")
		.withNonOmegaAcceptance("accept")
		.withTransition("S0", "S0", "~p", S ->  ! p.test(S))
		.withTransition("S0", "S1", "p", S -> p.test(S))
	    .withTransition("S1", "S1", "~q", S -> !q.test(S))
	    .withTransition("S1", "accept", "q", S -> q.test(S));
		return buchi ;
	}
	
	
	Buchi eventuallyeventually(Predicate<IExplorableState> p,Predicate<IExplorableState> q, Predicate<IExplorableState> r) {
		
		
		Buchi buchi = new Buchi() ;
		buchi.withStates("S0","S1","S2","accept") 
		.withInitialState("S0")
		.withNonOmegaAcceptance("accept")
		.withTransition("S0", "S0", "~p", S ->  ! p.test(S))
		.withTransition("S0", "S1", "p", S -> p.test(S))
	    .withTransition("S1", "S2", "q", S -> q.test(S))
	    .withTransition("S2", "S2", "~r", S -> !r.test(S))
	    .withTransition("S2", "accept", "r", S -> r.test(S));
		return buchi ;
	}
	
}

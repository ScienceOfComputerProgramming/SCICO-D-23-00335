package eu.iv4xr.ux.pxmbt;

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

/**
 * @author sansari
 */
public class MCtest_Generationxxx {

	// use a logger to save output execution information
	protected static final Logger logger = LoggerFactory.getLogger(Main.class);	
	protected List <EFSMState> goalstates = new ArrayList<EFSMState>();
	protected List<AbstractTestSequence> absTestsuite=null;
	private BuchiModelChecker bmc;
	


	
	//run a test for test generation using MC developed by Wishnu
	//Saba: right now it is written for one single goal but later on can be upgraded 
	//since I already included list of goalstates and absTestsuite --->just path_ needs to be replaced by list of path.
	//@Test
	public void runMCGenerationTest(EFSM efsm) {
	

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
		
		  List<AbstractTestSequence>  absTestsuite_Rand= eu.iv4xr.ux.pxmbt.RandomSampling.RandomSampling(absTestsuite, 10	);
		  List<AbstractTestSequence> absTestsuite_Subset=  RandomSampling.AdaptiveRandomSampling(absTestsuite, 10  );
		  
		  
			
			  Distance dis=new Distance("jaro-winkler"); double jarodistance=
			  dis.distance(absTestsuite_Rand);
			  System.out.println("Rand-testsuite size is: "+ absTestsuite_Rand.size());
			  System.out.println( " Rand Distance: "+jarodistance);
			  
			  jarodistance= dis.distance(absTestsuite_Subset);
			  System.out.println("Sub-testsuite size is: "+ absTestsuite_Subset.size());
			  System.out.println( "  Jaro Distance is "+jarodistance); jarodistance=
			  dis.distance(absTestsuite);
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
	
	//@AfterEach 
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
	
	/**
	 * Convert an execution/test-case produced by the model checker to an abtract test case
	 * (an instance of {@link AbstractTestSequence}.
	 */
	private AbstractTestSequence fromMCTestCase_toAbsTestCase(Path<Pair<IExplorableState,String>> path) {
		
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
		
		return absTestSeq ;
	}
	
	private Predicate<IExplorableState> atState(String stateId) {
		Predicate<IExplorableState> P = state -> {
			InterfaceToIv4xrModelCheker.EFSMStateWrapper state_ = (InterfaceToIv4xrModelCheker.EFSMStateWrapper) state ;
			return state_.conf.getState().getId().equals(stateId) ;
		} ;
		return P ;
	}
	
	private boolean coverState(AbstractTestSequence seq, String stateId) {
		return seq.getPath().getStates().stream().anyMatch(st -> st.getId().equals(stateId)) ;
	}
	
	private boolean coverTransition(AbstractTestSequence seq, 
			String srcStateId,
			String destinationStateId) {
		return seq.getPath().getTransitions().stream()
				.anyMatch(t -> 
					t.getSrc().getId().equals(srcStateId)
					&& t.getTgt().getId().equals(destinationStateId)) ;
	}

	public List<AbstractTestSequence> generateWithTransitionCoverage(EFSM efsm,
			boolean useCompleteBoundedDSFMode,
			boolean useBitHashingMode,
			int maxDepth,
			String idFinalState) {
		
		//Buchi model checking	
		BuchiModelChecker bmc = new BuchiModelChecker(new InterfaceToIv4xrModelCheker(efsm)) ;
		bmc.completeBoundedDSFMode = useCompleteBoundedDSFMode ;
		bmc.useHashInsteadOfExplicitState = useBitHashingMode ;
				
		List<AbstractTestSequence> abstestsuite = new ArrayList<AbstractTestSequence>() ;
		// all transitions to cover:
		List<Pair<String,String>> transitions = efsm.getTransitons().stream()
		    .map(tr -> new Pair<String,String>(tr.getSrc().getId(), tr.getTgt().getId()))
		    .collect(Collectors.toList()) ;
		
		var starttime = System.currentTimeMillis() ;
		//System.out.println(">>> ") ;
		while (!transitions.isEmpty()) {
				
			var tr = transitions.remove(0) ;
			String st0 = tr.fst ;
			String st1 = tr.snd ;
			
			LTL<IExplorableState> do_tr = LTL.ltlAnd(
					LTL.now(atState(st0)), 
					LTL.next(LTL.now(atState(st1)))) ;
					
			LTL<IExplorableState> phi = LTL.eventually(do_tr) ;
			if (idFinalState != null) {
				phi = LTL.eventually(LTL.ltlAnd(
								do_tr, 
							    LTL.eventually(atState(idFinalState))
							)) ;
			}
				
			// Invoke MC to find a solving execution:
			Path<Pair<IExplorableState,String>> path = bmc.find(phi, maxDepth) ; 
			
			if (path == null) {
				System.out.print("x");
				continue ;
			}
			System.out.print(".");
			
			var tc = fromMCTestCase_toAbsTestCase(path) ;
			abstestsuite.add(tc) ;
			
			// remove target-states that are accidentally covered:
			transitions.removeIf(aState -> coverTransition(tc,st0,st1)) ;	
		}
		// check again which states are left uncovered:
		
		transitions = efsm.getTransitons().stream()
			    .map(tr -> new Pair<String,String>(tr.getSrc().getId(), tr.getTgt().getId()))
			    .collect(Collectors.toList()) ;
				
		int N = transitions.size() ;
		List<Pair<String,String>> covered = transitions.stream()
				.filter(tr -> abstestsuite.stream().anyMatch(tc -> coverTransition(tc,tr.fst,tr.snd)))
				.collect(Collectors.toList()) ;
		List<Pair<String,String>> uncovered = new LinkedList<>() ;
		if (covered.size() < N) {
			transitions.removeIf(tr -> covered.contains(tr)) ;
			uncovered = transitions ;
		}
		float duration = ((float) (System.currentTimeMillis() - starttime)) / 1000f ; 
		
		System.out.println(">>>") ;
		System.out.println("** #abs-transitions in efsm: " + N) ;
		System.out.println("** #covered transitions  : " + covered.size()) ;
		System.out.println("** #uncovered transitions: " + uncovered.size()) ;
		System.out.println(">>> runtime(s): " + duration) ;	
		return abstestsuite;	
	}
	
	public List<AbstractTestSequence> generateWithStateCoverage(EFSM efsm,
			boolean useCompleteBoundedDSFMode,
			boolean useBitHashingMode,
			int maxDepth,
			String idFinalState) {
		
		
		//Buchi model checking	
		BuchiModelChecker bmc = new BuchiModelChecker(new InterfaceToIv4xrModelCheker(efsm)) ;
		bmc.completeBoundedDSFMode = useCompleteBoundedDSFMode ;
		bmc.useHashInsteadOfExplicitState = useBitHashingMode ;
		
		List<AbstractTestSequence> abstestsuite = new ArrayList<AbstractTestSequence>() ;
		
		// all the states to cover:
		List<String> states =  efsm.getStates().stream().map(st -> st.getId()).collect(Collectors.toList()) ;
			
		// for every abstract-state in the EFSM:
		var starttime = System.currentTimeMillis() ;
		//System.out.println(">>> ") ;
		while (!states.isEmpty()) {
				
			String st = states.remove(0) ;
			
			LTL<IExplorableState> at_st = LTL.now(atState(st)) ;
			LTL<IExplorableState> phi = LTL.eventually(at_st) ;
			if (idFinalState != null) {
				phi = LTL.eventually(LTL.ltlAnd(
								at_st, 
							    LTL.eventually(atState(idFinalState))
							)) ;
			}
				
			// Invoke MC to find a solving execution:
			Path<Pair<IExplorableState,String>> path = bmc.find(phi, maxDepth) ; 
			
			if (path == null) {
				System.out.print("x");
				continue ;
			}
			System.out.print(".");
			
			var tc = fromMCTestCase_toAbsTestCase(path) ;
			abstestsuite.add(tc) ;
			
			// remove target-states that are accidentally covered:
			states.removeIf(aState -> coverState(tc,aState)) ;	
		}
		// check again which states are left uncovered:
		states =  efsm.getStates().stream().map(st -> st.getId()).collect(Collectors.toList()) ;
		int N = states.size() ;
		List<String> covered = states.stream()
				.filter(st -> abstestsuite.stream().anyMatch(tc -> coverState(tc,st)))
				.collect(Collectors.toList()) ;
		List<String> uncovered = new LinkedList<>() ;
		if (covered.size() < N) {
			states.removeIf(st -> covered.contains(st)) ;
			uncovered = states ;
		}
		float duration = ((float) (System.currentTimeMillis() - starttime)) / 1000f ; 
		
		System.out.println(">>>") ;
		System.out.println("** #abs-states in efsm: " + N) ;
		System.out.println("** #covered states: " + covered.size()) ;
		System.out.println("** #uncovered states: " + uncovered.size()) ;
		System.out.println(">>> runtime(s): " + duration) ;
		
		return abstestsuite;
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

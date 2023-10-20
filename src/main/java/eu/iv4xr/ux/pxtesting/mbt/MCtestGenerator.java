package eu.iv4xr.ux.pxtesting.mbt;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


import eu.fbk.iv4xr.mbt.efsm.EFSM;
import eu.fbk.iv4xr.mbt.efsm.EFSMTransition;
import eu.fbk.iv4xr.mbt.efsm.modelcheckingInterface.InterfaceToIv4xrModelCheker;
import eu.fbk.iv4xr.mbt.efsm.modelcheckingInterface.InterfaceToIv4xrModelCheker.EFSMTransitionWrapper;
import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import eu.iv4xr.framework.extensions.ltl.BuchiModelChecker;
import eu.iv4xr.framework.extensions.ltl.IExplorableState;
import eu.iv4xr.framework.extensions.ltl.ITransition;
import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.ltl.BasicModelChecker.Path;

import nl.uu.cs.aplib.utils.Pair;


/**
 * Provide functions to generate test cases from an EFSM model using
 * model checking.
 * 
 * @author sansari
 */
public class MCtestGenerator {

	/**
	 * Convert an execution/test-case produced by the model checker to an abstract test case
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
	
	/**
	 * Check if a test-case passes a state with the given id.
	 */
	public static boolean coverState(AbstractTestSequence seq, String stateId) {
		return seq.getPath().getStates().stream().anyMatch(st -> st.getId().equals(stateId)) ;
	}
	
	/**
	 * Check if a test-case passes a transition from a state s1 to s2. The states are 
	 * specified by their ids.
	 */
	public static boolean coverTransition(AbstractTestSequence seq, 
			String srcStateId,
			String destinationStateId) {
		return seq.getPath().getTransitions().stream()
				.anyMatch(t -> 
					t.getSrc().getId().equals(srcStateId)
					&& t.getTgt().getId().equals(destinationStateId)) ;
	}

	/**
	 * Generate test-cases from an EFSM model, aiming at transition coverage.
	 * 
	 * @param useCompleteBoundedDSFMode
	 * @param useBitHashingMode
	 * @param maxDepth
	 * @param idFinalState
	 * @return a set of abstract test-cases.
	 */
	@SuppressWarnings("unchecked")
	public List<AbstractTestSequence> generateWithTransitionCoverage(EFSM efsm,
			boolean useCompleteBoundedDSFMode,
			boolean useBitHashingMode,
			boolean minimizeLength,
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
			Path<Pair<IExplorableState,String>> path = null ;
			if (minimizeLength)
				path = bmc.findShortest(phi, maxDepth) ; 
			else
				path = bmc.find(phi, maxDepth) ; 
			
			if (path == null) {
				System.out.print("x");
				continue ;
			}
			System.out.print(".");
			
			var tc = fromMCTestCase_toAbsTestCase(path) ;
			abstestsuite.add(tc) ;
			
			// remove target-states that are accidentally covered:
			transitions.removeIf(tr2 -> coverTransition(tc,tr2.fst,tr2.snd)) ;	
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
		//if (uncovered.size()>0) {
		//	for (var tr : uncovered) {
		//		System.out.println("      " + tr) ;
		//	}
		//}
		System.out.println(">>> runtime(s): " + duration) ;	
		return abstestsuite;	
	}
	
	/**
	 * Generate test-cases from an EFSM model, aiming at state coverage.
	 * 
	 * @param useCompleteBoundedDSFMode
	 * @param useBitHashingMode
	 * @param maxDepth
	 * @param idFinalState
	 * @return a set of abstract test-cases.
	 */
	@SuppressWarnings("unchecked")
	public List<AbstractTestSequence> generateWithStateCoverage(EFSM efsm,
			boolean useCompleteBoundedDSFMode,
			boolean useBitHashingMode,
			boolean minimizeLength,
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
			Path<Pair<IExplorableState,String>> path = null ;
			if (minimizeLength)
				path = bmc.findShortest(phi, maxDepth) ; 
			else
				path = bmc.find(phi, maxDepth) ; 
			
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
	
}

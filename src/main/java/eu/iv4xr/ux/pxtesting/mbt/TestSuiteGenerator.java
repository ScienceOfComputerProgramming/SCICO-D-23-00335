package eu.iv4xr.ux.pxtesting.mbt;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import eu.fbk.iv4xr.mbt.MBTProperties;
import eu.fbk.iv4xr.mbt.MBTProperties.Algorithm;
import eu.fbk.iv4xr.mbt.MBTProperties.ModelCriterion;
import eu.fbk.iv4xr.mbt.efsm.EFSM;
import eu.fbk.iv4xr.mbt.efsm.EFSMProvider;
import eu.fbk.iv4xr.mbt.strategy.SearchBasedStrategy;
import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import eu.fbk.iv4xr.mbt.testcase.MBTChromosome;
import eu.fbk.iv4xr.mbt.testsuite.SuiteChromosome;
import nl.uu.cs.aplib.utils.Pair;

/**
 * Generate an abstract test suite from an EFSM. The generator keeps the test suite
 * generated so far. Each time the generate-method is invoked, the produced suite is
 * added to the one currently kept by the generator. We can use either a search-based
 * algorithm or a model checking algorithm to generate the suite. Random sampling 
 * id also available to reduce the test suite to a subset of it.
 */
public class TestSuiteGenerator {
	
	/**
	 * The name of the class that acts as the efsm-provider.
	 */	
	public String efsmProvider ;
	
	EFSM efsm ;
	
	/**
	 * Id of the final-state, if any. If this is specified, then all test-cases
	 * must end in this state. 
	 */
	public String idFinalState = null ;
	
	/**
	 * Note: when {@link #idFinalState} is specified, EvoMBT currently does not
	 * support state-coverage in combination with a final-state constraint, so we
	 * will default to transition-coverage with final-state constraint. The model
	 * checker has no such issue.
	 */
	public static final String STATE_COV = "STATE_COV" ;
	public static final String TRANSITION_COV = "TRANSITION_COV" ;
	
	/**
	 * Specify the aimed coverage. Currently only state and transition
	 * coverage is supported. The default is transition-coverage.
	 */
	public String aimedCoverage = TRANSITION_COV ;
	
	/***
	 * The current test suite held by this generator.
	 */
	List<AbstractTestSequence> testSuite = new LinkedList<>() ;
	
	/**
	 * The ids of all states in the target EFSM.
	 */
	List<String> states  ;
	
	
	/**
	 * The transitions in the target EFSM. Each is stored as a pair (s,d)
	 * of the ids of the source and destination node/state.
	 */
	List<Pair<String,String>> transitions ;
	
	/**
	 * Specify the fully-qualified name of the class that acts as the efsm-provider (it
	 * implements the interface {@link EFSMProvider}.
	 * @param efsmLoader
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TestSuiteGenerator(String efsmProvider) throws Exception {
		this.efsmProvider = efsmProvider ;
		MBTProperties.SUT_EFSM = efsmProvider;
		
		// we will also load the EFSM, which is convenient (EvoMBT will still
		// load through the efsmProvider though)
		Class efmLoaderClass = Class.forName(efsmProvider) ;
		Constructor cz = efmLoaderClass.getConstructor() ;
		var loader = (EFSMProvider) cz.newInstance() ;
		efsm = loader.getModel() ;
		
		states =  efsm.getStates().stream()
				.map(st -> st.getId()).collect(Collectors.toList()) ;
		
		transitions = efsm.getTransitons().stream()
				.map(tr -> new Pair<String,String>(tr.getSrc().getId(), tr.getTgt().getId()))
				.collect(Collectors.toList()) ;
	}
	
	/**
	 * Return the test suite currently held by this generator. Note that this
	 * returns a direct reference to the suite (no cloning is performed).
	 */
	public List<AbstractTestSequence> getTestSuite() {
		return testSuite ;
	}
	
	/**
	 * Clear the currently held test-suite and copy all test cases in the given
	 * suite to the suite managed by this generator.
	 */
	public void setTestSuite(List<AbstractTestSequence> suite) {
		clear() ;
		testSuite.addAll(suite) ;
	}
	
	
	void configureEvoMBT(int searchBudget, Integer maxTestCaseLength) {

		MBTProperties.SEARCH_BUDGET = searchBudget ;
		
		if (idFinalState != null) {
			MBTProperties.STATE_TARGET = idFinalState ;
			// defaulting to transition-cov with final state constraint:
			MBTProperties.MODELCRITERION = new ModelCriterion[] { ModelCriterion.TRANSITION_FIX_END_STATE } ;
			MBTProperties.TEST_FACTORY = MBTProperties.TestFactory.RANDOM_LENGTH_FIX_TARGET;
		}
		else {	
			if (aimedCoverage.equals(TRANSITION_COV))
				MBTProperties.MODELCRITERION = new ModelCriterion[] { ModelCriterion.TRANSITION };	
			else {
				MBTProperties.MODELCRITERION = new ModelCriterion[] { ModelCriterion.STATE };	
			}
			MBTProperties.TEST_FACTORY = MBTProperties.TestFactory.RANDOM_LENGTH ;
		}
		// which algorithm to use:
		MBTProperties.ALGORITHM = Algorithm.MOSA;
		// This needs a later version of MBT:
		MBTProperties.MINIMIZE_SUITE = true ;
		MBTProperties.SHOW_PROGRESS = true;
		
		
		//MBTProperties.TEST_FACTORY = MBTProperties.TestFactory.RANDOM_LENGTH_FIX_TARGET;
		if (maxTestCaseLength != null && maxTestCaseLength > 0) {
			MBTProperties.MAX_LENGTH = maxTestCaseLength ; 	
		}
		else {
			MBTProperties.MAX_LENGTH = 100; 		
		}
	}
	
	/**
	 * Clear the test suite currently held by this generator.
	 */
	public void clear() {
		testSuite.clear(); 
	}
	
	/**
	 * Generate a test suite using a search-based testing algorithm. The suite
	 * is added to the current suite held by this generator. You can obtain the
	 * resulting test suite using {@link #getTestSuite()}.
	 * 
	 * @param searchBudget
	 * @param maxTestCaseLength
	 */
	@SuppressWarnings("rawtypes")
	public void generateWithSBT(int searchBudget, Integer maxTestCaseLength) {
		
		configureEvoMBT(searchBudget,maxTestCaseLength) ;
		
		SearchBasedStrategy sbStrategy = new SearchBasedStrategy<>();
		SuiteChromosome generatedTests = sbStrategy.generateTests();
		
		for (var tc : generatedTests.getTestChromosomes()) {
			testSuite.add((AbstractTestSequence) tc.getTestcase()) ;	
		}
	}
	
	/**
	 * Generate a test suite using a model checking algorithm. The suite
	 * is added to the current suite held by this generator. You can obtain the
	 * resulting test suite using {@link #getTestSuite()}.
	 * 
	 * @param useCompleteBoundedDSFMode
	 * @param useBitHashingMode
	 * @param minimizeLength
	 * @param maxDepth
	 * @throws Exception
	 */
	public void generateWithMC(boolean useCompleteBoundedDSFMode,
			boolean useBitHashingMode,
			boolean minimizeLength,
			int maxDepth
			){
		
		var mcgen = new MCtestGenerator() ;
		List<AbstractTestSequence> suite = null ;
		if (aimedCoverage.equals(TRANSITION_COV)) {
			suite = mcgen.generateWithTransitionCoverage(efsm, 
					useCompleteBoundedDSFMode, 
					useBitHashingMode, 
					minimizeLength, 
					maxDepth, idFinalState) ;
		}
		else {
			suite = mcgen.generateWithStateCoverage(efsm, 
					useCompleteBoundedDSFMode, 
					useBitHashingMode, 
					minimizeLength, 
					maxDepth, idFinalState) ;
		}
		testSuite.addAll(suite) ;	
	}
	
	/**
	 * Obtain a subset of the specified size of the test suite currently held by this
	 * generator. The current suite will then be replaced by the subset, The sampler
	 * uses an adaptive random selection algorithm.
	 * 
	 * @param k
	 * @param size
	 */
	public void applySampling(int k, int size) {
		var sampler = new RandomSampling() ;
		var suite = sampler.adaptiveRandomSampling(testSuite, k, size) ;
		testSuite.clear();
		testSuite.addAll(suite) ;
	}
	
	/**
	 * Print some statistics of the current test-suite held by this generator.
	 */
	public void printStats() {
		AbstractTestUtils.printStats(testSuite) ;
		int nState = states.size() ;
		int covState = states.size() - getUncoveredStates().size() ;
		float coverageState = (float) covState / (float) nState ;
		int nTrans = transitions.size() ;
		int covTrans = nTrans - getUncoveredTransitions().size() ;
		float coverageTrans = (float) covTrans / (float) nTrans ;
		System.out.println("**   covered-state     = " + covState + " (" + coverageState + ")") ;
		System.out.println("**   covered-trans     = " + covTrans + " (" + coverageTrans + ")") ;
		if (idFinalState != null)
			System.out.println("**   #tc passing final = " + countTestCasesThatCoverFinalState()) ;
	}
	
	public List<String> getUncoveredStates() {
		List<String> notCovered = states.stream()
				.filter(st -> testSuite.stream().allMatch(tc -> ! MCtestGenerator.coverState(tc,st)))
				.collect(Collectors.toList()) ;
		return notCovered ;
	}
	
	public List<Pair<String,String>> getUncoveredTransitions() {
		List<Pair<String,String>> notCovered = transitions.stream()
				.filter(tr -> testSuite.stream().allMatch(tc -> ! MCtestGenerator.coverTransition(tc,tr.fst,tr.snd)))
				.collect(Collectors.toList()) ;
		return notCovered ;
	}
	
	public int countTestCasesThatCoverFinalState() {
		if (idFinalState == null) {
			return testSuite.size() ;
		}
		int n = (int) testSuite.stream().filter(tc -> MCtestGenerator.coverState(tc,idFinalState)).count() ;
		return n ;
	}
	
	public void save(String dir, String basefileName) throws IOException {
		AbstractTestUtils.save(dir, basefileName, testSuite);
	}
	
	/**
	 * Just for some quick testing ...
	 */
	public static void main(String[] args) throws Exception {
		var gen = new TestSuiteGenerator("eu.iv4xr.ux.pxmbt.EFSMSimple0") ;
		gen.idFinalState = "b3" ;
		gen.generateWithSBT(60,null) ;
		gen.printStats();
		gen.generateWithMC(false, false, false, 20);
		gen.printStats();
	}
	
}

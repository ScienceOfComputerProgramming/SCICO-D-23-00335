package eu.iv4xr.ux.pxmbt;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import eu.fbk.iv4xr.mbt.MBTProperties;
import eu.fbk.iv4xr.mbt.MBTProperties.Algorithm;
import eu.fbk.iv4xr.mbt.MBTProperties.ModelCriterion;
import eu.fbk.iv4xr.mbt.efsm.EFSM;
import eu.fbk.iv4xr.mbt.efsm.EFSMProvider;
import eu.fbk.iv4xr.mbt.strategy.SearchBasedStrategy;
import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import eu.fbk.iv4xr.mbt.testcase.MBTChromosome;
import eu.fbk.iv4xr.mbt.testsuite.SuiteChromosome;

/**
 * Generate an abstract test suite from an EFSM.
 */
public class TestSuiteGenerator {
	
	/**
	 * The name of the class that acts as the efsm-provider.
	 */	
	public String efsmProvider ;
	
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
	
	public String aimedCoverage = TRANSITION_COV ;
	
	/***
	 * The current test suite held by this generator.
	 */
	List<AbstractTestSequence> testSuite = new LinkedList<>() ;
	
	/**
	 * Return the test suite currently held by this generator. Note that this
	 * returns a direct reference to the suite (no cloning is performed).
	 */
	public List<AbstractTestSequence> getTestSuite() {
		return testSuite ;
	}
	
	/**
	 * Specify the fully-qualified name of the class that acts as the efsm-provider (it
	 * implements the interface {@link EFSMProvider}.
	 * @param efsmLoader
	 */
	public TestSuiteGenerator(String efsmProvider) {
		this.efsmProvider = efsmProvider ;
		MBTProperties.SUT_EFSM = efsmProvider;
	}
	
	void configureEvoMBT(int searchBudget, Integer maxTestCaseLength) {

		MBTProperties.SEARCH_BUDGET = searchBudget ;
		
		if (idFinalState != null) {
			MBTProperties.STATE_TARGET = idFinalState ;
			// defaulting to transition-cov with final state constraint:
			MBTProperties.MODELCRITERION = new ModelCriterion[] { ModelCriterion.TRANSITION_FIX_END_STATE } ;
		}
		else {	
			if (aimedCoverage.equals(TRANSITION_COV))
				MBTProperties.MODELCRITERION = new ModelCriterion[] { ModelCriterion.TRANSITION };	
			else {
				MBTProperties.MODELCRITERION = new ModelCriterion[] { ModelCriterion.STATE };	
			}
		}
		// which algorithm to use:
		MBTProperties.ALGORITHM = Algorithm.MOSA;
		// This needs a later version of MBT:
		MBTProperties.MINIMIZE_SUITE = true ;
		MBTProperties.SHOW_PROGRESS = true;
		
		
		MBTProperties.TEST_FACTORY = MBTProperties.TestFactory.RANDOM_LENGTH_FIX_TARGET;
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
			) throws Exception {
		
		Class efmLoaderClass = Class.forName(efsmProvider) ;
		Constructor cz = efmLoaderClass.getConstructor() ;
		var loader = (EFSMProvider) cz.newInstance() ;
		EFSM efsm = loader.getModel() ;
		
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
	
	public void printStats() {
		AbstractTestUtils.printStats(testSuite) ;
	}
	
	public static void main(String[] args) throws Exception {
		var sbt = new TestSuiteGenerator("eu.iv4xr.ux.pxmbt.EFSMSimple0") ;
		sbt.idFinalState = "b3" ;
		sbt.generateWithSBT(60,null) ;
		sbt.printStats();
		sbt.generateWithMC(false, false, false, 20);
		sbt.printStats();
	}
	
	

}

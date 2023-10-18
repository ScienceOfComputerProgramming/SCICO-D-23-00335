package eu.iv4xr.ux.pxmbt;

import java.util.LinkedList;
import java.util.List;

import eu.fbk.iv4xr.mbt.MBTProperties;
import eu.fbk.iv4xr.mbt.MBTProperties.Algorithm;
import eu.fbk.iv4xr.mbt.MBTProperties.ModelCriterion;
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
	
	public Integer maxTestCaseLength = null ;
	
	/**
	 * Note: when {@link #idFinalState} is specified, EvoMBT currently does not
	 * support state-coverage in combination with a final-state constraint, so we
	 * will default to transition-coverage with final-state constraint. The model
	 * checker has no such issue.
	 */
	public static final String STATE_COV = "STATE_COV" ;
	public static final String TRANSITION_COV = "TRANSITION_COV" ;
	
	public String aimedCoverage = TRANSITION_COV ;
	
	/**
	 * Specify the fully-qualified name of the class that acts as the efsm-provider (it
	 * implements the interface {@link EFSMProvider}.
	 * @param efsmLoader
	 */
	public TestSuiteGenerator(String efsmProvider) {
		this.efsmProvider = efsmProvider ;
		MBTProperties.SUT_EFSM = efsmProvider;
	}
	
	public void configureEvoMBT(int searchBudget) {

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
		if (maxTestCaseLength != null) {
			MBTProperties.MAX_LENGTH = maxTestCaseLength ; 	
		}
		else {
			MBTProperties.MAX_LENGTH = 100; 		
		}
	}
	
	public List<AbstractTestSequence> generate() {
		
		SearchBasedStrategy sbStrategy = new SearchBasedStrategy<>();
		SuiteChromosome generatedTests = sbStrategy.generateTests();
		
		List<AbstractTestSequence> suite = new LinkedList<>() ;
		
		for (var tc : generatedTests.getTestChromosomes()) {
			
			suite.add((AbstractTestSequence) tc.getTestcase()) ;
			
		}
		
		return suite ;
	}
	
	public void genMC() {
		
	}
	
	public static void main(String[] args) {
		var sbt = new TestSuiteGenerator("eu.iv4xr.ux.pxmbt.EFSMSimple0") ;
		sbt.idFinalState = "b3" ;
		sbt.configureEvoMBT(60);
		var suite = sbt.generate() ;
		AbstractTestUtils.printStats(suite);

	}
	
	

}

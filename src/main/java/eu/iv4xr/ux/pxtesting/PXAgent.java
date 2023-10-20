package eu.iv4xr.ux.pxtesting;

import static eu.iv4xr.ux.pxtesting.study.minidungeon.MiniDungeonPlayerCharacterization.shrineCleansed;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;

import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.occ.Goal;
import eu.iv4xr.framework.extensions.occ.Iv4xrOCCEngine;
import eu.iv4xr.framework.mainConcepts.EmotiveTestAgent;
import eu.iv4xr.framework.mainConcepts.Iv4xrAgentState;
import eu.iv4xr.framework.mainConcepts.SyntheticEventsProducer;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.ux.pxtesting.occ.OCCState;
import eu.iv4xr.ux.pxtesting.occ.XUserCharacterization;
import eu.iv4xr.ux.pxtesting.study.minidungeon.MiniDungeonEventsProducer;
import eu.iv4xr.ux.pxtesting.study.minidungeon.MiniDungeonPlayerCharacterization;
import nl.uu.cs.aplib.Logging;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MyAgentEnv;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MyAgentState;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.Utils;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
//import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.utils.Pair;

public class PXAgent {
	
	/**
	 * The test agent that will execute the test cases.
	 */
	public EmotiveTestAgent agent ;
	public Iv4xrOCCEngine occ ;
	
	/**
	 * Function to convert a given abstract test-case to an instance
	 * of {@link GoalStructure}, which would then be executable by
	 * the test {@link PXAgent#agent}.
	 */
	Function<AbstractTestSequence,GoalStructure> concretizationFunction ;
	
	/**
	 * This function is called at every agent update-cycle to sample the 
	 * agent state to produce a vector of numeric values that summerize
	 * the state. At the end of the execution of every test case, the
	 * collected values will be saved in a trace file for off-line analyses.
	 * You can provide a custom state-instrumenter, else a default instrumenter
	 * will be used, which only record the agent's emotional state. If
	 * the agent's state is an instance of {@link Iv4xrAgentState}, the
	 * default instrumenter will also record time stamps and the agent's
	 * xyz positions.
	 */
	Function<SimpleState,Pair<String,Number>[]> stateInstrumenter ;
	
	Pair<Goal,Integer>[] goals ;
	XUserCharacterization playerCharacterization ;
	
	void resetEmotionState() {
		// setting of OCC-extension:
		occ = new Iv4xrOCCEngine(agent.getId()) 
						. attachToEmotiveTestAgent(agent) 
						. withUserModel(playerCharacterization) ;
				
		for(var g : goals) {
			// add every goal and its initial likelihood
			occ.addGoal(g.fst, g.snd) ;
		}
				
		// generate initial emotion:
		occ.addInitialEmotions();
	}
	
	/**
	 * Configure a test-agent to be a PX-test-agent. We need a agent that is already
	 * equipped with a state and a reference to an instance of {@link nl.uu.cs.aplib.mainConcepts.Environment}
	 * (which is an interface to the game-under-test).
	 */
	public PXAgent(EmotiveTestAgent agent,
			XUserCharacterization playerCharacterization,
			SyntheticEventsProducer eventsProducer,
			Function<AbstractTestSequence,GoalStructure> concretizationFunction,
			Function<SimpleState,Pair<String,Number>[]> customStateInstrumenter,
			Pair<Goal,Integer> ... goals
			) {
		
		if (agent.state() == null) {
			throw new IllegalArgumentException("agent.state() is null. Need an agent that has a state.") ;
		}
		if (agent.env() == null) {
			throw new IllegalArgumentException("agent.env() is null. Need an agent that has a reference to an Environment.") ;
			
		}
		if (! (agent.state() instanceof Iv4xrAgentState)) {
			Logging.getAPLIBlogger().log(Level.WARNING, "The state of the emotive agent " 
					+ agent.getId() 
					+ " is not an instance of Iv4xrAgentState. You may need to use a custom instrumenter.") ;
		}
		
		// setting-up events-producer:
		agent.attachSyntheticEventsProducer(eventsProducer) ;
		
		// setting-up a state instrumenter:
		if (customStateInstrumenter != null) {
			stateInstrumenter = customStateInstrumenter ;
		}
		else {
			if ((agent.state() instanceof Iv4xrAgentState)) {
				stateInstrumenter = S -> minimalStateInstrumenter((Iv4xrAgentState) S) ;
			}
			else {
				stateInstrumenter = S -> simplestStateInstrumenter(S) ;
			}
		}

		this.playerCharacterization = playerCharacterization ;
		this.goals = goals ;
		this.concretizationFunction = concretizationFunction ;
			
	}
	
	Set<String> getOCCgoals() {
		return occ.beliefbase.getGoalsStatus().statuses.keySet() ;
	}
	
	@SuppressWarnings("unchecked")
	Pair<String,Number>[] simplestStateInstrumenter(SimpleState S) {
		var E = (OCCState) agent.getEmotionState() ;
		var goals = getOCCgoals() ;
		int NumGoals = goals.size() ;
		Pair<String,Number>[] emotions = new Pair[6*NumGoals] ;
		
		int k = 0 ;
		for(var g : goals) {
			int j = k*NumGoals ;
			emotions[j] = new Pair<String,Number>("hope_" + g, E.hope(g)) ;
			emotions[j+1] = new Pair<String,Number>("joy_" + g, E.joy(g)) ;
			emotions[j+2] = new Pair<String,Number>("satisfaction_" + g , E.satisfaction(g)) ;
			emotions[j+3] = new Pair<String,Number>("fear_" + g, E.fear(g)) ;
			emotions[j+4] = new Pair<String,Number>("distress_" + g, E.distress(g)) ;
			emotions[j+5] = new Pair<String,Number>("disappointment_" + g, E.disappointment(g)) ;
			k++ ;
		}
		return emotions ;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	Pair<String,Number>[] minimalStateInstrumenter(Iv4xrAgentState S) {
		Pair<String,Number>[] emotions = simplestStateInstrumenter(S) ;
		Pair<String,Number>[] values = new Pair[emotions.length + 4] ;
		values[0] = new Pair<String,Number>("time", S.worldmodel.timestamp) ;
		values[1] = new Pair<String,Number>("x", S.worldmodel.position.x) ;
		values[2] = new Pair<String,Number>("y", S.worldmodel.position.y) ;
		values[3] = new Pair<String,Number>("z", S.worldmodel.position.z) ;
		
		for(int k=0; k<emotions.length; k++) {
			values[k+4] = emotions[k] ;
		}
		return values ;
	}
	
	public boolean printRunDebug = false ;

	
	public void run(List<Pair<String,AbstractTestSequence>> suite, 
			String saveDir,
			int budgetPerTesCase,
			int delayBetweenAgentUpdateCycles) throws InterruptedException {

		System.out.println("** About to execute a test-suite #=" + suite.size());

		agent.setTestDataCollector(new TestDataCollector()) ;
		agent.withScalarInstrumenter(stateInstrumenter) ;
		
		// iterate over every test-case in the suite:
		int tcCount = 0 ;
		for (var tc : suite) {
			String tc_name = tc.fst ;
			GoalStructure tcG = concretizationFunction.apply(tc.snd) ;
			this.resetEmotionState(); 
			// run the test-case tc:
			System.out.println("** Start executing tc " + tc_name + " (" + tcCount + ")");
			int k = 0;
			while (tcG.getStatus().inProgress()) {
				agent.update();
				k++ ;
				if (printRunDebug) {
					String info = ">> [" + k + "]"  ;
					System.out.println(info);
				}
				// delay to slow it a bit for displaying:
				if (delayBetweenAgentUpdateCycles>0)
					Thread.sleep(delayBetweenAgentUpdateCycles);
				k++ ;
				if (k > budgetPerTesCase) {
					System.out.println(">> budget exhausted; breaking execution.") ;
					break ;
				}
			}
			System.out.println(">> goal-status at the end: " + tcG.getStatus()) ;
			tcCount++ ;
		}
		
	}

}

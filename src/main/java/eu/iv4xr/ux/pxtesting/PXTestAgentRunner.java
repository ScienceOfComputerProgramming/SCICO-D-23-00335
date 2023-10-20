package eu.iv4xr.ux.pxtesting;

import static eu.iv4xr.ux.pxtesting.study.minidungeon.MiniDungeonPlayerCharacterization.shrineCleansed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import nl.uu.cs.aplib.mainConcepts.ProgressStatus;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
//import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.utils.Pair;

/**
 * A runner for executing abstract testsuite coming from MBT on an actual
 * game-under-test, via an aplib test-agent.
 */
public class PXTestAgentRunner {
	
	/**
	 * A function that can construct a test-agent. Each time this runner must
	 * run a test-case, it will invoke this function to get an instance of
	 * test-agent to run the test-case. This test-agent should already come
	 * with a state and a reference to the Environment (so, agent.state() and
	 * agent.env() should not return null).
	 */
	Function<Void,EmotiveTestAgent> agentConstructor ;

	
	/**
	 * Function to convert a given abstract test-case to an instance
	 * of {@link GoalStructure}, which would then be executable by
	 * the test {@link PXTestAgentRunner#agent}.
	 */
	Function<EmotiveTestAgent,Function<AbstractTestSequence,GoalStructure>> concretizationFunction ;
	
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
	Function<EmotiveTestAgent,Function<SimpleState,Pair<String,Number>[]>> customStateInstrumenter ;
	
	Pair<Goal,Integer>[] goals ;
	XUserCharacterization playerCharacterization ;
	SyntheticEventsProducer eventsProducer ;
	
	/**
	 * The agent prepped to execute the next test-case.
	 */
	EmotiveTestAgent currentAgent ;
	
	/**
	 * The occ-module hooked to {@link #currentAgent}.
	 */
	Iv4xrOCCEngine currentOcc ;
	
	
	
	/**
	 * Configure a test-agent to be a PX-test-agent. We need a agent that is already
	 * equipped with a state and a reference to an instance of {@link nl.uu.cs.aplib.mainConcepts.Environment}
	 * (which is an interface to the game-under-test).
	 */
	public PXTestAgentRunner(Function<Void,EmotiveTestAgent> agentConstructor,
			XUserCharacterization playerCharacterization,
			SyntheticEventsProducer eventsProducer,
			Function<EmotiveTestAgent,Function<AbstractTestSequence,GoalStructure>> concretizationFunction,
			Function<EmotiveTestAgent,Function<SimpleState,Pair<String,Number>[]>> customStateInstrumenter,
			Pair<Goal,Integer> ... goals
			) {
		this.agentConstructor = agentConstructor ;
		this.playerCharacterization = playerCharacterization ;
		this.eventsProducer = eventsProducer ;
		this.concretizationFunction = concretizationFunction ;
		this.customStateInstrumenter = customStateInstrumenter ;
		this.goals = goals ;			
	}
	
	Set<String> getOCCgoals() {
		return currentOcc.beliefbase.getGoalsStatus().statuses.keySet() ;
	}
	
	@SuppressWarnings("unchecked")
	Pair<String,Number>[] simplestStateInstrumenter(SimpleState S) {
		
		var E = (OCCState) currentAgent.getEmotionState() ;
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

	private boolean Iv4xrAgentStateWarningGiven = false ;
	
	private void prepareTheTestAgent() {
		
		// get an executing agent:
		currentAgent = agentConstructor.apply(null) ;
		
		// check if the agent has a state and a reference to an Environment:
		if (currentAgent.state() == null) {
			throw new IllegalArgumentException("agent.state() is null. Need an agent that has a state.") ;
		}
		if (currentAgent.env() == null) {
			throw new IllegalArgumentException("agent.env() is null. Need an agent that has a reference to an Environment.") ;	
		}
		if (!Iv4xrAgentStateWarningGiven && !(currentAgent.state() instanceof Iv4xrAgentState)) {
			Logging.getAPLIBlogger().log(Level.WARNING, "The state of the emotive agent " 
					+ currentAgent.getId() 
					+ " is not an instance of Iv4xrAgentState. You may need to use a custom state-instrumenter.") ;
			Iv4xrAgentStateWarningGiven = true ;
		}
		
		// Prepare the agent:
		currentAgent.attachSyntheticEventsProducer(eventsProducer) ;
		currentAgent.setTestDataCollector(new TestDataCollector()) ;

		currentOcc = new Iv4xrOCCEngine(currentAgent.getId()) 
				 . attachToEmotiveTestAgent(currentAgent) 
				 . withUserModel(playerCharacterization) ;
		
		for (var mentalGoal : goals) {
			int initialLikelihood = mentalGoal.snd ; // force-clone the int
			currentOcc.addGoal(mentalGoal.fst, initialLikelihood) ;
		}	
		currentOcc.addInitialEmotions();

		if (customStateInstrumenter != null) {
			currentAgent.withScalarInstrumenter(customStateInstrumenter.apply(currentAgent)) ;
		}
		else if(currentAgent.state() instanceof Iv4xrAgentState) {
			currentAgent.withScalarInstrumenter(S -> minimalStateInstrumenter((Iv4xrAgentState) S)) ;
		}
		else {
			currentAgent.withScalarInstrumenter(S -> simplestStateInstrumenter(S)) ;
		}
		
	}
	
	public void run(List<Pair<String,AbstractTestSequence>> suite, 
			String saveDir,
			int budgetPerTesCase,
			int delayBetweenAgentUpdateCycles) throws InterruptedException {

		System.out.println("** About to execute a test-suite #=" + suite.size());
		Map<String,ProgressStatus> tcsStatus = new HashMap<>() ;
		Map<String,Long> runtime = new HashMap<>() ;
		
		// iterate over every test-case in the suite:
		int tcCount = 0 ;
		for (var tc : suite) {
			
			prepareTheTestAgent() ;
			String tc_name = tc.fst ;
			GoalStructure tcG = concretizationFunction
					.apply(currentAgent)
					.apply(tc.snd) ;
			
			currentAgent.setGoal(tcG) ;

			// run the test-case tc:
			System.out.println("** Start executing tc " + tc_name + " (" + tcCount + ")");
			long time = System.currentTimeMillis() ;
			int k = 0;
			while (tcG.getStatus().inProgress()) {
				currentAgent.update();
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
			long duration = (System.currentTimeMillis() - time) ;
			tcsStatus.put(tc_name, tcG.getStatus()) ;
			runtime.put(tc_name, duration) ;
			System.out.println(">> goal-status at the end: " + tcG.getStatus()) ;
			tcCount++ ;
		}
		int numberOfSuccess = 0 ;
		int numberOfFail = 0 ;
		int numberOfTimeOut = 0 ;
		long totTime = 0 ;
		for (var tc : suite) {
			String tc_name = tc.fst ;
			long time = runtime.get(tc_name) ;
			ProgressStatus st = tcsStatus.get(tc_name) ;
			if (st.success()) numberOfSuccess++ ;
			if (st.failed()) numberOfFail++ ;
			if (st.inProgress()) numberOfTimeOut++ ;
			System.out.println("** tc " + tc_name +": "
					+ st + ", "
					+ time + "ms" ) ;
			totTime += time ;
		}
		System.out.println("** #success: " + numberOfSuccess) ;
		System.out.println("** #fail   : " + numberOfFail) ;
		System.out.println("** #timeout: " + numberOfTimeOut) ;
		System.out.println("** tot-time: " + totTime) ;
	}

}

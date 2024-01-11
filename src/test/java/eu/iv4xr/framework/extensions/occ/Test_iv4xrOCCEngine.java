package eu.iv4xr.framework.extensions.occ;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.mainConcepts.EmotiveTestAgent;
import eu.iv4xr.ux.pxtesting.study.minidungeon.MiniDungeonPlayerCharacterization;
import nl.uu.cs.aplib.agents.State;

public class Test_iv4xrOCCEngine {
	
	@Test
	public void test1() {
		
		Iv4xrOCCEngine occEngine = new Iv4xrOCCEngine("Alice") ;
		EmotiveTestAgent agent = new EmotiveTestAgent("Player","Alice_") ;
		State S = new State() ;
		agent.attachState(S) ;
		occEngine.attachToEmotiveTestAgent(agent) ;
		Goal g = new Goal("Winning this game") ;
		occEngine.addMentalGoal(g,1) ;
		var playerProfile = new MiniDungeonPlayerCharacterization() ;
		occEngine.withUserModel(playerProfile) ;
		
		assertTrue(occEngine.userModel == playerProfile) ;
		assertTrue(agent.getEmotionState() != null) ;
		assertTrue(occEngine.agentName.equals(agent.getId())) ;
		assertTrue(occEngine.beliefbase != null) ;
	}

}

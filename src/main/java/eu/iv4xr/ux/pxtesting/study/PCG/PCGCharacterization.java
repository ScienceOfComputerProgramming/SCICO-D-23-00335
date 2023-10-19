package eu.iv4xr.ux.pxtesting.study.PCG;

import static agents.EventsProducer.*;

import eu.fbk.iv4xr.mbt.efsm.EFSMState;
import eu.iv4xr.framework.extensions.occ.BeliefBase;
import eu.iv4xr.framework.extensions.occ.Emotion;
import eu.iv4xr.framework.extensions.occ.Event;
import eu.iv4xr.framework.extensions.occ.Goal;
import eu.iv4xr.framework.extensions.occ.GoalStatus;
import eu.iv4xr.framework.extensions.occ.UserCharacterization;
import eu.iv4xr.framework.extensions.occ.BeliefBase.Goals_Status;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.ux.pxtesting.study.labrecruits.PlayerOneCharacterization;
import eu.iv4xr.ux.pxtesting.study.labrecruits.PlayerOneCharacterization.EmotionBeliefBase;
import world.BeliefState;
import world.LabEntity;

/**
* @author sansari
*/

public class PCGCharacterization extends UserCharacterization {
	
	public static Goal questIsCompleted = new Goal("quest is completed").withSignificance(8) ;
	static String KeyDoor;


	public PCGCharacterization() {
		
	}
	public PCGCharacterization(String goalstate) {
		KeyDoor=goalstate.toString();
	}
	/**
	 *  we need to alter the keyDoor for every level.
	 */
	//static String KeyDoor = "door3" ; //SimplerExperiment_1
	//static String KeyDoor = "d1" ; //Experiment_2
	//static String KeyDoor="d_finish";  //Lab1

	static String GoalIten = "levelEnd" ;
	static int maxScore = 620 ; // 20 buttons, 2 goal-flags for SimpleExperiemnt_1   //need to be changed for everylevel!!!
	public int health;
	public int totalenemies;
	public int leftenemies;
	/**
	 * Modeling the effect of various non-tick events in terms of how they affect the goals.
	 */
	@Override
	public void eventEffect(Event e, BeliefBase beliefbase) {
	    
	    if (!(e instanceof PCGEvent)) {
	        return ;
	    }
		
		//EmotionBeliefBase bbs = (EmotionBeliefBase) beliefbase ;
		 PCGEvent e1=(PCGEvent)e;
		  health=e1.health;
		  totalenemies=e1.totalenemies;
		  leftenemies=e1.leftenemies;
		
		switch(e.name) {
		  case "getdamage"   : getdamage(beliefbase) ; break ;
		  case "getweapon" : getweapon(beliefbase) ; break ;
		  case "killenemy" : killenemy(beliefbase) ; break ;
		  case "clearroom" : clearroom(beliefbase) ; break ;
		  case "getkilled" : getkilled(beliefbase) ; break ;
		 // case "insight" : effectOfLevelCompletionInSightEvent(beliefbase) ; break ;

		}
	}
	
 //balance btw fear- distress-disappointment 
	private void getdamage(BeliefBase beliefbase) {
		EmotionBeliefBase bbs = (EmotionBeliefBase) beliefbase ;
		

		// updating belief on the quest-completed goal; if the health drops below 50,
		// decrease this goal likelihood by 3.
		// If the health drops to 0, game over. The goal is marked as failed.
		GoalStatus status = bbs.getGoalsStatus().goalStatus(questIsCompleted.name) ;
		status.likelihood = Math.max(0,status.likelihood - 10) ;
		if(status != null && health<50) {
			if(health <=0) {
				status.setAsFailed();
				status.likelihood=0;
			}
		}
		
		if(status != null && health<50) {
			status.likelihood = Math.max(0,status.likelihood - 5) ;
			if(health <=0) {
				status.setAsFailed();
			}
		}	
	}

	private void getweapon(BeliefBase beliefbase) {
		EmotionBeliefBase bbs = (EmotionBeliefBase) beliefbase ;

		GoalStatus status = bbs.getGoalsStatus().goalStatus(questIsCompleted.name) ;
		if(status != null) {
			status.likelihood = Math.min(80,status.likelihood + 5) ;

		}		
	}
	private void killenemy(BeliefBase beliefbase) {
		EmotionBeliefBase bbs = (EmotionBeliefBase) beliefbase ;

//		boolean finalDoorIsOpen = false ;
//		int numberOfDoorsMadeOpen =  0 ;
//		int numberOfDoorsMadeClosed = 0 ;
//
//		if (bbs.functionalstate.changedEntities != null) {
//			for(WorldEntity e : bbs.functionalstate.changedEntities) {
//				if(e.type == LabEntity.DOOR) {
//					if(e.getBooleanProperty("isOpen")) {
//						numberOfDoorsMadeOpen++ ;
////						if(e.id.equals(KeyDoor)) {
////							finalDoorIsOpen = true ;
////						}
//					}
//					else numberOfDoorsMadeClosed++ ;
//					
//				}	
//			}
//		}	

		// updating belief on the quest-completed goal
		GoalStatus status = bbs.getGoalsStatus().goalStatus(questIsCompleted.name) ;
		if(status != null) {
			status.likelihood = Math.min(80,status.likelihood + 10*(totalenemies - leftenemies)) ;
			if(leftenemies==0) {
				status.likelihood = 100 ;
			}
		}
	}
	private void getkilled(BeliefBase beliefbase) {
		EmotionBeliefBase bbs = (EmotionBeliefBase) beliefbase ;

		GoalStatus status = bbs.getGoalsStatus().goalStatus(questIsCompleted.name) ;

		status.setAsFailed();
		status.likelihood=0;
		
	
	}
	

	private void clearroom(BeliefBase beliefbase) {
		EmotionBeliefBase bbs = (EmotionBeliefBase) beliefbase ;
		GoalStatus status = bbs.getGoalsStatus().goalStatus(questIsCompleted.name) ;
		if(status != null && health>0) {
			status.setAsAchieved();
		}
	}
		

	private void effectOfLevelCompletionInSightEvent(BeliefBase beliefbase) {
		
		EmotionBeliefBase bbs = (EmotionBeliefBase) beliefbase ;
		GoalStatus status = bbs.getGoalsStatus().goalStatus(questIsCompleted.name) ;
		status.likelihood = 100 ;
		
	}
	
		

	// various rules:
	
	public int desirabilityAppraisalRule(Goals_Status goals_status, String eventName, String goalName) {
		if(eventName.equals("getdamage") &&goals_status.goalStatus(questIsCompleted.name).likelihood==0) 
			return -800 ;
		if(eventName.equals("getdamage")) 
		return -100 ;
		if(eventName.equals("killenemy") && goalName.equals(questIsCompleted.name)) {
			   return 400 ;
		}
		if(eventName.equals("killenemy") ) {
			   return 400 ;
		}
		   if(eventName.equals("getweapon") ) {
			   return 100 ;
		   }
		   if(eventName.equals("clearroom") && goalName.equals(questIsCompleted.name))
		   {
			   return 800;
		   }
		   if(eventName.equals("getkilled") && goalName.equals(questIsCompleted.name))
		   {
			   return -800;
		   }
		   return 0 ;

	}

	
	public int emotionIntensityDecayRule(Emotion.EmotionType etype) { return 2 ; }

	public int intensityThresholdRule(Emotion.EmotionType etyp) { return 0 ; }
		

}

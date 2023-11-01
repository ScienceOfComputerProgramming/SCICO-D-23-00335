package eu.iv4xr.ux.pxtesting.study.labrecruits;

import static agents.EventsProducer.LevelCompletedEventName;
import static agents.EventsProducer.LevelCompletionInSightEventName;
import static agents.EventsProducer.OpeningADoorEventName;
import static agents.EventsProducer.OuchEventName;

import eu.iv4xr.framework.extensions.occ.BeliefBase;
import eu.iv4xr.framework.extensions.occ.Goal;
import eu.iv4xr.framework.extensions.occ.GoalStatus;
import eu.iv4xr.framework.extensions.occ.BeliefBase.Goals_Status;
import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.ux.pxtesting.occ.OCCBeliefBase;
import eu.iv4xr.ux.pxtesting.occ.XEvent;
import eu.iv4xr.ux.pxtesting.occ.XUserCharacterization;
import world.LabEntity;
import world.LabWorldModel;
import world.BeliefState;

/**
 * A version of {@link PlayerOneCharacterization}, but which extends
 * {@link XUserCharacterization}.
 * 
 */
public class PlayerThreeCharacterization extends XUserCharacterization{

	public static Goal questIsCompleted = new Goal("quest is completed").withSignificance(8) ;
	
	private LabWorldModel getWOM(BeliefBase beliefbase) {
		OCCBeliefBase bbs = (OCCBeliefBase) beliefbase ;
		return ((BeliefState) bbs.functionalstate).worldmodel() ;
	}
	
	@SuppressWarnings("unused")
	@Override
	public void eventEffect(XEvent e, BeliefBase beliefbase) {
		int health = getWOM(beliefbase).health ;
		int point  = getWOM(beliefbase).score ;
		
		GoalStatus gQIC_status = ((OCCBeliefBase) beliefbase).getGoalsStatus().goalStatus(questIsCompleted.name) ;
		
		// logic for Ouch-event:
		switch(e.name) {
		  case OuchEventName         : effectOfOuchEvent(beliefbase) ; break ;
		  case OpeningADoorEventName : effectOfOpeningADoorEvent(beliefbase) ; break ;
		  case LevelCompletedEventName : effectOfLevelCompletedEvent(beliefbase) ; break ;
		  case LevelCompletionInSightEventName : effectOfLevelCompletionInSightEvent(beliefbase) ; break ;
		}	
	}
	
	private void effectOfOuchEvent(BeliefBase beliefbase) {
		int health = getWOM(beliefbase).health ;

		// updating belief on the quest-completed goal; if the health drops below 50,
		// decrease this goal likelihood by 3.
		// If the health drops to 0, game over. The goal is marked as failed.
		GoalStatus status = ((OCCBeliefBase) beliefbase).getGoalsStatus().goalStatus(questIsCompleted.name) ;
		status.likelihood = Math.max(0,status.likelihood - 10) ;
		if(status != null && health<50) {
			if(health <=0) {
				status.setAsFailed();
				status.likelihood=0;
			}
		}
	}
	
	private void effectOfLevelCompletedEvent(BeliefBase beliefbase) {
		int health = getWOM(beliefbase).health ;
		GoalStatus status = ((OCCBeliefBase) beliefbase).getGoalsStatus().goalStatus(questIsCompleted.name) ;
		if(status != null && health>0) {
			status.setAsAchieved();
		}
	}
	
	private void effectOfOpeningADoorEvent(BeliefBase beliefbase) {
		OCCBeliefBase bbs = (OCCBeliefBase) beliefbase ;
		var functionalState =  (BeliefState) bbs.functionalstate ;
		
		int numberOfDoorsMadeOpen =  0 ;
		int numberOfDoorsMadeClosed = 0 ;

		if (functionalState.changedEntities != null) {
			for(WorldEntity e : functionalState.changedEntities) {
				if(e.type == LabEntity.DOOR) {
					if(e.getBooleanProperty("isOpen")) {
						numberOfDoorsMadeOpen++ ;
					}
					else numberOfDoorsMadeClosed++ ;					
				}	
			}
		}	

		// updating belief on the quest-completed goal
		GoalStatus status = bbs.getGoalsStatus().goalStatus(questIsCompleted.name) ;
		if(status != null) {
			status.likelihood = Math.min(80,status.likelihood + 10*(numberOfDoorsMadeOpen - numberOfDoorsMadeClosed)) ;
		}		
	}

	private void effectOfLevelCompletionInSightEvent(BeliefBase beliefbase) {	
		GoalStatus status = ((OCCBeliefBase) beliefbase).getGoalsStatus().goalStatus(questIsCompleted.name) ;
		status.likelihood = 100 ;	
	}
	

	@Override
	public int desirabilityAppraisalRule(Goals_Status goals_status, String eventName, String goalName) {
		if (eventName.equals(OuchEventName) && goalName.equals(questIsCompleted.name))
			return -100;
		if (eventName.equals(OpeningADoorEventName) && goalName.equals(questIsCompleted.name)) {
			return 400;
		}
		if (eventName.equals(LevelCompletionInSightEventName) && goalName.equals(questIsCompleted.name)) {
			return 800;
		}
		return 0;
	}

	@Override
	public int emotionIntensityDecayRule(EmotionType etype) {
		return 2 ;
	}

	@Override
	public int intensityThresholdRule(EmotionType etyp) {
		return 0;
	}

}

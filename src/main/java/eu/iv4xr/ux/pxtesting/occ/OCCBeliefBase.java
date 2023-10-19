package eu.iv4xr.ux.pxtesting.occ;

import eu.iv4xr.framework.extensions.occ.BeliefBase;
import eu.iv4xr.framework.extensions.occ.BeliefBase.Goals_Status;
import nl.uu.cs.aplib.mainConcepts.SimpleState;

/**
 * A class to hold OCC goals and their statuses, and also a reference
 * to an agent's functional state.
 */
public class OCCBeliefBase implements BeliefBase  {

	/**
	 * This keeps track of OCC-goals and their statuses.
	 */
	Goals_Status goals_status = new Goals_Status() ;
	
	/**
	 * A reference to the test-agent's own functional (non-emotional) state.
	 */
	public SimpleState functionalstate ;
	
	public OCCBeliefBase() { }
	
	public OCCBeliefBase attachFunctionalState(SimpleState functionalstate) {
		this.functionalstate = functionalstate ;
		return this ;
	}

	@Override
	public Goals_Status getGoalsStatus() { return goals_status ;  }

}

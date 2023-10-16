package agents.tactics;

import static nl.uu.cs.aplib.AplibEDSL.*;

import java.util.LinkedList;
import java.util.List;

import agents.tactics.TacticLib;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.agents.MiniMemory;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.GoalStructure.PrimitiveGoal;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import nl.uu.cs.aplib.utils.Pair;
import world.BeliefState;
import world.LabEntity;
import static agents.tactics.TacticLib.* ;
import static agents.tactics.GoalLib.* ;
import static nl.uu.cs.aplib.mainConcepts.GoalTacticUtils.* ;

/**
 * Some LabRecruit custom goals and tactics. Only applicable for
 * the LabRecruits case study.
 */
public class MyGoalAndTacticLib {

	/**
	 * This goal will make agent to navigate towards the given entity, and make sure that
	 * the agent has the latest observation of the entity. Getting the entity within
	 * sight is enough to complete this goal.
	 * 
	 * This goal fails if the agent no longer believes that the entity is reachable.
	 */
    public static PrimitiveGoal entityStateRefreshed2(String id){
    	var g =  goal("The belief on this entity is refreshed: " + id)
                .toSolve((BeliefState b) -> {
                	
                /*	System.out.println("entity state refresh: " + 
                b.evaluateEntity(id, e -> b.age(e) == 0)
                	+"get goal location "+ b.getGoalLocation()
                		); */
                	
                  var entity = b.worldmodel.getElement(id);
                  return   (b.evaluateEntity(id, e -> b.age(e) == 0)
                		  
                		  );
                  
                })
                .withTactic(FIRSTof(
                        //TacticLib.navigateToClosestReachableNode(id),   
                		navigateToCloseByPosition2(id,1f),
                        TacticLib.explore(),
                        ABORT()))
                .lift() ;
        	
       		 return g;
        	// g.maxbudget(8);
       		// return FIRSTof(g, SUCCESS()) ;
    }
    
    /**
	 * Navigate to a location, nearby the given entity, if the location is reachable.
	 * Locations east/west/south/north of the entity of distance delta  will be tried.
	 * 
	 * This is a variation of the original navigateToCloseByPosition; in the original
	 * the delta is fixed to 0.5.
	 */
	static Tactic navigateToCloseByPosition2(String id, float delta) {

		MiniMemory memory = new MiniMemory("S0") ;

		Tactic.PrimitiveTactic unguardedNavigateTo_ 
		    = (Tactic.PrimitiveTactic) getSubtactics(navigateToCloseByPosition(id)).get(2) ;
		
		Tactic.PrimitiveTactic move = unguardedNavigateTo_

				// this is a copy of the original logic of navigateToCloseByPosition; we just make
				// the delta parameterized :
				. on((BeliefState belief) -> {

					var e = (LabEntity) belief.worldmodel.getElement(id) ;
    			    if (e==null) return null ;

					Vec3 closeByLocation = null ;
					if (!memory.memorized.isEmpty()) {
						// if the position has been calculated before, retrieve it from memory:
						closeByLocation = (Vec3) memory.memorized.get(0) ;
					}
					Vec3 currentGoalLocation = belief.getGoalLocation() ;

					if (closeByLocation == null
					    || currentGoalLocation == null
					    || Vec3.dist(closeByLocation,currentGoalLocation) >= 0.05
					    || belief.getMemorizedPath() == null) {
						// in all these cases we need to calculate the location to go

						//var agent_location = belief.worldmodel.getFloorPosition() ;
	    			    var entity_location = e.getFloorPosition() ;
	    			    // Calculate the center of the square on which the target entity is located.
	    			    // Note: the bottom-left position of the bottom-left corner is (0.5,-,0.5) so this need to be taken into
	    			    // account.
	    			    // First, substract 0.5 from (x,z) ... then round it down. Add 0.5 to get the center position.
	    			    // Then add another 0.5 to compensate the 0.5 that we substracted earlier.
	    			    var entity_sqcenter = new Vec3((float) Math.floor((double) entity_location.x - 0.5f) + 1f,
	    			    		entity_location.y,
	    			    		(float) Math.floor((double) entity_location.z - 0.5f) + 1f) ;
	    			    
	    			    List<Vec3> candidates = new LinkedList<>() ;
	    			    // adding North and south candidates
	    			    candidates.add(Vec3.add(entity_sqcenter, new Vec3(0,0,delta))) ;
	    			    candidates.add(Vec3.add(entity_sqcenter, new Vec3(0,0,-delta))) ;
	    			    // adding east and west candidates:
	    			    candidates.add(Vec3.add(entity_sqcenter, new Vec3(delta,0,0))) ;
	    			    candidates.add(Vec3.add(entity_sqcenter, new Vec3(-delta,0,0))) ;

	    			    // iterate over the candidates, if one would be reachable:
	    			    for (var c : candidates) {
	    			    	// if c (a candidate point near the entity) is on the navigable,
	    			    	// we should ignore it:
	    			    	if (getCoveringFaces(belief,c) == null) continue ;
	    			    	var result = belief.findPathTo(c, true) ; 
	    			    	if (result != null) {
	    			    		// found our target
	    			    		System.out.println(">>> a reachable closeby position found :" + c + ", path: " + result.snd) ;
	    			    		memory.memorized.clear();
	    			    		memory.memorize(c);
	    			    		return result ;
	    			    	}
	    			    }
	    			    System.out.println(">>> i tried few nearby locations, but none are reachable :|") ;
	    			    // no reachable node can be found. We will clear the memory, and declare the tactic as disabled
	    			    memory.memorized.clear() ;
	    			    return null ;
					}
					else {
						// else the memorized location and the current goal-location coincide. No need to
						// recalculate the path, so we will just return the pair (memorized-loc,null)
						return new Pair (closeByLocation,null) ;
					}
				}) ;

		return  FIRSTof(
				 forceReplanPath(),
				 tryToUnstuck(),
				 move
			   ) ;
	}
	
	
}

package nl.uu.cs.aplib.mainConcepts;

import java.util.List;

import nl.uu.cs.aplib.mainConcepts.GoalStructure.PrimitiveGoal;

public class GoalTacticUtils {

	public static Goal getGoal(PrimitiveGoal G) {
		return G.goal ;
	}
	
	public static PrimitiveGoal setGoal(PrimitiveGoal G, Goal g) {
		G.goal = g ;
		return G ;
	}
	
	public static List<Tactic> getSubtactics(Tactic T) {
		return T.subtactics ;
	}
}

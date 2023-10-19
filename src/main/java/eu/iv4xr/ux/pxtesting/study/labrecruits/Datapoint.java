package eu.iv4xr.ux.pxtesting.study.labrecruits;

import java.util.HashSet;
import java.util.Set;

import eu.iv4xr.framework.extensions.occ.Emotion;

public class Datapoint {
	
	// "t", "x", "y", "hope", "joy", "satisfaction", "fear", "score", "losthealth" , "remainedhealth"

	//to do- change it to private getter/setter
	public float x;
	public float y;
	public float time;
	public Set<Emotion> emo = new HashSet<>();
	public float score;
	public float losthealth;
	public float health;
	
	public Datapoint(float x, float y, float time, Set<Emotion> emo,float score, float losthealth, float remainedhealth ) 
	{ 
		this.x = x; this.y = y; this.time = time; this.score=score;this.losthealth=losthealth; this.health=remainedhealth;
		this.emo=emo;
	}
	public Datapoint () {
		
	}


}

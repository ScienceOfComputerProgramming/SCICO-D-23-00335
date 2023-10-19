package eu.iv4xr.ux.pxtesting.study.labrecruits;

import eu.iv4xr.framework.mainConcepts.IEmotion;

import java.util.List;

import eu.iv4xr.framework.extensions.occ.Emotion ;
import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;
import eu.iv4xr.framework.spatial.Vec3;

public class LRState {
	
	static public class LREmotion implements IEmotion{
		
		public Emotion.EmotionType type ;
		public float intensity ;

		@Override
		public String getEmotionType() {
			return type.toString() ;
		}

		@Override
		public String getAgentId() {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public String getTargetId() {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public float getIntensity() {
			return intensity ;
		}

		@Override
		public Long getTime() {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public Long getActivationTime() {
			throw new UnsupportedOperationException() ;
		}	
	}
	
	public int updateTurn;
	public float x;
	public float y;
	public List<? extends IEmotion> emotions ;
	public int score;
	public int losthealth;
	public int health;
	
	public LRState previous ;

	public LRState() {
	}
	
	public Vec3 getPosition() {
		return new Vec3(x,0,y) ;
	}
	
	public float getEmotionIntensity(String emotionType) {
		for (var e : emotions) {
			if (e.getEmotionType().equals(emotionType)) {
				return e.getIntensity() ;
			}
		}
		throw new IllegalArgumentException() ;
	}

	public Float getEmotionDif(String emotionType) {
		if (previous != null)
			return this.getEmotionIntensity(emotionType) - previous.getEmotionIntensity(emotionType) ;		
		return null ;
	}
	
	static public void printLRStateSequence(List<LRState> exec) {
		int k = 0 ;
		for(var st : exec) {
			System.out.print("== " + k + ":") ;
			System.out.print(" hope: " + st.getEmotionIntensity(EmotionType.Hope.toString())) ;
			System.out.print("[" + st.getEmotionDif(EmotionType.Hope.toString()) + "]") ;
			System.out.print(" joy: " + st.getEmotionIntensity(EmotionType.Joy.toString())) ;
			System.out.print("[" + st.getEmotionDif(EmotionType.Joy.toString()) + "]") ;
			System.out.print(" satisfaction: " + st.getEmotionIntensity(EmotionType.Satisfaction.toString())) ;
			System.out.print("[" + st.getEmotionDif(EmotionType.Satisfaction.toString()) + "]") ;
			System.out.print(" fear: " + st.getEmotionIntensity(EmotionType.Fear.toString())) ;
			System.out.print("[" + st.getEmotionDif(EmotionType.Fear.toString()) + "]") ;
			System.out.print(" distress: " + st.getEmotionIntensity(EmotionType.Distress.toString())) ;
			System.out.print("[" + st.getEmotionDif(EmotionType.Distress.toString()) + "]") ;
			System.out.print(" disappointment: " + st.getEmotionIntensity(EmotionType.Disappointment.toString())) ;
			System.out.print("[" + st.getEmotionDif(EmotionType.Disappointment.toString()) + "]") ;

			System.out.println("") ;
			k++ ;
		}
	}

}
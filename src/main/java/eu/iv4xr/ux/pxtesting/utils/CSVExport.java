package eu.iv4xr.ux.pxtesting.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.iv4xr.framework.extensions.occ.Emotion;
import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;
import eu.iv4xr.ux.pxtesting.study.labrecruits.Datapoint;

public class CSVExport {
	
	public static void exportToCSV(List<String[]> data, String filename) throws IOException {
		
		StringBuffer buf = new StringBuffer() ;
		int k=0 ;
		for(String[] row : data) {
			if(k>0) buf.append("\n") ;
			for(int y=0; y<row.length; y++) {
				if(y>0) buf.append(",") ;
				buf.append(row[y]) ;
			}
			k++ ;
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(buf.toString()) ;
        writer.close();
		
	}
	
	public static List<Datapoint> importFromCSV(String Filename) throws IOException {

		List<Datapoint> data = new ArrayList<Datapoint>();
		// String[] csvRow = { "t", "x", "y", "hope", "joy", "satisfaction", "fear",
		// "score", "losthealth" , "remainedhealth"};

		try (BufferedReader br = new BufferedReader(new FileReader(Filename))) {
			String line = "";
			String[] tempArr;
			line = br.readLine();
			while ((line = br.readLine()) != null) {
				tempArr = line.split(",");
				// for(String tempStr : tempArr) {
				data.add(creatDatapoint(tempArr));
				// }
				System.out.println();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}


	private static Datapoint creatDatapoint(String[] metadata) {

		float time = Integer.parseInt(metadata[0]);
		float x = Float.parseFloat(metadata[1]);
		float y = Float.parseFloat(metadata[2]);
		float score = Float.parseFloat(metadata[9]);
		float losthealth = Float.parseFloat(metadata[10]);
		float remainedhealth = Float.parseFloat(metadata[11]);

		Set<Emotion> emo = new HashSet<>();

		Emotion hope = new Emotion(EmotionType.Hope, null, 0, (int) (800f * Float.parseFloat(metadata[3])));
		Emotion joy = new Emotion(EmotionType.Joy, null, 0, (int) (800f * Float.parseFloat(metadata[4])));
		Emotion satisfaction = new Emotion(EmotionType.Satisfaction, null, 0,
				(int) (800f * Float.parseFloat(metadata[5])));
		Emotion fear = new Emotion(EmotionType.Fear, null, 0, (int) (800f * Float.parseFloat(metadata[6])));
		Emotion distress = new Emotion(EmotionType.Disappointment, null, 0,
				(int) (800f * Float.parseFloat(metadata[7])));
		Emotion disap = new Emotion(EmotionType.Disappointment, null, 0, (int) (800f * Float.parseFloat(metadata[8])));

		emo.add(hope);
		emo.add(joy);
		emo.add(satisfaction);
		emo.add(fear);
		emo.add(distress);
		emo.add(disap);

		return new Datapoint(x, y, time, emo, score, losthealth, remainedhealth);
	}
}

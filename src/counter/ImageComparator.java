package counter;

import java.io.File;
import java.util.ArrayList;

public class ImageComparator {
	private static final String[] SAMPLE_BOXES = {"C"}; //, "B", "C"};
	private static final int SAMPLES_PER_BOX = 9;
	private static final String[] SAMPLE_REGIONS = {"NW", "NE", "SW", "SE"};
	
	private ArrayList<ImageRecord> imageRecords = new ArrayList<ImageRecord>();
	private File beforeDir;
	private File afterDir;
		
	
	public ImageComparator(String before, String after) {
		this.beforeDir = new File(before);
		this.afterDir  = new File(after);
		
		for (String box : SAMPLE_BOXES) {
			for(int sampleNum = 1; sampleNum <= SAMPLES_PER_BOX; sampleNum++) {
				for (String region : SAMPLE_REGIONS) {
					String sample = String.format("%s%d%s", box, sampleNum, region);
					ImageRecord record = new ImageRecord(sample, this.beforeDir, this.afterDir);
					File[] b = Utilities.searchMatchingFiles(sample, beforeDir);
					File[] a = Utilities.searchMatchingFiles(sample, afterDir);
					if(b.length == 0) continue;
					record.setBefore(b);
					record.setAfter(a);
					imageRecords.add(record);
				}
			}
		}
		
		
	}
	
	public ArrayList<ImageRecord> getImageRecords(){
		return this.imageRecords;
	}
	
	public void setImageRecords(ArrayList<ImageRecord> irList) {
		this.imageRecords = irList;
	}
	
	

}

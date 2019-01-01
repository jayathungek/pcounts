package counter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import ij.ImagePlus;
import ij.process.ImageStatistics;
import ij.measure.ResultsTable;

public class Utilities{
	private static final int BRIGHTNESS_CUTOFF = 75;  //brightfield or darkfield
    private static final double ADJUSTMENT = 2; // adjust the standard deviation scaling

	public static int getMaxIndex(long[] histogram){
	    if (histogram.length == 0) return -1;
	    int maxIndex = 0;
	    for(int i = 0; i<histogram.length; i++){
	        if (histogram[i] > histogram[maxIndex]) maxIndex = i;
	    }
	    return maxIndex;
	}

	public static double getGradient(double x1, double y1, double x2, double y2){
	    return (y2 - y1)/(x2 - x1);
	}

	public static int getIndexOfSteepestGradient(long[] histogram, int interval, Boolean positiveGradient){
	    if (histogram.length == 0) return -1;
	    double maxGradient = 0;
	    int maxGradientIndex = 0;
	    for (int currIndex = 0; currIndex < histogram.length - interval; currIndex += interval){
	        double x1 = (double)(currIndex);
	        double y1 = (double)(histogram[currIndex]);
	        double x2 = (double)(currIndex + interval);
	        double y2 = (double)(histogram[currIndex + interval]);
	        double gradient = getGradient(x1, y1, x2, y2);

	        if (positiveGradient){
	            if (gradient > maxGradient){
	                maxGradient = gradient;
	                maxGradientIndex = currIndex;
	            }
	        }else{
	            if (gradient < maxGradient){
	                maxGradient = gradient;
	                maxGradientIndex = currIndex;
	            }
	        }

	        
	    }

	    return maxGradientIndex;

	}

	public static int[] getThresholdBounds(ImagePlus imP){
        ImageStatistics stat = ImageStatistics.getStatistics(imP.getProcessor());
        double mean = stat.mean;
        int meanInt = (int) Math.ceil(mean);
        double std = stat.stdDev;
        int[] bounds = {0, 0};
        int upperBound = (int) (Math.ceil(mean - ADJUSTMENT*std));
        bounds[1] = upperBound;

        // System.out.printf("Mean:%.2f, StdDev:%.2f\n", mean, std);
//        if (meanInt >= BRIGHTNESS_CUTOFF){ // brightfield
//            int upperBound = (int) (Math.ceil(mean - ADJUSTMENT*std));
//            bounds[1] = upperBound;
//        }else{                           // darkfield
//            int lowerBound = (int) (Math.ceil(mean + ADJUSTMENT*std));
//            bounds[0] = lowerBound;
//            bounds[1] = 255;
//        }
                
        return bounds;
    }

    public static double getAreaCovered(ImagePlus image, ResultsTable rt, Double scale){
    	double total_area = 0;
    	for (int i = 0; i< rt.getCounter(); i++){
    		total_area += rt.getValue("Area", i);
    	}

    	double width = image.getWidth() * scale;
    	double height= image.getHeight() * scale;
        
        return total_area/(width*height);
    }
    
    public static boolean isImageFile(File f) throws IOException {
    	String mimetype = Files.probeContentType(f.toPath());
    	if (mimetype != null && mimetype.split("/")[0].equals("image")) return true;
    	return false;
    }
    
    public static ResultsTable setScale(ResultsTable rt, Double scale) {
    	for (int index = 0; index < rt.getCounter(); index++) {
    		Double currArea = rt.getValue("Area", index);
    		Double rescaledArea = Math.pow(scale, 2) * currArea;
    		Double rescaledX = rt.getValue("X", index) * scale;
    		Double rescaledY = rt.getValue("Y", index) * scale;
    		rt.setValue("Area", index, rescaledArea);
    		rt.setValue("X", index, rescaledX);
    		rt.setValue("Y", index, rescaledY);
    	}
    	return rt;
	}
    
    public static Double rescaleArea(Double area, Double scale) {
    	return Math.pow(scale,2)*area;
    }
    
    public static File[] searchMatchingFiles(String name, File dir) {
		return dir.listFiles(new FilenameFilter()
		{
			  public boolean accept(File dir, String filename)
			  {
				  return filename.startsWith(name);
			  }
			});
	}
    public static void saveRecordsToCSV(ArrayList<ImageRecord> irList, String filename) throws FileNotFoundException {
    	 PrintWriter pw = new PrintWriter(new File(filename));
         StringBuilder sb = new StringBuilder();
         sb.append("sample,countBefore,countAfter,areaBefore,areaAfter");
         HashMap<String, Integer> grades = irList.get(0).getAvgBefore().getGrades();
         ArrayList<String> keys = new ArrayList<String>();
         for (String label : grades.keySet()) {
        	 sb.append(","+label+"_before");
        	 sb.append(","+label+"_after");
        	 keys.add(label);
         }
         sb.append('\n');
         for(ImageRecord ir : irList) {
        	 sb.append(String.format("%s,%d,%d,%.5f,%.5f", ir.getName(), ir.getAvgBefore().getTotalCount(), ir.getAvgAfter().getTotalCount()
        			 , ir.getAvgAreaCoveredBefore(), ir.getAvgAreaCoveredAfter()));
        	 for (String label : keys) {
            	 sb.append(String.format(",%.2f,%.2f", ir.getCountForGradeBefore(label), ir.getCountForGradeAfter(label)));
             }
        	 sb.append('\n');
         }
         pw.write(sb.toString());
         pw.close();
    }
    
    
    
    

}
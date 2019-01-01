package counter;
import java.io.File;
import java.util.HashMap;

public class ImageRecord {
//	private static final String TOP    = "_top";
//	private static final String MIDDLE = "_mid";
//	private static final String BOTTOM = "_bot";
	
	private String name;
	private File[] before;  // top, mid and bottom values for each
	private File[] after;
	
	private Count avgBefore;
	private Count avgAfter;
	
	private double avgCountBefore;
	private double avgAreaCoveredBefore;
	
	private double avgCountAfter;
	private double avgAreaCoveredAfter;
	
	private double normCountChange;
	private double normAreaChange;
		
	
	public double getNormCountForGrade(String label) {
		double num = 1.0*(avgAfter.getCountForLabel(label) - avgBefore.getCountForLabel(label));
		return num/avgBefore.getCountForLabel(label);
	}
	
	public double getCountForGradeBefore(String label) {
		return avgBefore.getCountForLabel(label);
	}
	
	public double getCountForGradeAfter(String label) {
		return avgAfter.getCountForLabel(label);
	}
	
	public Count getAvgBefore() {
		return avgBefore;
	}

	public void setAvgBefore(Count avgBefore) {
		this.avgBefore = avgBefore;
	}

	public Count getAvgAfter() {
		return avgAfter;
	}

	public void setAvgAfter(Count avgAfter) {
		this.avgAfter = avgAfter;
	}

	public String getName() {
		return name;
	}

	public double getAvgCountBefore() {
		return avgCountBefore;
	}

	public void setAvgCountBefore(double avgCountBefore) {
		this.avgCountBefore = avgCountBefore;
	}

	public double getAvgAreaCoveredBefore() {
		return avgAreaCoveredBefore;
	}

	public void setAvgAreaCoveredBefore(double avgAreaCoveredBefore) {
		this.avgAreaCoveredBefore = avgAreaCoveredBefore;
	}

	public double getAvgCountAfter() {
		return avgCountAfter;
	}

	public void setAvgCountAfter(double avgCountAfter) {
		this.avgCountAfter = avgCountAfter;
	}

	public double getAvgAreaCoveredAfter() {
		return avgAreaCoveredAfter;
	}

	public void setAvgAreaCoveredAfter(double avgAreaCovered) {
		this.avgAreaCoveredAfter = avgAreaCovered;
	}

	public double getNormCountChange() {
		return this.normCountChange;
	}

	public void setNormCountChange(double normCountChange) {
		this.normCountChange = normCountChange;
	}

	public double getNormAreaChange() {
		return this.normAreaChange;
	}

	public void setNormAreaChange(double normAreaChange) {
		this.normAreaChange = normAreaChange;
	}
	
	public void setBefore(File[] before) {
		this.before = before;
	}

	public void setAfter(File[] after) {
		this.after = after;
	}

	public File[] getBefore() {
		return this.before;
	}

	public File[] getAfter() {
		return this.after;
	}

	public ImageRecord(String name, File beforeDir, File afterDir) {
		this.name = name;
		this.before = Utilities.searchMatchingFiles(name, beforeDir);
		
		this.after= Utilities.searchMatchingFiles(name, afterDir);  
		
		
	}
	
	
	
//	public void addAreas(File[] beforeCont, File[] afterCont, File[] afterCl ) {
//		for (File area : beforeCont) {
//			this.beforeContamination.add(area.getName());
//		}
//		
//		for (File area : afterCont) {
//			this.afterContamination.add(area.getName());
//		}
//		
//		for (File area : afterCl) {
//			this.afterClean.add(area.getName());
//		}
//		
//	}
	
	public void prettyPrint() {
		System.out.println("-------------------------------------------------------");
		System.out.println(this.name);
		System.out.println("Average count before: " + this.avgCountBefore );
		System.out.println("Average count after: " + this.avgCountAfter);
		System.out.println("Average area covered before: " + this.avgAreaCoveredBefore );
		System.out.println("Average area covered after: " + this.avgAreaCoveredAfter);
		System.out.println("Change in count: " + this.normCountChange +"%");
		System.out.println("Change in area: " + this.normAreaChange+"%");
		System.out.println("-------------------------------------------------------");
	}
		
}

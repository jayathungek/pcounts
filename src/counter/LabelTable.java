package counter;

public class LabelTable {
	// IMPORTANT: If one of these 3 changes, the others must be updated to match!!
	private Integer[] PARTICLE_AREA_GRADES = {500, 5000, 15000};
	private String[] PARTICLE_AREA_LABELS = {"s", "m", "l"};
	private String[] PARTICLE_AREA_LABELS_VERBOSE = {"Small", "Medium", "Large"};
	
	public Integer[] getGrades() {
		return this.PARTICLE_AREA_GRADES;
	}
	
	public String[] getLabels() {
		return this.PARTICLE_AREA_LABELS;
	}
	
	public String[] getVerboseLabels() {
		return this.PARTICLE_AREA_LABELS_VERBOSE;
	}
	
	public void setTables(Integer[] grades, String[] labels, String[] labels_V) {
		if(grades.length == labels.length && grades.length == labels_V.length) {
			this.PARTICLE_AREA_GRADES = grades;
			this.PARTICLE_AREA_LABELS = labels;
			this.PARTICLE_AREA_LABELS_VERBOSE = labels_V;			
		}else {
			throw new IllegalArgumentException("All tables must be the same length!");
		}
		
	}
	
	public LabelTable() {
		
	}

}

package counter;

import java.util.HashMap;

public class Count {
	private int totalCount;
	private HashMap<String, Integer> grades;
	
	public Count(Result r) {
		this.totalCount = r.getResultsTable().getCounter();
		this.grades = new HashMap<String, Integer>();
		for(String key : r.getLabelTable().getLabels()) {
			this.grades.put(key, r.getCountForLabel(key));
		}
	}
	
	public Count(LabelTable lt) {
		this.totalCount = 0;
		this.grades = new HashMap<String, Integer>();
		for(String key : lt.getLabels()) {
			this.grades.put(key, 0);
		}
	}
	
	public int getTotalCount() {
		return this.totalCount;
	}
	
	public int getCountForLabel(String label) {
		return grades.get(label);
	}
	
	public void setTotalCount(int count) {
		this.totalCount = count;
	}
	
	public void setCountForLabel(String label, int value) {
		this.grades.put(label, value);
	}
	public HashMap<String, Integer> getGrades(){
		return this.grades;
	}
	
	public void addToGrade(HashMap<String, Integer> g1) {
		for (String label : this.grades.keySet()) {
			this.grades.put(label, g1.get(label) + this.grades.get(label));
		}
		
	}
	
	public void averageCounts(int number) {
		this.totalCount = this.totalCount / number;
		for (String label : this.grades.keySet()) {
			this.grades.put(label, this.grades.get(label) / number);
		}
	}

}

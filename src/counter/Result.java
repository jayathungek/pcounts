package counter;

import ij.measure.ResultsTable;
import java.util.HashMap;	
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import ij.ImagePlus;

public class Result{

	private ResultsTable rt;
	private String name;
	private int particle_count;
	private Double contaminated_area;
	private HashMap<String, ArrayList<Particle>> particles = new HashMap<String, ArrayList<Particle>>();
	private HashMap<String, Integer> grades = new HashMap<String, Integer>();
	private LabelTable labels;
	private String unit;     // nm or um
	private Double scale;    // number of nm/um to 1 pixel 
	

	public Result(String name, ResultsTable rt, ImagePlus image, LabelTable lt, String unit, Double scale){
		this.labels = lt;
		this.unit = unit;
		this.scale = scale;
		this.name = name;
		
		this.rt = Utilities.setScale(rt, this.scale);
		this.particle_count = rt.getCounter();
		this.contaminated_area = Utilities.getAreaCovered(image, this.rt, this.scale);
		int i = 0;
		for (String label : labels.getLabels()){
			this.grades.put(label, labels.getGrades()[i]);
			i++;
		}
		this.populateMap();
	}
	
	

	public void populateMap(){

		ArrayList<Particle> pList = new ArrayList<Particle>();
		for (int index = 0; index < this.particle_count; index++){
			double[] pos = {this.rt.getValue("X", index), this.rt.getValue("Y", index)};
			double a = this.rt.getValue("Area", index);
			String label = getLabelFromArea(a);
			Particle p = new Particle(pos, a, label);
			pList.add(p);
		}
		
		for (String key : grades.keySet()){
			List<Particle> subList = pList
									   .stream()
									   .filter(particle -> (particle.getLabel() == key))
									   .collect(Collectors.toList());
			particles.put(key, new ArrayList<Particle> (subList));
		}
		

	}

	public String getLabelFromArea(double area){
		double lowerBound = 0;
		int index = 0;
		for (double grade : this.labels.getGrades()){
			if(area > lowerBound && area < grade) return this.labels.getLabels()[index];
			index ++;
			lowerBound = grade; 
		}
		return this.labels.getLabels()[index-1];
	}

	public ResultsTable getResultsTable(){
		return this.rt;
	}

	public String getName(){
		return this.name;
	}

	public int getParticleCount(){
		return this.particle_count;
	}

	public int getParticleCount(String label){
		return (this.particles.get(label)).size();
	}

	public void prettyPrint(){
		System.out.printf("%-30s\n", "Image name: " + this.name);
		String header = String.format("|____Particle count (total): %d", this.particle_count);

		ArrayList<String> subheaders= new ArrayList<String>();
		int index = 0;
		int lastLabelIndex = this.labels.getLabels().length - 1;
		for (String label : this.labels.getLabels()){
			int count = getParticleCount(label);
			if (index > 0 && index != lastLabelIndex) {
				int prev_index = index - 1;
				subheaders.add(String.format("%s (%d - %d sq %s): %d", this.labels.getVerboseLabels()[index],
					       this.labels.getGrades()[prev_index], this.labels.getGrades()[index], this.unit, count));
			}else if (index > 0 && index == lastLabelIndex){
				int prev_index = index - 1;
				subheaders.add(String.format("%s (>%d sq %s): %d", this.labels.getVerboseLabels()[index],
					       this.labels.getGrades()[prev_index], this.unit, count));
			}else {
				subheaders.add(String.format("%s (%d - %d sq %s): %d", this.labels.getVerboseLabels()[index],
					       0, this.labels.getGrades()[index], this.unit, count));
			}
			
			index++;
		}

		System.out.printf("%-30s\n", treeBuilder(header, subheaders.toArray(new String[subheaders.size()])));
		System.out.printf("%-30s%.2f%%\n", "|____Percentage contaminated: ", (this.contaminated_area*100));
		System.out.printf(String.format("%40s\n", " " ).replace(' ', '*'));
	}
	
	

	public static String treeBuilder(String title, String[] subtitles){
		StringBuilder sb = new StringBuilder();
		sb.append(title+"\n|");
		for (String sub : subtitles){
			sb.append("\t|____" + sub + "\n|");
		}
		return sb.toString();
	}

	


}
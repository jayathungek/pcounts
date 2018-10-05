package counter;


public class Particle implements Comparable<Particle> {
	private double[] position;
	private double area;
	private String label;

	

	public Particle(double[] position, double area, String label){
		this.position = position;
		this.area = area;
		this.label = label;
	}

	public int compareTo(Particle p){
		if(Double.compare(this.area, p.area) < 0){
			return -1;
		}else if(Double.compare(this.area, p.area) > 0){
			return 1;
		}else{
			return 0;
		}
	}

	public double[] getPosition(){
		return this.position;
	}

	public double getArea(){
		return this.area;
	}

	public String getLabel(){
		return this.label;
	}

	 
}
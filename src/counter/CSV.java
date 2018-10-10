package counter;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CSV {
	
	private LabelTable lt = new LabelTable();
	private String unit;
	private Double scale;
	
	public LabelTable getLabelTable() {
		return this.lt;
	}
	
	public String getUnit() {
		return this.unit;
	}
	
	public Double getScale() {
		return this.scale;
	}
	
	
	public CSV(String filename) throws IOException {
		
		CSVReader reader = new CSVReader(new FileReader(filename));
		String [] nextLine;	
		ArrayList<Integer> g = new ArrayList<Integer>();
		while ((nextLine = reader.readNext()) != null) {
			try {
				g.add(Integer.parseInt((nextLine[0])));
			}catch(NumberFormatException e) {
				this.unit = (nextLine[0]).split(" ")[0];
				this.scale = Double.parseDouble((nextLine[0]).split(" ")[1]);				
			}
			
		}
		
		Integer[] grades = g.toArray(new Integer[g.size()]);
		reader.close();
		
		reader = new CSVReader(new FileReader(filename));
		ArrayList<String> l = new ArrayList<String>();
		while ((nextLine = reader.readNext()) != null) {
			try {
				if (!(nextLine[1]).isEmpty()) l.add((nextLine[1]));
			}catch(ArrayIndexOutOfBoundsException e) {}
			
		}
		String[] labels = l.toArray(new String[l.size()]);
		reader.close();
		
		reader = new CSVReader(new FileReader(filename));
		ArrayList<String> v = new ArrayList<String>();
		while ((nextLine = reader.readNext()) != null) {
			try {
				if (!(nextLine[2]).isEmpty()) v.add((nextLine[2]));
			}catch(ArrayIndexOutOfBoundsException e) {}
		}
		String[] verbose_labels = v.toArray(new String[v.size()]);
		reader.close();
		
		this.lt.setTables(grades, labels, verbose_labels);
	}
	
}

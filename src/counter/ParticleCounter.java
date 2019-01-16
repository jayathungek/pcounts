package counter;

import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.plugin.filter.ParticleAnalyzer;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.Option;
import jargs.gnu.CmdLineParser.UnknownOptionException;

public class ParticleCounter
{

	private final String separator;
	private static final String[] MEASUREMENT_UNITS = { "px", "nm", "um"};
    private static final int MEASUREMENTS =  Measurements.AREA + Measurements.CENTROID;
    private static final int OPTIONS = ParticleAnalyzer.SHOW_NONE;
	private static final char[] CHAR_OPTIONS = {'s', 'S', 'c', 'C', 'u', 'n', 'm', 'd'};
	private static final String[] STRING_OPTIONS = {"minsize", "maxsize", "mincircularity", 
			                                       "maxcircularity", "userdefined", "nm", "um", "diff"};
    
	private static double MIN_PARTICLE_SIZE = 0;
    private static double MAX_PARTICLE_SIZE = 99999; //maximum is 99999
    private static double MIN_CIRCULARITY = 0.4;
    private static double MAX_CIRCULARITY = 1.0;
    private String basePath;
    private String resultsPath;
    private File file;
    private Boolean isFile = false;
    private ArrayList<String> files;
    private ArrayList<Result> results;
    private LabelTable labels = new LabelTable();
    private String unit;     // nm or um
	private Double scale;    // number of nm/um to 1 pixel 

    public < T extends NumericType< T > & NativeType< T > > ParticleCounter( String directory, String operatingSystem)
    {
    	if (operatingSystem.contains("Windows")){
    		this.separator = "\\";
    	}else {
    		this.separator = "/";
    	}
        File file = new File(directory);
        this.labels = new LabelTable();

        if( isFile(file) ){
        	this.file = file;
            this.isFile = true;
            this.resultsPath = file.getAbsoluteFile().getParent() + this.separator;      
            

        }else{
            this.files = new ArrayList<>();
            this.results = new ArrayList<>();
            this.basePath = directory;
            this.resultsPath = this.basePath  + this.separator + "counts" + this.separator;
        }
        
    }

    // The results table holds all measurements associated with a certain image
    public static ResultsTable analyzeImage(ImagePlus image, String filename){
        ResultsTable rt = new ResultsTable();
        rt.setDefaultHeadings();
        ParticleAnalyzer pa = new ParticleAnalyzer(OPTIONS, MEASUREMENTS, rt, MIN_PARTICLE_SIZE,
         MAX_PARTICLE_SIZE, MIN_CIRCULARITY, MAX_CIRCULARITY);
        try{
            pa.analyze(image);
        }catch(ArrayIndexOutOfBoundsException e){
            System.out.printf("Error while processing image at %s\n", filename);
        }
        return rt;
    }

    public static ImagePlus processInputImage(File image){
        ImagePlus imp = new Opener().openImage( image.getAbsolutePath() );
        ImageConverter conv = new ImageConverter(imp);
        conv.convertToGray8();
        int[] bounds = Utilities.getThresholdBounds(imp);
        imp.getProcessor().setThreshold(bounds[0], bounds[1], ImageProcessor.BLACK_AND_WHITE_LUT);
        return imp;
    }
    
    public static ArrayList<ImageRecord> generateImageRecords(ArrayList<ImageRecord> irList, LabelTable lt, String unit, Double scale ){
//    	ProgressBar pb = new ProgressBar(irList.size());
//    	pb.start();
    	for(ImageRecord ir : irList) {
    		ir = analyseImageRecord(ir, lt, unit, scale);
//    		pb.tick();
    	}
    	return irList;
    }
    
    public static ImageRecord analyseImageRecord(ImageRecord ir, LabelTable lt, String unit, Double scale) {
    	
    	File[] before = ir.getBefore();
    	File[] after = ir.getAfter();
    	int numBefore = before.length;
    	int numAfter  = after.length;
    	
    	Double avgAreaCoveredBefore = 0.0;
    	Count avgCountBefore = new Count(lt); // average counts set to 0
    	for (File image : before) {
    		ImagePlus imp = processInputImage(image);
    		ResultsTable rt = analyzeImage(imp, image.getName());
    		Result r = new Result(image.getName(), rt, imp, lt, unit, scale);
    		Count c = new Count(r);
    		
    		avgCountBefore.setTotalCount(avgCountBefore.getTotalCount()+c.getTotalCount());    		
    		avgCountBefore.addToGrade(c.getGrades());
    		avgAreaCoveredBefore+= r.getAreaCovered();
    	}
    	avgCountBefore.averageCounts(numBefore);
    	avgAreaCoveredBefore /= numBefore;
    	
    	Double avgAreaCoveredAfter = 0.0;
    	Count avgCountAfter = new Count(lt); // average counts set to 0
    	for (File image : after) {
    		ImagePlus imp = processInputImage(image);
    		ResultsTable rt = analyzeImage(imp, image.getName());
    		Result r = new Result(image.getName(), rt, imp, lt, unit, scale);
    		Count c = new Count(r);
    		avgCountAfter.setTotalCount(avgCountAfter.getTotalCount()+c.getTotalCount());    		
    		avgCountAfter.addToGrade(c.getGrades());
    		avgAreaCoveredAfter += r.getAreaCovered();
    	}
    	avgCountAfter.averageCounts(numAfter);
    	avgAreaCoveredAfter /= numAfter;
    	
    	ir.setAvgBefore(avgCountBefore);
    	ir.setAvgAfter(avgCountAfter);
    	
    	Double normCountChange = (double)(avgCountAfter.getTotalCount() - avgCountBefore.getTotalCount())/avgCountBefore.getTotalCount();
    	Double normAreaChange = (avgAreaCoveredAfter - avgAreaCoveredBefore)/avgAreaCoveredBefore;
    	
    	ir.setAvgCountBefore(avgCountBefore.getTotalCount());
    	ir.setAvgAreaCoveredBefore(avgAreaCoveredBefore);
    	ir.setAvgBefore(avgCountBefore);
    	
    	ir.setAvgCountAfter(avgCountAfter.getTotalCount());
    	ir.setAvgAreaCoveredAfter(avgAreaCoveredAfter);
    	ir.setAvgAfter(avgCountAfter);
    	
    	ir.setNormCountChange(normCountChange);
    	ir.setNormAreaChange(normAreaChange);
    	
    	
    	return ir;
    	
    }


    public void analyseAllFiles(){
        for (String filename : this.files){
            File file = new File(basePath +this.separator+ filename);  
            ImagePlus image = processInputImage(file);         
            ResultsTable entry = analyzeImage(image, filename);
            Result result = new Result(filename, entry, image, labels, this.unit, this.scale);
            this.results.add(result);
        }
    }

    public void addFiles() throws IOException{
        File folder = new File(this.basePath + this.separator);
        File[] fList = folder.listFiles();
        for (File item : fList){
        	if (Utilities.isImageFile((item))){
        		String name = item.getName();
                this.files.add(name);
        	}
        }
    }

    public void saveAllResultsToCSV(){
        File saveDir = new File(this.resultsPath); 
        if (!saveDir.exists()) new File (resultsPath).mkdir();
        for(Result result : this.results){
            saveResultsToCSV(result);
        }
    }

    // Retrieves the statistics for an image and uses them to calculate
    // upper and lower bounds for thresholding
    

    public String saveResultsToCSV(Result result){
        ResultsTable rt = result.getResultsTable();
        String saveFile = String.format(this.resultsPath+"%d_%s_areas_in_%s.csv", rt.getCounter(), result.getName(), this.unit);
        rt.save(saveFile);
        return saveFile;
    }

    public void start() throws IOException{
        if (this.isFile){
        	if (Utilities.isImageFile(this.file)) {
        		ImagePlus image = processInputImage(this.file);
                ResultsTable rt = analyzeImage(image, this.file.getName());
                Result r = new Result(file.getName(), rt, image, labels, this.unit, this.scale);
                r.prettyPrint();
                saveResultsToCSV(r);
        	}else {
        		throw new IOException("Input must be an image file!");
        	}
                        
            
        }else{
            this.addFiles();
            this.analyseAllFiles();
            
            this.saveAllResultsToCSV();

            for (Result r : this.results){
                r.prettyPrint();
            }

        }

    }


    public Boolean isFile(File f){
        return f.isFile();
    }
    
    public void setMinParticleSize(double size) {
    	MIN_PARTICLE_SIZE = Utilities.rescaleArea(size, this.scale);
    }
    
    public void setMaxParticleSize(double size) {
    	MAX_PARTICLE_SIZE = Utilities.rescaleArea(size, this.scale);
    }
    
    public void setMinCircularity(double circ) {
    	MIN_CIRCULARITY= circ;
    }
    
    public void setMaxCircularity(double circ) {
    	MAX_CIRCULARITY= circ;
    }
    
    public void setTable(LabelTable lt) {
    	this.labels = lt;
    }
    
    public void setUnit(String unit) {
    	this.unit = unit;
    }
    
    public void setScale(Double scale) {
    	this.scale = scale;
    }
    
    public static void main( String[] args ) throws IllegalOptionValueException, UnknownOptionException, IOException{
    	    	
    	
    	final CmdLineParser parser = new CmdLineParser();
    	final Option.IntegerOption minSizeOption = new Option.IntegerOption(CHAR_OPTIONS[0], STRING_OPTIONS[0]);
    	final Option.IntegerOption maxSizeOption = new Option.IntegerOption(CHAR_OPTIONS[1], STRING_OPTIONS[1]);
    	final Option.DoubleOption minCircOption = new Option.DoubleOption(CHAR_OPTIONS[2], STRING_OPTIONS[2]);
    	final Option.DoubleOption maxCircOption = new Option.DoubleOption(CHAR_OPTIONS[3], STRING_OPTIONS[3]);
    	final Option.StringOption userDefOption = new Option.StringOption(CHAR_OPTIONS[4], STRING_OPTIONS[4]);
    	final Option.DoubleOption nanoScaleOption = new Option.DoubleOption(CHAR_OPTIONS[5], STRING_OPTIONS[5]);
    	final Option.DoubleOption microScaleOption = new Option.DoubleOption(CHAR_OPTIONS[6], STRING_OPTIONS[6]);
    	final Option.StringOption diffOption = new Option.StringOption(CHAR_OPTIONS[7], STRING_OPTIONS[7]);
    	parser.addOption(minSizeOption);
    	parser.addOption(maxSizeOption);
    	parser.addOption(minCircOption);
    	parser.addOption(maxCircOption);
    	parser.addOption(userDefOption);
    	parser.addOption(nanoScaleOption);
    	parser.addOption(microScaleOption);
    	parser.addOption(diffOption);
    	parser.parse(args);
    	
    	String operatingSystem = System.getProperty("os.name");    	
		String[] remainingArgs = parser.getRemainingArgs();
		if (remainingArgs.length == 0)throw new IOException("Argument must contain a file path!");
		
		String path = remainingArgs[0];
		int lastIndex = path.length() - 1;
		if ( path.charAt(lastIndex)== '\\' || path.charAt(lastIndex)== '/') {
			path = path.substring(0, lastIndex); 
		} 
		ParticleCounter pc = new ParticleCounter(path, operatingSystem);    
		pc.setUnit(MEASUREMENT_UNITS[0]);
		pc.setScale(1.0);
    
        try {
        	int minSize = (int)parser.getOptionValue(minSizeOption);
        	pc.setMinParticleSize(minSize);
        }catch(NullPointerException e) {}
        
        try {
        	int maxSize = (int) parser.getOptionValue(maxSizeOption);
        	pc.setMaxParticleSize(maxSize);
        }catch(NullPointerException e) {}
    	 
        try {
        	double minCirc = (double) parser.getOptionValue(minCircOption);
        	if (minCirc < 0.0 || minCirc > 1.0) throw new IllegalArgumentException("Circularity must be between 0 and 1");
        	pc.setMinCircularity(minCirc); 
        }catch(NullPointerException e) {}        	
    	
    	
    	try {
    		double maxCirc = (double) parser.getOptionValue(maxCircOption);
    		if (maxCirc < 0.0 || maxCirc > 1.0) throw new IllegalArgumentException("Circularity must be between 0 and 1");
    		pc.setMaxCircularity(maxCirc);
    	}catch (NullPointerException e) {}
    	
    	try {
    		String userDef = (String) parser.getOptionValue(userDefOption);
    		CSV csv = new CSV(userDef);
    		pc.labels = csv.getLabelTable();
    		pc.unit = csv.getUnit();
    		pc.scale = csv.getScale();
    	}catch (NullPointerException e) {}
    	
    	// MAKE --nm AND --um FLAGS MUTUALLY EXCLUSIVE
        try {	    		
    		Double nanoScale = ((Double) parser.getOptionValue(nanoScaleOption)).doubleValue();
    		pc.setUnit(MEASUREMENT_UNITS[1]);
    		pc.setScale(nanoScale);
    	}catch (NullPointerException e) {}
        
        try {	    		
    		Double microScale = ((Double) parser.getOptionValue(microScaleOption)).doubleValue();
    		pc.setUnit(MEASUREMENT_UNITS[2]);
    		pc.setScale(microScale);
    	}catch (NullPointerException e) {}
        
        try {	     
    		Vector<String> v = parser.getOptionValues(diffOption); 
    		String beforeDir = v.get(0);
    		String afterDir = v.get(1); 
    		ImageComparator ic = new ImageComparator(beforeDir, afterDir); 
            ArrayList<ImageRecord> irList = ic.getImageRecords();
            irList = generateImageRecords(irList, pc.labels, pc.unit, pc.scale);
            for (ImageRecord ir : irList) {
            	ir.prettyPrint();
            }
            Utilities.saveRecordsToCSV(irList, "boxC.csv");
    	}catch (ArrayIndexOutOfBoundsException e) {
    	}

        pc.start();
        
    }
}

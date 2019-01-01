package counter;

public class ProgressBar {
	private int totalItems;
	private int completedItems;
	private static final int BAR_LENGTH = 50;
	private static final String DONE = "=";
	private static final String NOT_DONE = "-";
	
	public ProgressBar(int numItems) {
		this.totalItems = numItems;
		this.completedItems = 0;
	}
	
	private void draw() {
		int filled = (this.completedItems/this.totalItems)*BAR_LENGTH;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i<BAR_LENGTH; i++) {
			sb.append((i<filled)?DONE:NOT_DONE);
		}
		System.out.print(String.format("[%s]\r",sb));
	}
	
	public void start() {
		this.draw();		
	}
	
	public void tick() {
		this.completedItems++;
		this.draw();
	}

}

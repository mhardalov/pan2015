package pan.pers.console;

import org.junit.Test;

public class EvaluateTests {
	
	/**
	 * Path to gate application (xgapp) directory including model
	 */
	static final String GATE_DIR = "/home/momchil/Desktop/TextMining/workspace/pan2015/src/main/resources";
	
	/**
	 * Path to extracted data
	 */
	static final String INPUT_DIR = "/home/momchil/Desktop/TextMining/pan-data/pan15-author-profiling-training-dataset-english-2015-03-02";
	
	
	/**
	 * Path were the generated result file will be produced 
	 */
	static final String OUTPUT_DIR = "/home/momchil/Desktop/TextMining/output";
	
	@Test
	public void evaluateMainTest() throws Exception {
		String[] args = { "-o", OUTPUT_DIR, 
						  "-i", INPUT_DIR, 
						  "-m", GATE_DIR };
		
		Evaluate.main(args);
	}
}

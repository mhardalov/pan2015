package pan.pers.console;

import gate.creole.ConditionalSerialAnalyserController;
import gate.util.persistence.PersistenceManager;

import java.io.File;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;

public class Evaluate {
	
	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("o", true, "path to output directory");
		options.addOption("i", true, "path to test corpus");
		options.addOption("m", true, "path to model (gate folder)");
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		String gatePath = cmd.getOptionValue("m");
		String input = cmd.getOptionValue("i");
		String output = cmd.getOptionValue("o");
		
		File gateHome = new File(gatePath);
		Utils.initGate(gateHome);
		ConditionalSerialAnalyserController gapp = (ConditionalSerialAnalyserController) 
				PersistenceManager.loadObjectFromFile(new File(gateHome, "application.xgapp"));
		
		GateZipProcessor proc = new GateZipProcessor(gapp);
		proc.run(input, output);
	}

	// TODO; read input (probably zip file? ) create gate docs, process, output in correct format.
	
}

package pan.pers.console;

import gate.Factory;
import gate.Gate;
import gate.persist.PersistenceException;
import gate.persist.SerialDataStore;
import gate.util.GateException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Utils {

	public static SerialDataStore startGate(URL dir, File gateHome) throws GateException, PersistenceException {
		initGate(gateHome);
		SerialDataStore ds = (SerialDataStore) Factory.createDataStore("gate.persist.SerialDataStore",
				dir.toString());
		ds.open();
		return ds;
	}
	
	public static void initGate(File gateHome) throws GateException {
		Gate.setGateHome(gateHome);
		Gate.setPluginsHome(gateHome);
		Gate.init();
	}

	public static List<String> readFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(
				new FileReader(file));
		String line = null;
		List<String> lines = new ArrayList<>();
		while ((line = reader.readLine()) != null) {
			if(line.length() > 0) {
				lines.add(line);
			}
		}
		reader.close();
		return lines;
	}

}

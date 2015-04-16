package pan.pers.console;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.creole.ConditionalSerialAnalyserController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import pan.pers.corpora.Profile;

public class GateZipProcessor {

	ConditionalSerialAnalyserController gapp;
	Corpus corpus;
	
	public GateZipProcessor(ConditionalSerialAnalyserController gapp) throws Exception {
		this.gapp = gapp;
		corpus = Factory.newCorpus("corpus");
		gapp.setCorpus(corpus);
	}

	public void run(String inDir, String outputDir) throws Exception {
		File in = new File(inDir);
		for(File single : in.listFiles()) {
			String fileName = single.getName();
			if (fileName.equals("truth.txt")) {
				continue;
			}
			FileInputStream fis = new FileInputStream(single);
			String text = IOUtils.toString(fis);
			fis.close();
			
			Profile p = gateProcess(text);
			p.id = fileName.substring(0, fileName.lastIndexOf('.'));
			output(p, outputDir);
		}

		System.out.println("Done");
	}

	private Profile gateProcess(String text) throws Exception {
		Profile p = new Profile();
		ArrayList<String> arr = new ArrayList<>(Arrays.asList(text.toString().split("\n"))); 
		p.addTweets(arr);
		Document doc = p.toDoc();
		
		corpus.add(doc);
		gapp.execute();
		p.set(doc.getFeatures());
		
		corpus.clear();
		Factory.deleteResource(doc);
		return p;
	}

	private void output(Profile p, String outDir) throws IOException {
		File folder = new File(outDir);
		folder.mkdirs();
		File out = new File(folder, p.id + ".txt");
		FileWriter writer = new FileWriter(out);
		writer.write("<author id=\"" + p.id + "\"\n");
		writer.write(" type=\"twitter\"\n");
		writer.write(" lang=\""+ p.getLang() + "\"\n");
		writer.write(" age_group=\"" + p.age + "\"\n");
		writer.write(" gender=\"" + p.getGender() + "\"\n");
		writer.write(" extroverted=\"" + p.e + "\"\n");
		writer.write(" stable=\"" + p.s + "\"\n");
		writer.write(" agreeable=\"" + p.a + "\"\n");
		writer.write(" conscientious=\"" + p.c + "\"\n");
		writer.write(" open=\"" + p.o + "\"\n");
		writer.write("/>");
		writer.close();
	}
}

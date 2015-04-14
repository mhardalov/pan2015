package pan.pers.corpora;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.persist.SerialDataStore;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pan.pers.console.Utils;


public class PanCorpusCreator {
	
	public static void main(String argv[]) throws Exception {
		File trFile = new File(
				"/home/yasen/projects/other/personality/code/src/main/resources/pan/pers/corpora/en/truth.txt");
		Map<String, Profile> profiles = getTruth(trFile);
		
		File dir = new File("/home/yasen/projects/other/personality/code/src/main/resources/pan/pers/corpora/en/docs");
		
		for (File f : dir.listFiles()) {
			String fName = f.getName();
			Profile current = profiles.get(fName.substring(0, fName.indexOf(".")));
			current.addTweets(f);
		}
		
		URL dsDir = new URL("file:///home/yasen/projects/other/personality/data/ds1");
		File gateHome = new File("/home/yasen/programs/gate-8.0-build4825-ALL");
		SerialDataStore ds = Utils.startGate(dsDir, gateHome);

		String corpusName = "en";
		Corpus train = (Corpus) ds.adopt(Factory.newCorpus(corpusName + "-train"));
		Corpus test = (Corpus) ds.adopt(Factory.newCorpus(corpusName + "-test"));
		List<Anno> docAnns = new ArrayList<Anno>();
		
		for(Profile p : profiles.values()) {
			StringBuilder docText = new StringBuilder();
			for(String tw : p.tweets) {
				Anno ann = new Anno(docText.length(), docText.length() + tw.length(), "tweet");
				docText.append(tw).append("\n\n");
				docAnns.add(ann);
			}
			Corpus c = train;
			if(Math.random() > 0.9) {
				c = test;
			}
			addDocument(docAnns, docText.toString(), ds, c, p);
			docAnns.clear();
		}
		ds.close();
	}
	

	// userid:::gender:::age_group:::extraverted:::stable:::agreeable:::conscientious:::open
	// user552:::M:::25-34:::0.3:::0.5:::0.1:::0.2:::0.2
	private static Map<String, Profile> getTruth(File trFile) throws Exception {
		Map<String, Profile> result = new HashMap<>();
		List<String> lines = Utils.readFile(trFile);
		for(String line: lines) {
			String[] parts = line.split(":::");
			String id = parts[0];
			Profile p = new Profile(id, parts[1], parts[2], Double.parseDouble(parts[3]),
					Double.parseDouble(parts[4]),
					Double.parseDouble(parts[5]),
					Double.parseDouble(parts[6]),
					Double.parseDouble(parts[7]));
			result.put(id, p);
		}
		return result;
	}
	
	private static void addDocument(List<Anno> docAnnots, String docText, SerialDataStore ds, Corpus cor,
			Profile p) throws Exception {
		FeatureMap params = Factory.newFeatureMap(); 
		params.put(Document.DOCUMENT_STRING_CONTENT_PARAMETER_NAME, docText);
		params.put(Document.DOCUMENT_ENCODING_PARAMETER_NAME, "UTF-8");
		Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);
		doc.setName(p.id);
		for (Anno ann : docAnnots) {
			doc.getAnnotations("GOLD").add((long) ann.start, (long) ann.end, ann.type, Factory.newFeatureMap());
		}
		FeatureMap fm = Factory.newFeatureMap();
		fm.put("id", p.id);
		fm.put("age", p.age);
		fm.put("sex", p.sex);
		fm.put("o", p.o);
		fm.put("c", p.c);
		fm.put("e", p.e);
		fm.put("a", p.a);
		fm.put("s", p.s);
		doc.getAnnotations("GOLD").add(0l, (long) docText.length(), "person", fm);
		cor.add(doc);
		ds.sync(cor);
	}
	
}

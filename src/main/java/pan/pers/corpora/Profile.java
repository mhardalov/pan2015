package pan.pers.corpora;

import gate.Document;
import gate.Factory;
import gate.FeatureMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pan.pers.console.GenderEnum;
import pan.pers.console.LanguageEnum;
import pan.pers.console.Utils;

public class Profile {

	/**
	 * User identifier
	 */
	public String id;

	/**
	 * Gender
	 */
	public GenderEnum sex;

	/**
	 * Age
	 */
	public String age;

	/**
	 * Openness
	 */
	public double o;

	/**
	 * Conscientious
	 */
	public double c;

	/**
	 * Extroverted
	 */
	public double e;

	/**
	 * Agreeable
	 */
	public double a;

	/**
	 * Stable
	 */
	public double s;

	/**
	 * Language of tweets
	 */
	public LanguageEnum language;

	public Profile(String id, String sex, String age, double e, double s,
			double a, double c, double o, String language) {
		this();
		this.id = id;
		this.age = age;
		this.sex = GenderEnum.findByText(sex);
		this.o = o;
		this.c = c;
		this.e = e;
		this.a = a;
		this.s = s;
		this.language = LanguageEnum.findByText(language);
	}

	public Profile() {
		this.tweets = new ArrayList<>();
	}

	// userid:::gender:::age_group:::extraverted:::stable:::agreeable:::conscientious:::open
	// user552:::M:::25-34:::0.3:::0.5:::0.1:::0.2:::0.2
	public List<String> tweets;

	public void addTweets(File f) throws Exception {
		List<String> lines = Utils.readFile(f);
		addTweets(lines);
	}

	public void addTweets(List<String> lines) {
		lines.remove(0);
		lines.remove(lines.size() - 1);
		String pre = "<![CDATA[";
		String post = "		]]>";
		String text = "";
		boolean in = false;
		for (String line : lines) {
			if (in) {
				text += "   " + line;
			} else {
				int start = line.indexOf(pre) + pre.length();
				if (start < pre.length()) {
					System.out.println("ERROR: " + line);
					continue;
				}
				text = line.substring(start);
			}

			if (text.contains(post)) {
				text = text.substring(0, text.indexOf(post));
				tweets.add(text);
				in = false;
			} else {
				in = true;
			}
		}
	}

	public Document toDoc() throws Exception {
		FeatureMap params = Factory.newFeatureMap();

		StringBuilder docText = new StringBuilder();
		List<Anno> docAnns = new ArrayList<Anno>();

		for (String l : tweets) {
			Anno ann = new Anno(docText.length(),
					docText.length() + l.length(), "tweet");
			docText.append(l).append("\n\n");
			docAnns.add(ann);
		}

		params.put(Document.DOCUMENT_STRING_CONTENT_PARAMETER_NAME,
				docText.toString());
		params.put(Document.DOCUMENT_ENCODING_PARAMETER_NAME, "UTF-8");
		Document doc = (Document) Factory.createResource(
				"gate.corpora.DocumentImpl", params);
		doc.setName("doc name");
		doc.getAnnotations("GOLD").add(0l, (long) docText.length(), "person",
				Factory.newFeatureMap());
		for (Anno ann : docAnns) {
			doc.getAnnotations("GOLD").add((long) ann.start, (long) ann.end,
					ann.type, Factory.newFeatureMap());
		}
		return doc;
	}

	public void set(FeatureMap docFeatures) {
		this.age = (String) docFeatures.get("age");
		this.sex = GenderEnum.findByText((String) docFeatures.get("sex"));
		this.o = (double) docFeatures.get("o");
		this.c = (double) docFeatures.get("c");
		this.e = (double) docFeatures.get("e");
		this.a = (double) docFeatures.get("a");
		this.s = (double) docFeatures.get("s");
		this.language = LanguageEnum.findByText((String) docFeatures
				.get("lang"));
	}

	public String getGender() {
		return this.sex.toString();
	}

	public String getLang() {
		return this.language.toString();
	}
}
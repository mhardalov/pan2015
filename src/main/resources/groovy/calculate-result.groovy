// Calculates errors for each of the five traits : o, c, e, a, s
// error is sum of square differences

traits = new ArrayList<String>();
  traits.add("o");
	traits.add("c");
	traits.add("e");
	traits.add("a");
	traits.add("s");
	
void beforeCorpus(c) {
	// list of lists (one for each doc) with scores for each trait
	gold = new ArrayList<List<Double>>();
	guess = new ArrayList<List<Double>>();
	docNum = 0
}

// for current doc:
currGold = new ArrayList<Double>();
currGuess = new ArrayList<Double>();

// gold is in "person" annotations in GOLD AS
FeatureMap goldMap = doc.getAnnotations("GOLD").get("person").iterator().next().getFeatures();

// guesses are set as document features (see script label-to-doc.groovy)
FeatureMap guessMap = doc.getFeatures();
for(String name : traits) {
	println("gold " + name + ": " + goldMap[name ])
	println("guess " + name + ": " + guessMap[name])
	currGold.add(goldMap[name]);
	currGuess.add(guessMap[name]);
}

gold.add(currGold);
guess.add(currGuess);

void afterCorpus(c) {
	List<Double> result = new ArrayList<Double>(5);
	for(int i = 0; i < 5; ++i) {
		result.add(0.0);
	}
	int docs = gold.size();

	for(int i = 0; i < docs; ++i) {
		for(int j = 0; j < 5; ++j) {
			Double goldValue = gold.get(i).get(j);
			Double guessValue = guess.get(i).get(j);
			double sq = Math.pow((goldValue - guessValue), 2);
			double old = result.get(j);
			result.set(j, old + sq / (double) docs);
		}
	}
	
	for(int j = 0; j < 5; ++j) {
		println(traits.get(j) + ": " + result.get(j));
	}
}

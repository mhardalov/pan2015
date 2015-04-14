List<String> names = new ArrayList<String>();
  names.add("o");
	names.add("c");
	names.add("e");
	names.add("a");
	names.add("s");
	
void beforeCorpus(c) {
	// list of maps (one for each doc) class : score
	gold = new ArrayList<List<Double>>();
	guess = new ArrayList<List<Double>>();
	docNum = 0
}

// for current doc:
currGold = new ArrayList<Double>();
currGuess = new ArrayList<Double>();

FeatureMap pm = inputAS["person"].iterator().next().getFeatures();
FeatureMap dm = doc.getFeatures();
for(String name : names) {
	println("gold " + name + ": " + pm[name])
	println("guess " + name + ": " + dm[name])
	currGold.add(pm[name]);
	currGuess.add(dm[name]);
}


gold.add(currGold);
guess.add(currGuess);

void afterCorpus(c) {
	
	List<String> names = new ArrayList<String>();
	names.add("o");
	names.add("c");
	names.add("e");
	names.add("a");
	names.add("s");
	
	List<Double> result = new ArrayList<Double>(5);
	for(int i = 0; i < 5; ++i) {
		result.add(0.0);
	}
	int docs = gold.size();
	for(int i = 0; i < docs; ++i) {
		for(int j = 0; j < 5; ++j) {
			Double g = gold.get(i).get(j);
			Double gu = guess.get(i).get(j);
			double sq = (g - gu) * (g - gu);
			double old = result.get(j);
			result.set(j, old + sq / (double) docs);
		}
	}
	
	for(int j = 0; j < 5; ++j) {
		println( names.get(j) + ": " + result.get(j));
	}
}

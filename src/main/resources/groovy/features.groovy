inputAS["person"].each { sn ->
	AnnotationSet tokens = inputAS.get("Token", sn.start(), sn.end());

	FeatureMap df = doc.getFeatures();
	FeatureMap sf = sn.getFeatures();
	df["o"] = sf.remove("o");
	df["c"] = sf.remove("c");
	df["e"] = sf.remove("e");
	df["a"] = sf.remove("a");
	df["s"] = sf.remove("s");
	df["age"] = sf.remove("age");
	df["sex"] = sf.remove("sex");
	df["id"] = sf.remove("id");

  AnnotationSet tweets = doc.getAnnotations("GOLD").get("tweet");
  AnnotationSet foundUrls = inputAS.get("URL");
  AnnotationSet foundHashtags = inputAS.get("Hashtag");
	AnnotationSet foundUserMetions = inputAS.get("UserID");

//================================ N GRAMS ==================================
	// n-grams:
  int minN = 1;
  int maxN = 2;
  String nGram;
  tweets.each { tweet ->
    twToks = new ArrayList<Annotation>(inputAS.get("Token", tweet.start(), tweet.end()))
    Collections.sort(twToks, new OffsetComparator())
    strings = twToks.features.string
    for (int n = minN; n <= maxN; n++) {
      for(int i = 0; i < twToks.size() - n + 1; ++i) {
        StringBuilder sb = new StringBuilder();
        String delim = ""
        for (int j = 0; j < n; j++) {
            word = strings[i + j].toLowerCase();
            String kind = twToks[i + j].features["kind"]
            if(kind == "number") {
              word = "0";
            } else if (kind == "URL") {
              word = "_URL";
            }
            sb.append(delim).append(word);
            delim = "_";
        }
        fName = n + "gr:" + sb.toString();
        if(sn.features[fName] == null) {
          sn.features[fName] = 1
        } else {
          sn.features[fName] = sn.features[fName] + 1
        }
      }
    }
  }
  
//================================ / N GRAMS ==================================
    
//=============================== POS TAG COUNTS ==============================

  Map<String, Integer> posMap = new HashMap<String, Integer>();
  Map<String, Integer> caseMap = new HashMap<String, Integer>();

  tokens.each { token ->
		String letterCase = (String)token.getFeatures().get("orth");
		if (letterCase != null) {
      //Couting the different letter cases for tokens in text
      // TODO: it could be easier with groovy
      if(caseMap.containsKey(letterCase)) {
        caseMap.put(letterCase, caseMap.get(letterCase) + 1);  
      } else {
        caseMap.put(letterCase, 1);
      }
		}
    String kind = (String)token.getFeatures().get("kind");
    String category = (String)token.getFeatures().get("category");
		//We must skip it if it's not a word, it is an url, or hashtag.
		if (kind == "word") { // || foundHashtags.get(token.getStartNode().getOffset(), token.getEndNode().getOffset()).size() > 0
      fName = "cat:" + category;
      Integer tagCount = posMap.get(fName);
      if(tagCount == null) {
        tagCount = 1
      } else {
        tagCount++;
      }      
      posMap.put(fName, tagCount);
    }
  }

  int tweetSize = tweets.size();
  for (java.util.Map.Entry<String, Integer> tag : posMap.entrySet()) {
		sn.features[tag.getKey()] = (double) tag.getValue() / tweetSize;
  }
  for (java.util.Map.Entry<String, Integer> letterCase : caseMap.entrySet()) {
		sn.features[letterCase.getKey()] = (double) letterCase.getValue() / tweetSize;
  }
//=============================== / POS TAG COUNTS ==============================

//=============================== TWITTER TAG COUNTS ==============================
  
  sn.features["hashtags"] = (double)foundHashtags.size() / tweetSize;
  sn.features["urls"] = (double)foundUrls.size() / tweetSize;
	sn.features["mentions"] = (double)foundUserMetions.size() / tweetSize;

//=============================== / TWITTER TAG COUNTS ==============================

//=============================== ORTOGRAPHIC FEATURES ==============================
  AnnotationSet sentences = inputAS.get("Sentence")
  
  // average sentence length
  int sentLength = 0;
  sentences.each { sentLength += it.end() - it.start() }
  sn.features["avgSentLength"] = Math.log((double) sentLength / (double) sentences.size())
  
  
//=============================== / ORTOGRAPHIC FEATURES ==============================

//=============================== DICTIONARIES ==============================
  
  // scores for O C E A S classes from wwbp
	lookups = inputAS.get("Lookup", sn.start(), sn.end())
	String[] types = ["o", "a", "c", "e", "s"]
  String[] emotions = ["anticipation", "fear", "anger", "trust", "surprise", "sadness", "joy", "disgust"]
  
	(types + emotions).each { type ->
		double posScore = 0.0
    double negScore = 0.0
		int posSize = 0
    int negSize = 0
		lookups.findAll{ it.features["minorType"] == type }.each{
      double s = Double.parseDouble(it.features["score"])
      if(s > 0.0) {
        posScore += s
        posSize++
      } else {
        negScore += s
        negSize++
      }
    }
    double factor = 1.0
    if(emotions.contains(type)) {
      factor = tweetSize
    }
    sn.features[type + "_pos_score"] = posScore / factor
    sn.features[type + "_pos_size"] = (double) posSize / tweetSize
    if(negSize > 0) {
      sn.features[type + "_neg_score"] = negScore / factor
      sn.features[type + "_neg_size"] = (double) negSize / tweetSize
      sn.features[type + "_score"] = (posScore + negScore) / factor
      sn.features[type + "_size"] = (double) (posSize + negSize) / tweetSize
    }
	}
  
//=============================== / DICTIONARIES ==============================
}

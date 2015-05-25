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

  AnnotationSet tweets = inputAS.get("tweet");
  int tweetSize = tweets.size();
  AnnotationSet foundUrls = inputAS.get("URL");
  AnnotationSet foundHashtags = inputAS.get("Hashtag");
	AnnotationSet foundUserMetions = inputAS.get("UserID");
  AnnotationSet lookups = inputAS.get("Lookup", sn.start(), sn.end());
  
//================================ N GRAMS ==================================
	// n-grams:
  int minN = 1;
  int maxN = 2;
  String nGram;
  Set<String> vocabulary = new HashSet<String>();
  tweets.each { tweet ->
    twToks = new ArrayList<Annotation>(inputAS.get("Token", tweet.start(), tweet.end()))
    Collections.sort(twToks, new OffsetComparator())
    localStrings = twToks.features.string
    for (int n = minN; n <= maxN; n++) {
      for(int i = 0; i < twToks.size() - n + 1; ++i) {
        StringBuilder sb = new StringBuilder();
        String delim = ""
        for (int j = 0; j < n; j++) {
            word = localStrings[i + j].toLowerCase();
            vocabulary.add(word);
            String kind = twToks[i + j].features["kind"]
            if(kind == "number") {
              word = "0";
            } else if (kind == "URL") {
              word = "_URL";
            }
            if(word != "_upper") {
              sb.append(delim).append(word);
              delim = "_";
            }
        }
        if(sb.size() > 0) {
          fName = n + "gr:" + sb.toString();
          if(sn.features[fName] == null) {
            sn.features[fName] = 1
          } else {
            sn.features[fName] = sn.features[fName] + 1
          }
        }
      }
    }
  }
  sn.features["voc_size"] = (double)vocabulary.size() / tweetSize;
  
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

  
  for (java.util.Map.Entry<String, Integer> tag : posMap.entrySet()) {
		sn.features[tag.getKey()] = (double) tag.getValue() / tweetSize;
  }
  for (java.util.Map.Entry<String, Integer> letterCase : caseMap.entrySet()) {
		sn.features[letterCase.getKey()] = (double) letterCase.getValue() / tweetSize;
  }
//=============================== / POS TAG COUNTS ==============================

//=============================== TWITTER TAG COUNTS ==============================
  
  // AnnotationSet foundPictures = doc.getAnnotations().get("Pictures");
	// sn.features["pic_post"] = (double)foundPictures.size() / tweetSize;
  sn.features["hashtags"] = (double)foundHashtags.size() / tweetSize;
  sn.features["urls"] = (double)foundUrls.size() / tweetSize;
	sn.features["mentions"] = (double)foundUserMetions.size() / tweetSize;
  sn.features["mention_start"] = (double)inputAS.get("StartsWithMention").size() / tweetSize;
  sn.features["retweets"] = (double) tokens.findAll{ it.features["category"] == "RT" }.size() / tweetSize;

//=============================== / TWITTER TAG COUNTS ==============================

//=============================== ORTOGRAPHIC FEATURES ==============================
  AnnotationSet sentences = inputAS.get("Sentence")
  
  // average sentence length
  int sentLength = 0;
  sentences.each { sentLength += it.end() - it.start() }
  sn.features["avgSentLength"] = Math.log((double) sentLength / (double) sentences.size())
  
  // - elongated words: the number of words with one character repeated more than 2 times, e.g. 'soooo';
	sn.features["elongCount"] = tokens.features.string.findAll { it.matches(".*(\\w)\\1{2,}.*") }.size()
  
  // - punctuation: - the number of contiguous sequences of exclamation marks, question marks, and both exclamation and question marks;
  //  sn.features["exclSequenceCount"] = inputAS.get("exclamation", sn.start(), sn.end()).size();

  
//=============================== / ORTOGRAPHIC FEATURES ==============================

//=============================== DICTIONARIES ==============================
  
  
  // badword counts:
  sn.features["badwords"] = (double)lookups.findAll { it.features["majorType"].equals("swear")}.size() / tweetSize;
	
  // emoticons - total number:
  //feats["posEmoExists"] = lookups.findAll { it.features["majorType"].equals("emoticon") && it.features["minorType"].equals("pos")}.size() > 0 ? 1 : 0;

  
  
  // scores for O C E A S classes from wwbp
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

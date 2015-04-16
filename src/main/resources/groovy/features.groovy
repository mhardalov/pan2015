inputAS["person"].each { sn ->

	sortedToks = new ArrayList<Annotation>(inputAS.get("Token", sn.start(), sn.end()));
    Collections.sort(sortedToks, new OffsetComparator());
	strings = sortedToks.features.string
	
	// label = sn.features[scriptParams.labelFeature];
	// sn.features[scriptParams.labelFeature] = label;

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

	// n-grams:
    int minN = 1;
    int maxN = 2;
    String nGram;
    for (int n = minN; n <= maxN; n++) {
        for(int i = 0; i < sortedToks.size() - n + 1; ++i) {
            StringBuilder sb = new StringBuilder();
            String delim = ""
            
            for (int j = 0; j < n; j++) {
								word = strings[i + j].toLowerCase();
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

    AnnotationSet foundUrls = doc.getAnnotations().get("Address");
    AnnotationSet foundHashtags = doc.getAnnotations().get("Hashtag");
		
	//Pos tag features
    for(int i = 0; i < sortedToks.size(); ++i) {
    	Annotation token = sortedToks[i];
		String kind = (String)token.getFeatures().get("kind");		

		//We must skip it if it's not a word, it is an url, or hashtag.
		if (kind != "word" || foundUrls.get(token.getStartNode().getOffset(), token.getEndNode().getOffset()).size() > 0 ||
			foundHashtags.get(token.getStartNode().getOffset(), token.getEndNode().getOffset()).size() > 0) {
			continue;
		}
	
        String category = (String)token.getFeatures().get("category");
				fName = "cat:" + category;

        if(sn.features[fName] == null) {
			sn.features[fName] = 1
		} else {
			sn.features[fName] = sn.features[fName] + 1
		}        
    }
    
	lookups = inputAS.get("Lookup", sn.start(), sn.end());

	String[] types = ["o", "a", "c", "e", "s"]

	types.each { type ->
		double score = 0.0;
		int size = 0;
		lookups.findAll{ it.features["minorType"] == type }.each{
			score += Double.parseDouble(it.getFeatures()["score"])
			size ++;
			
		}
		sn.features[type + "_score"] = score
		sn.features[type + "_size"] = size
		
	}

}

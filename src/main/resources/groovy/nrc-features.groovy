inputAS["snippet"].each { sn ->
	sn.features.remove("id")
    sortedToks = new ArrayList<Annotation>(inputAS.get("Token", sn.start(), sn.end()));
    Collections.sort(sortedToks, new OffsetComparator());
	sentences = inputAS.get("Sentence", sn.start(), sn.end());
	strings = sortedToks.features.string
	hashtags = inputAS.get("Hashtag", sn.start(), sn.end());
	lookups = inputAS.get("Lookup", sn.start(), sn.end());
	negs = inputAS.get("negation", sn.start(), sn.end());
	feats =  Factory.newFeatureMap()
	
// - all-caps: the number of words with all characters in upper case;
    feats["allcapsCount"] = sortedToks.findAll { t -> t.features["orth"] == "allCaps" }.size()

// - clusters: presence/absence of tokens from each of the 1000 clusters (provided by Carnegie Mellon University's Twitter NLP tool);
	lookups.findAll { it.features["majorType"].equals("clusters") }.each { l ->
		feats["cl:" + l.features["cluster"]] = 1 }

// - elongated words: the number of words with one character repeated more than 2 times, e.g. 'soooo';
	feats["elongCount"] = strings.findAll { it.matches(".*(\\w)\\1{2,}.*") }.size()

// - emoticons: - presence/absence of positive and negative emoticons at any position in the tweet;
	feats["negEmoExists"] = lookups.findAll { it.features["majorType"].equals("emoticon") && it.features["minorType"].equals("neg")}.size() > 0 ? 1 : 0;
	feats["posEmoExists"] = lookups.findAll { it.features["majorType"].equals("emoticon") && it.features["minorType"].equals("pos")}.size() > 0 ? 1 : 0;

// - emoticons: - whether the last token is a positive or negative emoticon;
	feats["lastPosEmo"] = lookups.findAll {it.features["majorType"].equals("emoticon") && it.features["minorType"].equals("pos") && it.end() >= sn.end() - 1}.size() > 0 ? 1 : 0;
	feats["lastNegEmo"] = lookups.findAll {it.features["majorType"].equals("emoticon") && it.features["minorType"].equals("neg") && it.end() >= sn.end() - 1}.size() > 0 ? 1 : 0;

// - hashtags: the number of hashtags;
    feats["tagCount"] = hashtags.size();

// - negation: the number of negated contexts. 
    feats["negCCount"] = negs.size();

// TODO: A negated context also affects the ngram and lexicon features: each word and associated with it polarity in a negated context become negated 
// (e.g., 'not perfect' becomes 'not perfect_NEG', 'POLARITY_positive' becomes 'POLARITY_positive_NEG');
    
// - POS: the number of occurrences for each part-of-speech tag;
	posCounts = [:]
	sortedToks.features.category.each { // TODO: check for null values
		if(posCounts[it] == null) {
			posCounts[it] = 1;
		}
		else {
			posCounts[it] ++;
		}
	}
	posCounts.each { feats["partsCount" + it.key] = it.value }

// - punctuation: - the number of contiguous sequences of exclamation marks, question marks, and both exclamation and question marks;
    feats["exclSequenceCount"] = inputAS.get("exclamation", sn.start(), sn.end()).size();

// - punctuation: - whether the last token contains exclamation or question mark;
    feats["lastIsExcl"] = (strings[-1] == "!" || strings[-1] == "?") ? 1 : 0;

	stringsWithNeg = new ArrayList<String>()
	sortedToks.each { t ->
		str = t.features["string"].toLowerCase()
		if("true".equals(t.features["neg"])) {
			str += "NEG"
		}
		stringsWithNeg.add(str)
	}

// word n-grams:
    int minN = 1;
    int maxN = 4;
	for (int n = minN; n <= maxN; n++) {
		for(int i = 0; i < stringsWithNeg.size() - n + 1; ++i) {
			skips = [-1] // -1 are regular ngrams, 2 and 3 are skip-grams of the kind [we, will, *, you]
			for(sk = 1; sk < n-1; sk++) {
				skips.add(sk)
			}
			
			skips.each { skip ->
				StringBuilder sb = new StringBuilder();
				String delim = ""
				for (int j = 0; j < n; j++) {
					if(j == skip) {
						word = "*";
					} else {
						word = stringsWithNeg[i + j];
					}
					sb.append(delim).append(word);
					delim = "_";
				}
				feats["ngr:" + sb.toString()] = 1;
			}
		}
	}

// character n-grams: // TODO: Try with no word-boundaries
    minN = 3;
    maxN = 5;
    for (int n = minN; n <= maxN; n++) {
		strings.each { tok ->
			for (int j = 0; j < tok.length() - n + 1; j++) {
				word = tok.substring(j, j + n).toLowerCase().replace(":", "");
				feats["charngr:" + word] = 1;
			}
		}
	}
	
	// only add != 0 values 
	snFeats = sn.features
	feats.entrySet().each {
		if(it.value != 0) {
			snFeats[it.key] = it.value
		}
	}

}
/*
 * negVerbs = 0;
    verbs.each { v ->
		negs = inputAS.get("negation", v.start(), v.end());
		if(negs != null && negs.size() > 0) {
			negVerbs++;
		}
	}
	feats["negVerbCount"] = negVerbs;
 */

/*
majors = ["sen140", "nrchash"]
otherMajors = ["bingliu", "mpqa", "nrcemo"]
biMinors = ["posbi", "negbi"]
minors = ["posuni", "neguni"] + biMinors
pairs = ["negpair", "pospair"]

// enrich lookups with inPOS, inHashtag, inAllCaps:
AnnotationSet toks = inputAS.get("Token", sn.start(), sn.end())
// uni and bigram dicts
lookups.findAll {it.features.majorType in majors + otherMajors && it.features.minorType not in pairs}.each { l ->
	// allcaps
	if(toks.get(l.start(), l.end()).every { it.features.orth == "allCaps" }) {
		l.features["inAllCaps"] = true
	}
}
// only unigram dicts
lookups.findAll {it.features.majorType in majors + otherMajors && it.features.minorType not in pairs + biMinors}.each { l ->
	// pos
	t = toks.get(l.start(), l.end()
	if(t.size() == 1) {
		l.features["inPOS"] = t.iterator().next().features.category
	// hashtags
	if(!inputAS.get("Hashtag", l.start(), l.end()).isEmpty()) {
		l.features["inHashtag"] = true
	}
}

orthos = ["inHashtag", "inAllCaps"]

// add features for majors dicts
majors.each { major ->
	minors.each { minor ->
		ls = lookups.findAll {it.features["majorType"].equals(major) && it.features["minorType"].equals(minor)}
		fname = major + minor
		
		feats[fname + "count"] = ls.size()
		feats[fname + "sum"] = ls.sum { Double.parseDouble(it.features["s"]) }
		feats[fname + "max"] = ls.max { Math.abs(Double.parseDouble(it.features["s"])) }
		feats[fname + "last"] = Math.abs (Double.parseDouble(ls[-1]?.features["s"] ?: "0"))
		orthos.each { ort
			finerLs = ls.findAll {it.features[ort] == true}
			feats[fname + ort + "count"] = finerLs.size()
			feats[fname + ort + "sum"] = finerLs.sum { Double.parseDouble(it.features["s"]) }
			feats[fname + ort + "max"] = finerLs.max { Math.abs(Double.parseDouble(it.features["s"])) }
			feats[fname + ort + "last"] = Math.abs (Double.parseDouble(finerLs[-1]?.features["s"] ?: "0"))
		}
	}
	pairs.each { pair ->
		count = 0
		sum = 0
		max = 0.0
		Annotation last = null
		lastScore = 0.0
		lookups.findAll {it.features["majorType"].equals(major) && it.features["minorType"].equals(pair)}.each { look ->
			// parse map: ends=just¥-4.999£they¥-4.999£
			endsWithScores = new HashMap<String, Double>();
			look.features.ends.split('£').each { 
				kv = it.split("¥")
				endsWithScores.put(kv[0], Math.abs(Double.parseDouble(kv[1])))
			}
			restString = doc.content.getContent(look.end(), sn.end()).toLowerCase()
			EntrySet matching = endsWithScores.entrySet.findAll { restString.contains(it.key) }
			if(!matching.isEmpty()) {
				count += matching.size()
				sum += matching.sum { it.value }
				tempMax = matching.max { it.value }
				max = tempMax > max ? tempMax : max
				if(last.end() < matching[-1].end()) {
					lastScore = matching[-1].value
				} else if(last.end() == matching[-1].end()) {
					lastScore += matching[-1].value
				}
			}
		}
		fname = major + pair
		feats[fname + "count"] = count
		feats[fname + "sum"] = sum
		feats[fname + "max"] = max
		feats[fname + "last"] = lastScore
	}
}

majors.each { major ->
	minors.each { minor ->
		ls = lookups.findAll {it.features["majorType"].equals(major) && it.features["minorType"].equals(minor)}
		fname = major + minor
		feats[fname + "count"] = ls.size()
		feats[fname + "sum"] = ls.sum { Double.parseDouble(it.features["s"]) }
		feats[fname + "max"] = ls.max { Math.abs(Double.parseDouble(it.features["s"])) }
		feats[fname + "last"] = Math.abs (Double.parseDouble(ls[-1]?.features["s"] ?: "0"))
	}
	
	

hashes = lookups.findAll {it.features["majorType"].equals("nrchash") && it.features["minorType"].equals("pos")}
String fname = "nrchashes"
fmap[fname + "count"] = hashes.size()
fmap[fname + "sum"] = hashes.sum { Double.parseDouble(it.features["s"]) }
fmap[fname + "max"] = hashes.max { Double.parseDouble(it.features["s"]) }
fmap[fname + "last"] = Double.parseDouble(hashes[-1]?.features["s"] ?: "0")


NRC Hashtag: majorType="nrchash", s="double between -9 and 9"
	nrcHashes = lookups.findAll { it.features["majorType"].equals("nrchash") }

def dictFeatures(AnnotationSet toks, String fname, FeatureMap fmap){
	fmap[fname + "count"] = toks.size()
	fmap[fname + "sum"] = toks.sum { Double.parseDouble(it.features["s"]) }
	fmap[fname + "max"] = toks.max { Double.parseDouble(it.features["s"]) } // TODO: check if min is correct for negative
	fmap[fname + "last"] = Double.parseDouble(toks[-1]?.features["s"] ?: "0")
}
*/


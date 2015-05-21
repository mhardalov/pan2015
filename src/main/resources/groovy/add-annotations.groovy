text = doc.getContent().toString()
long start = 0l
long end = text.length() - 1l
FeatureMap map = Factory.newFeatureMap()
outputAS.add (start, end, "person", map)

FeatureMap df = doc.getFeatures();	
df["o"] = "";
df["c"] = "";
df["e"] = "";
df["a"] = "";
df["s"] = "";
df["age"] = "";
df["sex"] = "";
df["id"] = "";
df["lang"] = "";


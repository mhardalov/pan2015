text = doc.getContent().toString()
long start = 0l
long end = text.length() - 1l
FeatureMap map = Factory.newFeatureMap()
outputAS.add (start, end, "person", map)

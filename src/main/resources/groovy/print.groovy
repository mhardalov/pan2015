Annotation gold = doc.getAnnotations("GOLD").get(scriptParams.instanceAnn).iterator().next();
Annotation input = inputAS[scriptParams.instanceAnn].iterator().next();
	
goldLabel = gold.features[scriptParams.labelFeature];
label = input.features[scriptParams.labelFeature];
println(goldLabel + " " + label);

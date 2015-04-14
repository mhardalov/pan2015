inputAS["person"].each { sn ->
	label = doc.features[scriptParams.docFeature];
	println label;
	sn.features["label"] = label;
}

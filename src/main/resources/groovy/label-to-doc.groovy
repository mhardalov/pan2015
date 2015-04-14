inputAS["person"].each { sn ->
	doc.features[scriptParams.docFeature] = sn.features["label"];
}

inputAS["person"].each { sn ->
	lang = doc.features["lang"];
	
	if (lang != "english" && lang != "italian" &&
 			lang != "dutch" && lang != "spanish") {
		doc.features["lang"] = "english"
	}
}

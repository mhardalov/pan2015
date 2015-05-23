age = doc.features["age"]
sex = doc.features["sex"]
doc.getAnnotations("labels").add(0, doc.getContent().size(), age, Factory.newFeatureMap())
doc.getAnnotations("labels").add(0, doc.getContent().size(), sex, Factory.newFeatureMap())

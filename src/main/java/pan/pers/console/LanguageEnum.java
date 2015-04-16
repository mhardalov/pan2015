package pan.pers.console;

/**
 * @author momchil
 * Enum used to map GATE Language Identification languages to PAN standard (lang="en|es|it|nl")
 */
public enum LanguageEnum {
	ENGLISH("english", "en"),
	SPANISH("spanish", "es"),
	ITALIAN("italina", "it"),
	DUTCH("dutch", "nl");
	
	
	private final String lang;
	private final String langShort;

	private LanguageEnum(final String lang, final String langShort) {
		this.lang = lang;
		this.langShort = langShort;
	}
	
	public static LanguageEnum findByText(String lang){
	    for(LanguageEnum v : values()){
	        if( v.getLang().contains(lang) || lang.contains(v.getLang())) {
	            return v;
	        }
	    }
	    return null;
	}

	@Override
	public String toString() {
		return langShort;
	}

	public String getLang() {
		return lang;
	}
}

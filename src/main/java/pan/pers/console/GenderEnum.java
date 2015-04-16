package pan.pers.console;

/**
 * @author momchil
 * Enum used to map Gender char to PAN standard (gender="male|female")
 */
public enum GenderEnum {
	FEMALE("F", "female"),
	MALE("M", "male");
	
	
	private final String genderChar;
	private final String gender;

	private GenderEnum(final String genderChar, final String gender) {
		this.genderChar = genderChar;
		this.gender = gender;
	}
	
	public static GenderEnum findByText(String gender){
	    for(GenderEnum v : values()){
	        if( v.getGenderChar().contains(gender) || gender.contains(v.getGenderChar())) {
	            return v;
	        }
	    }
	    return null;
	}

	@Override
	public String toString() {
		return gender;
	}

	public String getGenderChar() {
		return genderChar;
	}
}

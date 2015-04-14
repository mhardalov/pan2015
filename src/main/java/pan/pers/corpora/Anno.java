package pan.pers.corpora;

public class Anno {
	public Anno(int start, int end, String type) {
		this.type = type;
		this.start = start;
		this.end = end;
	}

	public String type;
	public int start;
	public int end;
}
package tweetsAnalyzer.arff.filter;

import java.util.Vector;

import dictionary.chunk.AbsChunk;

public abstract class ParamFilterAbs {

	protected String[] values;
	
	public void setValues(String[] values){
		this.values = values;
	}
	
	public abstract String apply(Vector<AbsChunk> vc);
	
}

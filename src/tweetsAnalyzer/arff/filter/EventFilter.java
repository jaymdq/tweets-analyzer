package tweetsAnalyzer.arff.filter;

import java.util.Vector;

import dictionary.chunk.AbsChunk;

public class EventFilter extends ParamFilterAbs {
	
	private int limit;
	private String defValue = null;
	
	public EventFilter(int toCheck){
		this.setLimit(toCheck);
	}
	
	public EventFilter(int toCheck, String defValue){
		this.setLimit(toCheck);
		this.setDefValue(defValue);
	}
	
	public void setDefValue(String defValue) {
		this.defValue = defValue;
	}
	
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	@Override
	public String apply(Vector<AbsChunk> chunks) {
		String out = "";
		if (chunks.isEmpty() && this.defValue != null)
			return this.defValue;
		for(int i=0; i < this.limit; i++){
			boolean found = false;
			for(int j=0; !found && j < chunks.size(); j++){
				AbsChunk c = chunks.elementAt(j);
				found = c.getCategoryType().toLowerCase().contains(this.values[i].toLowerCase());
			}
			if( found && !out.contains(this.values[i]) ){
				out += this.values[i]+"_";
				
			}
		}
		out = out.trim();
		if(out.endsWith("_"))
			out = out.substring(0, out.length()-1);
		if (out.isEmpty() && this.defValue != null)
			out = this.defValue;
		return out;
	}
}

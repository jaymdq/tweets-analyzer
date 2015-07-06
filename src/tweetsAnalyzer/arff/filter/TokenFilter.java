package tweetsAnalyzer.arff.filter;

import java.util.Vector;

import dictionary.chunk.AbsChunk;

public class TokenFilter extends ParamFilterAbs {

	private Vector<String> categoryList = new Vector<String>();
	
	public TokenFilter(Vector<String> categoryList){
		this.addCategory(categoryList);
	}
	
	public void addCategory(Vector<String> categoryList){
		this.categoryList.addAll(categoryList);
	}
	
	public void addCategory(String category){
		this.categoryList.addElement(category);
	}
			
	@Override
	public String apply(Vector<AbsChunk> chunks) {
		boolean exist = false;
		for(int i=0; !exist && i < this.categoryList.size(); i++){
			String category = this.categoryList.get(i);
			for(int j=0; !exist && j < chunks.size(); j++){
				AbsChunk c = chunks.elementAt(j);
				exist = c.getCategoryType().toLowerCase().contains(category.toLowerCase());
			}
		}
		if(exist)
			return this.values[0]; //Si
		
		return this.values[1]; // No
	}

}

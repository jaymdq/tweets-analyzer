package tweetsAnalyzer.arff;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Vector;

import tweetsAnalyzer.arff.filter.EventFilter;
import tweetsAnalyzer.arff.filter.ParamFilterAbs;
import tweetsAnalyzer.arff.filter.TokenFilter;
import dictionary.chunk.AbsChunk;

public class ArffGenerator {
	
	private Vector<ArffParameter> parameterList;
	private String nameArff;
	private String path="";
	
	public ArffGenerator(String name){
		this.nameArff = name;
		this.initParams();
	}
	
	public void setPath(String path) {
		this.path = path;
		if(!this.path.endsWith(File.separator))
			this.path += File.separator;
	}

	private void initParams() {
		this.parameterList = new Vector<ArffParameter>();
		
		String[] heridosList = new String[]{ "si_heridos", "no_heridos" };
		ArffParameter heridoParameter = new ArffParameter("herido");
		heridoParameter.setType(heridosList);
		Vector<String> heridosCategories = new Vector<String>();
		heridosCategories.add("Evento Accidente");
		ParamFilterAbs heridosFilter = new TokenFilter(heridosCategories);
		heridosFilter.setValues(heridosList);
		heridoParameter.setFilter(heridosFilter);
		this.parameterList.addElement(heridoParameter);
		
		String[] coches_involucradosList = new String[]{ "si_coches", "no_coches" };
		ArffParameter cochesParameter = new ArffParameter("coches_involucrados");
		cochesParameter.setType(coches_involucradosList);
		Vector<String> cochesCategories = new Vector<String>();
		cochesCategories.add("Vehículo");
		cochesCategories.add("Marca De Auto");
		cochesCategories.add("Marca de Motocicleta");
		cochesCategories.add("Modelo");
		ParamFilterAbs cochesFilter = new TokenFilter(cochesCategories);
		cochesFilter.setValues(coches_involucradosList);
		cochesParameter.setFilter(cochesFilter);
		this.parameterList.addElement(cochesParameter);
		
		String[] ubicacionList = new String[]{ "si_ubicacion", "no_ubicacion" };
		ArffParameter ubicacionParameter = new ArffParameter("ubicacion");
		ubicacionParameter.setType(ubicacionList);
		Vector<String> ubicacionCategories = new Vector<String>();
		ubicacionCategories.add("Calle");
		ubicacionCategories.add("Ruta");
		ubicacionCategories.add("Autopista");
		ubicacionCategories.add("Avenida");
		ubicacionCategories.add("Intersección");
		ubicacionCategories.add("Dirección");
		ParamFilterAbs ubicacionFilter = new TokenFilter(ubicacionCategories);
		ubicacionFilter.setValues(ubicacionList);
		ubicacionParameter.setFilter(ubicacionFilter);
		this.parameterList.addElement(ubicacionParameter);
		
		String[] eventoList = new String[]{ "accidente", "demora", "accidente_demora", "otro" };
		ArffParameter eventoParameter = new ArffParameter("evento");
		eventoParameter.setType(eventoList);
		ParamFilterAbs eventoFilter = new EventFilter(2, "otro"); // Accidente y Demora
		eventoFilter.setValues(eventoList);
		eventoParameter.setFilter(eventoFilter);
		this.parameterList.addElement(eventoParameter);
	}
	
	public void execute(Vector< Vector<AbsChunk> > data){
		Vector<String> fileLines = new Vector<String>();
		fileLines.add("@relation "+this.nameArff);
		fileLines.add("");
		
		for(ArffParameter p: this.parameterList){
			fileLines.add(p.toString());
		}
		fileLines.add("");
		
		fileLines.add("@data");
		for(Vector<AbsChunk> vc : data){
			String tmp = "";
			for(ArffParameter p: this.parameterList){
				tmp += p.getValue(vc)+", ";
			}
			tmp = tmp.substring(0, tmp.length()-2);
			fileLines.add(tmp);
		}
		saveFile(fileLines);
	}

	private void saveFile(Vector<String> fileLines){
		Writer out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.path+this.nameArff+".arff")));
			for(String s: fileLines){
				out.write(s+"\r\n");
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
		    try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

package tweetsAnalyzer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;

import ner.NER;
import preprocess.PreProcess;
import segmentation.Segmenter;
import stream.StreamWorkerAbs;
import stream.plaintext.PlainTextFormatAbs;
import stream.plaintext.PlainTextFormatSimple;
import stream.plaintext.PlainTextFormatTwitter;
import stream.plaintext.StreamPlainTextWorker;
import syntax.SyntaxChecker;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import utils.Pair;
import configuration.ApproximatedDictionaryConfigurator;
import configuration.ExactDictionaryConfigurator;
import configuration.PreProcessConfigurator;
import configuration.RuleBasedConfigurator;
import configuration.SyntaxCheckerConfigurator;
import configuration.TweventConfigurator;
import dictionary.approximatedDictionaries.ApproximatedDictionary;
import dictionary.dictionaryEntry.DictionaryEntry;
import dictionary.exactDictionaries.ExactDictionary;
import dictionary.io.DictionaryIO;
import dictionary.ruleBasedDictionaries.RegExMatcher;
import dictionary.ruleBasedDictionaries.RuleBasedDictionary;
import event.twevent.Twevent;

public class MainWindow {

	private static final String titulo = "Tweets Analyzer";

	private JFrame frame;
	private String[] selectedFilePath = new String[]{ null, null};
	private StreamWorkerAbs streamWorker;
	private JTree tree;
	private Vector<String> tweets = new Vector<String>();
	private Vector<Vector<String>> hashtags = new Vector<Vector<String>>();
	private NER ner;
	private Twevent detector;
	private boolean analyzingTweets;
	private TweetClassifier tweetClassifier;
	private AnalyzerWorker analyzer;
	private JToggleButton btnProcess;
	private JMenuItem mntmTreeToText;

	private String classifierConfigurationLine = "weka.classifiers.trees.J48 -C 0.25 -M 2";
	private boolean configuredNER;
	private JMenuItem mntmClassifyTest;
	private JMenuItem mntmClassify;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame(titulo);
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/tweetsAnalyzer/twitter_64x64.png")));
		frame.setBounds(0,0,java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width,java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height);
		frame.setLocationRelativeTo(null);
		frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmOpenTweets = new JMenuItem("Open Tweets");
		mntmOpenTweets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openTweets();
			}
		});

		JMenuItem mntmNew = new JMenuItem("Clear");
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				clearTree();
			}
		});
		mnFile.add(mntmNew);

		JSeparator separator_2 = new JSeparator();
		mnFile.add(separator_2);
		mnFile.add(mntmOpenTweets);

		JMenuItem mntmOpenText = new JMenuItem("Open Text");
		mntmOpenText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openText();
			}
		});
		mnFile.add(mntmOpenText);

		JSeparator separator = new JSeparator();
		mnFile.add(separator);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frame.dispose();
			}
		});
		mnFile.add(mntmExit);

		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);

		mntmTreeToText = new JMenuItem("Tree to Text");
		mntmTreeToText.setEnabled(false);
		mntmTreeToText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				treeToText();
			}
		});
		mnTools.add(mntmTreeToText);

		JMenu mnNewMenu = new JMenu("Configuration");
		menuBar.add(mnNewMenu);

		JMenuItem mntmLoadConfigurationFile = new JMenuItem("Load Configuration File");
		mntmLoadConfigurationFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				configuration();
			}
		});
		mnNewMenu.add(mntmLoadConfigurationFile);

		JMenu mnClassification = new JMenu("Classification");
		menuBar.add(mnClassification);

		JMenuItem mntmConfigureClassifier = new JMenuItem("Configure Classifier");
		mntmConfigureClassifier.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clasiffierConfiguration();
			}
		});
		mnClassification.add(mntmConfigureClassifier);

		JSeparator separator_3 = new JSeparator();
		mnClassification.add(separator_3);

		JMenuItem mntmTrainClassifier = new JMenuItem("Train Classifier");
		mntmTrainClassifier.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				trainClassifier();
			}
		});
		mnClassification.add(mntmTrainClassifier);

		JSeparator separator_1 = new JSeparator();
		mnClassification.add(separator_1);

		mntmClassify = new JMenuItem("Classify - Test Model");
		mntmClassify.setEnabled(false);
		mntmClassify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				classify();
			}
		});
		mnClassification.add(mntmClassify);

		mntmClassifyTest = new JMenuItem("Classify - Test Tweets");
		mntmClassifyTest.setEnabled(false);
		mntmClassifyTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				classifyTweets();
			}
		});
		mnClassification.add(mntmClassifyTest);

		menuBar.add(Box.createHorizontalGlue());
		btnProcess = new JToggleButton("Process");
		menuBar.add(btnProcess);
		btnProcess.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnProcess.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		btnProcess.setEnabled(false);
		btnProcess.setPreferredSize(mnClassification.getPreferredSize());
		btnProcess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				process(btnProcess.isSelected());
			}
		});

		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		panel.add(scrollPane, BorderLayout.CENTER);

		tree = new JTree();
		tree.setVisibleRowCount(30);
		tree.setFont(new Font("Consolas", Font.PLAIN, 12));
		tree.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tree.setModel(new DefaultTreeModel(
			new TweetDefaultMutableTreeNode("Open a file and load a configuration to start.") {
				private static final long serialVersionUID = 1L;

				{
				}
			}
		));
		tree.setCellRenderer(new TweetRenderer());
		scrollPane.setViewportView(tree);


	}

	//----------------------------------------------------------------------------------------------------------------------------------------

	private void clearTree() {
		tree.setModel(new DefaultTreeModel(
				new TweetDefaultMutableTreeNode("Open a file and load a configuration to start.") {
					private static final long serialVersionUID = 1L;
					{
					}
				}
				));
		tweets.clear();
		hashtags.clear();
		btnProcess.setEnabled(false);
		mntmTreeToText.setEnabled(false);
		configuredNER = false;
	}

	private void openText() {
		selectFile();
		load(0);
	}

	private void openTweets() {
		selectFile();
		load(1);	
	}

	private void selectFile() {
		JFileChooser chooser = new JFileChooser("./");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Plain Text (*.txt)", "txt");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(frame);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			selectedFilePath[0] = chooser.getSelectedFile().getPath();
			selectedFilePath[1] = chooser.getSelectedFile().getName();
		}
	}

	private void load(int option){
		
		PlainTextFormatAbs format = null;
		switch(option){
		case 0:
			format = new PlainTextFormatSimple();
			analyzingTweets = false;
			break;
		case 1:
			format = new PlainTextFormatTwitter();
			analyzingTweets = true;
			break;
		}
		this.streamWorker = new StreamPlainTextWorker(this.selectedFilePath[0], format);
		this.streamWorker.start();

		//Dormirlo para que llegue a levantar los tweets
	/*	try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
*/
		while(streamWorker.isAlive()){
			
		}
		
		//Create the tree
		frame.setTitle(titulo + " - [Loading...]");
		createTree();
		frame.setTitle(titulo);

		mntmTreeToText.setEnabled(!tweets.isEmpty());
		if (configuredNER)
			btnProcess.setEnabled(!tweets.isEmpty());
	}
	
	private void treeToText() {
		if (tweets != null && tweets.size() > 0 ){
			String text = treeToText((TweetDefaultMutableTreeNode) tree.getModel().getRoot(),0);
			TextDialog textDialog = new TextDialog("Tree to Text",text,false);
			textDialog.setVisible(true);
			textDialog.setLocationRelativeTo(null);
		}

	}

	private String treeToText(TweetDefaultMutableTreeNode node,int level){
		String aux="";
		String out=""; 
		if (level > 0)
			aux+="|";
		for (int i = 0; i < level; i++)
			aux+="-";
		out =aux + node.toString() + "<br>\r\n";
		Enumeration<?> childrens = node.children();
		while (childrens.hasMoreElements()){
			TweetDefaultMutableTreeNode nextElement = (TweetDefaultMutableTreeNode) childrens.nextElement();
			out += treeToText(nextElement,level + 1);
		}
		return out;
	}

	private void configuration() {
		//Open FileChoose
		JFileChooser chooser = new JFileChooser("./");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Plain Text (*.txt)", "txt");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(frame);
		String path="";
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			path = chooser.getSelectedFile().getPath();
		}
		if (path.isEmpty())
			return;

		//Read File
		boolean huboError = false;
		String configurationText="";
		Vector<String> lines = new Vector<String>();
		File file = null;
		FileReader fr = null;
		BufferedReader br = null;
		try {
			file = new File (path);
			fr = new FileReader (file);
			br = new BufferedReader(fr);
			String line;
			while( (line=br.readLine()) != null ){
				if (!line.startsWith("#")){
					line = line.trim();
					if (!line.isEmpty()){
						configurationText+=line+"\n";
						lines.add(line);
					}
				}
			}
		}
		catch(Exception e){
			//e.printStackTrace();
			huboError = true;
		}finally{
			try{                   
				if( null != fr ){  
					fr.close();    
				}            
			if (huboError)
				return;
			}catch (Exception e2){}
		}

		//Create de textDialog
		TextDialog textDialog = new TextDialog("Configuration [" + path + "]", configurationText,true);
		textDialog.setEditable(false);
		textDialog.setModal(true);
		textDialog.setLocationRelativeTo(null);
		textDialog.setVisible(true);
		configurationText = textDialog.getText();

		//Configure
		parseAndConfigure(lines);
		configuredNER=true;
		if (!tweets.isEmpty())
			btnProcess.setEnabled(true);

	}

	private void parseAndConfigure(Vector<String> lines) {
		String fileEntries = null;
		boolean toLowerCase = false; //falta
		Vector<Pair<String,String>> preProcessRules = new Vector<Pair<String,String>>();
		String configurationExactDictionary = null;
		Vector<RegExMatcher> ruleBasedRules = new Vector<RegExMatcher>();
		String configurationApproximatedDictionary = null;
		String configurationSyntaxChecker = null;
		Vector<Vector<String>> synonyms = new Vector<Vector<String>>();
		Vector<Pair<Vector<String>,String>> rulesSyntaxChecker = new Vector<Pair<Vector<String>,String>>();
		String configurationTwevent = null;
		
		for (String line : lines){

			int posEqual = line.indexOf("=");
			String option = line.substring(0, posEqual).trim();
			String rightMember = line.substring(posEqual+1, line.length()).trim();

			switch (option){
			case "FILE-ENTRIES":
				fileEntries = rightMember;
				break;
			case "TO-LOWERCASE":
				if (rightMember.toLowerCase().equals("true")){
					toLowerCase=true;
				}
				break;
			case "PREPROCESS-RULE":
				String search = null;
				String replace = null;

				search = rightMember.split("-s")[1].split("-r")[0].trim();
				search = search.substring(1, search.length()-1);
				replace = rightMember.split("-r")[1].trim();
				replace = replace.substring(1, replace.length()-1);

				if (search != null && replace != null){
					preProcessRules.add(new Pair<String,String>(search,replace));
				}

				break;
			case "EXACT-DICTIONARY":
				configurationExactDictionary = rightMember;
				break;
			case "RULE-BASED-DICTIONARY-RULE":
				String search1 = null;
				String means1 = null;

				search1 = rightMember.split("-s")[1].split("-m")[0].trim();
				search1 = search1.substring(1, search1.length()-1);
				means1 = rightMember.split("-m")[1].trim();
				means1 = means1.substring(1, means1.length()-1);

				if (search1 != null && means1 != null){
					ruleBasedRules.add(new RegExMatcher(search1, means1));
				}

				break;
			case "APPROXIMATED-DICTIONARY":
				configurationApproximatedDictionary = rightMember;
				break;
			case "SYNTAX-CHECKER":
				configurationSyntaxChecker = rightMember;
				break;
			case "SYNONYMS":
				Vector<String> arguments2 = Segmenter.getSegmentation(rightMember);
				Vector<String> cleanList = new Vector<String>();
				for (String arg : arguments2){
					cleanList.add(arg.substring(1, arg.length()-1));
				}
				synonyms.add(cleanList);				
				break;
			case "SYNTAX-CHECKER-RULE":
				//-s "Calle" "esquina" "Calle" -m "Interseccion"	
				Vector<String> searches = new Vector<String>();
				Vector<String> cleanSearches = new Vector<String>();

				String means3 = "";

				String toSearch = rightMember.split("-s")[1].split("-m")[0].trim();
				searches = Segmenter.getSegmentation(toSearch);
				for (String s : searches){
					cleanSearches.add(s.substring(1, s.length()-1));
				}

				means3 = rightMember.split("-m")[1].trim();
				means3 = means3.substring(1, means3.length()-1);
				if (!synonyms.isEmpty())
					rulesSyntaxChecker.addAll(SyntaxChecker.createRules(cleanSearches.toArray(new String[]{}), means3, synonyms));
				else
					rulesSyntaxChecker.add(SyntaxChecker.createRule(cleanSearches.toArray(new String[]{}), means3));
				break;
			case "TWEVENT":
				configurationTwevent= rightMember;
				break;
			}

		}

		//Lista de clases a usar
		//Creacion del NER
		ner = new NER(false);
		ner.setToLowerCase(toLowerCase);
		Vector<DictionaryEntry> entradas = new Vector<DictionaryEntry>();
		PreProcess preProcess = null;
		ExactDictionary dic1 = null;
		RuleBasedDictionary dic2 = null;
		ApproximatedDictionary dic3 = null;
		SyntaxChecker syntaxChecker = null;

		//Se crea todo aca
		if (fileEntries != null){

			//Entradas		
			Set<DictionaryEntry> entries = new HashSet<DictionaryEntry>();
			entries.addAll(DictionaryIO.loadPlainTextWithCategories(fileEntries));
			entradas.addAll(new Vector<DictionaryEntry>(entries));

			//Creacion del PreProcess
			if (preProcessRules != null){
				PreProcessConfigurator preProcessConfigurator = new PreProcessConfigurator(PreProcess.class.getName(), preProcessRules);
				preProcess = (PreProcess) preProcessConfigurator.configure("");
				ner.setPreProcess(preProcess);
				ner.setDoPreProcess(!preProcessRules.isEmpty());
			}

			//Creacion de Diccionarios
			//Diccionarios Exactos
			if (configurationExactDictionary != null){
				ExactDictionaryConfigurator eDC = new ExactDictionaryConfigurator(ExactDictionary.class.getName(),entradas);
				dic1 = (ExactDictionary) eDC.configure(configurationExactDictionary);
				ner.addDictionary(dic1);
			}

			//Diccionarios basados en reglas
			if (!ruleBasedRules.isEmpty() ){
				RuleBasedConfigurator rBC = new RuleBasedConfigurator(RuleBasedDictionary.class.getName(), entradas, ruleBasedRules);
				dic2 = (RuleBasedDictionary) rBC.configure("");
				ner.addDictionary(dic2);
			}

			//Diccionarios Aproximados
			if (configurationApproximatedDictionary != null){
				ApproximatedDictionaryConfigurator aDC = new ApproximatedDictionaryConfigurator(ApproximatedDictionaryConfigurator.class.getName(), entradas);
				dic3 = (ApproximatedDictionary) aDC.configure(configurationApproximatedDictionary);
				ner.addDictionary(dic3);
			}

			//Creacion del SyntaxChecker
			if (configurationSyntaxChecker != null && !rulesSyntaxChecker.isEmpty()){
				SyntaxCheckerConfigurator syntaxCheckerConfigurator = new SyntaxCheckerConfigurator(SyntaxChecker.class.getName(), null);
				syntaxChecker = (SyntaxChecker) syntaxCheckerConfigurator.configure(configurationSyntaxChecker);
				syntaxChecker.addRules(rulesSyntaxChecker);
				ner.setSyntaxChecker(syntaxChecker);
			}

		}
		
		//Creacion del Detector de eventos
		detector = null;
		if (configurationTwevent != null){
			TweventConfigurator tweventConfigurator = new TweventConfigurator(Twevent.class.getName());
			detector = (Twevent) tweventConfigurator.configure(configurationTwevent);
		}
		 
		
	}

	private void clasiffierConfiguration() {
		ClassifierConfigurationEditor editor = new ClassifierConfigurationEditor();
		editor.setVisible(true);

		classifierConfigurationLine = editor.getConfiguration();
	}

	private void trainClassifier() {
		JFileChooser chooser = new JFileChooser("./");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("ARFF file (*.arff)", "arff");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(frame);
		String path="";
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			path = chooser.getSelectedFile().getPath();
		}
		if (path.isEmpty())
			return;

		tweetClassifier = new TweetClassifier(classifierConfigurationLine);
		tweetClassifier.trainClassifier(path);

		mntmClassifyTest.setEnabled(true);
		mntmClassify.setEnabled(true);
	}

	private void classify() {
		if (tweetClassifier == null)
			return;

		JFileChooser chooser = new JFileChooser("./");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("ARFF file (*.arff)", "arff");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(frame);
		String path="";
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			path = chooser.getSelectedFile().getPath();
		}
		if (path.isEmpty())
			return;

		String results = tweetClassifier.classify(path);
		results += "\nScheme: [" + classifierConfigurationLine + "]";

		TextDialog textDialog = new TextDialog("Classification [" + path + "]", results,true);
		textDialog.setEditable(false);
		textDialog.setModal(true);
		textDialog.setLocationRelativeTo(null);
		textDialog.setVisible(true);		
	}

	private void classifyTweets() {
		if (tweetClassifier == null)
			return;
		JFileChooser chooser = new JFileChooser("./");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("ARFF file (*.arff)", "arff");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(frame);
		String path="";
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			path = chooser.getSelectedFile().getPath();
		}
		if (path.isEmpty())
			return;

		String results = tweetClassifier.classifyTweets(tweets,path);
		results += "\nScheme: [" + classifierConfigurationLine + "]";

		TextDialog textDialog = new TextDialog("Classification [" + path + "]", results,true);
		textDialog.setEditable(false);
		textDialog.setModal(true);
		textDialog.setLocationRelativeTo(null);
		textDialog.setVisible(true);		
	}

	private void createTree(){
		TweetDefaultMutableTreeNode root = new TweetDefaultMutableTreeNode("Tweets");
		DefaultTreeModel model = new DefaultTreeModel(root);
		this.tweets.clear();
		hashtags.clear();

		String tweet = null;
		Status status = null;
		Integer i = 1;
		if (analyzingTweets){
			while ( ( status = (Status) this.streamWorker.getNextObject()) != null ){
				tweet = status.getText();
				tweets.add(tweet);
				HashtagEntity[] hte = status.getHashtagEntities();
				Vector<String> toAdd = new Vector<String>();
				for (int j = 0; j < hte.length; j++){
					toAdd.add(hte[j].getText());
				}
				hashtags.add(toAdd);

				TweetDefaultMutableTreeNode node = new TweetDefaultMutableTreeNode("<span style=\"color:blue\"><b>[" + i + "]</b></span> [" + tweet + "] ");
				root.add(node);
				i++;
			}
		}else{
			while ( ( tweet = this.streamWorker.getNextTweet()) != null ){
				tweets.add(tweet);			
				TweetDefaultMutableTreeNode node = new TweetDefaultMutableTreeNode("<span style=\"color:blue\"><b>[" + i + "]</b></span> [" + tweet + "] ");
				root.add(node);
				i++;
			}
		}
		root.setText(root.getText() + " [" + (i - 1) + "]");
		tree.setModel(model);
		tree.validate();
	}


	private void process(boolean isSelected) {
		switchButton(!isSelected);
		if (!isSelected){

			if (analyzer != null){
				analyzer.stopRunning();
				while (analyzer.isAlive()){

				}
				analyzer = null;

			}
		}
		else{
			analyzer = new AnalyzerWorker(this, tweets, ner, detector,tree, hashtags);
			analyzer.start();
		}
	}

	public void switchButton(boolean isSelected) {

		if (isSelected){
			btnProcess.setText("Process");
			frame.setTitle(titulo);
			btnProcess.setSelected(false);

		}
		else{
			btnProcess.setText("Stop");
			frame.setTitle(titulo + " - [Processing...]");

		}
	}

}

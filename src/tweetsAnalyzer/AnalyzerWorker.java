package tweetsAnalyzer;

import java.util.Collections;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import ner.NER;
import tweetsAnalyzer.arff.ArffGenerator;
import utils.Pair;
import dictionary.chunk.AbsChunk;
import dictionary.chunk.Chunk;
import dictionary.chunk.ChunkEvent;
import dictionary.chunk.comparator.ChunkComparatorByStart;
import event.twevent.Twevent;

public class AnalyzerWorker extends Thread {

	// Variables

	private MainWindow main;
	private int actualTweetCount;
	private Vector<String> tweets;
	private NER ner;
	private Twevent detector;
	private JTree tree;
	private Vector<Vector<String>> hashtags;
	private volatile boolean running = true;

	// Constructors
	public AnalyzerWorker(MainWindow mainWindow, Vector<String> tweets, NER ner, Twevent detector, JTree tree, Vector<Vector<String>> hashtags){
		setActualTweetCount(0);
		setTweets(tweets);
		setTree(tree);
		setNer(ner);
		setHashtags(hashtags);
		setMain(mainWindow);
		setDetector(detector);
	}

	// Getters and Setters

	public int getActualTweetCount() {
		return actualTweetCount;
	}

	public void setActualTweetCount(int actualTweetCount) {
		this.actualTweetCount = actualTweetCount;
	}

	public Vector<String> getTweets() {
		return tweets;
	}

	public void setTweets(Vector<String> tweets) {
		this.tweets = tweets;
	}

	public NER getNer() {
		return ner;
	}

	public void setNer(NER ner) {
		this.ner = ner;
	}

	public JTree getTree() {
		return tree;
	}

	public void setTree(JTree tree) {
		this.tree = tree;
	}

	public Vector<Vector<String>> getHashtags() {
		return hashtags;
	}

	public void setHashtags(Vector<Vector<String>> hashtags) {
		this.hashtags = hashtags;
	}


	public MainWindow getMain() {
		return main;
	}

	public void setMain(MainWindow main) {
		this.main = main;
	}

	public Twevent getDetector() {
		return detector;
	}

	public void setDetector(Twevent detector) {
		this.detector = detector;
	}

	// Methods

	private Vector<Chunk> sortChunks(Vector<Chunk> chunks){
		@SuppressWarnings("unchecked")
		Vector<Chunk> out = (Vector<Chunk>) chunks.clone();

		Collections.sort(out, new ChunkComparatorByStart());

		return out;
	}

	private Vector<Chunk> removeSamePosChunks(Vector<Chunk> chunks){
		@SuppressWarnings("unchecked")
		Vector<Chunk> out = (Vector<Chunk>) chunks.clone();

		for (Chunk chunk1 : chunks){
			for (Chunk chunk2 : chunks){
				if( out.contains(chunk2) ){
					if ( ! chunk1.equals(chunk2) ){
						if  ( chunk1.start() >= chunk2.start() && chunk1.end() <= chunk2.end() ) {
							if( ! chunk1.getText().equals(chunk2.getText()) ||
									( chunk1.getText().equals(chunk2.getText()) && chunk1.getCategoryType().equals(chunk2.getCategoryType()) ) ){
								if ( out.contains(chunk1) )
									out.remove(chunk1);
							}
						}
					}
				}
			}
		}

		return out;
	}

	public void stopRunning()
	{
		running = false;
	}

	private Vector<Chunk> preProcess(Vector<Chunk> in,String text){
		Vector<Chunk> out = new Vector<Chunk>();

		Chunk chunk = null;
		int i;
		for ( i = 0; i < in.size() - 1; i++){

			if (in.elementAt(i).start() <= in.elementAt(i+1).end()){
				if (chunk == null){
					chunk = new Chunk(in.elementAt(i+1).start(),in.elementAt(i).end(),"",text,1.0);
				}else{
					chunk = new Chunk(in.elementAt(i+1).start(),chunk.end(),"",text,1.0);
				}

			}else{
				if(chunk == null)
					chunk = new Chunk(in.elementAt(i).start(),in.elementAt(i).end(),"",text,1.0);
				out.add(chunk);
				chunk = null;
			}
		}
		if(in.size() > 1){
			if (in.elementAt(i-1).start() <= in.elementAt(i).end()){
				if (chunk == null){
					chunk = new Chunk(in.elementAt(i).start(),in.elementAt(i-1).end(),"",text,1.0);
				}else{
					chunk = new Chunk(in.elementAt(i).start(),chunk.end(),"",text,1.0);
				}

			}
			if(chunk == null)
				chunk = new Chunk(in.elementAt(i).start(),in.elementAt(i).end(),"",text,1.0);
			out.add(chunk);
		}else if(in.size() == 1)
			out.add(in.firstElement());

		return out;
	}

	@Override
	public void run() {

		running = true;
		TweetDefaultMutableTreeNode root = new TweetDefaultMutableTreeNode("Tweets [0" + "/" + this.tweets.size() + "]");
		DefaultTreeModel model = new DefaultTreeModel(root);
		tree.setModel(model);

		//Detect Event things
		Vector< Pair< String, Vector<Chunk> > > toAnalyze = new Vector< Pair< String, Vector<Chunk> > >();


		//Start
		for (int i = 0; i < tweets.size() && running; i++){
			actualTweetCount++; 

			String tweet = tweets.elementAt(i);
			Vector<Chunk> chunks = this.ner.recognize(tweet);
			toAnalyze.add( new Pair< String, Vector<Chunk> >(tweet, chunks) );
			String preProcessedTweet = this.ner.getLastPreProcessedString();

			chunks = sortChunks(chunks);
			Collections.reverse(chunks);
			chunks = removeSamePosChunks(chunks);

			Vector<Chunk> temp = preProcess(chunks,preProcessedTweet);
			StringBuilder auxTweet = new StringBuilder(preProcessedTweet);
			for(Chunk chunk : temp){	
				auxTweet.insert(chunk.end(), "</span>");	
				auxTweet.insert(chunk.start(), "<span style=\"color:red\">");
			}

			TweetDefaultMutableTreeNode node = new TweetDefaultMutableTreeNode( "<span style=\"color:blue\"><b>[" + (i + 1) + "]:</b></span> [" + auxTweet.toString() + "]");
			root.add(node);

			TweetDefaultMutableTreeNode originalTweet = new TweetDefaultMutableTreeNode("Original Tweet: ["+ tweet +"] - ["+ tweet.length() +"]");
			node.add(originalTweet);

			TweetDefaultMutableTreeNode preProcessedNode = new TweetDefaultMutableTreeNode("Pre-Processed Tweet: ["+ preProcessedTweet +"] - [" + preProcessedTweet.length() + "]");
			node.add(preProcessedNode);

			TweetDefaultMutableTreeNode chunksNode = new TweetDefaultMutableTreeNode("Named Entities: ["+chunks.size()+"]");
			node.add(chunksNode);

			Collections.reverse(chunks);
			for (Chunk chunk : chunks){
				TweetDefaultMutableTreeNode newNode = new TweetDefaultMutableTreeNode(chunk.toString());
				chunksNode.add(newNode);
			}

			if (hashtags != null && !hashtags.isEmpty()){
				TweetDefaultMutableTreeNode hashTagsNode = new TweetDefaultMutableTreeNode("HashTags: ["+hashtags.elementAt(i).size()+"]");
				node.add(hashTagsNode);

				for (String ht : hashtags.elementAt(i)){
					TweetDefaultMutableTreeNode newNode = new TweetDefaultMutableTreeNode(ht);
					hashTagsNode.add(newNode);
				}
			}

			root.setText("Tweets [" + actualTweetCount + "/" + this.tweets.size() + "]");

			//Reload
			model.reload();
		}

		// Detect Events
		Vector<ChunkEvent> eventResults = new Vector<ChunkEvent>();
		if (detector != null){
			detector.getFixedWindow().setFixedSize(tweets.size());
			eventResults = detector.detectEvents(toAnalyze,false); 
			TweetDefaultMutableTreeNode eventsDetectedNode = new TweetDefaultMutableTreeNode("<span style=\"color:blue\"><b>Events</b></span>");
			root.add(eventsDetectedNode);

			for (ChunkEvent ce : eventResults){
				eventsDetectedNode.add(new TweetDefaultMutableTreeNode(ce.toString()));
			}

		}

		//This generate arff file
		Vector< Vector<AbsChunk> > chunkList = new Vector< Vector<AbsChunk> >();
		for(Pair< String, Vector<Chunk> > pair : toAnalyze){
			Vector<AbsChunk> vector_tmp = new Vector<AbsChunk>(); 
			vector_tmp.addAll(pair.getPair2());
			vector_tmp.addAll(eventResults);
			chunkList.add(vector_tmp);
		}
		ArffGenerator arffGenerator = new ArffGenerator("lastRun");
		arffGenerator.execute(chunkList);

		model.reload();
		main.switchButton(true);
	}

}



package tweetsAnalyzer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

public class TweetDefaultMutableTreeNode extends DefaultMutableTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String text;
	private Icon openIcon;
	private Icon closedIcon;
	private Icon tweetIcon;

	public TweetDefaultMutableTreeNode() {
		this("Default");
	}

	public TweetDefaultMutableTreeNode( String text ) {
		this.text = text;
		this.setOpenIcon(new ImageIcon("./src/tweetsAnalyzer/twitter_minus_16x16.png"));
		this.setClosedIcon(new ImageIcon("./src/tweetsAnalyzer/twitter_plus_16_16.png"));
		this.setTweetIcon(new ImageIcon("./src/tweetsAnalyzer/twitter_16x16.png"));
	}

	public String getText() {
		return text;
	}

	public void setText( String text ) {
		this.text = text;
	}

	public Icon getOpenIcon() {
		return openIcon;
	}

	public void setOpenIcon( Icon icon ) {
		this.openIcon = icon;
	}

	public Icon getClosedIcon() {
		return closedIcon;
	}

	public void setClosedIcon( Icon icon ) {
		this.closedIcon = icon;
	}

	public Icon getTweetIcon() {
		return tweetIcon;
	}

	public void setTweetIcon(Icon tweetIcon) {
		this.tweetIcon = tweetIcon;
	}
	
	@Override
	public String toString() {
		return text;
	}

}
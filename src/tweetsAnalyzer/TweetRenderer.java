package tweetsAnalyzer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultTreeCellRenderer;

public class TweetRenderer extends DefaultTreeCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Component getTreeCellRendererComponent(JTree m_tree, Object m_value, boolean m_isSelected, boolean m_isExpanded, boolean m_isLeaf, int m_row,  boolean m_hasFocus) {
		Component compObject = super.getTreeCellRendererComponent(m_tree, m_value, m_isSelected, m_isExpanded, m_isLeaf, m_row, m_hasFocus);

		if (m_value instanceof TweetDefaultMutableTreeNode){
			TweetDefaultMutableTreeNode node = (TweetDefaultMutableTreeNode)m_value;
			setText( "" );
			if (m_isLeaf)
				setIcon(node.getTweetIcon());
			else
				setIcon(m_isExpanded ? node.getOpenIcon() : node.getClosedIcon() );
			setBackground(Color.white);
			JPanel panel = new JPanel();
			panel.setLayout( new BorderLayout() );
			panel.add( compObject, BorderLayout.WEST );
			JTextPane textPane = new JTextPane();
			textPane.setContentType("text/html");
			textPane.setText(node.getText());
			setJTextPaneFont(textPane, new Font("Consolas", Font.PLAIN, 12), Color.black);
			panel.add( textPane, BorderLayout.EAST );
			compObject = panel;
		}
		else
		{
			setText(m_value.toString());
		}

		return compObject;
	}

	public static void setJTextPaneFont(JTextPane jtp, Font font, Color c) {
		// Start with the current input attributes for the JTextPane. This
		// should ensure that we do not wipe out any existing attributes
		// (such as alignment or other paragraph attributes) currently
		// set on the text area.
		MutableAttributeSet attrs = jtp.getInputAttributes();

		// Set the font family, size, and style, based on properties of
		// the Font object. Note that JTextPane supports a number of
		// character attributes beyond those supported by the Font class.
		// For example, underline, strike-through, super- and sub-script.
		StyleConstants.setFontFamily(attrs, font.getFamily());
		StyleConstants.setFontSize(attrs, font.getSize());
		StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
		StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);

		// Set the font color
		StyleConstants.setForeground(attrs, c);

		// Retrieve the pane's document object
		StyledDocument doc = jtp.getStyledDocument();

		// Replace the style for the entire document. We exceed the length
		// of the document by 1 so that text entered at the end of the
		// document uses the attributes.
		doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
	}
}
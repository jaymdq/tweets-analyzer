package tweetsAnalyzer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

public class TextDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JScrollPane scrollPane;
	private JTextPane textPane;
	private JPanel noWrapPanel;

	public TextDialog(String title, String text, boolean b) {
		setTitle(title);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setAlwaysOnTop(true);
		this.setBounds(0,0,java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width - 200,java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height - 150);
		this.setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						close();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
		{
			textPane = new JTextPane();
			textPane.setContentType("text/html");
			textPane.setFont(new Font("Consolas", Font.PLAIN, 12));
			//textPane.setEditable(false);
			noWrapPanel = new JPanel( new BorderLayout() );
			noWrapPanel.add( textPane );
		}
		{
			scrollPane = new JScrollPane(noWrapPanel);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			contentPanel.add(scrollPane, BorderLayout.CENTER);
		}
		
		setPlainText(b);
		textPane.setText(text);
	}

	private void close() {
		this.dispose();		
	}

	public String getText() {
		return textPane.getText();
	}

	public void setPlainText(boolean b) {
		if (b)
			textPane.setContentType("plain/text");
		else
			textPane.setContentType("text/html");
	}

	public void setEditable(boolean b) {
		textPane.setEditable(b);
	}
	
}

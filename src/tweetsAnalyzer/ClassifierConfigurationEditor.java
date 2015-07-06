package tweetsAnalyzer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ClassifierConfigurationEditor extends JDialog {

	// Variables
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField txtConfiguration;
	private String line = "weka.classifiers.trees.J48 -C 0.25 -M 2";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ClassifierConfigurationEditor dialog = new ClassifierConfigurationEditor();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	// Constructors
	/**
	 * Create the dialog.
	 */
	public ClassifierConfigurationEditor() {
		setModal(true);
		setResizable(false);
		setTitle("Classifier Scheme Configuration");
		setBounds(0,0,java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width - 500, 120);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			txtConfiguration = new JTextField();
			txtConfiguration.setText(line);
			txtConfiguration.setFont(new Font("Consolas", Font.PLAIN, 14));
			contentPanel.add(txtConfiguration);
			txtConfiguration.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBorder(null);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ok();
					}
				});
				okButton.setPreferredSize(new Dimension(100, 30));
				okButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancel();
					}
				});
				cancelButton.setPreferredSize(new Dimension(100, 30));
				cancelButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	// Getters and Setters

	protected void cancel() {
		txtConfiguration.setText(line);
		this.dispose();
	}


	protected void ok() {
		line = txtConfiguration.getText();
		this.dispose();
		
	}

	public String getConfiguration(){
		return txtConfiguration.getText();
	}

}

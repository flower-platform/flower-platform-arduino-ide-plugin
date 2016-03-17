package org.flowerplatform.flowerino.otaupload;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.flowerplatform.flowerino_plugin.FlowerinoPlugin;

import processing.app.Editor;

public class OtaUploadDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private static final int METHOD_LAN_NON_SECURE = 0;
	private static final int METHOD_LAN_DISPATCHER = 1;
	private static final int METHOD_INTERNET = 2;
	
	private final JPanel contentPanel = new JPanel();
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JTextField tServerSignature;
	private JTextField tBoardIp;
	private JTextField tDispatcherUrl;
	private JTextField tDispatcherUploadKey;
	private JPanel pParamsLan;
	private JRadioButton rdbtnLanDispatcher;
	private JRadioButton rdbtnLanNonSecure;

	private int method = -1;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			OtaUploadDialog dialog = new OtaUploadDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			dialog.setMethod(METHOD_LAN_DISPATCHER);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public OtaUploadDialog() {
		setTitle("Upload OTA - MKR1000 / Zero");
		setBounds(100, 100, 479, 312);
		getContentPane().setLayout(new BorderLayout(0, 0));
		contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.NORTH);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
		{
			JPanel panel = new JPanel();
			panel.setAlignmentX(Component.LEFT_ALIGNMENT);
			panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select method", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
			contentPanel.add(panel);
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			{
				rdbtnLanNonSecure = new JRadioButton("LAN + non secure");
				rdbtnLanNonSecure.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						setMethod(METHOD_LAN_NON_SECURE);
					}
				});
				buttonGroup.add(rdbtnLanNonSecure);
				panel.add(rdbtnLanNonSecure);
			}
			{
				rdbtnLanDispatcher = new JRadioButton("LAN + secure via dispatcher");
				rdbtnLanDispatcher.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						setMethod(METHOD_LAN_DISPATCHER);
					}
				});
				buttonGroup.add(rdbtnLanDispatcher);
				panel.add(rdbtnLanDispatcher);
			}
		}
		{
			pParamsLan = new JPanel();
			pParamsLan.setBorder(new TitledBorder(null, "Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			pParamsLan.setAlignmentX(Component.LEFT_ALIGNMENT);
			contentPanel.add(pParamsLan);
			GridBagLayout gbl_pParamsLan = new GridBagLayout();
			gbl_pParamsLan.columnWidths = new int[]{156, 156, 0};
			gbl_pParamsLan.rowHeights = new int[]{20, 20, 20, 20, 0};
			gbl_pParamsLan.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_pParamsLan.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			pParamsLan.setLayout(gbl_pParamsLan);
			{
				JLabel lblServerSignature = new JLabel("Server signature");
				GridBagConstraints gbc_lblServerSignature = new GridBagConstraints();
				gbc_lblServerSignature.anchor = GridBagConstraints.WEST;
				gbc_lblServerSignature.fill = GridBagConstraints.VERTICAL;
				gbc_lblServerSignature.insets = new Insets(0, 0, 5, 5);
				gbc_lblServerSignature.gridx = 0;
				gbc_lblServerSignature.gridy = 0;
				pParamsLan.add(lblServerSignature, gbc_lblServerSignature);
			}
			{
				tServerSignature = new JTextField();
				tServerSignature.setEditable(false);
				tServerSignature.setAlignmentX(Component.LEFT_ALIGNMENT);
				GridBagConstraints gbc_tServerSignature = new GridBagConstraints();
				gbc_tServerSignature.fill = GridBagConstraints.BOTH;
				gbc_tServerSignature.insets = new Insets(0, 0, 5, 0);
				gbc_tServerSignature.gridx = 1;
				gbc_tServerSignature.gridy = 0;
				pParamsLan.add(tServerSignature, gbc_tServerSignature);
				tServerSignature.setColumns(10);
				tServerSignature.setText(FlowerinoPlugin.getInstance().getGlobalProperties().getProperty("otaUpload.serverSignature"));
			}
			{
				JLabel lblBoardIp = new JLabel("Board IP");
				GridBagConstraints gbc_lblBoardIp = new GridBagConstraints();
				gbc_lblBoardIp.anchor = GridBagConstraints.WEST;
				gbc_lblBoardIp.fill = GridBagConstraints.VERTICAL;
				gbc_lblBoardIp.insets = new Insets(0, 0, 5, 5);
				gbc_lblBoardIp.gridx = 0;
				gbc_lblBoardIp.gridy = 1;
				pParamsLan.add(lblBoardIp, gbc_lblBoardIp);
			}
			{
				tBoardIp = new JTextField();
				GridBagConstraints gbc_tBoardIp = new GridBagConstraints();
				gbc_tBoardIp.fill = GridBagConstraints.BOTH;
				gbc_tBoardIp.insets = new Insets(0, 0, 5, 0);
				gbc_tBoardIp.gridx = 1;
				gbc_tBoardIp.gridy = 1;
				pParamsLan.add(tBoardIp, gbc_tBoardIp);
				tBoardIp.setColumns(10);
				tBoardIp.setText(FlowerinoPlugin.getInstance().getGlobalProperties().getProperty("otaUpload.boardIp"));
			}
			{
				JLabel lblDispatcherUrl = new JLabel("Dispatcher URL");
				GridBagConstraints gbc_lblDispatcherUrl = new GridBagConstraints();
				gbc_lblDispatcherUrl.anchor = GridBagConstraints.WEST;
				gbc_lblDispatcherUrl.fill = GridBagConstraints.VERTICAL;
				gbc_lblDispatcherUrl.insets = new Insets(0, 0, 5, 5);
				gbc_lblDispatcherUrl.gridx = 0;
				gbc_lblDispatcherUrl.gridy = 2;
				pParamsLan.add(lblDispatcherUrl, gbc_lblDispatcherUrl);
			}
			{
				tDispatcherUrl = new JTextField();
				GridBagConstraints gbc_tDispatcherUrl = new GridBagConstraints();
				gbc_tDispatcherUrl.fill = GridBagConstraints.BOTH;
				gbc_tDispatcherUrl.insets = new Insets(0, 0, 5, 0);
				gbc_tDispatcherUrl.gridx = 1;
				gbc_tDispatcherUrl.gridy = 2;
				pParamsLan.add(tDispatcherUrl, gbc_tDispatcherUrl);
				tDispatcherUrl.setColumns(10);
			}
			{
				JLabel lblDispatcherUploadKey = new JLabel("Dispatcher upload key");
				GridBagConstraints gbc_lblDispatcherUploadKey = new GridBagConstraints();
				gbc_lblDispatcherUploadKey.anchor = GridBagConstraints.WEST;
				gbc_lblDispatcherUploadKey.fill = GridBagConstraints.VERTICAL;
				gbc_lblDispatcherUploadKey.insets = new Insets(0, 0, 0, 5);
				gbc_lblDispatcherUploadKey.gridx = 0;
				gbc_lblDispatcherUploadKey.gridy = 3;
				pParamsLan.add(lblDispatcherUploadKey, gbc_lblDispatcherUploadKey);
			}
			{
				tDispatcherUploadKey = new JTextField();
				GridBagConstraints gbc_tDispatcherUploadKey = new GridBagConstraints();
				gbc_tDispatcherUploadKey.fill = GridBagConstraints.BOTH;
				gbc_tDispatcherUploadKey.gridx = 1;
				gbc_tDispatcherUploadKey.gridy = 3;
				pParamsLan.add(tDispatcherUploadKey, gbc_tDispatcherUploadKey);
				tDispatcherUploadKey.setColumns(10);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Upload");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						uploadClicked();
					}
				});
				okButton.setActionCommand("Upload");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public void setMethod(int method) {
		switch (method) {
		case METHOD_LAN_NON_SECURE:
			rdbtnLanNonSecure.setSelected(true);
			tDispatcherUrl.setEnabled(false);
			tDispatcherUploadKey.setEnabled(false);
			tBoardIp.requestFocus();
			break;
		case METHOD_LAN_DISPATCHER:
			rdbtnLanDispatcher.setSelected(true);
			tDispatcherUrl.setEnabled(true);
			tDispatcherUploadKey.setEnabled(true);
			tBoardIp.requestFocus();
			break;
		}
		
		this.method = method;
		
	}

	public void uploadClicked() {
		String ip = tBoardIp.getText();
		Pattern ipPattern = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");		
		if (!ipPattern.matcher(ip).matches()) {
			JOptionPane.showMessageDialog(null, "Invalid IP address!");
			return;
		}
		
		// save settings
		FlowerinoPlugin.getInstance().getGlobalProperties().setProperty("otaUpload.boardIp", ip);
		FlowerinoPlugin.getInstance().getGlobalProperties().setProperty("otaUpload.method", "" + method);
		FlowerinoPlugin.getInstance().writeGlobalProperties();
		
		Runnable uploadTask = () -> {
			try {
				Editor editor = FlowerinoPlugin.getInstance().getEditor();
				editor.statusNotice("Compiling...");
				String fileName = editor.getSketch().build(false, false);
				String filePath = FlowerinoPlugin.getBuildFolder(editor.getSketch()) + File.separator + fileName + ".bin";
				
				editor.statusNotice("Uploading OTA...");
				new OtaUpload(ip, 65500, filePath, FlowerinoPlugin.getInstance().getGlobalProperties().getProperty("otaUpload.serverSignature")).start();
				editor.statusNotice("Done.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		new Thread(uploadTask).start();
		this.dispose();
	}
	
}

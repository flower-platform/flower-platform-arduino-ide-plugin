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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import processing.app.SerialMonitor;
import java.awt.Dimension;

/**
 * 
 * @author Claudiu Matei
 *
 */
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
	private JPanel pParams;
	private JRadioButton rdbtnLanDispatcher;
	private JRadioButton rdbtnLanNonSecure;
	private JRadioButton rdbtnInternet;

	private int method = -1;
	private JTextField tAzureConnectionString;
	private JLabel lblDeviceId;
	private JTextField tDeviceId;
	private JLabel lblAzureConnectionString;
	private JLabel lblDispatcherUploadKey;
	private JLabel lblDispatcherUrl;
	private JLabel lblServerSignature;
	private JLabel lblBoardIp;
	
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
		setMinimumSize(new Dimension(480, 370));
		setModal(true);
		setTitle("Upload OTA - MKR1000 / Zero");
		setBounds(100, 100, 480, 370);
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
			{
				rdbtnInternet = new JRadioButton("Internet via Azure IoT Hub + secure via dispatcher");
				rdbtnInternet.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						setMethod(METHOD_INTERNET);
					}
				});
				buttonGroup.add(rdbtnInternet);
				panel.add(rdbtnInternet);
			}
		}
		{
			pParams = new JPanel();
			pParams.setBorder(new TitledBorder(null, "Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			pParams.setAlignmentX(Component.LEFT_ALIGNMENT);
			contentPanel.add(pParams);
			GridBagLayout gbl_pParams = new GridBagLayout();
			gbl_pParams.columnWidths = new int[]{156, 156, 0};
			gbl_pParams.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
			gbl_pParams.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_pParams.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			pParams.setLayout(gbl_pParams);
			{
				lblServerSignature = new JLabel("Server signature");
				GridBagConstraints gbc_lblServerSignature = new GridBagConstraints();
				gbc_lblServerSignature.anchor = GridBagConstraints.WEST;
				gbc_lblServerSignature.fill = GridBagConstraints.VERTICAL;
				gbc_lblServerSignature.insets = new Insets(0, 0, 5, 5);
				gbc_lblServerSignature.gridx = 0;
				gbc_lblServerSignature.gridy = 0;
				pParams.add(lblServerSignature, gbc_lblServerSignature);
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
				pParams.add(tServerSignature, gbc_tServerSignature);
				tServerSignature.setColumns(10);
				tServerSignature.setText(FlowerinoPlugin.getInstance().getGlobalProperties().getProperty("otaUpload.serverSignature"));
			}
			{
				lblBoardIp = new JLabel("Board IP");
				GridBagConstraints gbc_lblBoardIp = new GridBagConstraints();
				gbc_lblBoardIp.anchor = GridBagConstraints.WEST;
				gbc_lblBoardIp.fill = GridBagConstraints.VERTICAL;
				gbc_lblBoardIp.insets = new Insets(0, 0, 5, 5);
				gbc_lblBoardIp.gridx = 0;
				gbc_lblBoardIp.gridy = 1;
				pParams.add(lblBoardIp, gbc_lblBoardIp);
			}
			{
				tBoardIp = new JTextField();
				GridBagConstraints gbc_tBoardIp = new GridBagConstraints();
				gbc_tBoardIp.fill = GridBagConstraints.BOTH;
				gbc_tBoardIp.insets = new Insets(0, 0, 5, 0);
				gbc_tBoardIp.gridx = 1;
				gbc_tBoardIp.gridy = 1;
				pParams.add(tBoardIp, gbc_tBoardIp);
				tBoardIp.setColumns(10);
				tBoardIp.setText(FlowerinoPlugin.getInstance().getGlobalProperties().getProperty("otaUpload.boardIp"));
			}
			{
				lblDispatcherUrl = new JLabel("Dispatcher URL");
				GridBagConstraints gbc_lblDispatcherUrl = new GridBagConstraints();
				gbc_lblDispatcherUrl.anchor = GridBagConstraints.WEST;
				gbc_lblDispatcherUrl.fill = GridBagConstraints.VERTICAL;
				gbc_lblDispatcherUrl.insets = new Insets(0, 0, 5, 5);
				gbc_lblDispatcherUrl.gridx = 0;
				gbc_lblDispatcherUrl.gridy = 2;
				pParams.add(lblDispatcherUrl, gbc_lblDispatcherUrl);
			}
			{
				tDispatcherUrl = new JTextField();
				GridBagConstraints gbc_tDispatcherUrl = new GridBagConstraints();
				gbc_tDispatcherUrl.fill = GridBagConstraints.BOTH;
				gbc_tDispatcherUrl.insets = new Insets(0, 0, 5, 0);
				gbc_tDispatcherUrl.gridx = 1;
				gbc_tDispatcherUrl.gridy = 2;
				pParams.add(tDispatcherUrl, gbc_tDispatcherUrl);
				tDispatcherUrl.setColumns(10);
				tDispatcherUrl.setText(FlowerinoPlugin.getInstance().getGlobalProperties().getProperty("otaUpload.dispatcherUrl"));
			}
			{
				lblDispatcherUploadKey = new JLabel("Dispatcher upload key");
				GridBagConstraints gbc_lblDispatcherUploadKey = new GridBagConstraints();
				gbc_lblDispatcherUploadKey.anchor = GridBagConstraints.WEST;
				gbc_lblDispatcherUploadKey.fill = GridBagConstraints.VERTICAL;
				gbc_lblDispatcherUploadKey.insets = new Insets(0, 0, 5, 5);
				gbc_lblDispatcherUploadKey.gridx = 0;
				gbc_lblDispatcherUploadKey.gridy = 3;
				pParams.add(lblDispatcherUploadKey, gbc_lblDispatcherUploadKey);
			}
			{
				tDispatcherUploadKey = new JTextField();
				GridBagConstraints gbc_tDispatcherUploadKey = new GridBagConstraints();
				gbc_tDispatcherUploadKey.insets = new Insets(0, 0, 5, 0);
				gbc_tDispatcherUploadKey.fill = GridBagConstraints.BOTH;
				gbc_tDispatcherUploadKey.gridx = 1;
				gbc_tDispatcherUploadKey.gridy = 3;
				pParams.add(tDispatcherUploadKey, gbc_tDispatcherUploadKey);
				tDispatcherUploadKey.setColumns(10);
				tDispatcherUploadKey.setText(FlowerinoPlugin.getInstance().getGlobalProperties().getProperty("otaUpload.dispatcherUploadKey"));
			}
			{
				lblAzureConnectionString = new JLabel("Azure IoT Hub connection string");
				GridBagConstraints gbc_lblAzureIotHub = new GridBagConstraints();
				gbc_lblAzureIotHub.anchor = GridBagConstraints.EAST;
				gbc_lblAzureIotHub.insets = new Insets(0, 0, 5, 5);
				gbc_lblAzureIotHub.gridx = 0;
				gbc_lblAzureIotHub.gridy = 4;
				pParams.add(lblAzureConnectionString, gbc_lblAzureIotHub);
			}
			{
				tAzureConnectionString = new JTextField();
				GridBagConstraints gbc_tAzureConnectionString = new GridBagConstraints();
				gbc_tAzureConnectionString.insets = new Insets(0, 0, 5, 0);
				gbc_tAzureConnectionString.fill = GridBagConstraints.HORIZONTAL;
				gbc_tAzureConnectionString.gridx = 1;
				gbc_tAzureConnectionString.gridy = 4;
				pParams.add(tAzureConnectionString, gbc_tAzureConnectionString);
				tAzureConnectionString.setColumns(10);
				tAzureConnectionString.setText(FlowerinoPlugin.getInstance().getGlobalProperties().getProperty("otaUpload.azureConnectionString"));
			}
			{
				lblDeviceId = new JLabel("Device ID");
				GridBagConstraints gbc_lblDeviceId = new GridBagConstraints();
				gbc_lblDeviceId.anchor = GridBagConstraints.WEST;
				gbc_lblDeviceId.insets = new Insets(0, 0, 5, 5);
				gbc_lblDeviceId.gridx = 0;
				gbc_lblDeviceId.gridy = 5;
				pParams.add(lblDeviceId, gbc_lblDeviceId);
			}
			{
				tDeviceId = new JTextField();
				GridBagConstraints gbc_tDeviceId = new GridBagConstraints();
				gbc_tDeviceId.insets = new Insets(0, 0, 5, 0);
				gbc_tDeviceId.fill = GridBagConstraints.HORIZONTAL;
				gbc_tDeviceId.gridx = 1;
				gbc_tDeviceId.gridy = 5;
				pParams.add(tDeviceId, gbc_tDeviceId);
				tDeviceId.setColumns(10);
				tDeviceId.setText(FlowerinoPlugin.getInstance().getGlobalProperties().getProperty("otaUpload.azureDeviceId"));
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
			tServerSignature.setVisible(true);
			tBoardIp.setVisible(true);
			tDispatcherUrl.setVisible(false);
			tDispatcherUploadKey.setVisible(false);
			tAzureConnectionString.setVisible(false);
			tDeviceId.setVisible(false);
			lblServerSignature.setVisible(true);
			lblBoardIp.setVisible(true);
			lblDispatcherUrl.setVisible(false);
			lblDispatcherUploadKey.setVisible(false);
			lblAzureConnectionString.setVisible(false);
			lblDeviceId.setVisible(false);
			tBoardIp.requestFocus();
			break;
		case METHOD_LAN_DISPATCHER:
			rdbtnLanDispatcher.setSelected(true);
			tServerSignature.setVisible(false);
			tBoardIp.setVisible(true);
			tDispatcherUrl.setVisible(true);
			tDispatcherUploadKey.setVisible(true);
			tAzureConnectionString.setVisible(false);
			tDeviceId.setVisible(false);
			lblServerSignature.setVisible(false);
			lblBoardIp.setVisible(true);
			lblDispatcherUrl.setVisible(true);
			lblDispatcherUploadKey.setVisible(true);
			lblAzureConnectionString.setVisible(false);
			lblDeviceId.setVisible(false);
			tBoardIp.requestFocus();
			break;
		case METHOD_INTERNET:
			rdbtnInternet.setSelected(true);
			tServerSignature.setVisible(false);
			tBoardIp.setVisible(false);
			tDispatcherUrl.setVisible(true);
			tDispatcherUploadKey.setVisible(true);
			tAzureConnectionString.setVisible(true);
			tDeviceId.setVisible(true);
			lblServerSignature.setVisible(false);
			lblBoardIp.setVisible(false);
			lblDispatcherUrl.setVisible(true);
			lblDispatcherUploadKey.setVisible(true);
			lblAzureConnectionString.setVisible(true);
			lblDeviceId.setVisible(true);
			tDispatcherUrl.requestFocus();
			break;
		}
		pack();
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
		FlowerinoPlugin.getInstance().getGlobalProperties().setProperty("otaUpload.method", "" + method);
		FlowerinoPlugin.getInstance().getGlobalProperties().setProperty("otaUpload.boardIp", ip);
		FlowerinoPlugin.getInstance().getGlobalProperties().setProperty("otaUpload.dispatcherUrl", tDispatcherUrl.getText());
		FlowerinoPlugin.getInstance().getGlobalProperties().setProperty("otaUpload.dispatcherUploadKey", tDispatcherUploadKey.getText());
		FlowerinoPlugin.getInstance().getGlobalProperties().setProperty("otaUpload.azureConnectionString", tAzureConnectionString.getText());
		FlowerinoPlugin.getInstance().getGlobalProperties().setProperty("otaUpload.azureDeviceId", tDeviceId.getText());
		FlowerinoPlugin.getInstance().writeGlobalProperties();
		
		Runnable uploadTask = () -> {
			Editor editor = FlowerinoPlugin.getInstance().getEditor();
			try {
				
				// compile
				editor.statusNotice("Compiling...");
				String fileName = editor.getSketch().build(false, false);
				String filePath = FlowerinoPlugin.getBuildFolder(editor.getSketch()) + File.separator + fileName + ".bin";
				
				// suspend serial monitor
				Field field = Editor.class.getDeclaredField("serialMonitor");
				field.setAccessible(true);
				SerialMonitor serialMonitor = (SerialMonitor) field.get(editor);
				if (serialMonitor != null) {
					serialMonitor.suspend();
				}
		    	Thread.sleep(2000);
				
				// upload
				editor.statusNotice("Uploading OTA...");
				switch(method) {
					case METHOD_LAN_NON_SECURE:
						new OtaUpload(filePath).localUpload(ip, FlowerinoPlugin.getInstance().getGlobalProperties().getProperty("otaUpload.serverSignature"));
						break;
					case METHOD_LAN_DISPATCHER:
						new OtaUpload(filePath).dispatcherUpload(ip, tDispatcherUrl.getText(), tDispatcherUploadKey.getText(), "board1", "rAppGroup1");
						break;
					case METHOD_INTERNET:
						new OtaUpload(filePath).azureUpload(tAzureConnectionString.getText(), tDeviceId.getText(), tDispatcherUrl.getText(), tDispatcherUploadKey.getText(), "board1", "rAppGroup1");
						break;
				}
				editor.statusNotice("Done.");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			    try {
			    	Thread.sleep(3000);
			    	Method method = Editor.class.getDeclaredMethod("resumeOrCloseSerialMonitor");
			    	method.setAccessible(true);
			    	method.invoke(editor);
			    } catch (Exception e) {
			    	e.printStackTrace();
			    }
			}
		};
		new Thread(uploadTask).start();
		this.dispose();
	}

	public void uploadFileToDispatcher() {
		
	}
	
}

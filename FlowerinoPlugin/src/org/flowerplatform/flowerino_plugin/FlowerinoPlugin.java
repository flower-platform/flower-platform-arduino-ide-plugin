package org.flowerplatform.flowerino_plugin;

import java.awt.Desktop;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowerplatform.flowerino.otaupload.OtaUploadDialog;
import org.flowerplatform.flowerino_plugin.command.GetBoardsCommand;
import org.flowerplatform.flowerino_plugin.command.HeartbeatCommand;
import org.flowerplatform.flowerino_plugin.command.SelectBoardCommand;
import org.flowerplatform.flowerino_plugin.command.SetOptionsCommand;
import org.flowerplatform.flowerino_plugin.command.UpdateSourceFilesAndCompileCommand;
import org.flowerplatform.flowerino_plugin.command.UploadToBoardCommand;
import org.flowerplatform.flowerino_plugin.library_manager.LibraryManager;
import org.flowerplatform.flowerino_plugin.library_manager.compatibility.AbstractLibraryInstallerWrapper;
import org.flowerplatform.flowerino_plugin.library_manager.compatibility.LibraryInstallerWrapper;
import org.flowerplatform.flowerino_plugin.library_manager.compatibility.LibraryInstallerWrapperPre166;
import org.flowerplatform.tiny_http_server.HttpServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zafarkhaja.semver.Version;

import cc.arduino.contributions.VersionHelper;
import processing.app.BaseNoGui;
import processing.app.Editor;
import processing.app.Sketch;
import processing.app.SketchData;
import processing.app.tools.Tool;

/**
 * @author Cristian Spiescu
 */
public class FlowerinoPlugin implements Tool {

	public static final String RE_GENERATE_FROM_FLOWERINO_REPOSITORY = "(Re)generate from Flowerino Repository";
	
	/**
	 * The name of the folder in which we store temp data related to work happening within flower platform.
	 * Please note this name is not absolute, but relative (i.e. just the folder name, not the full path)
	 */
	public static final String FLOWER_PLATFORM_WORK_FOLDER_NAME = "flower-platform-work";

	protected ActionListener generateActionListener = new ResourceNodeRequiredActionListener(FlowerinoPlugin.this) {
		@Override
		protected void runAfterValidation() {
			if (!libraryVersionCheckedOnce.contains(resourceNodeUri)) {
				showLibraryManager(resourceNodeUri, true);
			}
				
			List<Map<String, Object>> generatedFiles = callService("templateGeneratorService/generateFiles?nodeUri=" + resourceNodeUri 
					+ "&generator=arduino-full&writeToDisk=false", false);

			// write files (with content from JSON)
			for (Map<String, Object> generatedFile : generatedFiles) {
				String fileName = StringUtils.substringAfterLast((String) generatedFile.get("fileNodeUri"), "/");
				File f = null;
				if (fileName.endsWith(".ino")) {
					// for the .ino, we use the name of the sketch, regardless of the name of the Flowerino repository
					// there shouldn't be more than 1 .ino in the list
					f = new File(editor.getSketch().getMainFilePath());
				} else {
					f = new File(editor.getSketch().getFolder(), fileName);
					if ((boolean) generatedFile.get("generateOnce") && f.exists()) {
						// if "generateOnce" and the file already exists => we skip it
						continue;
					}
				}
				try {
					BaseNoGui.saveFile((String) generatedFile.get("content"), f);
					log("File updated: " + f);
				} catch (IOException e1) {
					log("Error while saving file = " + f, e1);
				}
			}
			
			// reload project
			try {
				Method load = Sketch.class.getDeclaredMethod("load", boolean.class);
				load.setAccessible(true);
				load.invoke(editor.getSketch(), true);
			} catch (NoSuchMethodException | SecurityException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e1) {
				log("Error while reloading project", e1);
			}
			log("Sketch reloaded from Flowerino repository: " + fullRepository);
		}
	};
	
	protected ActionListener downloadLibsActionListener = new ResourceNodeRequiredActionListener(FlowerinoPlugin.this) {
		@Override
		protected void runAfterValidation() {
			showLibraryManager(resourceNodeUri, false);
		}
	};
	
	protected void showLibraryManager(String resourceNodeUri, boolean showDialogOnlyIfUpdateNeeded) {
		LibraryManager lm = new LibraryManager(FlowerinoPlugin.this, resourceNodeUri);
		boolean updateNeeded = lm.refreshTable();
		
		if (showDialogOnlyIfUpdateNeeded && !updateNeeded) {
			// I'm not sure if it's necessary, being given that I didn't show it
			lm.dispose();
		} else {
			if (showDialogOnlyIfUpdateNeeded) {
				JOptionPane.showMessageDialog(null, "One or more libraries need to be updated, hence the Required Libraries window will open.");
			}
			lm.setLocationRelativeTo(null);
			lm.setVisible(true);
		}
	}

	public static AbstractLibraryInstallerWrapper libraryInstallerWrapper;

	private static FlowerinoPlugin INSTANCE;

	protected Editor editor;
	protected String serverUrl;
	protected final static String SERVICE_PREFIX = "/ws-dispatcher";
	protected Set<String> libraryVersionCheckedOnce = new HashSet<>();
	protected String version;
	protected Properties globalProperties; 
	
	public Properties getGlobalProperties() {
		return globalProperties;
	}

	public Editor getEditor() {
		return editor;
	}

	public Set<String> getLibraryVersionCheckedOnce() {
		return libraryVersionCheckedOnce;
	}

	@Override
	public void init(Editor editor) {
		// getInstance() always returns first instance created
		if (INSTANCE == null) {
			INSTANCE = this;
		}
		// read version from file; we put it in the file to reuse it easily from ANT, when building the .jar file
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("flowerino-plugin-version.txt")));
			version = r.readLine();
			r.close();
		} catch (IOException e2) {
			log("", e2);
		}
		
		// get/create global properties
		globalProperties = readProperties(getGlobalPropertiesFile());
		boolean writeProperties = false;
		if (globalProperties.getProperty("serverUrl") == null) {
			globalProperties.put("serverUrl", "http://hub.flower-platform.com");
			writeProperties = true;
		}
		if (globalProperties.getProperty("commandServerPort") == null) {
			globalProperties.put("commandServerPort", "9000");
			writeProperties = true;
		}
		if (globalProperties.getProperty("otaUpload.serverSignature") == null) {
			try {
				globalProperties.put("otaUpload.serverSignature", new String(Base64.getEncoder().encode(SecureRandom.getInstanceStrong().generateSeed(32))));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			writeProperties = true;
		}
		if (globalProperties.getProperty("otaUpload.method") == null) {
			globalProperties.put("otaUpload.method", "0");
			writeProperties = true;
		}
		if (writeProperties) {
			writeProperties(globalProperties, getGlobalPropertiesFile());
		}
		serverUrl = globalProperties.getProperty("serverUrl");
		
		try {
			int serverPort = Integer.parseInt(globalProperties.getProperty("commandServerPort"));
			HttpServer server = new HttpServer(serverPort);
			//server.registerCommand("updateSourceFiles", UpdateSourceFilesCommand.class);
			server.registerCommand("uploadToBoard", UploadToBoardCommand.class);
			server.registerCommand("compile", UpdateSourceFilesAndCompileCommand.class);
			server.registerCommand("getBoards", GetBoardsCommand.class);
			server.registerCommand("selectBoard", SelectBoardCommand.class);
			server.registerCommand("setOptions", SetOptionsCommand.class);
			server.registerCommand("heartbeat", HeartbeatCommand.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.editor = editor;
		editor.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
				log("Flowerino Plugin v" + version + " is loading. Using server URL: " + serverUrl);
				initLegacySupport();
				new Thread(new Runnable() {
					@Override
					public void run() {
						// check with version from server
						Map<String, Object> info = callService("arduinoService/getDesktopAgentInfo", true);
						if (info != null) {
							// may be null if server down; so don't hang here
							Version serverVersion = VersionHelper.valueOf((String) info.get("version"));
							if (serverVersion.greaterThan(VersionHelper.valueOf(version))) {
								if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "A newer version of Flowerino Plugin is available. It's recommended to update it.\n"
										+ "Installed version = " + version
										+ ". Latest version = " + serverVersion
										+ ".\n\n"
										+ "Open the web page with download URL and instructions (external web browser)?", "Information", JOptionPane.YES_NO_OPTION)) {
									navigateUrl((String) info.get("downloadUrl"));
								}
							}
						}
					}
				}).start();
				
				// initialize the menu
				JMenu menu = new JMenu("Flowerino");
			    JMenuItem generateMenu = new JMenuItem(RE_GENERATE_FROM_FLOWERINO_REPOSITORY);
				menu.add(generateMenu);
				generateMenu.addActionListener(generateActionListener);
				menu.addSeparator();
				
			    JMenuItem associateMenu = new JMenuItem("Add/Edit Link to Flowerino Repository");
				menu.add(associateMenu);
				associateMenu.addActionListener(evt -> editLinkedRepository(false));
				
			    JMenuItem downloadLibs = new JMenuItem("Download Required Libs");
				menu.add(downloadLibs);
				downloadLibs.addActionListener(downloadLibsActionListener);
				menu.addSeparator();

				menu.add(new JMenuItem("Go to Diagrams: Flowerino > Linked Repository (external web browser)")).addActionListener(new ResourceNodeRequiredActionListener(FlowerinoPlugin.this) {
					@Override
					protected void runAfterValidation() {
						try {
							String[] spl = fullRepository.split("/");
							navigateUrl(serverUrl + "/#/repositories/page/" + spl[0] + URLEncoder.encode("|", "UTF-8") + spl[1] + "/diagram-editor");
						} catch (IOException e1) {
							log("Cannot open url: " + serverUrl, e1);
						}
					}
				});
				
				menu.add(new JMenuItem("Go to Flowerino > Browse Repositories (external web browser)")).addActionListener(e1 -> navigateUrl(serverUrl));
				menu.add(new JMenuItem("Go to Flowerino Web Site (external web browser)")).addActionListener(e1 -> navigateUrl("http://flower-platform.com/flowerino"));
				
				// add Zero OTA Upload menu item
				menu.add(new JMenuItem("Upload OTA - MKR1000 / Zero")).addActionListener(e1 -> zeroOtaUpload());

				editor.getJMenuBar().add(menu, editor.getJMenuBar().getComponentCount() - 1);
				editor.getJMenuBar().revalidate();
			}
			
			@Override
			public void componentResized(ComponentEvent e) {}
			@Override
			public void componentMoved(ComponentEvent e) {}
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
	}
	
	protected void initLegacySupport() {
		String currentVersionStr = "0.0.0";
		try {
			// we use reflection, because otherwise the compile time version would be "stamped" into the jar
			Field versionName = BaseNoGui.class.getField("VERSION_NAME");
			currentVersionStr = (String) versionName.get(null);
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			log("Error getting the Arduino IDE version", e);
		}
		Version currentVersion = VersionHelper.valueOf(currentVersionStr);
		Version v165 = VersionHelper.valueOf("1.6.5");
		if (currentVersion.lessThan(v165)) {
			log("WARNING! Your Arduino IDE v" + currentVersion + " is too old. Flowerino Plugin "
					+ "has been tested with Arduino IDE starting with v" + v165 + ". Unexpected errors may appear.");
		} else if (currentVersion.lessThan(VersionHelper.valueOf("1.6.6"))) {
			libraryInstallerWrapper = new LibraryInstallerWrapperPre166();
		} else {
			libraryInstallerWrapper = new LibraryInstallerWrapper();
		}
	}
	
	public void navigateUrl(String url) {
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (IOException | URISyntaxException e1) {
			log("Cannot open url: " + serverUrl);
		}		
	}
	
	/**
	 * @author Claudiu Matei
	 */
	public void zeroOtaUpload() {
		OtaUploadDialog dialog = new OtaUploadDialog();
		Integer method = Integer.parseInt(globalProperties.getProperty("otaUpload.method"));
		dialog.setMethod(method);
		dialog.setVisible(true);
	}
	
	@Override
	public void run() {
		generateActionListener.actionPerformed(null);
	}

	@Override
	public String getMenuTitle() {
		return RE_GENERATE_FROM_FLOWERINO_REPOSITORY;
	}
	
	public static void log(String message) {
		System.out.println(message);
	}
	
	public static void log(String message, Throwable t) {
		System.out.println(message);
		t.printStackTrace(System.out);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T callService(String urlWithoutPrefix, boolean failSilently) {
		URL url = null;
		BufferedReader in = null;
		try {
			url = new URL(serverUrl + SERVICE_PREFIX + "/" + urlWithoutPrefix);
			URLConnection yc = url.openConnection();
			in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> result = (Map<String, Object>) objectMapper.readValue(in, HashMap.class);
			return (T) result.get("messageResult");
		} catch (IOException e1) {
			if (!failSilently) {
				log("Error while accessing url = " + url, e1);
			}
			return null;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
	
	public File getProjectPropertiesFile() {
		return new File(editor.getSketch().getFolder(), ".flowerino-link");
	}
	
	public File getGlobalPropertiesFile() {
		return new File(BaseNoGui.getSketchbookFolder(), ".flowerino");
	}

	public Properties readProperties(File file) {
		Properties properties = new Properties();
		if (file.exists()) {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				properties.load(is);
			} catch (IOException e1) {
				log("Error while opening " + ".flowerino-link" + " file from " + file.getAbsolutePath(), e1);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e1) {
						log("Error while opening " + ".flowerino-link" + " file from " + file.getAbsolutePath(), e1);
					}
				}
			}
		}
		return properties;
	}
	
	public void writeProperties(Properties properties, File file) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			properties.store(os, null);
		} catch (IOException e1) {
			log("Error while saving " + ".flowerino-link" + " file in " + file.getAbsolutePath(), e1);
		} finally {
			try {
				os.close();
			} catch (IOException e1) {
				log("Error while saving " + ".flowerino-link" + " file in " + file.getAbsolutePath(), e1);
			}
		}
		log("Config info successfully saved in " + file.getAbsolutePath());
	}
	
	public void writeGlobalProperties() {
		writeProperties(globalProperties, getGlobalPropertiesFile());
	}
	
	public String getResourceNodeUri(String fullRepository) {
		if (fullRepository == null || fullRepository.isEmpty()) {
			return null;
		}
		String repository = StringUtils.substringAfter(fullRepository, "/");
		return "fpp:" + fullRepository + "|" + repository + ".flower-platform";
	}
	
	public String editLinkedRepository(boolean showAdditionalText) {
		Properties properties = readProperties(getProjectPropertiesFile());
		String message = "Full repository name from Flowerino (e.g. myUser/myRepo)";
		if (showAdditionalText) {
			message = "Before continuing, please link this project with a Flowerino repository.\n\n" + message;
		}
		
		String result = JOptionPane.showInputDialog(message, properties.get("fullRepository"));
		if (result == null) {
			return null;
		}
		properties.put("fullRepository", result);

		writeProperties(properties, getProjectPropertiesFile());
		return result;
	}

	public static File getBuildFolder(Sketch sketch) throws IOException {
		SketchData sketchData = null;
		try {
			Field f;
			f = sketch.getClass().getDeclaredField("data");
			f.setAccessible(true);
			sketchData = (SketchData) f.get(sketch); 
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		} 
		return BaseNoGui.getBuildFolder(sketchData);
	}

	public static File getFlowerPlatformWorkFolder() {
		File f = new File("C:\\" + FLOWER_PLATFORM_WORK_FOLDER_NAME);
		f.mkdirs();
		return f;
	}
	
	public static FlowerinoPlugin getInstance() {
		return INSTANCE;
	}
	
}
 
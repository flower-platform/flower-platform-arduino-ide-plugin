package org.flowerplatform.flowerino_plugin.library_manager;

import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;

import org.flowerplatform.flowerino_plugin.FlowerPlatformPlugin;
import org.flowerplatform.flowerino_plugin.library_manager.LibraryManagerEntry.Action;
import org.flowerplatform.flowerino_plugin.library_manager.LibraryManagerEntry.Status;
import org.flowerplatform.flowerino_plugin.library_manager.renderer.ActionCellRenderer;
import org.flowerplatform.flowerino_plugin.library_manager.renderer.StatusCellRenderer;

import processing.app.Base;
import processing.app.packages.UserLibrary;
import cc.arduino.contributions.VersionHelper;

import com.github.zafarkhaja.semver.Version;

/**
 * The UI for managing required libraries. It has been created and maintained with the visual editor from Eclipse (Window Builder Pro).
 * 
 * @author Cristian Spiescu
 */
public class LibraryManager extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTable table;
	
	private LibraryManagerTableModel model = new LibraryManagerTableModel();
	private boolean duplicateLibraries = false;
	
	private JTextPane textPane;

	private JScrollPane textPaneScrollPane;
	
	private FlowerPlatformPlugin flowerinoDesktopAgent;
	
	private String resourceNodeUri;

	/**
	 * Create the frame.
	 */
	public LibraryManager(FlowerPlatformPlugin flowerinoDesktopAgent, String resourceNodeUri) {
		super(flowerinoDesktopAgent.getEditor(), true);
		this.flowerinoDesktopAgent = flowerinoDesktopAgent;
		this.resourceNodeUri = resourceNodeUri;
		
		setTitle("Required Libraries");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 800, 700);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		
		Component rigidArea_5 = Box.createRigidArea(new Dimension(0, 5));
		contentPane.add(rigidArea_5);
		
		JLabel lblNewLabel = new JLabel("Required Libraries");
		contentPane.add(lblNewLabel);
		
		Component rigidArea = Box.createRigidArea(new Dimension(0, 5));
		contentPane.add(rigidArea);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		contentPane.add(scrollPane);
		
		table = new JTable();
		table.setIntercellSpacing(new Dimension(5, 1));
		table.setRowHeight(25);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(table);
		table.setAlignmentX(Component.LEFT_ALIGNMENT);
		table.setModel(model);

		table.getColumnModel().getColumn(0).setPreferredWidth(400);
		table.getColumnModel().getColumn(1).setPreferredWidth(200);
		table.getColumnModel().getColumn(2).setPreferredWidth(200);
		
		TableColumn statusColumn = table.getColumnModel().getColumn(1);
		statusColumn.setCellRenderer(new StatusCellRenderer());
		
		TableColumn actionColumn = table.getColumnModel().getColumn(2);
		actionColumn.setCellRenderer(new ActionCellRenderer());
		actionColumn.setCellEditor(new DefaultCellEditor(new ActionCellRenderer()));
		
		table.getSelectionModel().addListSelectionListener(e -> selectionChanged(e));
		
		Component rigidArea_2 = Box.createRigidArea(new Dimension(0, 10));
		contentPane.add(rigidArea_2);
		
		JLabel lblDescriptionforThe = new JLabel("Description (for the selected library)");
		contentPane.add(lblDescriptionforThe);
		
		Component rigidArea_4 = Box.createRigidArea(new Dimension(0, 5));
		contentPane.add(rigidArea_4);
		
		textPaneScrollPane = new JScrollPane();
		textPaneScrollPane.setMinimumSize(new Dimension(23, 50));
		textPaneScrollPane.setPreferredSize(new Dimension(2, 250));
		textPaneScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		contentPane.add(textPaneScrollPane);
		
		textPane = new JTextPane();
		textPane.setContentType("text/html");
		textPaneScrollPane.setViewportView(textPane);
		textPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JPanel panel = new JPanel();
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		contentPane.add(panel);
		
		JButton btnapplySelectedActions = new JButton("Apply Selected Actions");
		btnapplySelectedActions.setAlignmentX(0.5f);
		panel.add(btnapplySelectedActions);
		btnapplySelectedActions.addActionListener(e -> applyActions());
		
		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(e -> close());
		btnClose.setAlignmentX(0.5f);
		panel.add(btnClose);
	}

	@SuppressWarnings("unchecked")
	public boolean refreshTable() {
		flowerinoDesktopAgent.getLibraryVersionCheckedOnce().add(resourceNodeUri);
		boolean updateNeeded = false;
		model.getEntries().clear();
		
		List<Map<String, Object>> dependentLibraries = flowerinoDesktopAgent.callService("arduinoService/getDependentLibraryDescriptors?resourceNodeUri=" + resourceNodeUri, false);
		
		// index all the required libs by header file
		Map<String, Map<String, Object>> lookup = new HashMap<>();
		for (Map<String, Object> lib : dependentLibraries) {
			for (String headerFile : (List<String>) lib.get("headerFiles")) {
				lookup.put(headerFile, lib);
			}
		}
		
		// iteration over all libs on the disk
		for (UserLibrary lib : Base.INSTANCE.getUserLibs()) {
			LibraryManagerEntry entry = new LibraryManagerEntry();
			try {
				entry.setExistingHeaderFiles(Base.headerListFromIncludePath(lib.getSrcFolder()));
			} catch (IOException e) {
				FlowerPlatformPlugin.log("Cannot get header files for dir = " + lib.getSrcFolder(), e);
				continue;
			}
			
			// we try to match a required lib, based on the lib from the disk (based on header files)
			Set<String> remainingHeaderFilesToCheck = null;
			for (String headerFile : entry.getExistingHeaderFiles()) {
				if (remainingHeaderFilesToCheck == null) {
					// no match found yet; try again
					Map<String, Object> requiredLib = lookup.get(headerFile);
					if (requiredLib == null) {
						// try next file; maybe this lib IS among the required ones, but this particular
						// header file is not needed
						continue;
					}
					// aha, we found a match...
					model.getEntries().add(entry);
					if (requiredLib.containsKey("matched")) {
						duplicateLibraries = true;
					}
					requiredLib.put("matched", true);
					entry.setRequiredLibrary(requiredLib);
					entry.setName((String) requiredLib.get("name"));

					entry.setExistingLibrary(lib);
					// let's continue the iteration, to make sure that all required
					// header files exist on the disk
					remainingHeaderFilesToCheck = new HashSet<>((List<String>) requiredLib.get("headerFiles"));
				}
				// if we are here => we have found a library
				remainingHeaderFilesToCheck.remove(headerFile);
			}
			
			if (remainingHeaderFilesToCheck != null) {
				// i.e. we found a match
				if (remainingHeaderFilesToCheck.isEmpty()) {
					// all the required header files have been matched
					// what about the version?
					if (entry.getExistingLibrary().getVersion() == null) {
						if (entry.getRequiredLibrary().get("minVersion") == null) {
							entry.setStatus(Status.UNKNOWN);
							entry.setAction(Action.NONE);
						} else {
							updateNeeded = true;
							entry.setStatus(Status.NEEDS_UPDATE);
							entry.setAction(Action.DOWNLOAD);
						}
					} else {
						Version minVersion = VersionHelper.valueOf((String) entry.getRequiredLibrary().get("minVersion"));
						if (minVersion != null && minVersion.greaterThan(VersionHelper.valueOf(entry.getExistingLibrary().getVersion()))) {
							updateNeeded = true;
							entry.setStatus(Status.NEEDS_UPDATE);
							entry.setAction(Action.DOWNLOAD);
						} else {
							entry.setStatus(Status.OK);
							entry.setAction(Action.NONE);
						}
					}
				} else {
					// there are still required header files; probably the existing lib is older
					updateNeeded = true;
					entry.setStatus(Status.NEEDS_UPDATE);
					entry.setAction(Action.DOWNLOAD);
				}
			} // else => the current lib is not among the required ones
		}
		
		// and now let's see if all required libs have been matched
		for (Map<String, Object> requiredLib : dependentLibraries) {
			if (requiredLib.containsKey("matched")) {
				continue;
			}
			// aha, this one was not matched
			LibraryManagerEntry entry = new LibraryManagerEntry();
			model.getEntries().add(entry);
			entry.setRequiredLibrary(requiredLib);
			entry.setName((String) requiredLib.get("name"));

			updateNeeded = true;
			entry.setStatus(Status.NEEDS_DOWNLOAD);
			entry.setAction(Action.DOWNLOAD);
		}		

		model.fireTableDataChanged();
		return updateNeeded;
	}
	
	@SuppressWarnings("unchecked")
	protected void selectionChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}
		LibraryManagerEntry entry = (LibraryManagerEntry) model.getEntries().get(table.getSelectedRow());
		String content = "";
		if (duplicateLibraries) {
			content += "<font color='red'>WARNING: There seem to be duplicate libraries. This may cause unwanted compile/execution issues!</font><br/>";
		}
		
		if (entry.getStatus().equals(Status.UNKNOWN)) {
			content += "<font color='gray'>NOTE: This library doesn't have versioning information. If you didn't update it for a long time, and you suspect "
					+ "that the installed library is obsolete, then please update it.</font><br/>";
		}
		
		if (!content.isEmpty()) {
			content += "<br/>";
		}
		
		if (entry.getExistingLibrary() != null) {
			content += "<b>Existing library info:</b>"
					+ "<br/> Name: " + entry.getExistingLibrary().getName()
					+ "<br/> Version: " + entry.getExistingLibrary().getVersion()
					+ "<br/> Install dir: " + entry.getExistingLibrary().getSrcFolder()
					+ "<br/> Web site: " + entry.getExistingLibrary().getWebsite()
					+ "<br/> Header files: " + String.join(", ", entry.getExistingHeaderFiles())
					+ "<br/><br/>"; 
		}
		content += "<b>Required library info:</b>"
				+ "<br/>Name: " + entry.getRequiredLibrary().get("name")
				+ "<br/>Min version: " + entry.getRequiredLibrary().get("minVersion")
				+ "<br/>Web site: " + entry.getRequiredLibrary().get("website")
				+ "<br/>URL: " + entry.getRequiredLibrary().get("url")
				+ "<br/>Header files: " + (String.join(", ", (List<String>) entry.getRequiredLibrary().get("headerFiles")));
		textPane.setText(content);
		textPane.setCaretPosition(0);
	}

	@SuppressWarnings("incomplete-switch")
	protected void applyActions() {
		if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(this, "Apply selected actions: please confirm.\n(Results will be printed to the console)", "Confirmation Needed", JOptionPane.OK_CANCEL_OPTION)) {
			return;
		}
		close();
		
		try {
			for (LibraryManagerEntry entry : model.getEntries()) {
				FlowerPlatformPlugin.log("For required library: " + entry.getName() + ", applying action: " + entry.getAction());
				switch (entry.getAction()) {
				case DELETE:
					if (entry.getExistingLibrary() == null) {
						break;
					}
					FlowerPlatformPlugin.libraryInstallerWrapper.remove(entry.getExistingLibrary());
					break;
				case DOWNLOAD:
					RequiredLibraryWrapper requiredLibrary = new RequiredLibraryWrapper(entry.getRequiredLibrary());
					FlowerPlatformPlugin.libraryInstallerWrapper.install(requiredLibrary, entry.getExistingLibrary());
					break;
				}
			}
		} catch (Exception e) {
			FlowerPlatformPlugin.log("Error while applying actions", e);
		}
	}
	
	protected void close() {
		setVisible(false);
		dispose();
	}	
}

package org.flowerplatform.flowerino_plugin.library_manager.renderer;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.flowerplatform.flowerino_plugin.library_manager.LibraryManagerEntry;

/**
 * @author Cristian Spiescu
 */
public class ActionCellRenderer extends JComboBox<LibraryManagerEntry.Action> implements
		TableCellRenderer {

	private static final long serialVersionUID = 1L;

	public ActionCellRenderer() {
		super(LibraryManagerEntry.Action.values());
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		setSelectedItem(value);
		return this;
	}

}

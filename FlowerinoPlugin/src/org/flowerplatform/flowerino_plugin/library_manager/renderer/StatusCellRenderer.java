package org.flowerplatform.flowerino_plugin.library_manager.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.flowerplatform.flowerino_plugin.library_manager.LibraryManagerEntry;

/**
 * @author Cristian Spiescu
 */
public class StatusCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		switch ((LibraryManagerEntry.Status) value) {
		case OK:
			setForeground(Color.BLUE);
			break;
		case NEEDS_DOWNLOAD:
		case NEEDS_UPDATE:
			setForeground(Color.RED);
			break;
		case UNKNOWN:
			setForeground(Color.GRAY);
			break;

		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}

}

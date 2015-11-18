package org.flowerplatform.flowerino_plugin.library_manager;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * @author Cristian Spiescu
 */
public class LibraryManagerTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private List<LibraryManagerEntry> entries = new ArrayList<>();

	public List<LibraryManagerEntry> getEntries() {
		return entries;
	}

	@Override
	public int getRowCount() {
		return entries.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Library";
		case 1:
			return "Status";
		case 2:
			return "Action to Apply";
		default:
			return null;
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		LibraryManagerEntry entry = entries.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return entry.getName();
		case 1:
			return entry.getStatus();
		case 2:
			return entry.getAction();
		default:
			return null;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 2;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		LibraryManagerEntry entry = entries.get(rowIndex);
		entry.setAction((LibraryManagerEntry.Action) aValue);
	}

}

package org.flowerplatform.flowerino_plugin.command;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MenuListener;

import org.flowerplatform.flowerino_plugin.FlowerPlatformPlugin;
import org.flowerplatform.tiny_http_server.IHttpCommand;

import processing.app.Editor;

/**
 * 
 * @author Claudiu Matei
 *
 */
public class SelectBoardCommand implements IHttpCommand {

	private String board;
	
	public Object run() {
		Editor editor = FlowerPlatformPlugin.getInstance().getEditor();
		JMenuBar menuBar = editor.getJMenuBar();
		JMenu toolsMenu = menuBar.getMenu(3);
		toolsMenu.getListeners(MenuListener.class)[0].menuSelected(null);
		toolsMenu.getListeners(MenuListener.class)[1].menuSelected(null);

		// find "Board" menu
		JMenu boardMenu = null;
		for (Component c : toolsMenu.getMenuComponents()) {
			if (!(c instanceof JMenu) || !c.isVisible()) {
				continue;
			}
			JMenu menu = (JMenu) c;
			if (menu.getText().startsWith("Board")) {
				boardMenu = menu;
				break;
			}
		}
		
		// select board
		boolean boardFound = false;
		for (int i = 0; i < boardMenu.getItemCount(); i++) {
			JMenuItem item = boardMenu.getItem(i);
			if (item != null && item.isEnabled()) {
				if (item.getText().equals(board)) {
					boardFound = true;
					item.setSelected(true);
					item.getActionListeners()[0].actionPerformed(null);
					System.out.println("Board selected: " + board);
				}
			}
		}
		if (!boardFound) {
			throw new RuntimeException("Invalid board name or board package not installed.");
		}
		
		for (Component c : toolsMenu.getMenuComponents()) {
			if (!(c instanceof JMenu) || !c.isVisible()) {
				continue;
			}
			JMenu menu = (JMenu) c;
			if (menu.getText().startsWith("Board")) {
				boardMenu = menu;
				break;
			}
		}

		// get menu options
		Map<String, List<String>> options = new HashMap<>(); 
		for (Component c : toolsMenu.getMenuComponents()) {
			if ((c instanceof JMenu) && c.isVisible()) {
				JMenu menu = (JMenu) c;
				String option = menu.getText();
				if (option == null) {
					continue;
				}
				int index = option.indexOf(':');
				if (index > 0) {
					option = option.substring(0, index);
				}
				if (option.equals("Board") || option.equals("Programmer")) {
					continue;
				}
				List<String> values = new ArrayList<>();
				for (int i = 0; i <  menu.getItemCount(); i++) {
					JMenuItem item = menu.getItem(i);
					if (item != null && item.isVisible()) {
						values.add(item.getText());
					}
				}
				options.put(option, values);
			}
		}

		
		return options;
	}

	public String getBoard() {
		return board;
	}

	public void setBoard(String board) {
		this.board = board;
	}
	
}

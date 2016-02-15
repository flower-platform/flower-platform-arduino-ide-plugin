package org.flowerplatform.flowerino_plugin.command;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MenuListener;

import org.flowerplatform.flowerino_plugin.FlowerinoPlugin;
import org.flowerplatform.tiny_http_server.IHttpCommand;

import processing.app.Editor;

/**
 * 
 * @author Claudiu Matei
 *
 */
public class GetBoardsCommand implements IHttpCommand {

	public Object run() {
		Editor editor = FlowerinoPlugin.getInstance().getEditor();
		JMenuBar menuBar = editor.getJMenuBar();
		JMenu toolsMenu = menuBar.getMenu(3);
		toolsMenu.getListeners(MenuListener.class)[0].menuSelected(null);

		JMenu boardsMenu = null;
		for (Component c : toolsMenu.getMenuComponents()) {
			if (!(c instanceof JMenu) || !c.isVisible()) {
				continue;
			}
			JMenu menu = (JMenu) c;
			if (menu.getText().startsWith("Board")) {
				boardsMenu = menu;
				break;
			}
		}
		List<String> boards = new ArrayList<>();
		// start from 1 (skip first entry - "Boards manager")
		for (int i = 1; i < boardsMenu.getItemCount(); i++) {
			JMenuItem item = boardsMenu.getItem(i);
			if (item != null && item.isEnabled()) {
//				System.out.println(item.getText());
				boards.add(item.getText());
			}
		}
		
		return boards;
	}

}

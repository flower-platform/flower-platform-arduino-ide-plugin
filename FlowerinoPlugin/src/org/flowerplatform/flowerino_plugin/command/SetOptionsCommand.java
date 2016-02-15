package org.flowerplatform.flowerino_plugin.command;

import java.awt.Component;
import java.util.Map;

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
public class SetOptionsCommand implements IHttpCommand {

	private Map<String, String> options;
	
	public Object run() {
		Editor editor = FlowerinoPlugin.getInstance().getEditor();
		JMenuBar menuBar = editor.getJMenuBar();
		JMenu toolsMenu = menuBar.getMenu(3);
		toolsMenu.getListeners(MenuListener.class)[0].menuSelected(null);
		toolsMenu.getListeners(MenuListener.class)[1].menuSelected(null);

		// set menu options
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
				String newValue = options.get(option);
				for (int i = 0; i < menu.getItemCount(); i++) {
					JMenuItem item = menu.getItem(i);
					if (item != null && item.isVisible() && item.getText().equals(newValue)) {
						item.setSelected(true);
					}
				}
			}
		}

		
		return null;
	}

	public Map<String, String> getOptions() {
		return options;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}
	
	
	
}

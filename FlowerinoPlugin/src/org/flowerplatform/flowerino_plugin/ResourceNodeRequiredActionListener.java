package org.flowerplatform.flowerino_plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * If the project is linked => it's OK, and it delegates to {@link #runAfterValidation()}. Otherwise,
 * the "link" dialog is opened, and if an input has been made then it delegates to {@link #runAfterValidation()}.
 * 
 * @author Cristian Spiescu
 */
public abstract class ResourceNodeRequiredActionListener implements ActionListener {

	protected FlowerinoPlugin flowerinoDesktopAgent;
	
	protected String resourceNodeUri;
	
	protected String fullRepository;
	
	public ResourceNodeRequiredActionListener(
			FlowerinoPlugin flowerinoDesktopAgent) {
		super();
		this.flowerinoDesktopAgent = flowerinoDesktopAgent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		fullRepository = flowerinoDesktopAgent.readProperties(flowerinoDesktopAgent.getProjectPropertiesFile()).getProperty("fullRepository");
		if (fullRepository == null || fullRepository.isEmpty()) {
			fullRepository = flowerinoDesktopAgent.editLinkedRepository(true);
			if (fullRepository == null || fullRepository.isEmpty()) {
				return;
			} 
		}
		resourceNodeUri = flowerinoDesktopAgent.getResourceNodeUri(fullRepository);
		runAfterValidation();
	}
	
	protected abstract void runAfterValidation();

}

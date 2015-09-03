package com.redhat.importer;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

class Config {
	static void writeEclipseConfig() throws BackingStoreException {
		IEclipsePreferences node;
		
		node = ConfigurationScope.INSTANCE.getNode("org.eclipse.ui.ide");
		node.putBoolean("SHOW_WORKSPACE_SELECTION_DIALOG", false);
		node.flush();
		
		node = InstanceScope.INSTANCE.getNode("org.eclipse.ui.ide");
		node.putBoolean("EXIT_PROMPT_ON_CLOSE_LAST_WINDOW", false);
		node.flush();

		node = InstanceScope.INSTANCE.getNode("org.eclipse.egit.ui");
		node.putBoolean("show_initial_config_dialog", false);
		node.flush();
		
//		org.eclipse.egit.ui.Activator.getDefault().getPreferenceStore().setValue("show_initial_config_dialog", false);
	}
}

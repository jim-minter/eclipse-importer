package com.redhat.importer;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class Application implements IStartup {
	void add(IProgressMonitor pm, String name, String location) throws CoreException, URISyntaxException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(name);
		IProjectDescription desc = workspace.newProjectDescription(name);
		desc.setLocationURI(new URI("file://" + location));
		project.create(desc, pm);
		project.open(pm);

		ResolverConfiguration rc = new ResolverConfiguration();
		rc.setResolveWorkspaceProjects(false);
		IProjectConfigurationManager pcm = MavenPlugin.getProjectConfigurationManager();
		pcm.enableMavenNature(project, rc, pm);

		ConnectProviderOperation op = new ConnectProviderOperation(project);
		op.execute(pm);
	}

	void closeEclipse() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				workbench.close();
			}
		});
	}

	public void earlyStartup() {
		String[] args = Platform.getApplicationArgs();

		IProgressMonitor pm = new ProgressMonitor();

		try {
			for(int i = 0; i < args.length; i += 2) {
				add(pm, args[i], args[i + 1]);
			}

			ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, pm);
			// Config.writeEclipseConfig();

			System.out.println("Done.");

		} catch(Exception e) {
			System.out.println(e);
		}
		
		closeEclipse();
	}
}

package com.redhat.importer;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
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
	public void earlyStartup() {
		try {
			String[] args = Platform.getApplicationArgs();

			IProgressMonitor pm = new ProgressMonitor();

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(args[0]);
			IProjectDescription desc = ResourcesPlugin.getWorkspace()
					.newProjectDescription(args[0]);
			desc.setLocationURI(new URI("file://" + args[1]));
			project.create(desc, pm);
			project.open(pm);

			ResolverConfiguration rc = new ResolverConfiguration();
			rc.setResolveWorkspaceProjects(false);
			IProjectConfigurationManager pcm = MavenPlugin
					.getProjectConfigurationManager();
			pcm.enableMavenNature(project, rc, pm);

			project.build(IncrementalProjectBuilder.FULL_BUILD, pm);

			ConnectProviderOperation op = new ConnectProviderOperation(project);
			op.execute(pm);

			System.out.println("Done.");

			final IWorkbench workbench = PlatformUI.getWorkbench();
			workbench.getDisplay().asyncExec(new Runnable() {
				public void run() {
					workbench.close();
				}
			});

		} catch (Exception e) {
			System.out.println(e);
		}
	}
}

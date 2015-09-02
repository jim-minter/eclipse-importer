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
	void add(IProgressMonitor pm, String name, String location) throws Exception {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(name);
		IProjectDescription desc = ResourcesPlugin.getWorkspace().newProjectDescription(name);
		desc.setLocationURI(new URI("file://" + location));
		project.create(desc, pm);
		project.open(pm);

		ResolverConfiguration rc = new ResolverConfiguration();
		rc.setResolveWorkspaceProjects(false);
		IProjectConfigurationManager pcm = MavenPlugin.getProjectConfigurationManager();
		pcm.enableMavenNature(project, rc, pm);

		project.build(IncrementalProjectBuilder.FULL_BUILD, pm);

		ConnectProviderOperation op = new ConnectProviderOperation(project);
		op.execute(pm);
	}

	public void earlyStartup() {
		String[] args = Platform.getApplicationArgs();

		try {
			for(int i = 0; i < args.length; i+=2) {
				IProgressMonitor pm = new ProgressMonitor();
				add(pm, args[i], args[i + 1]);
			}

			System.out.println("Done.");


		} catch (Exception e) {
			System.out.println(e);
		}
		
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				workbench.close();
			}
		});
	}
}

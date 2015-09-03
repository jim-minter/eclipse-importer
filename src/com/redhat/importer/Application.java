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
import org.eclipse.core.runtime.jobs.Job;
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
			Config.writeEclipseConfig();

			for(int i = 0; i < args.length; i += 2) {
				add(pm, args[i], args[i + 1]);
			}

			ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, pm);

		} catch(Exception e) {
			System.out.println(e);
		}

		new Thread(new Runnable() {
			public void run() {
				int counter = 0;
				int jobs = 0;

				while(counter < 5) {
					int jobs2 = Job.getJobManager().find(null).length;
					System.out.println(Integer.toString(jobs2) + " jobs outstanding.");
					if (jobs == jobs2)
						counter++;
					else {
						jobs = jobs2;
						counter = 0;
					}

					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						System.out.println(e);
					}
				}

				closeEclipse();
			}
		}).start();
	}
}

package com.redhat.importer;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.ResolverConfiguration;

public class Application implements IApplication {
	public Object start(final IApplicationContext context) throws Exception {
		String[] args = (String[])context.getArguments().get("application.args");

		IProgressMonitor pm = new ProgressMonitor();

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(args[0]);
		IProjectDescription desc = ResourcesPlugin.getWorkspace().newProjectDescription(args[0]);
		desc.setLocationURI(new URI("file://" + args[1]));
		project.create(desc, pm);
		project.open(pm);

		ResolverConfiguration rc = new ResolverConfiguration();
		rc.setResolveWorkspaceProjects(false);
		IProjectConfigurationManager pcm = MavenPlugin.getProjectConfigurationManager();
		pcm.enableMavenNature(project, rc, pm);

		// project.build(IncrementalProjectBuilder.FULL_BUILD, pm);

		ConnectProviderOperation op = new ConnectProviderOperation(project);
		op.execute(pm);

		System.out.println("Done.");

		return IApplication.EXIT_OK;
	}

	public void stop() {
	}
}

package com.byluroid.eclipse.maven.android;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.maven.ide.eclipse.jdt.IClasspathDescriptor;
import org.maven.ide.eclipse.jdt.IJavaProjectConfigurator;
import org.maven.ide.eclipse.project.IMavenProjectFacade;
import org.maven.ide.eclipse.project.configurator.AbstractProjectConfigurator;
import org.maven.ide.eclipse.project.configurator.ProjectConfigurationRequest;

public class AndroidDevelopmentToolsProjectConfigurator extends AbstractProjectConfigurator implements IJavaProjectConfigurator {

	private static final String ANDROID_PLUGIN_GROUP_ID = "com.jayway.maven.plugins.android.generation2";
	private static final String ANDROID_PLUGIN_ARTIFACT_ID = "maven-android-plugin";
	private static final String ANDROID_NATURE_ID = "com.android.ide.eclipse.adt.AndroidNature";

	@Override
	public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
		if (hasAndroidPlugin(request.getMavenProject())) {
			IProject project = request.getProject();
			if (!project.hasNature(ANDROID_NATURE_ID)) {
				addNature(project, ANDROID_NATURE_ID, monitor);
			}
		}
	}

	public void configureClasspath(IMavenProjectFacade facade,  IClasspathDescriptor classpath, IProgressMonitor monitor) throws CoreException {
	}

	public void configureRawClasspath(ProjectConfigurationRequest request, IClasspathDescriptor classpath, IProgressMonitor monitor) throws CoreException {
		IProject project = request.getProject();

		if (project.hasNature(ANDROID_NATURE_ID)) {
			IJavaProject javaProject = JavaCore.create(request.getProject());

			// TODO change build path to "target" directory

			// add gen source folder if it does not already exist
			if (!hasGenSourceEntry(classpath)) {
				classpath.addSourceEntry(javaProject.getPath().append("gen"), javaProject.getOutputLocation(), true);
			}

			// add compile dependencies to core build path so they are included in ADT APK build
			for (Artifact artifact : request.getMavenProject().getArtifacts()) {
				if (artifact.getScope().equals(Artifact.SCOPE_COMPILE)) {
					classpath.addLibraryEntry(artifact, null, null, null);
				}
			}
		}
	}

	private boolean hasAndroidPlugin(MavenProject mavenProject) {
		List<Plugin> plugins = mavenProject.getBuildPlugins();

		for (Plugin plugin : plugins) {
			if (ANDROID_PLUGIN_GROUP_ID.equals(plugin.getGroupId()) && ANDROID_PLUGIN_ARTIFACT_ID.equals(plugin.getArtifactId())) {
				return true;
			}
		}

		return false;
	}

	private boolean hasGenSourceEntry(IClasspathDescriptor classpath) {
		for (IClasspathEntry entry : classpath.getEntries()) {
			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE && entry.getPath().toOSString().endsWith("gen")) {
				return true;
			}
		}
		return false;
	}

}

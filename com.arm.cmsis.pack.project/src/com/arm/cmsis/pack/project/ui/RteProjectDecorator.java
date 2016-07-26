/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.arm.cmsis.pack.project.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.configuration.IRteConfiguration;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpFileInfo;
import com.arm.cmsis.pack.project.CpProjectPlugIn;
import com.arm.cmsis.pack.project.IRteProject;
import com.arm.cmsis.pack.project.RteProjectManager;
import com.arm.cmsis.pack.project.utils.ProjectUtils;
import com.arm.cmsis.pack.ui.CpPlugInUI;

/**
 * Class to decorate RTE items in Project explorer for RTE projects
 * 
 * @see ILightweightLabelDecorator
 */
public class RteProjectDecorator implements ILightweightLabelDecorator {

	static public final String ID = "com.arm.cmsis.pack.project.decorators.RteProjectDecorator"; //$NON-NLS-1$

	@Override
	public void decorate(Object element, IDecoration decoration) {
		IResource resource = ProjectUtils.getRteResource(element);
		if (resource == null) {
			return;
		}
		int type = resource.getType();
		if (type != IResource.FOLDER && type != IResource.FILE) {
			return;
		}

		IPath path = resource.getProjectRelativePath();
		IProject project = resource.getProject();
		RteProjectManager rteProjectManager = CpProjectPlugIn.getRteProjectManager();
		IRteProject rteProject = rteProjectManager.getRteProject(project);
		if (rteProject == null) {
			return;
		}

		String ext = resource.getFileExtension();
		if (type == IResource.FOLDER || (ext != null && ext.equals(CmsisConstants.RTECONFIG))) {
			if (path.segmentCount() == 1) { // RTE folder itself
				IRteConfiguration rteConf = rteProject.getRteConfiguration();
				if (rteConf == null || !rteConf.isValid()) {
					addOverlay(decoration, CpPlugInUI.ICON_RTE_ERROR_OVR);
				} else if (type == IResource.FOLDER) {
					addOverlay(decoration, CpPlugInUI.ICON_RTE_OVR);
				}
			} else if (type == IResource.FOLDER) {
				int overlayType = getOverlayType(rteProject, path);
				if (overlayType == -1) {
					addOverlay(decoration, CpPlugInUI.ICON_RTE_ERROR_OVR);
				} else if (overlayType == 0) {
					addOverlay(decoration, CpPlugInUI.ICON_RTE_WARNING_OVR);
				}
			}
		}

		if (type == IResource.FILE) {
			ICpFileInfo fi = rteProject.getProjectFileInfo(path.toString());
			if (fi != null) {
				ICpComponentInfo ci = fi.getComponentInfo();
				String suffix = " [" + ci.getName() + "]"; //$NON-NLS-1$//$NON-NLS-2$
				decoration.addSuffix(suffix);
				if (ci.getComponent() == null) {
					addOverlay(decoration, CpPlugInUI.ICON_RTE_ERROR_OVR);
				}
				int versionDiff = fi.getVersionDiff();
				if (versionDiff > 2 || versionDiff < 0) {
					addOverlay(decoration, CpPlugInUI.ICON_RTE_WARNING_OVR);
				}
				return;
			}
		}
	}

	/**
	 * return -1 if error, 0 if warning, 1 if correct
	 */
	private int getOverlayType(IRteProject rteProject, IPath path) {
		ICpFileInfo[] fileInfos = rteProject.getProjectFileInfos(path.toString() + ".*"); //$NON-NLS-1$
		for (ICpFileInfo fileInfo : fileInfos) {
			if (fileInfo.getComponentInfo().getComponent() == null) {
				return -1;
			}
			int versionDiff = fileInfo.getVersionDiff();
			if (versionDiff > 2 || versionDiff < 0) {
				return 0;
			}
		}
		return 1;
	}

	private void addOverlay(IDecoration decoration, String iconFile) {
		ImageDescriptor descriptor = CpPlugInUI.getImageDescriptor(iconFile);
		if (descriptor == null) {
			return;
		}
		decoration.addOverlay(descriptor, IDecoration.TOP_LEFT);
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// does nothing
	}

	@Override
	public void dispose() {
		// does nothing
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// does nothing
	}

	/**
	 * Refreshes decoration of all RTE resources
	 */
	static public void refresh() {
		// Decorate using current UI thread
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IDecoratorManager decoratorManager = PlatformUI.getWorkbench().getDecoratorManager();
				if (decoratorManager != null) {
					decoratorManager.update(ID);
				}
			}
		});
	}
}
/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   {INITIAL_AUTHOR} - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.ui.internal.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.ui.AptUIPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * 
 */
public class FactoryPathPreferencePage extends BasePreferencePage {

	private static final String PREF_ID= "org.eclipse.jdt.apt.ui.preferences.factoryPathPreferences"; //$NON-NLS-1$
	private static final String PROP_ID= "org.eclipse.jdt.apt.ui.propertyPages.factoryPathPreferences"; //$NON-NLS-1$

	/**
	 * 
	 */
	public FactoryPathPreferencePage() {
		setPreferenceStore(AptUIPlugin.getDefault().getPreferenceStore());
		//TODO: real names
		setDescription("Java annotation processor factory path:"); 
		
		// only used when page is shown programatically
		setTitle("FactoryPathPreferencePageTitle");		 
	}

	/*
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		IWorkbenchPreferenceContainer container= (IWorkbenchPreferenceContainer) getContainer();
		setConfigurationBlock(new FactoryPathConfigurationBlock(getNewStatusChangedListener(), getProject(), container));
		
		super.createControl(parent);
		//TODO: enable Help
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IJavaHelpContextIds.ORGANIZE_IMPORTS_PREFERENCE_PAGE);
	}

	protected boolean hasProjectSpecificOptions(IProject project) {
		useProjectSettings();
		return (project == null) ? false : AptConfig.hasProjectSpecificFactoryPath(JavaCore.create(project));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#getPreferencePageID()
	 */
	protected String getPreferencePageID() {
		return PREF_ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#getPropertyPageID()
	 */
	protected String getPropertyPageID() {
		return PROP_ID;
	}

}

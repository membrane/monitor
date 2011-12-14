package com.predic8.plugin.membrane.wizards;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

import com.predic8.membrane.core.*;
import com.predic8.plugin.membrane.actions.ShowSecurityPreferencesAction;
import com.predic8.plugin.membrane.util.SWTUtil;

public abstract class SecurityWizardPage extends AbstractProxyWizardPage implements SecurityConfigurationChangeListener {

	protected Button btSecureConnection;
	
	protected boolean outgoing;
	
	protected SecurityWizardPage(String pageName, boolean outgoing) {
		super(pageName);
		this.outgoing = outgoing;
	}
	
	protected void createSecurityGroup(Composite parent) {
		Group group = createGroup(parent);
		group.setLayoutData(SWTUtil.getGreedyHorizontalGridData());

		createSecureConnectionButton(group);
		
		if (!outgoing) {
			Label label = new Label(group, SWT.NONE);
			label.setText("To enable secure connections you must provide a keystore at the");
			createLink(group, "<A>Security Preferences Page</A>");
		}
		
		Router.getInstance().getConfigurationManager().addSecurityConfigurationChangeListener(this);
	}
	
	private Group createGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(SWTUtil.createGridLayout(1, 5));
		group.setText("Security");
		return group;
	}
	
	protected abstract void addListenersToSecureConnectionButton();
	
	protected void createSecureConnectionButton(Composite composite) {
		btSecureConnection = new Button(composite, SWT.CHECK);
		btSecureConnection.setText(getCheckButtonText());
		btSecureConnection.setEnabled(getEnabledStatus());
	}

	private String getCheckButtonText() {
		StringBuffer buf = new StringBuffer();
		buf.append("Secure ");
		if (outgoing)
			buf.append("outgoing");
		else
			buf.append("incoming");
		buf.append(" ");
		buf.append("connections (SSL/TLS)");
		return buf.toString();
	}
	
	protected boolean getEnabledStatus() {
		return outgoing || Router.getInstance().getConfigurationManager().getProxies().isKeyStoreAvailable();
	}
	
	protected void createLink(Composite composite, String linkText) {
		Link link = new Link(composite, SWT.NONE);
		link.setText(linkText);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ShowSecurityPreferencesAction action = new ShowSecurityPreferencesAction();
				action.run();
			}
		});
	}

	public boolean getSecureConnection() {
		return btSecureConnection.getSelection();
	}
	
	protected void enableSecureConnectionButton() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				btSecureConnection.setEnabled(getEnabledStatus());
			}
		});
	}
	
	public void securityConfigurationChanged() {
		enableSecureConnectionButton();
	}
	
	@Override
	public void dispose() {
		Router.getInstance().getConfigurationManager().removeSecurityConfigurationChangeListener(this);
		super.dispose();
	}
}

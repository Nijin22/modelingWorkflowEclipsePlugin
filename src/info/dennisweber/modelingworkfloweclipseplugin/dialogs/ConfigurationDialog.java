package info.dennisweber.modelingworkfloweclipseplugin.dialogs;

import java.io.IOException;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import info.dennisweber.modelingworkfloweclipseplugin.model.ConfigCache;

public class ConfigurationDialog extends TitleAreaDialog {
	private Text bbBaseUrlTextfield;
	private Text bbRepoPathTextfieldText;
	private Text jiraTextfield;
	private Text jiraBoardTextfield;
	private Text usernameTextfield;
	private Text passwordTextfield;
	private Button storeConfigButton;
	private Shell parentShell;
	private ConfigCache configCache;

	public ConfigurationDialog(Shell parentShell, ConfigCache configCache) {
		super(parentShell);
		this.parentShell = parentShell;
		this.configCache = configCache;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Configure the modelling workflow");
		setMessage("Please configure the URLs to the repository and issue board.", IMessageProvider.NONE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);

		// Init all fields
		initBbBaseUrlInput(container);
		initBbRepoPathInput(container);
		initJiraApiInput(container);
		initJiraBoardInput(container);
		initUsernameInput(container);
		initPasswordInput(container);
		initStoreConfigInput(container);

		return area;
	}

	@Override
	protected void okPressed() {
		configCache.update(bbBaseUrlTextfield.getText(), bbRepoPathTextfieldText.getText(), jiraTextfield.getText(),
				jiraBoardTextfield.getText(), usernameTextfield.getText(), passwordTextfield.getText());

		if (storeConfigButton.getSelection()) {
			try {
				configCache.storeConfig();
			} catch (IOException e) {
				MessageDialog.openError(parentShell, "Failed to save configuration file", e.getLocalizedMessage());
			}
		}
		super.okPressed();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public boolean isHelpAvailable() {
		return false;
	}

	private void initBbBaseUrlInput(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("URL of Bitbucket");

		// Layout:
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		bbBaseUrlTextfield = new Text(container, SWT.BORDER);
		bbBaseUrlTextfield.setLayoutData(gd);
		bbBaseUrlTextfield.setText(configCache.getBbBaseUrl());

	}

	private void initBbRepoPathInput(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("Bitbucket path to project");

		// Layout:
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		bbRepoPathTextfieldText = new Text(container, SWT.BORDER);
		bbRepoPathTextfieldText.setLayoutData(gd);
		bbRepoPathTextfieldText.setText(configCache.getBbRepoPath());
	}

	private void initJiraApiInput(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("URL of Jira");

		// Layout:
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		jiraTextfield = new Text(container, SWT.BORDER);
		jiraTextfield.setLayoutData(gd);
		jiraTextfield.setText(configCache.getJiraUrl());
	}

	private void initJiraBoardInput(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("Jira Board ID");

		// Layout:
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		jiraBoardTextfield = new Text(container, SWT.BORDER);
		jiraBoardTextfield.setLayoutData(gd);
		jiraBoardTextfield.setText(configCache.getJiraBoardId());
	}

	private void initUsernameInput(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("Username");

		// Layout:
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		usernameTextfield = new Text(container, SWT.BORDER);
		usernameTextfield.setLayoutData(gd);
		usernameTextfield.setText(configCache.getUsername());
	}

	private void initPasswordInput(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("Password");

		// Layout:
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		passwordTextfield = new Text(container, SWT.BORDER | SWT.PASSWORD);
		passwordTextfield.setLayoutData(gd);
		passwordTextfield.setText(configCache.getPassword());
	}

	private void initStoreConfigInput(Composite container) {
		storeConfigButton = new Button(container, SWT.CHECK);
		storeConfigButton
				.setText("Save configuration file to .git directory. (Passwords will be stored in clear text)");
		storeConfigButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
	}
}

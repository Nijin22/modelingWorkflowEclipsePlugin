package info.dennisweber.modelingworkfloweclipseplugin.dialogs;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import info.dennisweber.modelingworkfloweclipseplugin.ConfigCache;

public class ConfigurationDialog extends TitleAreaDialog {
	private Text repoApiTextfield;
	private Text jiraApiTextfield;
	private Shell parentShell;

	public ConfigurationDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
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

		initRepoApiInput(container);
		initJiraApiInput(container);

		return area;
	}

	@Override
	protected void okPressed() {
		ConfigCache.setJiraApiUrl(jiraApiTextfield.getText());
		ConfigCache.setRepoApiUrl(repoApiTextfield.getText());
		MessageDialog.openInformation(parentShell, "Information",
				"In the prototype, changes are only stored in cache.");
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

	private void initRepoApiInput(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("URL of Repository API");

		// Layout:
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		repoApiTextfield = new Text(container, SWT.BORDER);
		repoApiTextfield.setLayoutData(gd);
		repoApiTextfield.setText(ConfigCache.getRepoApiUrl());

	}

	private void initJiraApiInput(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("URL of Jira Board API");

		// Layout:
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		jiraApiTextfield = new Text(container, SWT.BORDER);
		jiraApiTextfield.setLayoutData(gd);
		jiraApiTextfield.setText(ConfigCache.getJiraApiUrl());
	}
}

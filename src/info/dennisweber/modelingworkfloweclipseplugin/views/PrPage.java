package info.dennisweber.modelingworkfloweclipseplugin.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import info.dennisweber.modelingworkfloweclipseplugin.model.ConfigCache;
import info.dennisweber.modelingworkfloweclipseplugin.model.GitInterface;
import info.dennisweber.modelingworkfloweclipseplugin.model.Issue;
import info.dennisweber.modelingworkfloweclipseplugin.model.PrDto;
import info.dennisweber.modelingworkfloweclipseplugin.model.WebApi;

public class PrPage extends SubPage {
	private Issue issue;
	private PrDto pr;

	public PrPage(Composite originalParent, WebApi jiraApi, ConfigCache configCache, Shell shell,
			GitInterface gitInterface, MainView mainView, Issue issue, PrDto pr) {

		super(originalParent, jiraApi, configCache, shell, gitInterface, mainView);
		this.issue = issue;
		this.pr = pr;

		// Layout this Page
		parent = new Composite(originalParent, SWT.NONE);
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 1;
		parent.setLayout(mainLayout);

		initTopLabels();
	}

	private void initTopLabels() {
		Link first = new Link(parent, SWT.NONE);
		first.setText("Viewing Pull Request <a>" + pr.id + "</a> to merge changes from ");
		new Label(parent, SWT.NONE).setText(pr.fromRef.displayId);
		new Label(parent, SWT.NONE).setText("into");
		new Label(parent, SWT.NONE).setText(pr.toRef.displayId);
	}
}

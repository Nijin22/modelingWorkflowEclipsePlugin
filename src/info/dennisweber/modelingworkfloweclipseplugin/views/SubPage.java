package info.dennisweber.modelingworkfloweclipseplugin.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import info.dennisweber.modelingworkfloweclipseplugin.model.ConfigCache;
import info.dennisweber.modelingworkfloweclipseplugin.model.GitInterface;
import info.dennisweber.modelingworkfloweclipseplugin.model.WebApi;

public abstract class SubPage {
	protected WebApi webApi;
	protected GitInterface gitInterface;
	protected Shell shell;
	protected Composite parent;
	protected ConfigCache configCache;
	protected MainView mainView;

	public SubPage(Composite originalParent, WebApi webApi, ConfigCache configCache, Shell shell,
			GitInterface gitInterface, MainView mainView) {

		this.webApi = webApi;
		this.gitInterface = gitInterface;
		this.shell = shell;
		this.configCache = configCache;
		this.mainView = mainView;

	}
}

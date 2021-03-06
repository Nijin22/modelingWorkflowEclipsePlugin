package info.dennisweber.modelingworkfloweclipseplugin.model;

import java.util.Comparator;

public class Issue {
	private String id;
	private String title;
	private IssueStatus status;
	private String assignee;

	private static class StatusThenIdComparator implements Comparator<Issue> {
		@Override
		public int compare(Issue i1, Issue i2) {
			// -1 - less than, 1 - greater than, 0 - equal
			int statusCompare = i1.status.compareTo(i2.status);
			if (statusCompare != 0) {
				return statusCompare;
			} else {
				return i1.id.compareToIgnoreCase(i2.id);
			}

		}
	}

	public static StatusThenIdComparator getStatusThenIdComparator() {
		return new StatusThenIdComparator();
	}

	public Issue(String id, String title, IssueStatus status, String assignee) {
		super();
		this.id = id;
		this.title = title;
		this.status = status;
		this.assignee = assignee;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Issue other = (Issue) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public IssueStatus getStatus() {
		return status;
	}

	public void setStatus(IssueStatus status) {
		this.status = status;
	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

}

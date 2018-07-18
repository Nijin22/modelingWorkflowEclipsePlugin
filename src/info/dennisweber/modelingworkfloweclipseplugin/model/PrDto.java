package info.dennisweber.modelingworkfloweclipseplugin.model;

public class PrDto {
	public int id;
	public String title;
	public String description;
	public String state;
	public boolean open;
	public boolean closed;
	public Reference fromRef;
	public Reference toRef;

	public static class Reference {
		public String id;
		public String displayId;
	}
}

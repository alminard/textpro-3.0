package eu.fbk.textpro.api;

public class column {
	private String name="";
	private String value="";
	private boolean status=false;
	public String getValue() {
		return value;
	}
	public boolean getStatus() {
		return status;
	}
	public void active(){
		status=true;
	}
	public void deactive(){
		status=false;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setValue(String value) {
		this.value = value;
	}
}

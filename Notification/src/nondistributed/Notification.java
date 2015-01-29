package nondistributed;

import org.json.JSONException;
import org.json.JSONObject;

public class Notification {
	private String type;
	private String details;
	
	public Notification(String type, String details){
		if (type == null) throw new IllegalArgumentException("type can't be null");
		if (details == null) details = "";
		this.type = type;
		this.details = details;
	}
	
	public Notification(String type){
		if (type == null) throw new IllegalArgumentException("type can't be null");
		this.type = type;
		this.details = "";
	}
	
	public Notification(){
		this.type = "";
		this.details = "";
	}
	
	@Override
	public String toString(){
		return "{Notification: {Type: " + this.type + " Details: " + this.details + " }}";
	}
	
	public String toJSONString(){
		try {
			JSONObject obj = new JSONObject();
			obj.put("type", this.type);
			obj.put("details", this.details);
			return obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return this.toString();
		}
	}
	
	public String getType(){
		return this.type;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getDetails(){
		return this.details;
	}
	
	public void setDetails(String details){
		this.details = details;
	}
	
}

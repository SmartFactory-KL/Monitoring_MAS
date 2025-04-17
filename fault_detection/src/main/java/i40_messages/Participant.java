package i40_messages;

/**
 * This class is based on the VDI/VDE 2193 part 1 from year 2020
 * @author Alexis Bernhard
 */
public class Participant {

	private String aas_id;
	
	private String role;
	
	public Participant(String aas_id, String role) {
		this.set_id(aas_id);
		this.role = role;
	}

	public String get_id() {
		return aas_id;
	}

	public void set_id(String aas_id) {
		this.aas_id = aas_id;
	}

	public String getRole() {
		return role;
	}
}

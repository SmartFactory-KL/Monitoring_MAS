package i40_messages;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;

import main.Agent;

/**
 * This class is based on the VDI/VDE 2193 part 1 from year 2020
 * @author Alexis Bernhard
 */
public class I40_Message {

	
	private Frame frame;
	
	private List<Referable> interactionElements;
	
	public I40_Message(Frame frame) {
		this.frame = frame;
		interactionElements = new ArrayList<Referable>();
	}
	
	public I40_Message(Frame frame, List<Referable> interactionElements) {
		this.frame = frame;
		this.interactionElements = interactionElements;
	}

	public Frame getFrame() {
		return frame;
	}
	
	public List<Referable> getInteractionElements() {
		return interactionElements;
	}
	
	public Referable getInteractionElement(String idShort) {
		return interactionElements.stream().filter(element -> element.getIdShort().equals(idShort)).findFirst().orElse(null);
	}
	
	public void addInteractionElement(Referable element) {
		this.interactionElements.add(element);
	}
	
	public long getSize() {
		return Agent.getObjectSize(this);
	}
}

package skills;

import capacities.I40_Message_Capacity;
import i40_messages.Frame;
import i40_messages.I40_Message;
import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.Skill;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * @author Alexis Bernhard
 */
@SarlSpecification("0.12")
@SarlElementType(22)
@SuppressWarnings("all")
public class I40_Message_Skill extends Skill implements I40_Message_Capacity {
  @Override
  @Pure
  public I40_Message create_I40_Message(final String sender, final String receiver, final String msg_type) {
    Frame _frame = new Frame(sender, receiver, msg_type);
    return new I40_Message(_frame);
  }
  
  @Override
  @Pure
  public I40_Message create_I40_Message(final String sender, final String receiver, final String msg_type, final String conv_id, final List<Referable> interactionElements) {
    Frame frame = new Frame(sender, receiver, msg_type, conv_id);
    return new I40_Message(frame, interactionElements);
  }
  
  public I40_Message_Skill() {
  }
}

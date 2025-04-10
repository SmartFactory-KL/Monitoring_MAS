package capacities;

import i40_messages.I40_Message;
import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.AgentTrait;
import io.sarl.lang.core.Capacity;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;

/**
 * @author Alexis Bernhard
 */
@SarlSpecification("0.12")
@SarlElementType(20)
@SuppressWarnings("all")
public interface I40_Message_Capacity extends Capacity {
  I40_Message create_I40_Message(final String sender, final String receiver, final String msg_type);
  
  I40_Message create_I40_Message(final String sender, final String receiver, final String msg_type, final String aas_id, final List<Referable> interactionElements);
  
  /**
   * @ExcludeFromApidoc
   */
  class ContextAwareCapacityWrapper<C extends I40_Message_Capacity> extends Capacity.ContextAwareCapacityWrapper<C> implements I40_Message_Capacity {
    public ContextAwareCapacityWrapper(final C capacity, final AgentTrait caller) {
      super(capacity, caller);
    }
    
    public I40_Message create_I40_Message(final String sender, final String receiver, final String msg_type) {
      try {
        ensureCallerInLocalThread();
        return this.capacity.create_I40_Message(sender, receiver, msg_type);
      } finally {
        resetCallerInLocalThread();
      }
    }
    
    public I40_Message create_I40_Message(final String sender, final String receiver, final String msg_type, final String aas_id, final List<Referable> interactionElements) {
      try {
        ensureCallerInLocalThread();
        return this.capacity.create_I40_Message(sender, receiver, msg_type, aas_id, interactionElements);
      } finally {
        resetCallerInLocalThread();
      }
    }
  }
}

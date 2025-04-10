package events;

import i40_messages.I40_Message;
import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.annotation.SyntheticMember;
import io.sarl.lang.core.Event;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * @author Alexis Bernhard
 */
@SarlSpecification("0.12")
@SarlElementType(15)
@SuppressWarnings("all")
public class ExecutionRequest extends Event {
  public I40_Message message;
  
  public ExecutionRequest() {
  }
  
  @Override
  @Pure
  @SyntheticMember
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }
  
  @Override
  @Pure
  @SyntheticMember
  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
  
  /**
   * Returns a String representation of the ExecutionRequest event's attributes only.
   */
  @SyntheticMember
  @Pure
  protected void toString(final ToStringBuilder builder) {
    super.toString(builder);
    builder.add("message", this.message);
  }
  
  @SyntheticMember
  private static final long serialVersionUID = 1258344175L;
}

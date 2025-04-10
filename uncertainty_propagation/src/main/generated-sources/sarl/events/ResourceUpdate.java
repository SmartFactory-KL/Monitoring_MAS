package events;

import i40_messages.I40_Message;
import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.annotation.SyntheticMember;
import io.sarl.lang.core.Event;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

@SarlSpecification("0.12")
@SarlElementType(15)
@SuppressWarnings("all")
public class ResourceUpdate extends Event {
  public I40_Message message;
  
  public int uc;
  
  public ResourceUpdate() {
  }
  
  @Override
  @Pure
  @SyntheticMember
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ResourceUpdate other = (ResourceUpdate) obj;
    if (other.uc != this.uc)
      return false;
    return super.equals(obj);
  }
  
  @Override
  @Pure
  @SyntheticMember
  public int hashCode() {
    int result = super.hashCode();
    final int prime = 31;
    result = prime * result + Integer.hashCode(this.uc);
    return result;
  }
  
  /**
   * Returns a String representation of the ResourceUpdate event's attributes only.
   */
  @SyntheticMember
  @Pure
  protected void toString(final ToStringBuilder builder) {
    super.toString(builder);
    builder.add("message", this.message);
    builder.add("uc", this.uc);
  }
  
  @SyntheticMember
  private static final long serialVersionUID = 1258452332L;
}

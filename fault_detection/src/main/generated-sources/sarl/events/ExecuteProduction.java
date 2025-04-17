package events;

import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.annotation.SyntheticMember;
import io.sarl.lang.core.Event;

/**
 * @author Ali Karnoub
 */
@SarlSpecification("0.12")
@SarlElementType(15)
@SuppressWarnings("all")
public class ExecuteProduction extends Event {
  public ExecuteProduction() {
  }
  
  @SyntheticMember
  private static final long serialVersionUID = 588370691L;
}

package capacities;

import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.AgentTrait;
import io.sarl.lang.core.Capacity;
import java.util.List;

/**
 * @author Alexis Bernhard
 */
@SarlSpecification("0.12")
@SarlElementType(20)
@SuppressWarnings("all")
public interface UncertaintyEstimatorCapacity extends Capacity {
  double[] estimate_action_uncertainty(final String skillId);
  
  double[] fuse(final List<double[]> uncertainties);
  
  /**
   * @ExcludeFromApidoc
   */
  class ContextAwareCapacityWrapper<C extends UncertaintyEstimatorCapacity> extends Capacity.ContextAwareCapacityWrapper<C> implements UncertaintyEstimatorCapacity {
    public ContextAwareCapacityWrapper(final C capacity, final AgentTrait caller) {
      super(capacity, caller);
    }
    
    public double[] estimate_action_uncertainty(final String skillId) {
      try {
        ensureCallerInLocalThread();
        return this.capacity.estimate_action_uncertainty(skillId);
      } finally {
        resetCallerInLocalThread();
      }
    }
    
    public double[] fuse(final List<double[]> uncertainties) {
      try {
        ensureCallerInLocalThread();
        return this.capacity.fuse(uncertainties);
      } finally {
        resetCallerInLocalThread();
      }
    }
  }
}

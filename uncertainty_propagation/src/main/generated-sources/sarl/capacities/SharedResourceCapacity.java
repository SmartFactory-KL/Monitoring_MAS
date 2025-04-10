package capacities;

import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.AgentTrait;
import io.sarl.lang.core.Capacity;
import java.util.Map;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * @author Alexis Bernhard
 */
@SarlSpecification("0.12")
@SarlElementType(20)
@SuppressWarnings("all")
public interface SharedResourceCapacity extends Capacity {
  void changeCounter(final String key, final boolean increase);
  
  @Pure
  Map<String, Integer> getCounter();
  
  @Pure
  Integer getCounter(final String key);
  
  void addKeyCounter(final String key);
  
  void addKeyUncertainty(final String key);
  
  /**
   * @ExcludeFromApidoc
   */
  class ContextAwareCapacityWrapper<C extends SharedResourceCapacity> extends Capacity.ContextAwareCapacityWrapper<C> implements SharedResourceCapacity {
    public ContextAwareCapacityWrapper(final C capacity, final AgentTrait caller) {
      super(capacity, caller);
    }
    
    public void changeCounter(final String key, final boolean increase) {
      try {
        ensureCallerInLocalThread();
        this.capacity.changeCounter(key, increase);
      } finally {
        resetCallerInLocalThread();
      }
    }
    
    public Map<String, Integer> getCounter() {
      try {
        ensureCallerInLocalThread();
        return this.capacity.getCounter();
      } finally {
        resetCallerInLocalThread();
      }
    }
    
    public Integer getCounter(final String key) {
      try {
        ensureCallerInLocalThread();
        return this.capacity.getCounter(key);
      } finally {
        resetCallerInLocalThread();
      }
    }
    
    public void addKeyCounter(final String key) {
      try {
        ensureCallerInLocalThread();
        this.capacity.addKeyCounter(key);
      } finally {
        resetCallerInLocalThread();
      }
    }
    
    public void addKeyUncertainty(final String key) {
      try {
        ensureCallerInLocalThread();
        this.capacity.addKeyUncertainty(key);
      } finally {
        resetCallerInLocalThread();
      }
    }
  }
}

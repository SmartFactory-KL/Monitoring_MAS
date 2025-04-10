package skills;

import capacities.SharedResourceCapacity;
import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.annotation.SyntheticMember;
import io.sarl.lang.core.Skill;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * @author Alexis Bernhard
 */
@SarlSpecification("0.12")
@SarlElementType(22)
@SuppressWarnings("all")
public class SharedResourceSkill extends Skill implements SharedResourceCapacity {
  private Map<String, Integer> counter = new HashMap<String, Integer>();
  
  private Map<String, List<SubmodelElementCollection>> uncertainties = new HashMap<String, List<SubmodelElementCollection>>();
  
  public synchronized void changeCounter(final String key, final boolean increase) {
    if (increase) {
      Integer _get = this.counter.get(key);
      this.counter.put(key, Integer.valueOf((((_get) == null ? 0 : (_get).intValue()) + 1)));
    } else {
      Integer _get_1 = this.counter.get(key);
      this.counter.put(key, Integer.valueOf((((_get_1) == null ? 0 : (_get_1).intValue()) - 1)));
    }
  }
  
  public synchronized boolean changeUncertainty(final String key, final SubmodelElementCollection content) {
    return this.uncertainties.get(key).add(content);
  }
  
  @Pure
  public Map<String, Integer> getCounter() {
    return this.counter;
  }
  
  @Pure
  public Map<String, List<SubmodelElementCollection>> getUncertainty() {
    return this.uncertainties;
  }
  
  @Pure
  public Integer getCounter(final String key) {
    return this.counter.get(key);
  }
  
  @Pure
  public List<SubmodelElementCollection> getUncertainty(final String key) {
    return this.uncertainties.get(key);
  }
  
  public void addKeyCounter(final String key) {
    this.counter.putIfAbsent(key, Integer.valueOf(0));
  }
  
  public void addKeyUncertainty(final String key) {
    ArrayList<SubmodelElementCollection> _arrayList = new ArrayList<SubmodelElementCollection>();
    this.uncertainties.putIfAbsent(key, _arrayList);
  }
  
  public SharedResourceSkill() {
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
}

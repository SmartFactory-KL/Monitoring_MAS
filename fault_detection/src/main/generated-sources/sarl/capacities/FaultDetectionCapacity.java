package capacities;

import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.AgentTrait;
import io.sarl.lang.core.Capacity;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

/**
 * @author Ali Karnoub
 */
@SarlSpecification("0.12")
@SarlElementType(20)
@SuppressWarnings("all")
public interface FaultDetectionCapacity extends Capacity {
  /**
   * Berechnet die Fehlerwahrscheinlichkeit nach Bayes: \( P(F_i | D_i) \).
   * @param likelihood P(D_i | F_i): Wahrscheinlichkeit, dass die Sensordaten auftreten, wenn der Fehler vorliegt.
   * @param prior P(F_i): Vorwissen über Fehlerwahrscheinlichkeit.
   * @param evidence P(D_i): Gesamtwahrscheinlichkeit für die beobachteten Sensordaten.
   * @return: Die berechnete Fehlerwahrscheinlichkeit als Wert zwischen 0 und 1.
   */
  Double calculateFaultProbability(final Double likelihood, final Double prior, final Double evidence);
  
  /**
   * Berechnet die Gesamtfehlerwahrscheinlichkeit in einem hierarchischen System:  \( P(F) = 1 - \prod_{i=1}^{n} (1 - P(F_i)) \).
   * @param faultProbabilities: Liste der Fehlerwahrscheinlichkeiten der einzelnen Module.
   * @return: Die berechnete Fehlerwahrscheinlichkeit für das gesamte Produktionssystem.
   */
  Double calculateSystemFaultProbability(final List<Double> faultProbabilities);
  
  /**
   * Führt eine Analyse der Fehlerdaten durch und klassifiziert das Problem.
   * @param step: Die aktuellen Sensordaten aus dem SubmodelElementCollection des Produktionsschritts.
   * @return: Eine Liste von Fehlern (als String), falls welche erkannt werden.
   */
  List<Referable> detectFaults(final Submodel productionLog, final AssetAdministrationShell aas, final SubmodelElementCollection SubstepSMC);
  
  /**
   * @ExcludeFromApidoc
   */
  class ContextAwareCapacityWrapper<C extends FaultDetectionCapacity> extends Capacity.ContextAwareCapacityWrapper<C> implements FaultDetectionCapacity {
    public ContextAwareCapacityWrapper(final C capacity, final AgentTrait caller) {
      super(capacity, caller);
    }
    
    public Double calculateFaultProbability(final Double likelihood, final Double prior, final Double evidence) {
      try {
        ensureCallerInLocalThread();
        return this.capacity.calculateFaultProbability(likelihood, prior, evidence);
      } finally {
        resetCallerInLocalThread();
      }
    }
    
    public Double calculateSystemFaultProbability(final List<Double> faultProbabilities) {
      try {
        ensureCallerInLocalThread();
        return this.capacity.calculateSystemFaultProbability(faultProbabilities);
      } finally {
        resetCallerInLocalThread();
      }
    }
    
    public List<Referable> detectFaults(final Submodel productionLog, final AssetAdministrationShell aas, final SubmodelElementCollection SubstepSMC) {
      try {
        ensureCallerInLocalThread();
        return this.capacity.detectFaults(productionLog, aas, SubstepSMC);
      } finally {
        resetCallerInLocalThread();
      }
    }
  }
}

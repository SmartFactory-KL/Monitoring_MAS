package skills;

import capacities.UncertaintyEstimatorCapacity;
import io.sarl.core.Logging;
import io.sarl.lang.annotation.ImportedCapacityFeature;
import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.annotation.SyntheticMember;
import io.sarl.lang.core.AtomicSkillReference;
import io.sarl.lang.core.Skill;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * @author Alexis Bernhard
 */
@SarlSpecification("0.12")
@SarlElementType(22)
@SuppressWarnings("all")
public class UncertaintyEstimatorSkill extends Skill implements UncertaintyEstimatorCapacity {
  /**
   * Retrieves the estimated action uncertainty of one skill (given by the skillId) on the managed resource.
   */
  public double[] estimate_action_uncertainty(final String skillId) {
    Random random = new Random();
    double[] sublist = random.doubles(2).toArray();
    double _get = sublist[0];
    double _get_1 = sublist[1];
    double[] result = { _get, _get_1, 1.0 };
    Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
    double _get_2 = result[0];
    double _get_3 = result[1];
    double _get_4 = result[2];
    _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info((((((("Action uncertainty: [" + Double.valueOf(_get_2)) + ", ") + Double.valueOf(_get_3)) + ", ") + Double.valueOf(_get_4)) + "]"));
    return result;
  }
  
  /**
   * Fused all uncertainties in the list given in the uncertainties argument by applying a Bayesian Fusion algorithm.
   * This algorithm is only suitable for Gaussian distributions and is represented as array with the mean is first entry,
   * the variance f a given distribution as second value and the weight of the Fusion as third value.
   * The weight is an integer weight to represent the weight of a Kalman Filter entry towards all entries.
   */
  public double[] fuse(final List<double[]> uncertainties) {
    int _length = ((Object[])Conversions.unwrapArray(uncertainties, Object.class)).length;
    if ((_length > 0)) {
      double meanFused = uncertainties.get(0)[0];
      double varianceFused = uncertainties.get(0)[1];
      double weight = uncertainties.get(0)[2];
      for (int i = 1; (i < ((Object[])Conversions.unwrapArray(uncertainties, Object.class)).length); i++) {
        {
          double mean = uncertainties.get(i)[0];
          double variance = uncertainties.get(i)[1];
          double subweight = uncertainties.get(i)[2];
          meanFused = (((meanFused * weight) + (mean * subweight)) / (weight + weight));
          varianceFused = ((((weight * (varianceFused + (meanFused * meanFused))) + 
            (subweight * (variance + (mean * mean)))) / (subweight + weight)) - (meanFused * meanFused));
          weight = (weight + subweight);
        }
      }
      return new double[] { meanFused, varianceFused, weight };
    } else {
      return null;
    }
  }
  
  public double[] fuseSMC(final List<SubmodelElementCollection> uncertainties) {
    Random random = new Random();
    ArrayList<double[]> inputUncertainties = new ArrayList<double[]>();
    for (final SubmodelElementCollection uncertainty : uncertainties) {
      {
        double[] inputArray = random.doubles(uncertainty.getValue().size()).toArray();
        for (int i = 0; (i < ((List<Double>)Conversions.doWrapArray(inputArray)).size()); i++) {
          SubmodelElement _get = uncertainty.getValue().get(i);
          inputArray[i] = Double.parseDouble(((Property) _get).getValue());
        }
        inputUncertainties.add(inputArray);
      }
    }
    return this.fuse(inputUncertainties);
  }
  
  /**
   * private def subStepUncertaintyCalculation(step : SubmodelElementCollection) : double[] {
   * var uncertainties : List<double[]> = new ArrayList()
   * var substeps = aasHelper.getAASChild(step, "Substeps") as SubmodelElementList
   * if (substeps !== null) {
   * for (substep : substeps.value) {
   * var substepSMC = substep as SubmodelElementCollection
   * var substepUncertainty = this.calculateUncertainty(substepSMC)
   * uncertainties.add(substepUncertainty)
   * }
   * }
   * return this.fuse(uncertainties)
   * }
   */
  public UncertaintyEstimatorSkill() {
  }
  
  @Extension
  @ImportedCapacityFeature(Logging.class)
  @SyntheticMember
  private transient AtomicSkillReference $CAPACITY_USE$IO_SARL_CORE_LOGGING;
  
  @SyntheticMember
  @Pure
  private Logging $CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER() {
    if (this.$CAPACITY_USE$IO_SARL_CORE_LOGGING == null || this.$CAPACITY_USE$IO_SARL_CORE_LOGGING.get() == null) {
      this.$CAPACITY_USE$IO_SARL_CORE_LOGGING = $getSkill(Logging.class);
    }
    return $castSkill(Logging.class, this.$CAPACITY_USE$IO_SARL_CORE_LOGGING);
  }
}

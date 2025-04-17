package skills;

import capacities.FaultDetectionCapacity;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import helper.AasGenerator;
import helper.AasHelper;
import io.sarl.core.Logging;
import io.sarl.lang.annotation.ImportedCapacityFeature;
import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.annotation.SyntheticMember;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AtomicSkillReference;
import io.sarl.lang.core.Skill;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Predicate;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReferenceElement;
import org.eclipse.xtext.xbase.lib.DoubleExtensions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.ExclusiveRange;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * @author Ali Karnoub
 */
@SarlSpecification("0.12")
@SarlElementType(22)
@SuppressWarnings("all")
public class FaultDetectionSkill extends Skill implements FaultDetectionCapacity {
  private AasGenerator aasgen = new AasGenerator();
  
  private AasHelper aasHelper = new AasHelper();
  
  private final boolean simulateEmptyStorage = true;
  
  private final boolean simulateNotEmptyShuttle = true;
  
  public boolean isAborted = false;
  
  /**
   * Calculates the fault probability according to Bayes: \( P(F_i | D_i) \).
   */
  public Double calculateFaultProbability(final Double likelihood, final Double prior, final Double evidence) {
    if ((evidence != null && (evidence.doubleValue() == 0.0))) {
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.warning("(P(D_i) must not be 0).");
      return Double.valueOf(0.0);
    }
    double _multiply = DoubleExtensions.operator_multiply(likelihood, prior);
    return Double.valueOf((_multiply / ((evidence) == null ? 0 : (evidence).doubleValue())));
  }
  
  /**
   * Calculates the total fault probability of a hierarchical system.
   */
  @Pure
  public Double calculateSystemFaultProbability(final List<Double> faultProbabilities) {
    boolean _isEmpty = faultProbabilities.isEmpty();
    if (_isEmpty) {
      return Double.valueOf(0.0);
    }
    double systemFault = 1.0;
    for (final Double prob : faultProbabilities) {
      systemFault = (systemFault * (1 - ((prob) == null ? 0 : (prob).doubleValue())));
    }
    return Double.valueOf((1 - systemFault));
  }
  
  /**
   * Detects faults based on sensor data from the sensor values
   */
  @SuppressWarnings("potential_field_synchronization_problem")
  public List<Referable> detectFaults(final Submodel productionLog, final AssetAdministrationShell aas, final SubmodelElementCollection substepSMC) {
    final ArrayList<Referable> faultLogs = new ArrayList<Referable>();
    final String AAS1 = "https://smartfactory.de/shells/7b2e23f8-d645-4fa2-8124-459e9fcba01a";
    final String AAS2 = "https://smartfactory.de/shells/34729a69-938c-4456-8eb6-212f9a7d8063";
    final String aasId = aas.getId();
    String aasMode = "";
    boolean _equals = Objects.equal(aasId, AAS1);
    if (_equals) {
      aasMode = "assembly";
    } else {
      boolean _equals_1 = Objects.equal(aasId, AAS2);
      if (_equals_1) {
        aasMode = "transport";
      } else {
        return faultLogs;
      }
    }
    final Function1<SubmodelElement, Boolean> _function = (SubmodelElement it) -> {
      return Boolean.valueOf(((it instanceof SubmodelElementCollection) && Objects.equal(it.getIdShort(), "ResourceLogs")));
    };
    SubmodelElement _findFirst = IterableExtensions.<SubmodelElement>findFirst(productionLog.getSubmodelElements(), _function);
    SubmodelElementCollection resourceLogsWrapper = ((SubmodelElementCollection) _findFirst);
    if ((resourceLogsWrapper == null)) {
      resourceLogsWrapper = this.aasgen.createSMC("ResourceLogs");
      productionLog.getSubmodelElements().add(resourceLogsWrapper);
    }
    SubmodelElement _aASElement = this.getAASElement(resourceLogsWrapper, "ResourceLogs");
    SubmodelElementList resourceLogsList = ((SubmodelElementList) _aASElement);
    if ((resourceLogsList == null)) {
      ArrayList<SubmodelElement> _arrayList = new ArrayList<SubmodelElement>();
      resourceLogsList = this.aasgen.createSML("ResourceLogs", _arrayList);
      resourceLogsWrapper.getValue().add(resourceLogsList);
    }
    if ((this.simulateEmptyStorage && Objects.equal(aasMode, "assembly"))) {
      this.simulateResourceLog(productionLog, resourceLogsWrapper, resourceLogsList, "assembly", aas.getId());
    }
    if ((this.simulateNotEmptyShuttle && Objects.equal(aasMode, "transport"))) {
      this.simulateResourceLog(productionLog, resourceLogsWrapper, resourceLogsList, "transport", aas.getId());
    }
    final List<ReferenceElement> skills = this.extractSkillReferences(substepSMC);
    final List<Referable> generatedFaults = this.analyzeLogsAndGenerateFaults(resourceLogsList, resourceLogsWrapper, productionLog, aasMode, skills);
    faultLogs.addAll(generatedFaults);
    final Function1<SubmodelElement, Boolean> _function_1 = (SubmodelElement it) -> {
      boolean _and = false;
      if (!(it instanceof SubmodelElementCollection)) {
        _and = false;
      } else {
        SubmodelElement _aASElement_1 = this.getAASElement(((SubmodelElementCollection) it), "LogType");
        String _value = null;
        if (((Property) _aASElement_1)!=null) {
          _value=((Property) _aASElement_1).getValue();
        }
        String _lowerCase = null;
        if (_value!=null) {
          _lowerCase=_value.toLowerCase();
        }
        boolean _equals_2 = Objects.equal(_lowerCase, "fatal");
        _and = _equals_2;
      }
      return Boolean.valueOf(_and);
    };
    final boolean containsFatal = IterableExtensions.<SubmodelElement>exists(resourceLogsList.getValue(), _function_1);
    if (containsFatal) {
      this.isAborted = true;
    }
    final Function1<SubmodelElement, Boolean> _function_2 = (SubmodelElement it) -> {
      return Boolean.valueOf(((it instanceof SubmodelElementCollection) && Objects.equal(it.getIdShort(), "ResourceLogs")));
    };
    SubmodelElement _findFirst_1 = IterableExtensions.<SubmodelElement>findFirst(productionLog.getSubmodelElements(), _function_2);
    SubmodelElement _aASElement_1 = null;
    if (((SubmodelElementCollection) _findFirst_1)!=null) {
      final Function1<SubmodelElement, Boolean> _function_3 = (SubmodelElement it) -> {
        return Boolean.valueOf(((it instanceof SubmodelElementCollection) && Objects.equal(it.getIdShort(), "ResourceLogs")));
      };
      _aASElement_1=this.getAASElement(((SubmodelElementCollection) _findFirst_1), "FaultLogs");
    }
    final SubmodelElementList faultLogsSML = ((SubmodelElementList) _aASElement_1);
    if (((faultLogsSML == null) || faultLogsSML.getValue().isEmpty())) {
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info("No FaultLogs included in the ProductionLog.");
    } else {
      List<SubmodelElement> _value = faultLogsSML.getValue();
      for (final SubmodelElement elem : _value) {
        if ((elem instanceof SubmodelElementCollection)) {
          final SubmodelElementCollection smc = ((SubmodelElementCollection)elem);
          String _elvis = null;
          SubmodelElement _aASElement_2 = this.getAASElement(smc, "FaultType");
          String _value_1 = null;
          if (((Property) _aASElement_2)!=null) {
            _value_1=((Property) _aASElement_2).getValue();
          }
          if (_value_1 != null) {
            _elvis = _value_1;
          } else {
            _elvis = "?";
          }
          final String faultType = _elvis;
          String _elvis_1 = null;
          SubmodelElement _aASElement_3 = this.getAASElement(smc, "Description");
          String _value_2 = null;
          if (((Property) _aASElement_3)!=null) {
            _value_2=((Property) _aASElement_3).getValue();
          }
          if (_value_2 != null) {
            _elvis_1 = _value_2;
          } else {
            _elvis_1 = "?";
          }
          final String description = _elvis_1;
          Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
          _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1.info(((("[FaultLog]: " + faultType) + " – ") + description));
        }
      }
    }
    return faultLogs;
  }
  
  public void classifyFaults(final List<Referable> ownFaultLogs, final List<Referable> previousFaultLogs) {
    final ArrayList<Referable> combinedLogs = new ArrayList<Referable>();
    combinedLogs.addAll(ownFaultLogs);
    if (((previousFaultLogs != null) && (!previousFaultLogs.isEmpty()))) {
      combinedLogs.addAll(previousFaultLogs);
    }
    final int total = combinedLogs.size();
    if ((total == 0)) {
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info("No FaultLogs available for classification.");
      return;
    }
    final HashMap<String, Integer> groupedCounts = new HashMap<String, Integer>();
    final Function1<Referable, Boolean> _function = (Referable it) -> {
      return Boolean.valueOf((it instanceof SubmodelElementCollection));
    };
    Iterable<Referable> _filter = IterableExtensions.<Referable>filter(combinedLogs, _function);
    for (final Referable log : _filter) {
      {
        final SubmodelElementCollection smc = ((SubmodelElementCollection) log);
        SubmodelElement _aASElement = this.getAASElement(smc, "FaultType");
        String _value = null;
        if (((Property) _aASElement)!=null) {
          _value=((Property) _aASElement).getValue();
        }
        final String faultType = _value;
        if ((faultType != null)) {
          final Integer current = groupedCounts.getOrDefault(faultType, Integer.valueOf(0));
          groupedCounts.put(faultType, Integer.valueOf((((current) == null ? 0 : (current).intValue()) + 1)));
        }
      }
    }
    String maxType = "";
    double maxProb = (-1.0);
    final Function1<Referable, Boolean> _function_1 = (Referable it) -> {
      return Boolean.valueOf((it instanceof SubmodelElementCollection));
    };
    Iterable<Referable> _filter_1 = IterableExtensions.<Referable>filter(combinedLogs, _function_1);
    for (final Referable log_1 : _filter_1) {
      {
        final SubmodelElementCollection smc = ((SubmodelElementCollection) log_1);
        SubmodelElement _aASElement = this.getAASElement(smc, "FaultType");
        String _value = null;
        if (((Property) _aASElement)!=null) {
          _value=((Property) _aASElement).getValue();
        }
        final String faultType = _value;
        if ((faultType != null)) {
          final Integer count = groupedCounts.getOrDefault(faultType, Integer.valueOf(0));
          Double _double = Double.valueOf(count.doubleValue());
          final double evidence = (((_double) == null ? 0 : (_double).doubleValue()) / total);
          double _switchResult = (double) 0;
          if (faultType != null) {
            switch (faultType) {
              case "sensorFault":
                _switchResult = 0.8;
                break;
              case "shuttleOccupied":
                _switchResult = 0.6;
                break;
              case "missingProduct":
                _switchResult = 0.9;
                break;
              default:
                _switchResult = 0.5;
                break;
            }
          } else {
            _switchResult = 0.5;
          }
          final double likelihood = _switchResult;
          final double prior = 0.5;
          final Double prob = this.calculateFaultProbability(Double.valueOf(likelihood), Double.valueOf(prior), Double.valueOf(evidence));
          Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
          _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1.info(((faultType + ": ") + prob));
          if ((prob.doubleValue() > maxProb)) {
            maxProb = ((prob) == null ? 0 : (prob).doubleValue());
            maxType = faultType;
          }
        }
      }
    }
    Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
    _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1.info(("→ Classified as: " + maxType));
  }
  
  private void simulateResourceLog(final Submodel productionLog, final SubmodelElementCollection resourceLogsWrapper, final SubmodelElementList resourceLogsList, final String aasMode, final String aasId) {
    final String fakeLogId = UUID.randomUUID().toString();
    SubmodelElementCollection _switchResult = null;
    if (aasMode != null) {
      switch (aasMode) {
        case "assembly":
          _switchResult = this.generateSimulatedLog(fakeLogId, "emptyStorage", "fatal", aasId);
          break;
        case "transport":
          _switchResult = this.generateSimulatedLog(fakeLogId, "notEmptyShuttle", "error", aasId);
          break;
        default:
          _switchResult = null;
          break;
      }
    } else {
      _switchResult = null;
    }
    final SubmodelElementCollection simulatedLog = _switchResult;
    if ((simulatedLog != null)) {
      resourceLogsList.getValue().add(simulatedLog);
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info(("Simulated ResourceLog added: " + fakeLogId));
      try {
        this.aasHelper.updateSubmodel(productionLog);
      } catch (final Throwable _t) {
        if (_t instanceof Exception) {
          final Exception e = (Exception)_t;
          Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
          String _message = e.getMessage();
          _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1.warning(("Error while uploading the ResourceLogs: " + _message));
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
    }
  }
  
  private List<Referable> analyzeLogsAndGenerateFaults(final SubmodelElementList logsList, final SubmodelElementCollection resourceLogsWrapper, final Submodel productionLog, final String aasMode, final List<ReferenceElement> skills) {
    final ArrayList<Referable> generatedFaults = new ArrayList<Referable>();
    List<SubmodelElement> _value = logsList.getValue();
    for (final SubmodelElement entry : _value) {
      if ((entry instanceof SubmodelElementCollection)) {
        final SubmodelElementCollection log = ((SubmodelElementCollection)entry);
        SubmodelElement _aASElement = this.getAASElement(log, "LogType");
        String _value_1 = null;
        if (((Property) _aASElement)!=null) {
          _value_1=((Property) _aASElement).getValue();
        }
        String _lowerCase = null;
        if (_value_1!=null) {
          _lowerCase=_value_1.toLowerCase();
        }
        final String logType = _lowerCase;
        SubmodelElement _aASElement_1 = this.getAASElement(log, "Description");
        String _value_2 = null;
        if (((Property) _aASElement_1)!=null) {
          _value_2=((Property) _aASElement_1).getValue();
        }
        String _lowerCase_1 = null;
        if (_value_2!=null) {
          _lowerCase_1=_value_2.toLowerCase();
        }
        final String description = _lowerCase_1;
        SubmodelElement _aASElement_2 = this.getAASElement(log, "LogId");
        String _value_3 = null;
        if (((Property) _aASElement_2)!=null) {
          _value_3=((Property) _aASElement_2).getValue();
        }
        final String logId = _value_3;
        if ((((Objects.equal(logType, "error") || Objects.equal(logType, "fatal")) && (description != null)) && (logId != null))) {
          Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
          _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info(((("Found ResourceLog: " + logId) + " – ") + description));
          if (aasMode != null) {
            switch (aasMode) {
              case "assembly":
                boolean _contains = description.contains("emptystorage");
                if (_contains) {
                  generatedFaults.add(this.generateSensorFault2(logId, skills));
                  generatedFaults.add(this.generateMissingProductFault(logId, skills));
                }
                break;
              case "transport":
                boolean _contains_1 = description.contains("notemptyshuttle");
                if (_contains_1) {
                  generatedFaults.add(this.generateSensorFault(logId, skills));
                  generatedFaults.add(this.generateShuttleFullFault(logId, skills));
                }
                break;
            }
          }
        }
      }
    }
    SubmodelElement _aASElement_3 = this.getAASElement(resourceLogsWrapper, "FaultLogs");
    SubmodelElementList faultLogsSML = ((SubmodelElementList) _aASElement_3);
    if ((faultLogsSML == null)) {
      ArrayList<SubmodelElement> _arrayList = new ArrayList<SubmodelElement>();
      faultLogsSML = this.aasgen.createSML("FaultLogs", _arrayList);
      resourceLogsWrapper.getValue().add(faultLogsSML);
    }
    final Function1<Referable, Boolean> _function = (Referable it) -> {
      return Boolean.valueOf((it instanceof SubmodelElement));
    };
    final Function1<Referable, SubmodelElement> _function_1 = (Referable it) -> {
      return ((SubmodelElement) it);
    };
    Iterables.<SubmodelElement>addAll(faultLogsSML.getValue(), IterableExtensions.<Referable, SubmodelElement>map(IterableExtensions.<Referable>filter(generatedFaults, _function), _function_1));
    try {
      this.aasHelper.updateSubmodel(productionLog);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
        String _message = e.getMessage();
        _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1.warning(("Error while uploading the FaultLogs: " + _message));
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    return generatedFaults;
  }
  
  private List<ReferenceElement> extractSkillReferences(final SubmodelElementCollection substepSMC) {
    final ArrayList<ReferenceElement> skillRefs = new ArrayList<ReferenceElement>();
    final Function1<SubmodelElement, Boolean> _function = (SubmodelElement it) -> {
      return Boolean.valueOf(((it instanceof SubmodelElementList) && Objects.equal(it.getIdShort(), "Actions")));
    };
    SubmodelElement _findFirst = IterableExtensions.<SubmodelElement>findFirst(substepSMC.getValue(), _function);
    final SubmodelElementList actions = ((SubmodelElementList) _findFirst);
    if ((actions != null)) {
      List<SubmodelElement> _value = actions.getValue();
      for (final SubmodelElement actionEntry : _value) {
        if ((actionEntry instanceof SubmodelElementCollection)) {
          final Function1<SubmodelElement, Boolean> _function_1 = (SubmodelElement it) -> {
            return Boolean.valueOf(((it instanceof ReferenceElement) && Objects.equal(it.getIdShort(), "SkillReference")));
          };
          SubmodelElement _findFirst_1 = IterableExtensions.<SubmodelElement>findFirst(((SubmodelElementCollection)actionEntry).getValue(), _function_1);
          final ReferenceElement skills = ((ReferenceElement) _findFirst_1);
          if ((skills != null)) {
            skillRefs.add(skills);
          }
        }
      }
    }
    return skillRefs;
  }
  
  private SubmodelElementCollection generateSimulatedLog(final String logId, final String description, final String logType, final String aasId) {
    final ArrayList<SubmodelElement> elements = new ArrayList<SubmodelElement>();
    elements.add(this.aasgen.createProperty("LogId", DataTypeDefXsd.STRING, logId));
    elements.add(this.aasgen.createProperty("DateTime", DataTypeDefXsd.DATE_TIME, OffsetDateTime.now().toString()));
    elements.add(this.aasgen.createProperty("LogType", DataTypeDefXsd.STRING, logType));
    elements.add(this.aasgen.createProperty("Description", DataTypeDefXsd.STRING, description));
    final Map<KeyTypes, String> refMap = Map.<KeyTypes, String>of(KeyTypes.ASSET_ADMINISTRATION_SHELL, aasId);
    elements.add(this.aasgen.createReferenceElement("RefResourceAAS", ReferenceTypes.MODEL_REFERENCE, refMap));
    final ArrayList<SubmodelElement> dataObjects = new ArrayList<SubmodelElement>();
    dataObjects.add(this.aasgen.createProperty("DataType", DataTypeDefXsd.STRING, "SensorValue"));
    dataObjects.add(this.aasgen.createProperty("Value", DataTypeDefXsd.BOOLEAN, "false"));
    final SubmodelElementCollection dataObjectsSMC = this.aasgen.createSMC("Object000", dataObjects);
    final SubmodelElementList dataObjectsSML = this.aasgen.createSML("DataObjects", List.<SubmodelElement>of(dataObjectsSMC));
    elements.add(dataObjectsSML);
    return this.aasgen.createSMC(("ResourceLog_" + logId), elements);
  }
  
  private SubmodelElementCollection generateShuttleFullFault(final String logId, final List<ReferenceElement> skills) {
    final ArrayList<SubmodelElement> elements = new ArrayList<SubmodelElement>();
    elements.add(this.aasgen.createProperty("Status", DataTypeDefXsd.STRING, "open"));
    elements.add(this.aasgen.createProperty("Severity", DataTypeDefXsd.STRING, "high"));
    elements.add(this.aasgen.createProperty("FaultType", DataTypeDefXsd.STRING, "shuttleOccupied"));
    elements.add(this.aasgen.createProperty("AffectedComponent", DataTypeDefXsd.STRING, "TransportModule"));
    elements.add(
      this.aasgen.createProperty("Description", DataTypeDefXsd.STRING, "Shuttle is occupied"));
    elements.add(this.aasgen.createProperty("DetectionDateTime", DataTypeDefXsd.DATE_TIME, OffsetDateTime.now().toString()));
    final SubmodelElementCollection skillSMC = this.aasgen.createSMC("AffectedSkills");
    int _size = skills.size();
    ExclusiveRange _doubleDotLessThan = new ExclusiveRange(0, _size, true);
    for (final Integer i : _doubleDotLessThan) {
      {
        final ReferenceElement original = skills.get(((i) == null ? 0 : (i).intValue()));
        final DefaultReferenceElement refElem = new DefaultReferenceElement();
        refElem.setIdShort(("RefToAffectedSkill" + Integer.valueOf((((i) == null ? 0 : (i).intValue()) + 1))));
        refElem.setValue(original.getValue());
        skillSMC.getValue().add(refElem);
      }
    }
    elements.add(skillSMC);
    return this.aasgen.createSMC(("FaultLog_ShuttleFull_" + logId), elements);
  }
  
  private SubmodelElementCollection generateSensorFault(final String logId, final List<ReferenceElement> skills) {
    final ArrayList<SubmodelElement> elements = new ArrayList<SubmodelElement>();
    elements.add(this.aasgen.createProperty("Status", DataTypeDefXsd.STRING, "open"));
    elements.add(this.aasgen.createProperty("Severity", DataTypeDefXsd.STRING, "medium"));
    elements.add(this.aasgen.createProperty("FaultType", DataTypeDefXsd.STRING, "sensorFault"));
    elements.add(this.aasgen.createProperty("AffectedComponent", DataTypeDefXsd.STRING, "TransportModule"));
    elements.add(this.aasgen.createProperty("Description", DataTypeDefXsd.STRING, 
      "Sensor is faulty"));
    elements.add(this.aasgen.createProperty("DetectionDateTime", DataTypeDefXsd.DATE_TIME, OffsetDateTime.now().toString()));
    final SubmodelElementCollection skillSMC = this.aasgen.createSMC("AffectedSkills");
    int _size = skills.size();
    ExclusiveRange _doubleDotLessThan = new ExclusiveRange(0, _size, true);
    for (final Integer i : _doubleDotLessThan) {
      {
        final ReferenceElement original = skills.get(((i) == null ? 0 : (i).intValue()));
        final DefaultReferenceElement refElem = new DefaultReferenceElement();
        refElem.setIdShort(("RefToAffectedSkill" + Integer.valueOf((((i) == null ? 0 : (i).intValue()) + 1))));
        refElem.setValue(original.getValue());
        skillSMC.getValue().add(refElem);
      }
    }
    elements.add(skillSMC);
    return this.aasgen.createSMC(("FaultLog_SensorFailure_" + logId), elements);
  }
  
  private SubmodelElementCollection generateSensorFault2(final String logId, final List<ReferenceElement> skills) {
    final ArrayList<SubmodelElement> elements = new ArrayList<SubmodelElement>();
    elements.add(this.aasgen.createProperty("Status", DataTypeDefXsd.STRING, "open"));
    elements.add(this.aasgen.createProperty("Severity", DataTypeDefXsd.STRING, "medium"));
    elements.add(this.aasgen.createProperty("FaultType", DataTypeDefXsd.STRING, "sensorFault"));
    elements.add(this.aasgen.createProperty("AffectedComponent", DataTypeDefXsd.STRING, "AssembleModule"));
    elements.add(this.aasgen.createProperty("Description", DataTypeDefXsd.STRING, "Sensor is faulty"));
    elements.add(this.aasgen.createProperty("DetectionDateTime", DataTypeDefXsd.DATE_TIME, OffsetDateTime.now().toString()));
    final SubmodelElementCollection skillSMC = this.aasgen.createSMC("AffectedSkills");
    int _size = skills.size();
    ExclusiveRange _doubleDotLessThan = new ExclusiveRange(0, _size, true);
    for (final Integer i : _doubleDotLessThan) {
      {
        final ReferenceElement original = skills.get(((i) == null ? 0 : (i).intValue()));
        final DefaultReferenceElement refElem = new DefaultReferenceElement();
        refElem.setIdShort(("RefToAffectedSkill" + Integer.valueOf((((i) == null ? 0 : (i).intValue()) + 1))));
        refElem.setValue(original.getValue());
        skillSMC.getValue().add(refElem);
      }
    }
    elements.add(skillSMC);
    return this.aasgen.createSMC(("FaultLog_SensorFailure_" + logId), elements);
  }
  
  private SubmodelElementCollection generateMissingProductFault(final String logId, final List<ReferenceElement> skills) {
    final ArrayList<SubmodelElement> elements = new ArrayList<SubmodelElement>();
    elements.add(this.aasgen.createProperty("Status", DataTypeDefXsd.STRING, "open"));
    elements.add(this.aasgen.createProperty("Severity", DataTypeDefXsd.STRING, "high"));
    elements.add(this.aasgen.createProperty("FaultType", DataTypeDefXsd.STRING, "missingProduct"));
    elements.add(this.aasgen.createProperty("AffectedComponent", DataTypeDefXsd.STRING, "AssemblyModule"));
    elements.add(this.aasgen.createProperty("Description", DataTypeDefXsd.STRING, 
      "Product is missing"));
    elements.add(this.aasgen.createProperty("DetectionDateTime", DataTypeDefXsd.DATE_TIME, OffsetDateTime.now().toString()));
    final SubmodelElementCollection skillSMC = this.aasgen.createSMC("AffectedSkills");
    int _size = skills.size();
    ExclusiveRange _doubleDotLessThan = new ExclusiveRange(0, _size, true);
    for (final Integer i : _doubleDotLessThan) {
      {
        final ReferenceElement original = skills.get(((i) == null ? 0 : (i).intValue()));
        final DefaultReferenceElement refElem = new DefaultReferenceElement();
        refElem.setIdShort(("RefToAffectedSkill" + Integer.valueOf((((i) == null ? 0 : (i).intValue()) + 1))));
        refElem.setValue(original.getValue());
        skillSMC.getValue().add(refElem);
      }
    }
    elements.add(skillSMC);
    return this.aasgen.createSMC(("FaultLog_MissingProduct_" + logId), elements);
  }
  
  @Pure
  private SubmodelElement getAASElement(final SubmodelElementCollection smc, final String idShort) {
    try {
      final Predicate<SubmodelElement> _function = (SubmodelElement sme) -> {
        return sme.getIdShort().equals(idShort);
      };
      return smc.getValue().stream().filter(_function).findFirst().get();
    } catch (final Throwable _t) {
      if (_t instanceof NoSuchElementException) {
        Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
        String _idShort = smc.getIdShort();
        _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.warning((((("AAS-Element \'" + idShort) + "\' not found in \'") + _idShort) + "\'"));
        return null;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
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
    FaultDetectionSkill other = (FaultDetectionSkill) obj;
    if (other.simulateEmptyStorage != this.simulateEmptyStorage)
      return false;
    if (other.simulateNotEmptyShuttle != this.simulateNotEmptyShuttle)
      return false;
    if (other.isAborted != this.isAborted)
      return false;
    return super.equals(obj);
  }
  
  @Override
  @Pure
  @SyntheticMember
  public int hashCode() {
    int result = super.hashCode();
    final int prime = 31;
    result = prime * result + Boolean.hashCode(this.simulateEmptyStorage);
    result = prime * result + Boolean.hashCode(this.simulateNotEmptyShuttle);
    result = prime * result + Boolean.hashCode(this.isAborted);
    return result;
  }
  
  @SyntheticMember
  public FaultDetectionSkill() {
    super();
  }
  
  @SyntheticMember
  public FaultDetectionSkill(final Agent agent) {
    super(agent);
  }
}

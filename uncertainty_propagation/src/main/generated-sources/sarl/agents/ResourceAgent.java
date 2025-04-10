package agents;

import agents.AbstractAgent;
import capacities.SharedResourceCapacity;
import capacities.UncertaintyEstimatorCapacity;
import com.google.common.base.Objects;
import events.ExecutionRequest;
import events.ExecutionUpdate;
import events.RequestProduction;
import events.ResourceUpdate;
import helper.AasGenerator;
import i40_messages.I40_Message;
import i40_messages.I40_MessageTypes;
import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.ExternalContextAccess;
import io.sarl.core.Initialize;
import io.sarl.core.Lifecycle;
import io.sarl.core.Logging;
import io.sarl.lang.annotation.ImportedCapacityFeature;
import io.sarl.lang.annotation.PerceptGuardEvaluator;
import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.annotation.SyntheticMember;
import io.sarl.lang.core.AtomicSkillReference;
import io.sarl.lang.core.Event;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.EntityType;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import skills.SharedResourceSkill;
import skills.UncertaintyEstimatorSkill;

/**
 * @author Alexis Bernhard
 */
@SuppressWarnings("discouraged_occurrence_readonly_use")
@SarlSpecification("0.12")
@SarlElementType(19)
public class ResourceAgent extends AbstractAgent {
  private UncertaintyEstimatorSkill estimateUncertaintySkill;
  
  private Submodel bom;
  
  private Submodel skillSM;
  
  private Submodel productionPlan;
  
  private Submodel productionLog;
  
  private AasGenerator aasgen;
  
  private Map<String, String> skills;
  
  private Map<String, String[]> messageBuffer = new HashMap<String, String[]>();
  
  private SharedResourceSkill sharedResourceSkill3;
  
  private Map<String, String> childSteps = new HashMap<String, String>();
  
  protected void spawnSubAgents() {
    SubmodelElement resource_holon = this.bom.getSubmodelElements().stream().findFirst().get();
    if ((!(resource_holon instanceof Entity))) {
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      String _idShort = this.aas.getIdShort();
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.warning((("On the top level of the bom of AAS " + _idShort) + 
        " a single entity of the resource represented by the AAS is expected."));
      return;
    }
    Entity resource_holon_entity = ((Entity) resource_holon);
    int _size = resource_holon_entity.getStatements().size();
    if ((_size < 1)) {
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      String _idShort_1 = this.aas.getIdShort();
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1.info((("The topology of the resource AAS " + _idShort_1) + " does not contain any agent."));
    }
    List<SubmodelElement> _statements = resource_holon_entity.getStatements();
    for (final SubmodelElement resource : _statements) {
      {
        Entity resource_Entity = ((Entity) resource);
        String globalAssetId = resource_Entity.getGlobalAssetId();
        EntityType _entityType = resource_Entity.getEntityType();
        boolean _notEquals = (!Objects.equal(_entityType, EntityType.SELF_MANAGED_ENTITY));
        if (_notEquals) {
          Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_2 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
          String _idShort_2 = resource_Entity.getIdShort();
          String _idShort_3 = resource_holon.getIdShort();
          _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_2.info((((("The entity " + _idShort_2) + " in the bom of resource ") + _idShort_3) + 
            "is co-managed and no AAS is searched."));
        } else {
          if ((globalAssetId == null)) {
            Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_3 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
            String _idShort_4 = resource_Entity.getIdShort();
            _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_3.warning((("Warning the global asset id of the resource " + _idShort_4) + " is null."));
          } else {
            Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_4 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
            _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_4.info(("Spwan agnet with asset id: " + globalAssetId));
            Lifecycle _$CAPACITY_USE$IO_SARL_CORE_LIFECYCLE$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LIFECYCLE$CALLER();
            DefaultContextInteractions _$CAPACITY_USE$IO_SARL_CORE_DEFAULTCONTEXTINTERACTIONS$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_DEFAULTCONTEXTINTERACTIONS$CALLER();
            _$CAPACITY_USE$IO_SARL_CORE_LIFECYCLE$CALLER.spawnInContext(ResourceAgent.class, _$CAPACITY_USE$IO_SARL_CORE_DEFAULTCONTEXTINTERACTIONS$CALLER.getDefaultContext(), globalAssetId, this.sharedResourceSkill);
          }
        }
      }
    }
  }
  
  private void $behaviorUnit$Initialize$0(final Initialize occurrence) {
    boolean _init = this.init(occurrence.parameters, "Resource");
    if (_init) {
      this.bom = this.aasHelper.getSubmodelByIdShort(this.aas, "BillOfMaterial");
      this.skillSM = this.aasHelper.getSubmodelByIdShort(this.aas, "Skills");
      this.productionPlan = this.aasHelper.getSubmodelByIdShort(this.aas, "ProductionPlan");
      this.productionLog = this.aasHelper.getSubmodelByIdShort(this.aas, "ProductionLog");
      this.resetAAS();
      this.spawnSubAgents();
      AasGenerator _aasGenerator = new AasGenerator();
      this.aasgen = _aasGenerator;
      UncertaintyEstimatorSkill _uncertaintyEstimatorSkill = new UncertaintyEstimatorSkill();
      this.estimateUncertaintySkill = _uncertaintyEstimatorSkill;
      this.<UncertaintyEstimatorSkill>setSkill(this.estimateUncertaintySkill, UncertaintyEstimatorCapacity.class);
      SharedResourceSkill _sharedResourceSkill = new SharedResourceSkill();
      this.sharedResourceSkill3 = _sharedResourceSkill;
      this.<SharedResourceSkill>setSkill(this.sharedResourceSkill3, SharedResourceCapacity.class);
      boolean _notEquals = (!Objects.equal(this.skillSM, null));
      if (_notEquals) {
        this.loadSkills();
      }
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info(("Initializing CC Agent completed. AAS_id: " + this.aas_id));
    }
  }
  
  private void $behaviorUnit$ExecutionRequest$1(final ExecutionRequest occurrence) {
    Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
    long _nanoTime = System.nanoTime();
    _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info(("Start request at " + Long.valueOf(_nanoTime)));
    Referable _remove = occurrence.message.getInteractionElements().remove(0);
    SubmodelElementCollection step = ((SubmodelElementCollection) _remove);
    SubmodelElement _aASChild = this.aasHelper.getAASChild(this.productionPlan, "Steps");
    SubmodelElementList stepList = ((SubmodelElementList) _aASChild);
    stepList.getValue().add(step);
    this.aasHelper.updateSubmodel(this.productionPlan);
    SubmodelElement _aASChild_1 = this.aasHelper.getAASChild(step, "StepId");
    Property stepId = ((Property) _aASChild_1);
    ArrayList<SubmodelElementCollection> steps = new ArrayList<SubmodelElementCollection>();
    List<Referable> _interactionElements = occurrence.message.getInteractionElements();
    for (final Referable elem : _interactionElements) {
      if ((elem instanceof SubmodelElementCollection)) {
        SubmodelElementCollection step1 = ((SubmodelElementCollection)elem);
        steps.add(step1);
      }
    }
    ArrayList<SubmodelElement> elements = new ArrayList<SubmodelElement>();
    elements.add(this.aasgen.createProperty("ConversationId", DataTypeDefXsd.STRING, occurrence.message.getFrame().getConversationID()));
    elements.add(this.aasgen.createProperty("Sender", DataTypeDefXsd.STRING, occurrence.message.getFrame().getSender()));
    SubmodelElementCollection productMetaData = this.aasgen.createSMC("UncertaintyResult", elements);
    this.executeHeterarchicalProduction2(null, productMetaData, steps);
  }
  
  @SyntheticMember
  @Pure
  private boolean $behaviorUnitGuard$ExecutionRequest$1(final ExecutionRequest it, final ExecutionRequest occurrence) {
    String _receiver = occurrence.message.getFrame().getReceiver();
    boolean _equals = Objects.equal(_receiver, this.aas_id);
    return _equals;
  }
  
  private void $behaviorUnit$ResourceUpdate$2(final ResourceUpdate occurrence) {
    Referable _get = occurrence.message.getInteractionElements().get(1);
    Property stepId = ((Property) _get);
    Referable _get_1 = occurrence.message.getInteractionElements().get(0);
    SubmodelElementCollection logEntry = ((SubmodelElementCollection) _get_1);
    SubmodelElement _aASChild = this.aasHelper.getAASChild(logEntry, "DataObjects");
    SubmodelElementList dataObjects = ((SubmodelElementList) _aASChild);
    if ((occurrence.uc == 1)) {
      this.sharedResourceSkill.changeCounter(stepId.getValue(), false);
      SubmodelElement _get_2 = dataObjects.getValue().get(0);
      this.sharedResourceSkill.changeUncertainty(stepId.getValue(), ((SubmodelElementCollection) _get_2));
      Integer _counter = this.sharedResourceSkill.getCounter(stepId.getValue());
      if ((_counter.intValue() <= 0)) {
        double[] result = this.estimateUncertaintySkill.fuseSMC(this.sharedResourceSkill.getUncertainty(stepId.getValue()));
        Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
        double _get_3 = result[0];
        double _get_4 = result[1];
        double _get_5 = result[2];
        _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info(
          (((((("Resulting distributed uncertainty 1: [" + Double.valueOf(_get_3)) + ", ") + Double.valueOf(_get_4)) + ", ") + Double.valueOf(_get_5)) + "]"));
        this.emitExecutionUpdateMessage(result, 1, occurrence.message.getFrame().getConversationID(), occurrence.message.getFrame().getSender());
      }
    } else {
      if ((occurrence.uc == 3)) {
        String parentStepId = this.childSteps.remove(stepId.getValue());
        this.sharedResourceSkill3.changeCounter(parentStepId, false);
        SubmodelElement _get_6 = dataObjects.getValue().get(0);
        this.sharedResourceSkill3.changeUncertainty(parentStepId, ((SubmodelElementCollection) _get_6));
        this.generateFinalOutput(this.messageBuffer.get(parentStepId)[0], this.messageBuffer.get(parentStepId)[1], parentStepId);
      }
    }
  }
  
  @SyntheticMember
  @Pure
  private boolean $behaviorUnitGuard$ResourceUpdate$2(final ResourceUpdate it, final ResourceUpdate occurrence) {
    String _receiver = occurrence.message.getFrame().getReceiver();
    boolean _equals = Objects.equal(_receiver, this.aas_id);
    return _equals;
  }
  
  private void $behaviorUnit$RequestProduction$3(final RequestProduction occurrence) {
    if ((occurrence.uc == 1)) {
      Referable _get = occurrence.message.getInteractionElements().get(0);
      SubmodelElementCollection step = ((SubmodelElementCollection) _get);
      Referable _get_1 = occurrence.message.getInteractionElements().get(1);
      Property generalStepId = ((Property) _get_1);
      SubmodelElement _aASChild = this.aasHelper.getAASChild(step, "StepId");
      Property stepId = ((Property) _aASChild);
      SubmodelElement _aASChild_1 = this.aasHelper.getAASChild(step, "Actions");
      SubmodelElementList actions = ((SubmodelElementList) _aASChild_1);
      if ((actions != null)) {
        ArrayList<double[]> uncertainties = new ArrayList<double[]>();
        this.sharedResourceSkill.changeCounter(generalStepId.getValue(), true);
        List<SubmodelElement> _value = actions.getValue();
        for (final SubmodelElement action : _value) {
          {
            SubmodelElementCollection actionSMC = ((SubmodelElementCollection) action);
            SubmodelElement _aASChild_2 = this.aasHelper.getAASChild(actionSMC, "SkillReference");
            ReferenceElement actionReference = ((ReferenceElement) _aASChild_2);
            uncertainties.add(this.estimateUncertaintySkill.estimate_action_uncertainty(actionReference.toString()));
          }
        }
        double[] result = this.estimateUncertaintySkill.fuse(uncertainties);
        this.emitResourceUpdateMessage(result, 1, generalStepId.getValue(), occurrence.message.getFrame().getSender(), occurrence.message.getFrame().getConversationID());
      }
    } else {
      if ((occurrence.uc == 2)) {
        Referable _remove = occurrence.message.getInteractionElements().remove(0);
        SubmodelElementCollection uncertainty = ((SubmodelElementCollection) _remove);
        Referable _remove_1 = occurrence.message.getInteractionElements().remove(0);
        SubmodelElementCollection productMetaData = ((SubmodelElementCollection) _remove_1);
        ArrayList<SubmodelElementCollection> steps = new ArrayList<SubmodelElementCollection>();
        List<Referable> _interactionElements = occurrence.message.getInteractionElements();
        for (final Referable elem : _interactionElements) {
          if ((elem instanceof SubmodelElementCollection)) {
            SubmodelElementCollection step_1 = ((SubmodelElementCollection)elem);
            steps.add(step_1);
          }
        }
        this.executeHeterarchicalProduction2(uncertainty, productMetaData, steps);
      } else {
        if ((occurrence.uc == 3)) {
          Referable _get_2 = occurrence.message.getInteractionElements().get(0);
          SubmodelElementCollection step_2 = ((SubmodelElementCollection) _get_2);
          SubmodelElement _aASChild_2 = this.aasHelper.getAASChild(step_2, "StepId");
          Property stepId_1 = ((Property) _aASChild_2);
          this.sharedResourceSkill3.addKeyCounter(stepId_1.getValue());
          this.sharedResourceSkill3.addKeyUncertainty(stepId_1.getValue());
          String _sender = occurrence.message.getFrame().getSender();
          String _conversationID = occurrence.message.getFrame().getConversationID();
          List<String> messageMetaData = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_sender, _conversationID));
          final List<String> _converted_messageMetaData = (List<String>)messageMetaData;
          this.messageBuffer.put(stepId_1.getValue(), ((String[])Conversions.unwrapArray(_converted_messageMetaData, String.class)));
          this.executeHierarchicalProduction3(step_2);
        } else {
          Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
          _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.error("No connection type defined when requesting a component");
        }
      }
    }
  }
  
  @SyntheticMember
  @Pure
  private boolean $behaviorUnitGuard$RequestProduction$3(final RequestProduction it, final RequestProduction occurrence) {
    String _receiver = occurrence.message.getFrame().getReceiver();
    boolean _equals = Objects.equal(_receiver, this.aas_id);
    return _equals;
  }
  
  protected void loadSkills() {
    HashMap<String, String> _hashMap = new HashMap<String, String>();
    this.skills = _hashMap;
    final Predicate<SubmodelElement> _function = (SubmodelElement smc) -> {
      return smc.getIdShort().contains("SkillSet");
    };
    SubmodelElement _get = this.skillSM.getSubmodelElements().stream().filter(_function).findFirst().get();
    SubmodelElementList skillList = ((SubmodelElementList) _get);
    List<SubmodelElement> _value = skillList.getValue();
    for (final SubmodelElement resskill : _value) {
      {
        SubmodelElementCollection resskillSMC = ((SubmodelElementCollection) resskill);
        String buffer = "";
        List<SubmodelElement> _value_1 = resskillSMC.getValue();
        for (final SubmodelElement skillEntry : _value_1) {
          if ((skillEntry instanceof Property)) {
            boolean _equals = ((Property)skillEntry).getIdShort().equals("SkillId");
            if (_equals) {
              buffer = ((Property)skillEntry).getValue();
            } else {
              boolean _equals_1 = ((Property)skillEntry).getIdShort().equals("SkillName");
              if (_equals_1) {
                this.skills.put(buffer, ((Property)skillEntry).getValue());
              }
            }
          }
        }
      }
    }
  }
  
  protected SubmodelElementCollection arrayToSMC(final double[] array) {
    ArrayList<SubmodelElement> elements = new ArrayList<SubmodelElement>();
    elements.add(this.aasgen.createProperty("Mean", DataTypeDefXsd.DOUBLE, Double.valueOf(array[0]).toString()));
    elements.add(this.aasgen.createProperty("Variance", DataTypeDefXsd.DOUBLE, Double.valueOf(array[1]).toString()));
    elements.add(this.aasgen.createProperty("Weight", DataTypeDefXsd.DOUBLE, Double.valueOf(array[2]).toString()));
    return this.aasgen.createSMC("UncertaintyResult", elements);
  }
  
  protected SubmodelElementCollection generateLogEntry(final String dateTime, final String logType, final String description, final List<SubmodelElement> dataObjects) {
    ArrayList<SubmodelElement> logEntryElems = new ArrayList<SubmodelElement>();
    logEntryElems.add(this.aasgen.createProperty("LogId", DataTypeDefXsd.STRING, UUID.randomUUID().toString()));
    logEntryElems.add(this.aasgen.createProperty("DateTime", DataTypeDefXsd.DATE_TIME, dateTime));
    logEntryElems.add(this.aasgen.createProperty("LogType", DataTypeDefXsd.STRING, logType));
    logEntryElems.add(this.aasgen.createProperty("Description", DataTypeDefXsd.STRING, description));
    Map<KeyTypes, String> refKeys = new HashMap<KeyTypes, String>();
    refKeys.put(KeyTypes.ASSET_ADMINISTRATION_SHELL, this.aas_id);
    logEntryElems.add(this.aasgen.createReferenceElement("RefResourceAAS", ReferenceTypes.MODEL_REFERENCE, refKeys));
    logEntryElems.add(this.aasgen.createSML("DataObjects", dataObjects));
    return this.aasgen.createSMC("LogEntry", logEntryElems);
  }
  
  protected void resetAAS() {
    SubmodelElement _aASChild = this.aasHelper.getAASChild(this.productionPlan, "Steps");
    SubmodelElementList stepList = ((SubmodelElementList) _aASChild);
    stepList.getValue().clear();
    this.aasHelper.updateSubmodel(this.productionPlan);
    SubmodelElement _aASChild_1 = this.aasHelper.getAASChild(this.productionLog, "ResourceLogs");
    SubmodelElementCollection resLogs = ((SubmodelElementCollection) _aASChild_1);
    SubmodelElement _aASChild_2 = this.aasHelper.getAASChild(resLogs, "ResourceLogs");
    ((SubmodelElementList) _aASChild_2).getValue().clear();
    this.aasHelper.updateSubmodel(this.productionLog);
  }
  
  /**
   * This method calculates the uncertainty of a given step as argument and returns the fused uncertainty value.
   */
  protected Object executeHierarchicalProduction(final Property stepId, final SubmodelElementCollection step) {
    this.subStepCallExecution(stepId, step);
    SubmodelElement _aASChild = this.aasHelper.getAASChild(step, "Actions");
    SubmodelElementList actions = ((SubmodelElementList) _aASChild);
    if ((actions != null)) {
      ArrayList<Referable> interactionElements = new ArrayList<Referable>();
      interactionElements.add(step);
      interactionElements.add(stepId);
      this.emitRequestProductionMessage(step, interactionElements, 1);
    }
    return null;
  }
  
  /**
   * This method calculates the uncertainty of a given step as argument and returns the fused uncertainty value.
   */
  protected void executeHeterarchicalProduction2(final SubmodelElementCollection pastUncertainty, final SubmodelElementCollection productMetaData, final List<SubmodelElementCollection> steps) {
    int _length = ((Object[])Conversions.unwrapArray(steps, Object.class)).length;
    if ((_length > 0)) {
      SubmodelElementCollection step = steps.remove(0);
      SubmodelElement _aASChild = this.aasHelper.getAASChild(step, "Actions");
      SubmodelElementList actions = ((SubmodelElementList) _aASChild);
      if ((actions != null)) {
        ArrayList<double[]> uncertainties = new ArrayList<double[]>();
        List<SubmodelElement> _value = actions.getValue();
        for (final SubmodelElement action : _value) {
          {
            SubmodelElementCollection actionSMC = ((SubmodelElementCollection) action);
            SubmodelElement _aASChild_1 = this.aasHelper.getAASChild(actionSMC, "SkillReference");
            ReferenceElement actionReference = ((ReferenceElement) _aASChild_1);
            double[] actionUncertainty = this.estimateUncertaintySkill.estimate_action_uncertainty(actionReference.toString());
            uncertainties.add(actionUncertainty);
          }
        }
        SubmodelElementCollection actionUncertaintyCollection = this.arrayToSMC(this.estimateUncertaintySkill.fuse(uncertainties));
        ArrayList<SubmodelElementCollection> allUncertainties = new ArrayList<SubmodelElementCollection>();
        allUncertainties.add(actionUncertaintyCollection);
        if ((pastUncertainty != null)) {
          allUncertainties.add(pastUncertainty);
        }
        double[] result = this.estimateUncertaintySkill.fuseSMC(allUncertainties);
        List<Referable> interactionElements = new ArrayList<Referable>();
        int _length_1 = ((Object[])Conversions.unwrapArray(steps, Object.class)).length;
        if ((_length_1 > 0)) {
          interactionElements.add(this.arrayToSMC(result));
          interactionElements.add(productMetaData);
          interactionElements.addAll(steps);
          this.emitRequestProductionMessage(steps.get(0), interactionElements, 2);
        } else {
          Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
          double _get = result[0];
          double _get_1 = result[1];
          double _get_2 = result[2];
          _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info((((((("Resulting distributed uncertainty 2: [" + Double.valueOf(_get)) + ", ") + Double.valueOf(_get_1)) + ", ") + Double.valueOf(_get_2)) + "]"));
          SubmodelElement _aASChild_1 = this.aasHelper.getAASChild(productMetaData, "Sender");
          Property sender = ((Property) _aASChild_1);
          SubmodelElement _aASChild_2 = this.aasHelper.getAASChild(productMetaData, "ConversationId");
          Property convId = ((Property) _aASChild_2);
          this.emitExecutionUpdateMessage(result, 2, sender.getValue(), convId.getValue());
        }
      }
    }
  }
  
  protected void executeHierarchicalProduction3(final SubmodelElementCollection step) {
    this.subStepCallExecution3(step);
    SubmodelElement _aASChild = this.aasHelper.getAASChild(step, "StepId");
    Property stepId = ((Property) _aASChild);
    SubmodelElement _aASChild_1 = this.aasHelper.getAASChild(step, "Actions");
    SubmodelElementList actions = ((SubmodelElementList) _aASChild_1);
    if ((actions != null)) {
      ArrayList<double[]> uncertainties = new ArrayList<double[]>();
      List<SubmodelElement> _value = actions.getValue();
      for (final SubmodelElement action : _value) {
        {
          SubmodelElementCollection actionSMC = ((SubmodelElementCollection) action);
          SubmodelElement _aASChild_2 = this.aasHelper.getAASChild(actionSMC, "SkillReference");
          ReferenceElement actionReference = ((ReferenceElement) _aASChild_2);
          double[] result = this.estimateUncertaintySkill.estimate_action_uncertainty(actionReference.toString());
          uncertainties.add(result);
        }
      }
      this.sharedResourceSkill3.changeUncertainty(stepId.getValue(), 
        this.arrayToSMC(this.estimateUncertaintySkill.fuse(uncertainties)));
      this.generateFinalOutput(this.messageBuffer.get(stepId.getValue())[0], this.messageBuffer.get(stepId.getValue())[1], 
        stepId.getValue());
    }
  }
  
  /**
   * This routine represents the uncertainty of a substep und fetches all uncertainty of itschilds and returns one shared uncerainty to the product holen.
   */
  private void subStepCallExecution(final Property stepId, final SubmodelElementCollection step) {
    SubmodelElement _aASChild = this.aasHelper.getAASChild(step, "Substeps");
    SubmodelElementList substeps = ((SubmodelElementList) _aASChild);
    if ((substeps != null)) {
      List<SubmodelElement> _value = substeps.getValue();
      for (final SubmodelElement substep : _value) {
        {
          SubmodelElementCollection substepSMC = ((SubmodelElementCollection) substep);
          this.executeHierarchicalProduction(stepId, substepSMC);
        }
      }
    }
  }
  
  private void subStepCallExecution3(final SubmodelElementCollection step) {
    SubmodelElement _aASChild = this.aasHelper.getAASChild(step, "StepId");
    Property stepId = ((Property) _aASChild);
    SubmodelElement _aASChild_1 = this.aasHelper.getAASChild(step, "Substeps");
    SubmodelElementList substeps = ((SubmodelElementList) _aASChild_1);
    if ((substeps != null)) {
      List<SubmodelElement> _value = substeps.getValue();
      for (final SubmodelElement substep : _value) {
        {
          SubmodelElementCollection substepSMC = ((SubmodelElementCollection) substep);
          SubmodelElement _aASChild_2 = this.aasHelper.getAASChild(substepSMC, "StepId");
          String subStepId = ((Property) _aASChild_2).getValue();
          this.childSteps.put(subStepId, stepId.getValue());
          this.sharedResourceSkill3.changeCounter(stepId.getValue(), true);
          List<Referable> interactionElements = new ArrayList<Referable>();
          interactionElements.add(substepSMC);
          this.emitRequestProductionMessage(substepSMC, interactionElements, 3);
        }
      }
    }
  }
  
  protected void generateFinalOutput(final String sender, final String convId, final String stepId) {
    Integer _counter = this.sharedResourceSkill3.getCounter(stepId);
    if ((_counter.intValue() <= 0)) {
      double[] fusedUncertainty = this.estimateUncertaintySkill.fuseSMC(this.sharedResourceSkill3.getUncertainty(stepId));
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      double _get = fusedUncertainty[0];
      double _get_1 = fusedUncertainty[1];
      double _get_2 = fusedUncertainty[2];
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info(
        ((((((("Resulting distributed uncertainty 3: [" + Double.valueOf(_get)) + ", ") + Double.valueOf(_get_1)) + 
          ", ") + Double.valueOf(_get_2)) + "] of step: ") + stepId));
      int _length = this.messageBuffer.get(stepId).length;
      if ((_length > 2)) {
        this.emitExecutionUpdateMessage(fusedUncertainty, 3, sender, convId);
      } else {
        this.emitResourceUpdateMessage(fusedUncertainty, 3, stepId, sender, convId);
      }
    }
  }
  
  protected void emitRequestProductionMessage(final SubmodelElementCollection step, final List<Referable> interactionElements, final int usecase) {
    SubmodelElement _aASChild = this.aasHelper.getAASChild(step, "Resource");
    SubmodelElementCollection resource = ((SubmodelElementCollection) _aASChild);
    SubmodelElement _aASChild_1 = this.aasHelper.getAASChild(resource, "ResourceReference");
    ReferenceElement resourceRef = ((ReferenceElement) _aASChild_1);
    String resourceId = resourceRef.getValue().getKeys().get(0).getValue();
    I40_Message new_message = this.message_skill.create_I40_Message(this.aas_id, resourceId, 
      I40_MessageTypes.REQUIREMENT, UUID.randomUUID().toString(), interactionElements);
    ExternalContextAccess _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER();
    RequestProduction _requestProduction = new RequestProduction();
    final Procedure1<RequestProduction> _function = (RequestProduction it) -> {
      it.message = new_message;
      it.uc = usecase;
    };
    RequestProduction _doubleArrow = ObjectExtensions.<RequestProduction>operator_doubleArrow(_requestProduction, _function);
    _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER.emit(this.comspace, _doubleArrow);
  }
  
  protected void emitResourceUpdateMessage(final double[] result, final int usecase, final String stepId, final String sender, final String convId) {
    List<Referable> interactionElements = new ArrayList<Referable>();
    List<SubmodelElement> dataObjectLogList = new ArrayList<SubmodelElement>();
    dataObjectLogList.add(this.arrayToSMC(result));
    SubmodelElementCollection logEntry = this.generateLogEntry(OffsetDateTime.now().toString(), "Info", "UncertaintyCalculated", dataObjectLogList);
    SubmodelElement _aASChild = this.aasHelper.getAASChild(this.productionLog, "ResourceLogs");
    SubmodelElementCollection resLogs = ((SubmodelElementCollection) _aASChild);
    SubmodelElement _aASChild_1 = this.aasHelper.getAASChild(resLogs, "ResourceLogs");
    SubmodelElementList resLogsList = ((SubmodelElementList) _aASChild_1);
    resLogsList.getValue().add(logEntry);
    this.aasHelper.updateSubmodel(this.productionLog);
    interactionElements.add(logEntry);
    interactionElements.add(this.aasgen.createProperty("StepId", DataTypeDefXsd.STRING, stepId));
    I40_Message new_message = this.message_skill.create_I40_Message(this.aas_id, sender, 
      I40_MessageTypes.INFORMING, convId, interactionElements);
    ExternalContextAccess _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER();
    ResourceUpdate _resourceUpdate = new ResourceUpdate();
    final Procedure1<ResourceUpdate> _function = (ResourceUpdate it) -> {
      it.message = new_message;
      it.uc = usecase;
    };
    ResourceUpdate _doubleArrow = ObjectExtensions.<ResourceUpdate>operator_doubleArrow(_resourceUpdate, _function);
    _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER.emit(this.comspace, _doubleArrow);
  }
  
  protected void emitExecutionUpdateMessage(final double[] result, final int usecase, final String sender, final String convId) {
    Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
    long _nanoTime = System.nanoTime();
    _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info(("Result ready at " + Long.valueOf(_nanoTime)));
    List<SubmodelElement> dataObjectLogList = new ArrayList<SubmodelElement>();
    dataObjectLogList.add(this.arrayToSMC(result));
    SubmodelElementCollection outLogEntry = this.generateLogEntry(OffsetDateTime.now().toString(), "Info", "UncertaintyCalculated", dataObjectLogList);
    SubmodelElement _aASChild = this.aasHelper.getAASChild(this.productionLog, "ResourceLogs");
    SubmodelElementCollection resLogs = ((SubmodelElementCollection) _aASChild);
    SubmodelElement _aASChild_1 = this.aasHelper.getAASChild(resLogs, "ResourceLogs");
    SubmodelElementList resLogsList = ((SubmodelElementList) _aASChild_1);
    resLogsList.getValue().add(outLogEntry);
    this.aasHelper.updateSubmodel(this.productionLog);
    List<Referable> interactionElements = new ArrayList<Referable>();
    interactionElements.add(outLogEntry);
    I40_Message new_message = this.message_skill.create_I40_Message(this.aas_id, sender, 
      I40_MessageTypes.INFORMING, convId, interactionElements);
    ExternalContextAccess _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER();
    ExecutionUpdate _executionUpdate = new ExecutionUpdate();
    final Procedure1<ExecutionUpdate> _function = (ExecutionUpdate it) -> {
      it.message = new_message;
    };
    ExecutionUpdate _doubleArrow = ObjectExtensions.<ExecutionUpdate>operator_doubleArrow(_executionUpdate, _function);
    _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER.emit(this.comspace, _doubleArrow);
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
  
  @Extension
  @ImportedCapacityFeature(Lifecycle.class)
  @SyntheticMember
  private transient AtomicSkillReference $CAPACITY_USE$IO_SARL_CORE_LIFECYCLE;
  
  @SyntheticMember
  @Pure
  private Lifecycle $CAPACITY_USE$IO_SARL_CORE_LIFECYCLE$CALLER() {
    if (this.$CAPACITY_USE$IO_SARL_CORE_LIFECYCLE == null || this.$CAPACITY_USE$IO_SARL_CORE_LIFECYCLE.get() == null) {
      this.$CAPACITY_USE$IO_SARL_CORE_LIFECYCLE = $getSkill(Lifecycle.class);
    }
    return $castSkill(Lifecycle.class, this.$CAPACITY_USE$IO_SARL_CORE_LIFECYCLE);
  }
  
  @Extension
  @ImportedCapacityFeature(DefaultContextInteractions.class)
  @SyntheticMember
  private transient AtomicSkillReference $CAPACITY_USE$IO_SARL_CORE_DEFAULTCONTEXTINTERACTIONS;
  
  @SyntheticMember
  @Pure
  private DefaultContextInteractions $CAPACITY_USE$IO_SARL_CORE_DEFAULTCONTEXTINTERACTIONS$CALLER() {
    if (this.$CAPACITY_USE$IO_SARL_CORE_DEFAULTCONTEXTINTERACTIONS == null || this.$CAPACITY_USE$IO_SARL_CORE_DEFAULTCONTEXTINTERACTIONS.get() == null) {
      this.$CAPACITY_USE$IO_SARL_CORE_DEFAULTCONTEXTINTERACTIONS = $getSkill(DefaultContextInteractions.class);
    }
    return $castSkill(DefaultContextInteractions.class, this.$CAPACITY_USE$IO_SARL_CORE_DEFAULTCONTEXTINTERACTIONS);
  }
  
  @Extension
  @ImportedCapacityFeature(ExternalContextAccess.class)
  @SyntheticMember
  private transient AtomicSkillReference $CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS;
  
  @SyntheticMember
  @Pure
  private ExternalContextAccess $CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER() {
    if (this.$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS == null || this.$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS.get() == null) {
      this.$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS = $getSkill(ExternalContextAccess.class);
    }
    return $castSkill(ExternalContextAccess.class, this.$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS);
  }
  
  @SyntheticMember
  @PerceptGuardEvaluator
  private void $guardEvaluator$Initialize(final Initialize occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
    assert occurrence != null;
    assert ___SARLlocal_runnableCollection != null;
    ___SARLlocal_runnableCollection.add(() -> $behaviorUnit$Initialize$0(occurrence));
  }
  
  @SyntheticMember
  @PerceptGuardEvaluator
  private void $guardEvaluator$RequestProduction(final RequestProduction occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
    assert occurrence != null;
    assert ___SARLlocal_runnableCollection != null;
    if ($behaviorUnitGuard$RequestProduction$3(occurrence, occurrence)) {
      ___SARLlocal_runnableCollection.add(() -> $behaviorUnit$RequestProduction$3(occurrence));
    }
  }
  
  @SyntheticMember
  @PerceptGuardEvaluator
  private void $guardEvaluator$ExecutionRequest(final ExecutionRequest occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
    assert occurrence != null;
    assert ___SARLlocal_runnableCollection != null;
    if ($behaviorUnitGuard$ExecutionRequest$1(occurrence, occurrence)) {
      ___SARLlocal_runnableCollection.add(() -> $behaviorUnit$ExecutionRequest$1(occurrence));
    }
  }
  
  @SyntheticMember
  @PerceptGuardEvaluator
  private void $guardEvaluator$ResourceUpdate(final ResourceUpdate occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
    assert occurrence != null;
    assert ___SARLlocal_runnableCollection != null;
    if ($behaviorUnitGuard$ResourceUpdate$2(occurrence, occurrence)) {
      ___SARLlocal_runnableCollection.add(() -> $behaviorUnit$ResourceUpdate$2(occurrence));
    }
  }
  
  @SyntheticMember
  @Override
  public void $getSupportedEvents(final Set<Class<? extends Event>> toBeFilled) {
    super.$getSupportedEvents(toBeFilled);
    toBeFilled.add(ExecutionRequest.class);
    toBeFilled.add(RequestProduction.class);
    toBeFilled.add(ResourceUpdate.class);
    toBeFilled.add(Initialize.class);
  }
  
  @SyntheticMember
  @Override
  public boolean $isSupportedEvent(final Class<? extends Event> event) {
    if (ExecutionRequest.class.isAssignableFrom(event)) {
      return true;
    }
    if (RequestProduction.class.isAssignableFrom(event)) {
      return true;
    }
    if (ResourceUpdate.class.isAssignableFrom(event)) {
      return true;
    }
    if (Initialize.class.isAssignableFrom(event)) {
      return true;
    }
    return super.$isSupportedEvent(event);
  }
  
  @SyntheticMember
  @Override
  public void $evaluateBehaviorGuards(final Object event, final Collection<Runnable> callbacks) {
    super.$evaluateBehaviorGuards(event, callbacks);
    if (event instanceof ExecutionRequest) {
      final ExecutionRequest occurrence = (ExecutionRequest) event;
      $guardEvaluator$ExecutionRequest(occurrence, callbacks);
    }
    if (event instanceof RequestProduction) {
      final RequestProduction occurrence = (RequestProduction) event;
      $guardEvaluator$RequestProduction(occurrence, callbacks);
    }
    if (event instanceof ResourceUpdate) {
      final ResourceUpdate occurrence = (ResourceUpdate) event;
      $guardEvaluator$ResourceUpdate(occurrence, callbacks);
    }
    if (event instanceof Initialize) {
      final Initialize occurrence = (Initialize) event;
      $guardEvaluator$Initialize(occurrence, callbacks);
    }
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
  
  @SyntheticMember
  public ResourceAgent(final UUID parentID, final UUID agentID) {
    super(parentID, agentID);
  }
}

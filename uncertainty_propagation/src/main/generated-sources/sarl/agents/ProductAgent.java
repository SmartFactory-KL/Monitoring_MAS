package agents;

import agents.AbstractAgent;
import com.google.common.base.Objects;
import events.ExecutionRequest;
import events.ExecutionUpdate;
import i40_messages.I40_Message;
import i40_messages.I40_MessageTypes;
import io.sarl.core.ExternalContextAccess;
import io.sarl.core.Initialize;
import io.sarl.core.Logging;
import io.sarl.lang.annotation.ImportedCapacityFeature;
import io.sarl.lang.annotation.PerceptGuardEvaluator;
import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.annotation.SyntheticMember;
import io.sarl.lang.core.AtomicSkillReference;
import io.sarl.lang.core.Event;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * @author Alexis Bernhard
 */
@SarlSpecification("0.12")
@SarlElementType(19)
@SuppressWarnings("all")
public class ProductAgent extends AbstractAgent {
  private Submodel productionPlan;
  
  private Submodel productionLog;
  
  protected void executeSteps(final List<SubmodelElement> steps) {
    int _size = steps.size();
    if ((_size > 0)) {
      SubmodelElement _get = steps.get(0);
      SubmodelElementCollection step = ((SubmodelElementCollection) _get);
      SubmodelElement _aASChild = this.aasHelper.getAASChild(step, "Resource");
      SubmodelElementCollection resourceSMC = ((SubmodelElementCollection) _aASChild);
      SubmodelElement _aASChild_1 = this.aasHelper.getAASChild(resourceSMC, "ResourceReference");
      ReferenceElement resourceReference = ((ReferenceElement) _aASChild_1);
      String resourceId = resourceReference.getValue().getKeys().get(0).getValue().toString();
      List<Referable> interactionElements = new ArrayList<Referable>();
      interactionElements.addAll(steps);
      I40_Message new_message = this.message_skill.create_I40_Message(this.aas_id, resourceId, 
        I40_MessageTypes.REQUIREMENT, UUID.randomUUID().toString(), interactionElements);
      ExternalContextAccess _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER();
      ExecutionRequest _executionRequest = new ExecutionRequest();
      final Procedure1<ExecutionRequest> _function = (ExecutionRequest it) -> {
        it.message = new_message;
      };
      ExecutionRequest _doubleArrow = ObjectExtensions.<ExecutionRequest>operator_doubleArrow(_executionRequest, _function);
      _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER.emit(this.comspace, _doubleArrow);
    }
  }
  
  private void $behaviorUnit$Initialize$0(final Initialize occurrence) {
    boolean _init = this.init(occurrence.parameters, "Product");
    if (_init) {
      this.productionPlan = this.aasHelper.getSubmodelByIdShort(this.aas, "ProductionPlan");
      this.productionLog = this.aasHelper.getSubmodelByIdShort(this.aas, "ProductionLog");
      this.resetAAS();
      SubmodelElement _aASChild = this.aasHelper.getAASChild(this.productionPlan, "Steps");
      SubmodelElementList steps = ((SubmodelElementList) _aASChild);
      ArrayList<SubmodelElementCollection> heterarchicalSteps = new ArrayList<SubmodelElementCollection>();
      List<SubmodelElement> _value = steps.getValue();
      for (final SubmodelElement step : _value) {
        heterarchicalSteps.addAll(this.transformHierachicalStep(((SubmodelElementCollection) step)));
      }
      steps.getValue().addAll(heterarchicalSteps);
      this.executeSteps(steps.getValue());
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      String _idShort = this.aas.getIdShort();
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info((("Initialization process of product AAS " + _idShort) + " completed."));
    }
  }
  
  private void $behaviorUnit$ExecutionUpdate$1(final ExecutionUpdate occurrence) {
    Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
    String _idShort = occurrence.message.getInteractionElements().get(0).getIdShort();
    _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info(("Execution updated with SMC idshort: " + _idShort));
    Referable _get = occurrence.message.getInteractionElements().get(0);
    SubmodelElementCollection logEntry = ((SubmodelElementCollection) _get);
    SubmodelElement _aASChild = this.aasHelper.getAASChild(this.productionLog, "ProductLogs");
    SubmodelElementList productLogEntries = ((SubmodelElementList) _aASChild);
    productLogEntries.getValue().add(logEntry);
    this.aasHelper.updateSubmodel(this.productionLog);
    SubmodelElement _aASChild_1 = this.aasHelper.getAASChild(logEntry, "DataObjects");
    SubmodelElementList dataobjects = ((SubmodelElementList) _aASChild_1);
    List<SubmodelElement> _value = dataobjects.getValue();
    for (final SubmodelElement dataObject : _value) {
      {
        SubmodelElementCollection dataObjectSMC = ((SubmodelElementCollection) dataObject);
        List<SubmodelElement> _value_1 = dataObjectSMC.getValue();
        for (final SubmodelElement elem : _value_1) {
          Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
          String _idShort_1 = elem.getIdShort();
          _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1.info(("Element received: " + _idShort_1));
        }
      }
    }
  }
  
  @SyntheticMember
  @Pure
  private boolean $behaviorUnitGuard$ExecutionUpdate$1(final ExecutionUpdate it, final ExecutionUpdate occurrence) {
    String _receiver = occurrence.message.getFrame().getReceiver();
    boolean _equals = Objects.equal(_receiver, this.aas_id);
    return _equals;
  }
  
  protected void resetAAS() {
    Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
    String _idShort = this.aas.getIdShort();
    _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info(("Reset Production plan of AAS " + _idShort));
    SubmodelElement _aASChild = this.aasHelper.getAASChild(this.productionLog, "Status");
    Property totalState = ((Property) _aASChild);
    totalState.setValue("planned");
    SubmodelElement _aASChild_1 = this.aasHelper.getAASChild(this.productionLog, "ProcessLogs");
    SubmodelElementList processLogs = ((SubmodelElementList) _aASChild_1);
    List<SubmodelElement> _value = processLogs.getValue();
    for (final SubmodelElement step : _value) {
      {
        SubmodelElementCollection stepSMC = ((SubmodelElementCollection) step);
        SubmodelElement _aASChild_2 = this.aasHelper.getAASChild(stepSMC, "Status");
        Property state = ((Property) _aASChild_2);
        boolean _equals = state.equals("planned");
        if ((!_equals)) {
          state.setValue("planned");
        }
        SubmodelElement _aASChild_3 = this.aasHelper.getAASChild(stepSMC, "ActionLogs");
        SubmodelElementList actions = ((SubmodelElementList) _aASChild_3);
        List<SubmodelElement> _value_1 = actions.getValue();
        for (final SubmodelElement action : _value_1) {
          {
            SubmodelElementCollection actionSMC = ((SubmodelElementCollection) action);
            SubmodelElement _aASChild_4 = this.aasHelper.getAASChild(actionSMC, "Status");
            Property actionState = ((Property) _aASChild_4);
            boolean _equals_1 = actionState.equals("planned");
            if ((!_equals_1)) {
              actionState.setValue("planned");
            }
          }
        }
      }
    }
    SubmodelElement _aASChild_2 = this.aasHelper.getAASChild(this.productionLog, "ProductLogs");
    SubmodelElementList productLogEntries = ((SubmodelElementList) _aASChild_2);
    productLogEntries.getValue().clear();
    this.aasHelper.updateSubmodel(this.productionLog);
  }
  
  protected List<SubmodelElementCollection> transformHierachicalStep(final SubmodelElementCollection step) {
    List<SubmodelElementCollection> resultSteps = new ArrayList<SubmodelElementCollection>();
    SubmodelElement _aASChild = this.aasHelper.getAASChild(step, "Substeps");
    SubmodelElementList substeps = ((SubmodelElementList) _aASChild);
    if ((substeps != null)) {
      List<SubmodelElement> _value = substeps.getValue();
      for (final SubmodelElement substep : _value) {
        {
          SubmodelElementCollection substepSMC = ((SubmodelElementCollection) substep);
          SubmodelElement _aASChild_1 = this.aasHelper.getAASChild(substepSMC, "StepId");
          String subStepId = ((Property) _aASChild_1).getValue();
          this.sharedResourceSkill.changeCounter(subStepId, true);
          resultSteps.addAll(this.transformHierachicalStep(substepSMC));
        }
      }
    }
    SubmodelElement _aASChild_1 = this.aasHelper.getAASChild(step, "Actions");
    SubmodelElementList actions = ((SubmodelElementList) _aASChild_1);
    if ((actions != null)) {
      resultSteps.add(step);
    }
    return resultSteps;
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
  
  /**
   * TBD -> raus?
   * on ExecutionUpdate [occurrence.actionID.toLowerCase.equals(
   * (cur_action.getAASElement("ActionTitle") as Property).value.toLowerCase) && occurrence.resourceID ==
   * (cur_action.getAASElement("SkillReference") as ReferenceElement).value.keys.get(0).value.toString()] {
   * info("Test Action1: " + occurrence.actionID + ", Action2: " +
   * (cur_action.getAASElement("ActionTitle") as Property).value)
   * var actionState : ActionState = ActionState.valueOf(
   * (cur_action.getAASElement("Status") as Property).value.toUpperCase);
   * switch (occurrence.actionState) {
   * case "STOPPED": actionState = actionState.suspend()
   * case "EXECUTE": actionState = actionState.start()
   * case "IDLE": actionState = actionState.reset()
   * case "HELD": actionState = actionState.suspend()
   * case "SUSPENDED": actionState = actionState.suspend()
   * case "COMPLETE": actionState = actionState.complete()
   * case "ABORTED": actionState = actionState.abort()
   * }
   * info("Update action " + occurrence.actionID + ", current state: " + occurrence.actionState + ", new state: " +
   * actionState)
   * (cur_action.getAASElement("Status") as Property).value = actionState.toString.toLowerCase
   * aasHelper.updateSubmodel(productionPlan)
   * }
   */
  @SyntheticMember
  @PerceptGuardEvaluator
  private void $guardEvaluator$ExecutionUpdate(final ExecutionUpdate occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
    assert occurrence != null;
    assert ___SARLlocal_runnableCollection != null;
    if ($behaviorUnitGuard$ExecutionUpdate$1(occurrence, occurrence)) {
      ___SARLlocal_runnableCollection.add(() -> $behaviorUnit$ExecutionUpdate$1(occurrence));
    }
  }
  
  @SyntheticMember
  @Override
  public void $getSupportedEvents(final Set<Class<? extends Event>> toBeFilled) {
    super.$getSupportedEvents(toBeFilled);
    toBeFilled.add(ExecutionUpdate.class);
    toBeFilled.add(Initialize.class);
  }
  
  @SyntheticMember
  @Override
  public boolean $isSupportedEvent(final Class<? extends Event> event) {
    if (ExecutionUpdate.class.isAssignableFrom(event)) {
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
    if (event instanceof ExecutionUpdate) {
      final ExecutionUpdate occurrence = (ExecutionUpdate) event;
      $guardEvaluator$ExecutionUpdate(occurrence, callbacks);
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
  public ProductAgent(final UUID parentID, final UUID agentID) {
    super(parentID, agentID);
  }
}

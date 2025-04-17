package agents;

import agents.AbstractAgent;
import events.ExecutionCompleted;
import events.RequestProduction;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
 * @author Ali Karnoub
 */
@SarlSpecification("0.12")
@SarlElementType(19)
@SuppressWarnings("all")
public class ProductAgent extends AbstractAgent {
  private Submodel productionPlan;
  
  private Submodel productionLog;
  
  protected SubmodelElementCollection nextStep(final String lastStepId) {
    List<SubmodelElement> steps = this.productionPlan.getSubmodelElements().stream().collect(Collectors.<SubmodelElement>toList());
    for (final SubmodelElement step : steps) {
      {
        SubmodelElementList stepList = ((SubmodelElementList) step);
        List<SubmodelElement> _value = stepList.getValue();
        for (final SubmodelElement step2 : _value) {
          {
            SubmodelElementCollection stepSMC = ((SubmodelElementCollection) step2);
            SubmodelElement _aASElement = this.getAASElement(stepSMC, "StepId");
            Property stepId = ((Property) _aASElement);
            boolean _equals = stepId.getValue().equals(lastStepId);
            if (_equals) {
              boolean _hasNext = steps.listIterator().hasNext();
              if (_hasNext) {
                SubmodelElement _next = steps.listIterator().next();
                stepSMC = ((SubmodelElementCollection) _next);
                return stepSMC;
              } else {
                Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
                _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info("Production finished");
                break;
              }
            } else {
              boolean _equals_1 = lastStepId.equals("startStepId");
              if (_equals_1) {
                return stepSMC;
              }
            }
          }
        }
      }
    }
    return null;
  }
  
  protected void executeStep(final SubmodelElementCollection step) {
    SubmodelElement _aASElement = this.getAASElement(step, "Resource");
    SubmodelElementCollection resourceSMC = ((SubmodelElementCollection) _aASElement);
    SubmodelElement _aASElement_1 = this.getAASElement(resourceSMC, "ResourceReference");
    ReferenceElement resourceReference = ((ReferenceElement) _aASElement_1);
    String resourceId = resourceReference.getValue().getKeys().get(0).getValue().toString();
    List<Referable> interactionElements = new ArrayList<Referable>();
    interactionElements.add(step);
    I40_Message new_message = this.message_skill.create_I40_Message(this.aas_id, resourceId, 
      I40_MessageTypes.REQUIREMENT, UUID.randomUUID().toString(), interactionElements);
    ExternalContextAccess _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER();
    RequestProduction _requestProduction = new RequestProduction();
    final Procedure1<RequestProduction> _function = (RequestProduction it) -> {
      it.message = new_message;
    };
    RequestProduction _doubleArrow = ObjectExtensions.<RequestProduction>operator_doubleArrow(_requestProduction, _function);
    _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER.emit(this.comspace, _doubleArrow);
  }
  
  private void $behaviorUnit$Initialize$0(final Initialize occurrence) {
    boolean _init = this.init(occurrence.parameters, "Product");
    if (_init) {
      this.productionPlan = this.aasHelper.getSubmodelByIdShort(this.aas, "ProductionPlan");
      this.productionLog = this.aasHelper.getSubmodelByIdShort(this.aas, "ProductionLog");
      this.resetAAS();
      this.executeStep(this.nextStep("startStepId"));
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      String _idShort = this.aas.getIdShort();
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info((("Initialization process of product AAS " + _idShort) + " completed."));
    }
  }
  
  private void $behaviorUnit$ExecutionCompleted$1(final ExecutionCompleted occurrence) {
    this.executeStep(this.nextStep(occurrence.actionID));
  }
  
  protected void resetAAS() {
    Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
    String _idShort = this.aas.getIdShort();
    _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info(("Reset Production plan of AAS " + _idShort));
    final Predicate<SubmodelElement> _function = (SubmodelElement sme) -> {
      return sme.getIdShort().contains("Status");
    };
    SubmodelElement _get = this.productionLog.getSubmodelElements().stream().filter(_function).findFirst().get();
    Property totalState = ((Property) _get);
    totalState.setValue("planned");
    final Predicate<SubmodelElement> _function_1 = (SubmodelElement sme) -> {
      return sme.getIdShort().contains("ProcessLogs");
    };
    SubmodelElement _get_1 = this.productionLog.getSubmodelElements().stream().filter(_function_1).findFirst().get();
    SubmodelElementList processLogs = ((SubmodelElementList) _get_1);
    List<SubmodelElement> _value = processLogs.getValue();
    for (final SubmodelElement step : _value) {
      {
        SubmodelElementCollection stepSMC = ((SubmodelElementCollection) step);
        SubmodelElement _aASElement = this.getAASElement(stepSMC, "Status");
        Property state = ((Property) _aASElement);
        boolean _equals = state.equals("planned");
        if ((!_equals)) {
          state.setValue("planned");
          this.aasHelper.updateSubmodel(this.productionPlan);
          SubmodelElement _aASElement_1 = this.getAASElement(stepSMC, "ActionLogs");
          SubmodelElementList actions = ((SubmodelElementList) _aASElement_1);
          List<SubmodelElement> _value_1 = actions.getValue();
          for (final SubmodelElement action : _value_1) {
            {
              SubmodelElementCollection actionSMC = ((SubmodelElementCollection) action);
              SubmodelElement _aASElement_2 = this.getAASElement(actionSMC, "Status");
              Property actionState = ((Property) _aASElement_2);
              boolean _equals_1 = actionState.equals("planned");
              if ((!_equals_1)) {
                actionState.setValue("planned");
                this.aasHelper.updateSubmodel(this.productionPlan);
              }
            }
          }
        }
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
  private void $guardEvaluator$ExecutionCompleted(final ExecutionCompleted occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
    assert occurrence != null;
    assert ___SARLlocal_runnableCollection != null;
    ___SARLlocal_runnableCollection.add(() -> $behaviorUnit$ExecutionCompleted$1(occurrence));
  }
  
  @SyntheticMember
  @Override
  public void $getSupportedEvents(final Set<Class<? extends Event>> toBeFilled) {
    super.$getSupportedEvents(toBeFilled);
    toBeFilled.add(ExecutionCompleted.class);
    toBeFilled.add(Initialize.class);
  }
  
  @SyntheticMember
  @Override
  public boolean $isSupportedEvent(final Class<? extends Event> event) {
    if (ExecutionCompleted.class.isAssignableFrom(event)) {
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
    if (event instanceof ExecutionCompleted) {
      final ExecutionCompleted occurrence = (ExecutionCompleted) event;
      $guardEvaluator$ExecutionCompleted(occurrence, callbacks);
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

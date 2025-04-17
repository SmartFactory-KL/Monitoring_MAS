package agents;

import agents.AbstractAgent;
import agents.FaultDetectionMode;
import capacities.FaultDetectionCapacity;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import events.FaultLogBroadcast;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.EntityType;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import skills.FaultDetectionSkill;

@SuppressWarnings("discouraged_occurrence_readonly_use")
@SarlSpecification("0.12")
@SarlElementType(19)
public class ResourceAgent extends AbstractAgent {
  private FaultDetectionSkill detectFaultSkill;
  
  private FaultDetectionMode detectionMode = FaultDetectionMode.HIERARCHICAL;
  
  private Submodel skillSM;
  
  private AasGenerator aasgen;
  
  private Submodel bom;
  
  private Map<String, String> skills;
  
  private boolean isCentralAgent = false;
  
  private final String CentralAAS = "https://smartfactory.de/shells/99c880df-d65a-4180-8b19-aa73e4a826d8";
  
  private Map<String, List<SubmodelElementCollection>> faultLogBuffer = new HashMap<String, List<SubmodelElementCollection>>();
  
  protected void spawnSubAgents() {
    this.bom = this.aasHelper.getSubmodelByIdShort(this.aas, "BillOfMaterial");
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
            _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_4.info(("GlobalAssetId:" + globalAssetId));
            Lifecycle _$CAPACITY_USE$IO_SARL_CORE_LIFECYCLE$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LIFECYCLE$CALLER();
            DefaultContextInteractions _$CAPACITY_USE$IO_SARL_CORE_DEFAULTCONTEXTINTERACTIONS$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_DEFAULTCONTEXTINTERACTIONS$CALLER();
            _$CAPACITY_USE$IO_SARL_CORE_LIFECYCLE$CALLER.spawnInContext(ResourceAgent.class, _$CAPACITY_USE$IO_SARL_CORE_DEFAULTCONTEXTINTERACTIONS$CALLER.getDefaultContext(), globalAssetId);
          }
        }
      }
    }
  }
  
  private void $behaviorUnit$Initialize$0(final Initialize occurrence) {
    boolean _init = this.init(occurrence.parameters, "Resource");
    if (_init) {
      boolean _equals = Objects.equal(this.aas_id, this.CentralAAS);
      this.isCentralAgent = _equals;
      this.spawnSubAgents();
      AasGenerator _aasGenerator = new AasGenerator();
      this.aasgen = _aasGenerator;
      FaultDetectionSkill _faultDetectionSkill = new FaultDetectionSkill();
      this.detectFaultSkill = _faultDetectionSkill;
      this.<FaultDetectionSkill>setSkill(this.detectFaultSkill, FaultDetectionCapacity.class);
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info(("Initializing CC Agent completed. AAS_id: " + this.aas_id));
      this.resetAAS();
    }
  }
  
  private List<SubmodelElementCollection> pendingSubsteps = new ArrayList<SubmodelElementCollection>();
  
  private String currentStepConversationId = "";
  
  private void $behaviorUnit$RequestProduction$1(final RequestProduction occurrence) {
    if (this.isCentralAgent) {
      Referable _get = occurrence.message.getInteractionElements().get(0);
      final SubmodelElementCollection step = ((SubmodelElementCollection) _get);
      this.currentStepConversationId = occurrence.message.getFrame().getConversationID();
      final Function1<SubmodelElement, Boolean> _function = (SubmodelElement it) -> {
        return Boolean.valueOf(((it instanceof SubmodelElementList) && Objects.equal(it.getIdShort(), "Substeps")));
      };
      SubmodelElement _findFirst = IterableExtensions.<SubmodelElement>findFirst(step.getValue(), _function);
      final SubmodelElementList substeps = ((SubmodelElementList) _findFirst);
      if ((substeps != null)) {
        List<SubmodelElement> _value = substeps.getValue();
        for (final SubmodelElement substep : _value) {
          if ((substep instanceof SubmodelElementCollection)) {
            final List<SubmodelElementCollection> leafSubsteps = this.extractExecutableSubsteps(((SubmodelElementCollection)substep));
            this.pendingSubsteps.addAll(leafSubsteps);
          }
        }
      }
      this.startNextSubstep();
    } else {
      Referable _get_1 = occurrence.message.getInteractionElements().get(0);
      final SubmodelElementCollection substep_1 = ((SubmodelElementCollection) _get_1);
      String stepId = "unknown";
      List<SubmodelElement> _value_1 = substep_1.getValue();
      for (final SubmodelElement elem : _value_1) {
        if (((elem instanceof Property) && Objects.equal(elem.getIdShort(), "StepId"))) {
          stepId = ((Property) elem).getValue();
        }
      }
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info(("Resource agent executes substep: " + stepId));
      Submodel _submodelByIdShort = this.aasHelper.getSubmodelByIdShort(this.aas, "ProductionLog");
      final Submodel productionLog = _submodelByIdShort;
      if ((productionLog != null)) {
        final List<Referable> detectedFaultLogs = this.detectFaultSkill.detectFaults(productionLog, this.aas, substep_1);
        final Function1<Map.Entry<String, List<SubmodelElementCollection>>, Boolean> _function_1 = (Map.Entry<String, List<SubmodelElementCollection>> it) -> {
          String _key = it.getKey();
          return Boolean.valueOf((!Objects.equal(_key, this.aas_id)));
        };
        final Function1<Map.Entry<String, List<SubmodelElementCollection>>, List<SubmodelElementCollection>> _function_2 = (Map.Entry<String, List<SubmodelElementCollection>> it) -> {
          return it.getValue();
        };
        final Function1<SubmodelElementCollection, Referable> _function_3 = (SubmodelElementCollection it) -> {
          return it;
        };
        final List<Referable> allPreviousLogs = IterableExtensions.<Referable>toList(IterableExtensions.<SubmodelElementCollection, Referable>map(IterableExtensions.<Map.Entry<String, List<SubmodelElementCollection>>, SubmodelElementCollection>flatMap(IterableExtensions.<Map.Entry<String, List<SubmodelElementCollection>>>filter(this.faultLogBuffer.entrySet(), _function_1), _function_2), _function_3));
        if ((this.detectFaultSkill.isAborted == true)) {
          Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
          _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1.warning("FATAL: Production has been stopped");
        }
        if (((!detectedFaultLogs.isEmpty()) && Objects.equal(this.detectionMode, FaultDetectionMode.HETERARCHICAL))) {
          final Function1<Referable, Boolean> _function_4 = (Referable it) -> {
            return Boolean.valueOf((it instanceof SubmodelElementCollection));
          };
          final Function1<Referable, Referable> _function_5 = (Referable it) -> {
            return it;
          };
          final I40_Message faultLogMsg = this.message_skill.create_I40_Message(
            this.aas_id, 
            "", 
            I40_MessageTypes.INFORMING, 
            UUID.randomUUID().toString(), 
            IterableExtensions.<Referable>toList(IterableExtensions.<Referable, Referable>map(IterableExtensions.<Referable>filter(detectedFaultLogs, _function_4), _function_5)));
          ExternalContextAccess _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER();
          FaultLogBroadcast _faultLogBroadcast = new FaultLogBroadcast();
          final Procedure1<FaultLogBroadcast> _function_6 = (FaultLogBroadcast it) -> {
            it.message = faultLogMsg;
          };
          FaultLogBroadcast _doubleArrow = ObjectExtensions.<FaultLogBroadcast>operator_doubleArrow(_faultLogBroadcast, _function_6);
          _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER.emit(this.comspace, _doubleArrow);
          Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_2 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
          int _size = allPreviousLogs.size();
          String _plus = (Integer.valueOf(_size) + " received FaultLogs, ");
          int _size_1 = detectedFaultLogs.size();
          _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_2.info(((_plus + Integer.valueOf(_size_1)) + " own FaultLogs"));
          this.detectFaultSkill.classifyFaults(detectedFaultLogs, allPreviousLogs);
        } else {
          boolean _equals = Objects.equal(this.detectionMode, FaultDetectionMode.HIERARCHICAL);
          if (_equals) {
            final Function1<Referable, Boolean> _function_7 = (Referable it) -> {
              return Boolean.valueOf((it instanceof SubmodelElementCollection));
            };
            final Function1<Referable, Referable> _function_8 = (Referable it) -> {
              return it;
            };
            final I40_Message faultLogMsg_1 = this.message_skill.create_I40_Message(
              this.aas_id, 
              this.CentralAAS, 
              I40_MessageTypes.INFORMING, 
              UUID.randomUUID().toString(), 
              IterableExtensions.<Referable>toList(IterableExtensions.<Referable, Referable>map(IterableExtensions.<Referable>filter(detectedFaultLogs, _function_7), _function_8)));
            ExternalContextAccess _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER_1 = this.$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER();
            FaultLogBroadcast _faultLogBroadcast_1 = new FaultLogBroadcast();
            final Procedure1<FaultLogBroadcast> _function_9 = (FaultLogBroadcast it) -> {
              it.message = faultLogMsg_1;
            };
            FaultLogBroadcast _doubleArrow_1 = ObjectExtensions.<FaultLogBroadcast>operator_doubleArrow(_faultLogBroadcast_1, _function_9);
            _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER_1.emit(this.comspace, _doubleArrow_1);
          }
        }
      }
      if ((!this.detectFaultSkill.isAborted)) {
        final I40_Message updateMsg = this.message_skill.create_I40_Message(
          this.aas_id, 
          occurrence.message.getFrame().getSender(), 
          I40_MessageTypes.INFORMING, 
          occurrence.message.getFrame().getConversationID(), 
          List.<Referable>of(substep_1));
        ExternalContextAccess _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER_2 = this.$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER();
        ResourceUpdate _resourceUpdate = new ResourceUpdate();
        final Procedure1<ResourceUpdate> _function_10 = (ResourceUpdate it) -> {
          it.message = updateMsg;
        };
        ResourceUpdate _doubleArrow_2 = ObjectExtensions.<ResourceUpdate>operator_doubleArrow(_resourceUpdate, _function_10);
        _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER_2.emit(this.comspace, _doubleArrow_2);
        Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_3 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
        _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_3.info(("Substep completed: " + stepId));
      }
    }
  }
  
  @SyntheticMember
  @Pure
  private boolean $behaviorUnitGuard$RequestProduction$1(final RequestProduction it, final RequestProduction occurrence) {
    String _receiver = occurrence.message.getFrame().getReceiver();
    boolean _equals = Objects.equal(_receiver, this.aas_id);
    return _equals;
  }
  
  private void $behaviorUnit$ResourceUpdate$2(final ResourceUpdate occurrence) {
    if (this.isCentralAgent) {
      this.startNextSubstep();
    }
  }
  
  @SyntheticMember
  @Pure
  private boolean $behaviorUnitGuard$ResourceUpdate$2(final ResourceUpdate it, final ResourceUpdate occurrence) {
    String _receiver = occurrence.message.getFrame().getReceiver();
    boolean _equals = Objects.equal(_receiver, this.aas_id);
    return _equals;
  }
  
  private void $behaviorUnit$FaultLogBroadcast$3(final FaultLogBroadcast occurrence) {
    final String sender = occurrence.message.getFrame().getSender();
    final Function1<Referable, Boolean> _function = (Referable it) -> {
      return Boolean.valueOf((it instanceof SubmodelElementCollection));
    };
    final Function1<Referable, SubmodelElementCollection> _function_1 = (Referable it) -> {
      return ((SubmodelElementCollection) it);
    };
    final List<SubmodelElementCollection> logs = IterableExtensions.<SubmodelElementCollection>toList(IterableExtensions.<Referable, SubmodelElementCollection>map(IterableExtensions.<Referable>filter(occurrence.message.getInteractionElements(), _function), _function_1));
    this.faultLogBuffer.put(sender, logs);
    final Function1<Map.Entry<String, List<SubmodelElementCollection>>, String> _function_2 = (Map.Entry<String, List<SubmodelElementCollection>> it) -> {
      int _size = it.getValue().size();
      String _plus = (Integer.valueOf(_size) + " FaultLogs from ");
      String _key = it.getKey();
      return (_plus + _key);
    };
    final String summary = IterableExtensions.join(IterableExtensions.<Map.Entry<String, List<SubmodelElementCollection>>, String>map(this.faultLogBuffer.entrySet(), _function_2), ", ");
    Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
    _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info(("Received FaultLogs:  " + summary));
    final Function1<SubmodelElementCollection, Referable> _function_3 = (SubmodelElementCollection it) -> {
      return it;
    };
    final List<Referable> allLogs = IterableExtensions.<Referable>toList(IterableExtensions.<SubmodelElementCollection, Referable>map(Iterables.<SubmodelElementCollection>concat(this.faultLogBuffer.values()), _function_3));
    Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
    int _size = allLogs.size();
    _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1.info((("Start classification with  " + Integer.valueOf(_size)) + " Logs:"));
    ArrayList<Referable> _arrayList = new ArrayList<Referable>();
    this.detectFaultSkill.classifyFaults(_arrayList, allLogs);
  }
  
  @SyntheticMember
  @Pure
  private boolean $behaviorUnitGuard$FaultLogBroadcast$3(final FaultLogBroadcast it, final FaultLogBroadcast occurrence) {
    String _receiver = occurrence.message.getFrame().getReceiver();
    boolean _equals = Objects.equal(_receiver, this.aas_id);
    return _equals;
  }
  
  private void $behaviorUnit$FaultLogBroadcast$4(final FaultLogBroadcast occurrence) {
    final I40_Message msg = occurrence.message;
    final String sender = msg.getFrame().getSender();
    final String receiver = msg.getFrame().getReceiver();
    if (((!Objects.equal(sender, this.aas_id)) && (Objects.equal(receiver, "") || Objects.equal(receiver, this.aas_id)))) {
      final Function1<Referable, Boolean> _function = (Referable it) -> {
        return Boolean.valueOf((it instanceof SubmodelElementCollection));
      };
      final Function1<Referable, SubmodelElementCollection> _function_1 = (Referable it) -> {
        return ((SubmodelElementCollection) it);
      };
      final List<SubmodelElementCollection> logs = IterableExtensions.<SubmodelElementCollection>toList(IterableExtensions.<Referable, SubmodelElementCollection>map(IterableExtensions.<Referable>filter(msg.getInteractionElements(), _function), _function_1));
      this.faultLogBuffer.put(sender, logs);
    }
  }
  
  private List<SubmodelElementCollection> extractExecutableSubsteps(final SubmodelElementCollection substep) {
    final ArrayList<SubmodelElementCollection> result = new ArrayList<SubmodelElementCollection>();
    final Function1<SubmodelElement, Boolean> _function = (SubmodelElement it) -> {
      return Boolean.valueOf(((it instanceof SubmodelElementList) && Objects.equal(it.getIdShort(), "Substeps")));
    };
    SubmodelElement _findFirst = IterableExtensions.<SubmodelElement>findFirst(substep.getValue(), _function);
    final SubmodelElementList nestedSubsteps = ((SubmodelElementList) _findFirst);
    if (((nestedSubsteps != null) && (!nestedSubsteps.getValue().isEmpty()))) {
      List<SubmodelElement> _value = nestedSubsteps.getValue();
      for (final SubmodelElement nested : _value) {
        if ((nested instanceof SubmodelElementCollection)) {
          result.addAll(this.extractExecutableSubsteps(((SubmodelElementCollection)nested)));
        }
      }
    } else {
      result.add(substep);
    }
    return result;
  }
  
  protected void startNextSubstep() {
    boolean _isEmpty = this.pendingSubsteps.isEmpty();
    if ((!_isEmpty)) {
      final SubmodelElementCollection next = this.pendingSubsteps.remove(0);
      final Function1<SubmodelElement, Boolean> _function = (SubmodelElement it) -> {
        return Boolean.valueOf(((it instanceof SubmodelElementCollection) && Objects.equal(it.getIdShort(), "Resource")));
      };
      SubmodelElement _findFirst = IterableExtensions.<SubmodelElement>findFirst(next.getValue(), _function);
      final SubmodelElementCollection resource = ((SubmodelElementCollection) _findFirst);
      SubmodelElement _aASElement = null;
      if (resource!=null) {
        _aASElement=this.getAASElement(resource, "ResourceReference");
      }
      final ReferenceElement resourceRef = ((ReferenceElement) _aASElement);
      Reference _value = null;
      if (resourceRef!=null) {
        _value=resourceRef.getValue();
      }
      List<Key> _keys = null;
      if (_value!=null) {
        _keys=_value.getKeys();
      }
      Key _get = null;
      if (_keys!=null) {
        _get=_keys.get(0);
      }
      String _value_1 = null;
      if (_get!=null) {
        _value_1=_get.getValue();
      }
      final String resourceAASId = _value_1;
      if ((resourceAASId != null)) {
        final I40_Message msg = this.message_skill.create_I40_Message(
          this.aas_id, resourceAASId, 
          I40_MessageTypes.REQUIREMENT, 
          this.currentStepConversationId, 
          List.<Referable>of(next));
        ExternalContextAccess _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER();
        RequestProduction _requestProduction = new RequestProduction();
        final Procedure1<RequestProduction> _function_1 = (RequestProduction it) -> {
          it.message = msg;
        };
        RequestProduction _doubleArrow = ObjectExtensions.<RequestProduction>operator_doubleArrow(_requestProduction, _function_1);
        _$CAPACITY_USE$IO_SARL_CORE_EXTERNALCONTEXTACCESS$CALLER.emit(this.comspace, _doubleArrow);
        Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
        SubmodelElement _aASElement_1 = this.getAASElement(next, "StepId");
        String _value_2 = null;
        if (((Property) _aASElement_1)!=null) {
          _value_2=((Property) _aASElement_1).getValue();
        }
        _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.info(("Initiate substep with ID: " + _value_2));
      }
    } else {
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1.info("All substeps completed.");
    }
  }
  
  private void executeStep(final SubmodelElementCollection step) {
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
  
  protected void resetAAS() {
    Submodel _submodelByIdShort = this.aasHelper.getSubmodelByIdShort(this.aas, "ProductionLog");
    final Submodel productionLog = _submodelByIdShort;
    if ((productionLog == null)) {
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.warning("ProductionLog submodel not found.");
      return;
    }
    final Function1<SubmodelElement, Boolean> _function = (SubmodelElement it) -> {
      return Boolean.valueOf(((it instanceof SubmodelElementCollection) && Objects.equal(it.getIdShort(), "ResourceLogs")));
    };
    SubmodelElement _findFirst = IterableExtensions.<SubmodelElement>findFirst(productionLog.getSubmodelElements(), _function);
    final SubmodelElementCollection resourceLogsWrapper = ((SubmodelElementCollection) _findFirst);
    if ((resourceLogsWrapper == null)) {
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_1.warning("ResourceLogs SMC not found in ProductionLog.");
      return;
    }
    SubmodelElement _aASElement = this.getAASElement(resourceLogsWrapper, "ResourceLogs");
    final SubmodelElementList resourceLogsSML = ((SubmodelElementList) _aASElement);
    if ((resourceLogsSML != null)) {
      resourceLogsSML.getValue().clear();
    }
    SubmodelElement _aASElement_1 = this.getAASElement(resourceLogsWrapper, "FaultLogs");
    final SubmodelElementList faultLogsSML = ((SubmodelElementList) _aASElement_1);
    if ((faultLogsSML != null)) {
      faultLogsSML.getValue().clear();
    }
    try {
      this.aasHelper.updateSubmodel(productionLog);
      Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_2 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
      _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_2.info("ProductionLog reset.");
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_3 = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
        String _message = e.getMessage();
        _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER_3.warning(("Error while resetting productionLog: " + _message));
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
    if ($behaviorUnitGuard$RequestProduction$1(occurrence, occurrence)) {
      ___SARLlocal_runnableCollection.add(() -> $behaviorUnit$RequestProduction$1(occurrence));
    }
  }
  
  @SyntheticMember
  @PerceptGuardEvaluator
  private void $guardEvaluator$FaultLogBroadcast(final FaultLogBroadcast occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
    assert occurrence != null;
    assert ___SARLlocal_runnableCollection != null;
    if ($behaviorUnitGuard$FaultLogBroadcast$3(occurrence, occurrence)) {
      ___SARLlocal_runnableCollection.add(() -> $behaviorUnit$FaultLogBroadcast$3(occurrence));
    }
    ___SARLlocal_runnableCollection.add(() -> $behaviorUnit$FaultLogBroadcast$4(occurrence));
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
    toBeFilled.add(FaultLogBroadcast.class);
    toBeFilled.add(RequestProduction.class);
    toBeFilled.add(ResourceUpdate.class);
    toBeFilled.add(Initialize.class);
  }
  
  @SyntheticMember
  @Override
  public boolean $isSupportedEvent(final Class<? extends Event> event) {
    if (FaultLogBroadcast.class.isAssignableFrom(event)) {
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
    if (event instanceof FaultLogBroadcast) {
      final FaultLogBroadcast occurrence = (FaultLogBroadcast) event;
      $guardEvaluator$FaultLogBroadcast(occurrence, callbacks);
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
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ResourceAgent other = (ResourceAgent) obj;
    if (other.isCentralAgent != this.isCentralAgent)
      return false;
    if (!java.util.Objects.equals(this.CentralAAS, other.CentralAAS))
      return false;
    if (!java.util.Objects.equals(this.currentStepConversationId, other.currentStepConversationId))
      return false;
    return super.equals(obj);
  }
  
  @Override
  @Pure
  @SyntheticMember
  public int hashCode() {
    int result = super.hashCode();
    final int prime = 31;
    result = prime * result + Boolean.hashCode(this.isCentralAgent);
    result = prime * result + java.util.Objects.hashCode(this.CentralAAS);
    result = prime * result + java.util.Objects.hashCode(this.currentStepConversationId);
    return result;
  }
  
  @SyntheticMember
  public ResourceAgent(final UUID parentID, final UUID agentID) {
    super(parentID, agentID);
  }
}

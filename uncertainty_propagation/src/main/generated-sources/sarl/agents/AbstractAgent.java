package agents;

import capacities.I40_Message_Capacity;
import capacities.SharedResourceCapacity;
import helper.AasHelper;
import io.sarl.core.Behaviors;
import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.Logging;
import io.sarl.core.OpenEventSpace;
import io.sarl.core.OpenEventSpaceSpecification;
import io.sarl.lang.annotation.ImportedCapacityFeature;
import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.annotation.SyntheticMember;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AtomicSkillReference;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Pure;
import skills.I40_Message_Skill;
import skills.SharedResourceSkill;

/**
 * @author Alexis Bernhard
 */
@SarlSpecification("0.12")
@SarlElementType(19)
@SuppressWarnings("all")
public class AbstractAgent extends Agent {
  OpenEventSpace comspace;
  
  I40_Message_Skill message_skill;
  
  AasHelper aasHelper;
  
  AssetAdministrationShell aas;
  
  String aas_id;
  
  SharedResourceSkill sharedResourceSkill;
  
  public AbstractAgent(final UUID parentID, final UUID agentID) {
    super(parentID, agentID);
  }
  
  @SuppressWarnings("potential_field_synchronization_problem")
  protected boolean init(final Object[] parameters, final String assetType) {
    AasHelper _aasHelper = new AasHelper();
    this.aasHelper = _aasHelper;
    this.aas_id = null;
    if (((((List<Object>)Conversions.doWrapArray(parameters)).size() > 0) && (assetType != null))) {
      this.aas = this.aasHelper.getAASbyglobalAssetID(parameters[0].toString(), assetType);
      if ((this.aas == null)) {
        Logging _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER();
        String _string = parameters[0].toString();
        _$CAPACITY_USE$IO_SARL_CORE_LOGGING$CALLER.warning((("The AAS with global asset id " + _string) + 
          " could not be found. The agent is not initialized."));
        return false;
      }
      this.aas_id = this.aas.getId();
    }
    int _size = ((List<Object>)Conversions.doWrapArray(parameters)).size();
    if ((_size > 1)) {
      Object _get = parameters[1];
      this.sharedResourceSkill = ((SharedResourceSkill) _get);
      this.<SharedResourceSkill>setSkill(this.sharedResourceSkill, SharedResourceCapacity.class);
    } else {
      SharedResourceSkill _sharedResourceSkill = new SharedResourceSkill();
      this.sharedResourceSkill = _sharedResourceSkill;
      this.<SharedResourceSkill>setSkill(this.sharedResourceSkill, SharedResourceCapacity.class);
    }
    int _size_1 = ((List<Object>)Conversions.doWrapArray(parameters)).size();
    if ((_size_1 > 2)) {
      Object _get_1 = parameters[2];
      this.comspace = ((OpenEventSpace) _get_1);
      Behaviors _$CAPACITY_USE$IO_SARL_CORE_BEHAVIORS$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_BEHAVIORS$CALLER();
      this.comspace.registerWeakParticipant(_$CAPACITY_USE$IO_SARL_CORE_BEHAVIORS$CALLER.asEventListener());
    } else {
      DefaultContextInteractions _$CAPACITY_USE$IO_SARL_CORE_DEFAULTCONTEXTINTERACTIONS$CALLER = this.$CAPACITY_USE$IO_SARL_CORE_DEFAULTCONTEXTINTERACTIONS$CALLER();
      UUID _iD = this.getID();
      this.comspace = _$CAPACITY_USE$IO_SARL_CORE_DEFAULTCONTEXTINTERACTIONS$CALLER.getDefaultContext().<OpenEventSpace>getOrCreateSpaceWithSpec(OpenEventSpaceSpecification.class, _iD);
      Behaviors _$CAPACITY_USE$IO_SARL_CORE_BEHAVIORS$CALLER_1 = this.$CAPACITY_USE$IO_SARL_CORE_BEHAVIORS$CALLER();
      this.comspace.registerWeakParticipant(_$CAPACITY_USE$IO_SARL_CORE_BEHAVIORS$CALLER_1.asEventListener());
    }
    I40_Message_Skill _i40_Message_Skill = new I40_Message_Skill();
    this.message_skill = _i40_Message_Skill;
    this.<I40_Message_Skill>setSkill(this.message_skill, I40_Message_Capacity.class);
    return true;
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
  @ImportedCapacityFeature(Behaviors.class)
  @SyntheticMember
  private transient AtomicSkillReference $CAPACITY_USE$IO_SARL_CORE_BEHAVIORS;
  
  @SyntheticMember
  @Pure
  private Behaviors $CAPACITY_USE$IO_SARL_CORE_BEHAVIORS$CALLER() {
    if (this.$CAPACITY_USE$IO_SARL_CORE_BEHAVIORS == null || this.$CAPACITY_USE$IO_SARL_CORE_BEHAVIORS.get() == null) {
      this.$CAPACITY_USE$IO_SARL_CORE_BEHAVIORS = $getSkill(Behaviors.class);
    }
    return $castSkill(Behaviors.class, this.$CAPACITY_USE$IO_SARL_CORE_BEHAVIORS);
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
    AbstractAgent other = (AbstractAgent) obj;
    if (!Objects.equals(this.aas_id, other.aas_id))
      return false;
    return super.equals(obj);
  }
  
  @Override
  @Pure
  @SyntheticMember
  public int hashCode() {
    int result = super.hashCode();
    final int prime = 31;
    result = prime * result + Objects.hashCode(this.aas_id);
    return result;
  }
}

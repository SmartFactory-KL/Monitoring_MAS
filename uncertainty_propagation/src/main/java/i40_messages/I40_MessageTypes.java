package i40_messages;

/**
 * This class is based on the VDI/VDE 2193 part 1 from year 2020
 * @author Alexis Bernhard
 */
public class I40_MessageTypes {
	
	/** I will execute the function you requested. */
	public final static String CONSENT = "consent";
	/** I refuse to perform the activity you requested. */
	public final static String REFUSAL = "refusal";
	/** I was not able to execute your request. */
	public final static String ERROR = "error";
	/** I did not understand what you want me to do. */
	public final static String NOT_UNDERSTOOD = "notUnderstood";
	
	/** Please send me offers for the following call for proposals. */
	public final static String CALL_FOR_PROPOSALS = "callForProposals";
	/** I offer to execute this offer. */
	public final static String OFFER = "offer";
	/** I accept your offer. */
	public final static String OFFER_ACCEPTANCE = "acceptOffer";
	/** I reject your proposal. */
	public final static String OFFER_REJECTION = "rejectOffer";
	
	/** Believe me when I tell you that the following is true or untrue in my model of the world. */
	public final static String INFORMING = "informing";
	/** The statement received is consistent with my model of the world. */
	public final static String CONFIRMING = "confirming";
	/** The statement received is not consistent with my model of the world. */
	public final static String CONTRADICTING = "contradicting";
	/** Believe me when I tell you the following and pass it on. */
	public final static String SPREADING = "spreading";
	/** Send me constant updates. */
	public final static String SUBSCRIBING = "subscribing";
	
	/** Notify me as to whether the following statement is true in this model of the world. */
	public final static String QUERY_ALL = "queryAsToWhether";
	/** Notify me of the values for the following query. */
	public final static String QUERY_REF = "queryRef";
	/** Perform the following activity. */
	public final static String REQUIREMENT = "requirement";
	/** Execute the following activity if the condition occurs. */
	public final static String REQUIREMENT_WHEN = "requirementWhen";
	/** Like requirement as to when, but repeatedly when the condition occurs. */
	public final static String REQUIREMENT_HOW_OFTEN = "requirementWhen";
	/** Command reversal, cancellation of dialogues in general. */
	public final static String CANCELLING = "cancelling";
	/** Forward the message included here to the I4.0 Components specified. */
	public final static String PROXY = "proxy";
}

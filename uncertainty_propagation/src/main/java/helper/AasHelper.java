package helper;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.basyx.v3.clientfacade.BasyxFacadeManager;
import org.eclipse.digitaltwin.basyx.v3.clientfacade.BasyxReadFacade;
import org.eclipse.digitaltwin.basyx.v3.clientfacade.BasyxWriteFacade;
import org.eclipse.digitaltwin.basyx.v3.clientfacade.DefaultBasyxApiManager;
import org.eclipse.digitaltwin.basyx.v3.clientfacade.DefaultBasyxFacadeManager;
import org.eclipse.digitaltwin.basyx.v3.clientfacade.config.BasyxApiConfiguration;
import org.eclipse.digitaltwin.basyx.v3.clientfacade.config.SimpleBasyxApiConfiguration;
import org.eclipse.digitaltwin.basyx.v3.clientfacade.exception.IdentifiableNotFoundException;
import org.eclipse.digitaltwin.basyx.v3.clientfacade.references.SimpleSubmodelReferenceResolver;



public class AasHelper {

	private final static String AASRegistryURL = "http://localhost:8082";
	private final static String RepositoryURL = "http://localhost:8081";
	private final static String SubmodelRegistryURL = "http://localhost:8083";

	BasyxReadFacade readFacade;
	BasyxWriteFacade writeFacade;

	/**
	 * The constructor of this class initializes an aas helper by setting up internal parameters and initializing the connection to the Basyx server.
	 * This method can throw an ApiException if the connection could not be established. One potential cause are wrong or missing URLs in the AASRegistryURL, RepositoryURL, and SubmodelRegistryURL fields.
	 */
	public AasHelper() {
		BasyxApiConfiguration config = new SimpleBasyxApiConfiguration()
				.withEnvironmentUrl(RepositoryURL)
				.withAasRegistryUrl(AASRegistryURL)
				.withSubmodelRegistryUrl(SubmodelRegistryURL);

		BasyxFacadeManager manager = new DefaultBasyxFacadeManager(new DefaultBasyxApiManager(config));
		readFacade = manager.newReadFacade().withSubmodelResolver(new SimpleSubmodelReferenceResolver());
		writeFacade = manager.newWriteFacade();
	}

	/**
	 * Updates a specific submodel on the basyx server by uploading the submodel on the server.
	 * This method throws an IdentifiableNotFoundException, if the submodel could not be found.
	 * @param submodel The submodel to upload to the server.
	 */
	public synchronized void updateSubmodel(Submodel submodel) {
		try {
			writeFacade.updateSubmodel(submodel);
		} catch (IdentifiableNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves an AAS from the Basyx Server by its AAS id
	 * @param id The ID of the AAS uniquely identifies an AAS.
	 * @return The AAS or null if the AAS could not be found.
	 */
	public AssetAdministrationShell getAasById(String id) {
		Optional<AssetAdministrationShell> optShell = readFacade.getShellById(id);
		AssetAdministrationShell shell = optShell.orElse(null);
		return shell;
	}

	/**
	 * Retrieves a submodel from the Basyx Server by its submodel id
	 * @param smId The ID of the submodel uniquely identifies a submodel.
	 * @return The submodel or null if the submodel could not be found.
	 */
	public Submodel getSubmodelById(String smId) {
		for (Submodel eachSM : readFacade.getAllSubmodels()) {
//			System.out.println("sm: " + eachSM.getIdShort());

			if (eachSM.getId().equals(smId)) {
				return eachSM;
			}
		}	
		return null;		
	}

	/**
	 * Retrieves a submodel from the Basyx Server by its submodel idshort and its aasid. Warning: The smIdShort might not be unique!
	 * @param aasId The ID of the containing AAS of the submodel.
	 * @param smIdShort the idshort of the submodel
	 * @return The firstly discovered submodel with the idshort or null if the submodel could not be found.
	 */
	public Submodel getSubmodelByIdShort(AssetAdministrationShell aasId, String smIdShort) {
		for (Submodel eachSm : readFacade.getAllSubmodels(aasId)) {
			if(eachSm.getIdShort().equals(smIdShort)) {
				return eachSm;
			}
		}	
		return null;		
	}

	/**
	 * Retrieves a SubmodelElement based on a given submodel and an idshortpath
	 * @param sm The containing submodel to find the SubmodelElement
	 * @param idShortPath The full idshort path of the SubmodelElement as described in the SubmodelElement id.
	 * @return The retrieved Submodelelement as java optional (which can also raise a NoSuchElementException if nothing could be found)
	 */
	public SubmodelElement getSubmodelElementByIdShortPath(Submodel sm, String idShortPath) {
		return readFacade.getSubmodelElementByIdShortPath(sm, idShortPath).get();
	}

	/**
	 * Retrieves an AAS from the Basyx Server by its global asset id. Warning this method just works for AASs with AssetKind.Instance.
	 * @param assetID The Global Asset ID uniquely identifies the asset that an AAS represents. It ensures that the AAS is linked to the correct real-world asset across different systems.
	 * @param assettype AssetType is a classification label that describes the type or role of an asset. It helps in organizing and managing assets efficiently in an Industry 4.0 environment.
	 * @return The AAS or null if the AAS could not be found.
	 */
	public AssetAdministrationShell getAASbyglobalAssetID(String assetID, String assetType) {
		for (AssetAdministrationShell eachShell : readFacade.getAllShells(AssetKind.INSTANCE, assetType)) {
			if (eachShell.getAssetInformation().getGlobalAssetId() != null && eachShell.getAssetInformation().getGlobalAssetId().equals(assetID)) {
				return eachShell;
			}
		}
		return null;
	}
	

	
	public SubmodelElement getAASChild(SubmodelElementCollection smc, String idShort) {
		try {
			return smc.getValue().stream().filter(sme -> sme.getIdShort().contains(idShort)).findFirst().get();
		} catch (NoSuchElementException e) {
//			System.out.println("The AAS element " + idShort + " could not be found in smc " + smc.getIdShort());
		}
		return null;
	}
	
	public SubmodelElement getAASChild(Submodel sm, String idShort) {
		try {
			return sm.getSubmodelElements().stream().filter(sme -> sme.getIdShort().contains(idShort)).findFirst().get();
		} catch (NoSuchElementException e) {
//			System.out.println("The AAS element " + idShort + " could not be found in smc " + sm.getIdShort());
		} 
		return null; 
	}
}
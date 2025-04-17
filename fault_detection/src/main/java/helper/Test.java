package helper;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.basyx.v3.clientfacade.BasyxFacadeManager;
import org.eclipse.digitaltwin.basyx.v3.clientfacade.DefaultBasyxApiManager;
import org.eclipse.digitaltwin.basyx.v3.clientfacade.DefaultBasyxFacadeManager;
import org.eclipse.digitaltwin.basyx.v3.clientfacade.config.BasyxApiConfiguration;
import org.eclipse.digitaltwin.basyx.v3.clientfacade.config.SimpleBasyxApiConfiguration;
import org.eclipse.digitaltwin.basyx.v3.clientfacade.exception.ConflictingIdentifierException;

public class Test {

	public static void main(String[] args) throws ConflictingIdentifierException {

		BasyxApiConfiguration config = new SimpleBasyxApiConfiguration().withAasRegistryUrl("http://172.17.10.80:8002").withSubmodelRegistryUrl("http://172.17.10.80:8003");
		

		BasyxFacadeManager manager = new DefaultBasyxFacadeManager(new DefaultBasyxApiManager(config));

		// BasyxUpdateConfiguration updateConfig = SimpleBasyxUpdateConfiguration.forEnvironmentUrl("http://172.17.10.80:8000");

		// BasyxRegistryServiceConfiguration serviceConfig = new SimpleBasyxServiceConfiguration().withAasRegistryUrl("http://172.17.10.80:8002").withSubmodelRegistryUrl("http://172.17.10.80:8003");
		/*manager.newServiceFacade(serviceConfig).withEndpointResolver(new FirstEndpointResolver() {@Override
			protected Endpoint chooseEndpoint(List<Endpoint> endpoints) {
			System.out.println(endpoints.stream().map(Endpoint::getProtocolInformation).map(ProtocolInformation::getHref).collect(Collectors.toList()));
			System.out.flush();
			return super.chooseEndpoint(endpoints);
		}})*/manager.newReadFacade().getAllShells().stream().map(AssetAdministrationShell::getId).forEach(System.out::println);

		//		BasyxUpdateFacade updateFacade = manager.newUpdateFacade(updateConfig);

		/*		updateFacade.deleteAllShells();
		updateFacade.deleteAllSubmodels();

		Reference ref = updateFacade.postSubmodel(new DefaultSubmodel.Builder().id("http://sm.test.org/test-sm").idShort("test-sm").build());
		updateFacade.postShell(new DefaultAssetAdministrationShell.Builder().id("http://aas.test.org/test-aas").idShort("test-aas").submodels(ref).build());

		BasyxServiceFacade facade = manager.newServiceFacade(serviceConfig).withEndpointResolver(EndpointResolvers.firstWithAddress("127.0.0.1:8081"))
				.withSubmodelResolver(new SimpleSubmodelReferenceResolver());
		for (AssetAdministrationShell eachShell : facade.getAllShells()) {
			System.out.println(eachShell.getId());
			for (Submodel eachSm : facade.getAllSubmodels(eachShell)) {
				System.out.println(eachSm.getId());
			}
		}
		updateFacade.deleteAllShells();
		updateFacade.deleteAllSubmodels();*/

	}

}

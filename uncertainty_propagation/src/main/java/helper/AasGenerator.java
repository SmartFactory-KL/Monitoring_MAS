package helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;

/**
 * This class takes care of generating new AAS model elements based on given infomation defined as java datatype or object
 * @author Alexis Bernhard
 */
public class AasGenerator {
	
	/**
	 * Generates a new AAS property and sets its idshort, value and valuetype. Other fields are not set.
	 * @param idShort The idShort of the AAS property. An idshort is a mandatory key element used for naming and structuring properties within the AAS model.
	 * @param valueType Defines the xsd based data type of the value (see the parameter below) stored within that property. More information on xsd datatypes can be found in org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd
	 * @param value Represents the actual data stored within the property. It is associated with a valueType, which defines its data type.
	 * @return The Property AAS model element
	 */
	public Property createProperty(String idShort, DataTypeDefXsd valueType, String value) {
			Property prop = new DefaultProperty();
			prop.setIdShort(idShort);
			prop.setValue(value);
			try {
				prop.setValueType(valueType);
	        } catch (IllegalArgumentException e) {
	            System.out.println("Invalid inserted valuetype: " + valueType + ". The valuetype of property " + idShort + " is omitted");
	        }
			return prop;
	}
	
	/**
	 * Generates a new empty AAS model element called SubmodelElementCollection which is used to group multiple SubmodelElements into a container
	 * @param idShort The idShort of the AAS Submodelcollection. An idshort is a mandatory key element used for naming and structuring properties within the AAS model.
	 * @return The SubmodelElementCollection model element
	 */
	public SubmodelElementCollection createSMC(String idShort) {
		SubmodelElementCollection collection = new DefaultSubmodelElementCollection();
		collection.setIdShort(idShort);
		return collection;
	}

	/**
	 * Generates a new AAS model element called SubmodelElementCollection which is used to group multiple SubmodelElements into a container
	 * @param idShort The idShort of the AAS Submodelcollection. An idshort is a mandatory key element used for naming and structuring properties within the AAS model.#
	 * @param elements A list of SubmodelElements which should be added to the SubmodelElementCollection during creation of the element
	 * @return The SubmodelElementCollection model element
	 */
	public SubmodelElementCollection createSMC(String idShort, List<SubmodelElement> elements) {
		SubmodelElementCollection collection = new DefaultSubmodelElementCollection();
		collection.setIdShort(idShort);
		collection.setValue(elements);
		return collection;
	}
	
	/**
	 * Generates a new empty AAS model element called SubmodelElementlist which is used to list multiple SubmodelElements into an ordered list.
	 * @param idShort The idShort of the AAS SubmodelElementlist. An idshort is a mandatory key element used for naming and structuring properties within the AAS model.
	 * @return The SubmodelElementList model element
	 */
	public SubmodelElementList createSML(String idShort) {
		SubmodelElementList list = new DefaultSubmodelElementList();
		list.setIdShort(idShort);
		return list;
	}
	
	/**
	 * Generates a new empty AAS model element called SubmodelElementlist which is used to list multiple SubmodelElements into an ordered list.
	 * @param idShort The idShort of the AAS SubmodelElementlist. An idshort is a mandatory key element used for naming and structuring properties within the AAS model.
	 * @param orderedList A list of SubmodelElements which should be added to the SubmodelElementList as values in the same order as given in the list
	 * @return The SubmodelElementList model element
	 */
	public SubmodelElementList createSML(String idShort, List<SubmodelElement> orderedList) {
		SubmodelElementList list = new DefaultSubmodelElementList();
		list.setIdShort(idShort);
		list.setValue(orderedList);
		return list;
	}
	
	/**
	 * Generates a new reference element. A reference element is a submodel element used to store a reference to another AAS element, asset, or external source. It does not contain data itself but points to another entity within or outside the AAS.
	 * @param idShort The idShort of the AAS Reference Element. An idshort is a mandatory key element used for naming and structuring properties within the AAS model
	 * @param refType This defines the type of the reference. This is either a model reference which Points to an element inside an AAS or a global reference which points to an external resource
	 * @param values This map is the core of the reference and defines all keys as a list of identifiers leading to the referenced element. Every key is represented as map entry which maps a type (Specifies the referenced element type) on a value (The unique ID of the referenced element)
	 * @return The complete referenceElement
	 */
	public ReferenceElement createReferenceElement(String idShort, ReferenceTypes refType, Map<KeyTypes, String> values) {
		ReferenceElement refElem = new DefaultReferenceElement();
		refElem.setIdShort(idShort);
		Reference ref = new DefaultReference();
		ref.setType(refType);
		List<Key> keys = new ArrayList<Key>();
		for (Map.Entry<KeyTypes, String> valueTuple : values.entrySet()) {
			Key key = new DefaultKey();
			key.setType(valueTuple.getKey());
			key.setValue(valueTuple.getValue());
			keys.add(key);
		}
		ref.setKeys(keys);
		refElem.setValue(ref);
		return refElem;
	}
}

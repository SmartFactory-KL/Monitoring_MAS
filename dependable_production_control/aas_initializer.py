import requests
import base64
import os
import json
from datetime import datetime
import uuid

AAS_SERVER = "http://localhost:8081"
AAS_REGISTRY_URL = "http://localhost:8082/shell-descriptors"

plan_path = os.path.join(os.path.dirname(__file__), "Submodel_ProductionPlan.json")
log_path = os.path.join(os.path.dirname(__file__), "Submodel_ProductionLog.json")


def encode_id(aas_id: str) -> str:
    return base64.urlsafe_b64encode(aas_id.encode()).decode().rstrip("=")


def clear_all_aas_from_registry():
    try:
        response = requests.get(AAS_REGISTRY_URL)
        response.raise_for_status()
        results = response.json().get("result", [])
        print(f"Found {len(results)} AAS entries to delete.")
        for entry in results:
            try:
                href = entry["endpoints"][0]["protocolInformation"]["href"]
                r = requests.delete(href)
                print(f"Deleted: {href}" if r.status_code in (200, 204) else f"Failed to delete {href}: {r.status_code}")
            except Exception as e:
                print(f"Skipped AAS due to error: {e}")
    except Exception as e:
        print(f" Error accessing registry: {e}")


def clear_all_submodels():
    try:
        response = requests.get(f"{AAS_SERVER}/submodels")
        response.raise_for_status()
        for sm in response.json().get("result", []):
            sm_id = sm.get("id")
            if sm_id:
                encoded = encode_id(sm_id)
                r = requests.delete(f"{AAS_SERVER}/submodels/{encoded}")
                print(f" Deleted submodel: {sm_id}" if r.status_code in (200, 204) else f"Failed to delete {sm_id}")
    except Exception as e:
        print(f"Error clearing submodels: {e}")


def create_aas_for_holon(name: str, level: str, children: list[str]) -> str:
    aas_id = f"urn:aas:{name}"
    descriptor = {
        "idShort": name,
        "id": aas_id,
        "asset": {"kind": "Instance", "id": f"urn:asset:{name}"},
        "extensions": [
            {"name": "level", "valueType": "string", "value": level},
            {"name": "children", "valueType": "string", "value": str(children)},
        ]
    }
    try:
        r = requests.post(f"{AAS_SERVER}/shells", json=descriptor)
        if r.status_code in [200, 201]:
            print(f"Created AAS: {aas_id}")
        elif r.status_code == 409:
            print(f"AAS already exists: {aas_id}")
        else:
            print(f"Failed to create AAS: {r.status_code}")
            return None
        return aas_id
    except Exception as e:
        print(f"Error creating AAS: {e}")
        return None


def upload_submodel_to_aas(aas_id: str, submodel_path: str):
    try:
        with open(submodel_path, "r", encoding="utf-8") as f:
            sm_data = json.load(f)

        suffix = aas_id.split(":")[-1]
        sm_data["idShort"] += f"_{suffix}"
        sm_data["id"] += f"_{suffix}"

        r1 = requests.post(f"{AAS_SERVER}/submodels", json=sm_data)
        if r1.status_code in [200, 201]:
            print(f"Uploaded submodel {sm_data['idShort']}")
        elif r1.status_code == 409:
            print(f"Submodel already exists")
        else:
            print(f"Upload failed: {r1.status_code} - {r1.text}")

        submodel_ref = {
            "type": "ModelReference",
            "keys": [{
                "type": "Submodel", "local": True, "idType": "IRI", "value": sm_data["id"]
            }]
        }
        encoded_aas = encode_id(aas_id)
        r2 = requests.post(f"{AAS_SERVER}/shells/{encoded_aas}/submodel-refs", json=submodel_ref)
        if r2.status_code in [200, 201, 204]:
            print(f"Linked submodel to {aas_id}")
        elif r2.status_code == 409:
            print(f"Submodel already linked")
        else:
            print(f"Linking failed: {r2.status_code}")
    except Exception as e:
        print(f"Error uploading submodel: {e}")


def find_submodel_id_for_aas(aas_id: str, suffix: str) -> str:
    try:
        encoded = encode_id(aas_id)
        response = requests.get(f"{AAS_SERVER}/shells/{encoded}/submodel-refs")
        for ref in response.json().get("result", []):
            keys = ref.get("keys", [])
            for k in keys:
                if suffix in k.get("value", ""):
                    return k["value"]
    except Exception as e:
        print(f"Could not find submodel with suffix '{suffix}': {e}")
    return None
def create_aas_for_product(name: str) -> str:
    aas_id = f"https://template.smartfactory.de/shells/{name}"
    descriptor = {
        "idShort": name,
        "id": aas_id,
        "asset": {"kind": "Instance", "id": f"urn:asset:{name}"}
    }
    try:
        r = requests.post(f"{AAS_SERVER}/shells", json=descriptor)
        if r.status_code in [200, 201]:
            print(f" Created AAS: {aas_id}")
        elif r.status_code == 409:
            print(f" AAS already exists: {aas_id}")
        else:
            print(f" Failed to create AAS: {r.status_code}")
            return None
        return aas_id
    except Exception as e:
        print(f"Error creating AAS: {e}")
        return None

def initialize_product_aas_and_submodels(product):
    name = f"Product_{product.product_id}"
    aas_id = create_aas_for_product(name)
    if aas_id:
        product.aas_id = aas_id  # set it on the product so it can be used later
        upload_submodel_to_aas(aas_id, plan_path)
        init_production_plan_submodel(product)
    else:
        print(f"Could not initialize AAS for product {product.product_id}")


def update_production_plan_order_status_patch(product, order, step):
    submodel_id = find_submodel_id_for_aas(product.aas_id, "ProductionPlan")
    if not submodel_id:
        print(f"Can't update: ProductionPlan submodel not found for {product.aas_id}")
        return

    encoded_id = encode_id(submodel_id)
    patch_url = f"{AAS_SERVER}/submodels/{encoded_id}/submodel-elements/Orders.Orders/value"

    order_entry = {
        "idShort": f"Order_{order.order_id}",
        "modelType": "SubmodelElementCollection",
        "value": [
            {"idShort": "OrderId", "modelType": "Property", "value": str(order.order_id)},
            {"idShort": "Status", "modelType": "Property", "value": "finished"},
            {"idShort": "CompletedAtStep", "modelType": "Property", "value": str(step)}
        ]
    }

    try:
        response = requests.patch(patch_url, json={"value": [order_entry]})
        if response.status_code in [200, 204]:
            print(f"ProductionPlan updated for Product {product.product_id}")
        else:
            print(f"Failed to PATCH ProductionPlan: {response.status_code} - {response.text}")
    except Exception as e:
        print(f"Exception while updating ProductionPlan: {e}")


def init_production_plan_submodel(product):
    def order_to_step(order):
        """Recursively convert Order to a Step element."""
        step = {
            "idShort": f"Step_{order.order_id}",
            "modelType": "SubmodelElementCollection",
            "value": [
                {"idShort": "StepId", "modelType": "Property", "valueType": "xs:string", "value": order.order_id},
                {"idShort": "StepTitle", "modelType": "Property", "valueType": "xs:string", "value": f"{order.level} step"},
                {"idShort": "InitialState", "modelType": "SubmodelElementCollection", "value": []},
                {"idShort": "FinalState", "modelType": "SubmodelElementCollection", "value": []},
                {
                    "idShort": "PlannedProcessTimes",
                    "modelType": "SubmodelElementCollection",
                    "value": [
                        {"idShort": "PlannedStart", "modelType": "Property", "valueType": "xs:dateTime", "value": datetime.utcnow().isoformat() + "Z"},
                        {"idShort": "PlannedEnd", "modelType": "Property", "valueType": "xs:dateTime", "value": datetime.utcnow().isoformat() + "Z"},
                    ]
                },
                {
                    "idShort": "Resource",
                    "modelType": "SubmodelElementCollection",
                    "value": [
                        {"idShort": "Level", "modelType": "Property", "valueType": "xs:string", "value": order.level},
                        {"idShort": "Name", "modelType": "Property", "valueType": "xs:string", "value": ""},
                        {
                            "idShort": "ResourceReference",
                            "modelType": "ReferenceElement",
                            "value": {
                                "type": "ModelReference",
                                "keys": [
                                    {"type": "AssetAdministrationShell", "value": "https://smartfactory.de/shells/000"}
                                ]
                            }
                        }
                    ]
                }
            ]
        }

        # Recursively build substeps
        if order.suborders:
            step["value"].append({
                "idShort": "Substeps",
                "modelType": "SubmodelElementList",
                "typeValueListElement": "SubmodelElementCollection",
                "value": [order_to_step(sub) for sub in order.suborders]
            })

        return step

    # Convert all root-level orders into steps
    steps = [order_to_step(o) for o in product.orders]

    submodel_id_short = f"ProductionPlan_{product.product_id}"
    submodel_iri = f"https://template.smartfactory.de/sm/{submodel_id_short}"
    encoded_id = encode_id(submodel_iri)

    submodel = {
        "idShort": "ProductionPlan",
        "id": submodel_iri,
        "semanticId": {
            "type": "ExternalReference",
            "keys": [{
                "type": "GlobalReference",
                "value": "https://smartfactory.de/semantics/submodel/ProductionPlan#1/0"
            }]
        },
        "modelType": "Submodel",
        "submodelElements": [
            {
                "idShort": "Steps",
                "modelType": "SubmodelElementList",
                "typeValueListElement": "SubmodelElementCollection",
                "value": steps
            }
        ]
    }

    # Upload submodel
    try:
        response = requests.post(f"{AAS_SERVER}/submodels", json=submodel)
        if response.status_code in [200, 201]:
            print(f"Uploaded ProductionPlan submodel {submodel_id_short}")
        elif response.status_code == 409:
            print(f"ProductionPlan already exists for {product.aas_id}")
        else:
            print(f" Failed to create submodel: {response.status_code} - {response.text}")
            return
    except Exception as e:
        print(f"Exception during submodel POST: {e}")
        return

    # Link to product AAS
    try:
        encoded_aas = encode_id(product.aas_id)
        submodel_ref = {
            "type": "ModelReference",
            "keys": [{"type": "Submodel", "local": True, "idType": "IRI", "value": submodel_iri}]
        }
        response = requests.post(f"{AAS_SERVER}/shells/{encoded_aas}/submodel-refs", json=submodel_ref)
        if response.status_code in [200, 201, 204]:
            print(f"Linked ProductionPlan to {product.aas_id}")
        elif response.status_code == 409:
            print(f"ProductionPlan already linked")
        else:
            print(f"Failed to link submodel: {response.status_code} - {response.text}")
    except Exception as e:
        print(f"Exception linking ProductionPlan to AAS: {e}")


def mark_order_completed_in_production_plan(product, order, resource):
    submodel_id = f"ProductionPlan_{product.product_id}"
    submodel_iri = f"https://template.smartfactory.de/sm/{submodel_id}"
    encoded_id = encode_id(submodel_iri)
    get_url = f"{AAS_SERVER}/submodels/{encoded_id}"

    try:
        # Step 1: GET current submodel
        response = requests.get(get_url)
        response.raise_for_status()
        submodel = response.json()

        def update_step(steps):
            for step in steps:
                values = step.get("value", [])
                for elem in values:
                    if elem.get("idShort") == "StepId" and elem.get("value") == order.order_id:

                        final_state = {
                            "idShort": "FinalState",
                            "modelType": "SubmodelElementCollection",
                            "value": [
                                {"idShort": "Status", "modelType": "Property", "value": "Completed"},
                                {"idShort": "CompletedAt", "modelType": "Property", "value": datetime.utcnow().isoformat() + "Z"},
                                {"idShort": "ProcessedBy", "modelType": "Property", "value": f"RobotHolon_{resource.robot_id}"}
                            ]
                        }

                        for i, v in enumerate(values):
                            if v.get("idShort") == "FinalState":
                                values[i] = final_state
                                return True

                        values.append(final_state)
                        return True

                for elem in values:
                    if elem.get("idShort") == "Substeps":
                        if update_step(elem.get("value", [])):
                            return True
            return False

        steps = submodel.get("submodelElements", [])[0].get("value", [])
        if update_step(steps):
            # Step 3: PUT updated submodel back
            put_url = f"{AAS_SERVER}/submodels/{encoded_id}"
            r = requests.put(put_url, json=submodel)
            if r.status_code in [200, 204]:
                print(f"Marked Order {order.order_id} as completed in Product {product.product_id}'s ProductionPlan")
            else:
                print(f"Failed to PUT updated submodel: {r.status_code} - {r.text}")
        else:
            print(f"Could not find StepId {order.order_id} in nested steps for Product {product.product_id}")

    except Exception as e:
        print(f"Exception during PATCH of production plan: {e}")



def append_resource_log_entry2(aas_id: str, log_type: str, description: str, order):
    submodel_id = find_submodel_id_for_aas(aas_id, "ProductionLog")
    if not submodel_id:
        print(f"Can't log: submodel not found.")
        return

    encoded_id = encode_id(submodel_id)
    base_url = f"{AAS_SERVER}/submodels/{encoded_id}/submodel-elements"
    post_url = f"{base_url}/ResourceLogs.ResourceLogs"

    log_entry = {
        "idShort": f"log_{uuid.uuid4().hex[:6]}",
        "modelType": "SubmodelElementCollection",
        "value": [
            {
                "idShort": "LogId",
                "modelType": "Property",
                "valueType": "xs:string",
                "category": "CONSTANT",
                "value": str(uuid.uuid4())
            },
            {
                "idShort": "DateTime",
                "modelType": "Property",
                "valueType": "xs:dateTime",
                "category": "CONSTANT",
                "value": datetime.utcnow().isoformat() + "Z"
            },
            {
                "idShort": "LogType",
                "modelType": "Property",
                "valueType": "xs:string",
                "category": "CONSTANT",
                "value": log_type
            },
            {
                "idShort": "Description",
                "modelType": "Property",
                "valueType": "xs:string",
                "value": description
            },
            {
                "idShort": "RefResourceAAS",
                "modelType": "ReferenceElement",
                "value": {
                    "keys": [
                        {
                            "type": "AssetAdministrationShell",
                            "value": "https://template.smartfactory.de/shells/template"
                        }
                    ],
                    "type": "ModelReference"
                }
            },
            {
                "idShort": "DataObjects",
                "modelType": "SubmodelElementList",
                "orderRelevant": True,
                "typeValueListElement": "SubmodelElementCollection",
                "value": [
                    {
                        "idShort": "OrderInfo",
                        "modelType": "SubmodelElementCollection",
                        "value": [
                            {
                                "idShort": "DataType",
                                "modelType": "Property",
                                "valueType": "xs:string",
                                "value": "OrderInfo"
                            },
                            {
                                "idShort": "Values",
                                "modelType": "Property",
                                "valueType": "xs:string",
                                "value": f"orderId={order.order_id}, skill={order.required_skill}, time={order.process_time}"
                            }
                        ]
                    }
                ]
            }
        ]
    }

    try:
        response = requests.post(post_url, json=log_entry)
        if response.status_code in [200, 201, 204]:
            print(f" Log entry added to {submodel_id}")
        else:
            print(f" Failed to POST: {response.status_code} - {response.text}")
    except Exception as e:
        print(f" Exception during POST: {e}")


def append_resource_log_entry(aas_id: str, log_type: str, description: str, order):
    submodel_id = find_submodel_id_for_aas(aas_id, "ProductionLog")
    if not submodel_id:
        print(f"Can't log: submodel not found.")
        return

    encoded_id = encode_id(submodel_id)
    base_url = f"{AAS_SERVER}/submodels/{encoded_id}/submodel-elements"
    patch_url = f"{base_url}/ResourceLogs.ResourceLogs/value"

    log_entry = {
        "idShort": f"log_{uuid.uuid4().hex[:6]}",
        "modelType": "SubmodelElementCollection",
        "value": [
            {"idShort": "LogId", "modelType": "Property", "value": str(uuid.uuid4())},
            {"idShort": "DateTime", "modelType": "Property", "value": datetime.utcnow().isoformat() + "Z"},
            {"idShort": "LogType", "modelType": "Property", "value": log_type},
            {"idShort": "Description", "modelType": "Property", "value": description},
            {
                "idShort": "DataObjects",
                "modelType": "SubmodelElementCollection",
                "value": [
                    {
                        "idShort": "OrderInfo",
                        "modelType": "SubmodelElementCollection",
                        "value": [
                            {"idShort": "DataType", "modelType": "Property", "value": "OrderInfo"},
                            {"idShort": "Values", "modelType": "Property", "value": f"orderId={order.order_id}, skill={order.required_skill}, time={order.process_time}"}
                        ]
                    }
                ]
            }
        ]
    }

    try:
        patch_response = requests.patch(patch_url, json={"value": [log_entry]})
        if patch_response.status_code in [200, 204]:
            print(f"Log entry added to {submodel_id}")
        else:
            print(f"Failed to PATCH: {patch_response.status_code} - {patch_response.text}")
    except Exception as e:
        print(f"Exception during PATCH: {e}")


def initialize_holon_aas(name: str, level: str, children: list[str]):
    aas_id = create_aas_for_holon(name, level, children)
    if aas_id:
        upload_submodel_to_aas(aas_id, log_path)
    else:
        print(f"AAS creation failed. Skipping submodel uploads.")
    return aas_id

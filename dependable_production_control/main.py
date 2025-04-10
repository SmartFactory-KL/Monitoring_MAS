# mainSim.py

from mesa import Agent, Model
from mesa.space import MultiGrid
from mesa.datacollection import DataCollector
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from aas_initializer import initialize_holon_aas, clear_all_aas_from_registry,clear_all_submodels, append_resource_log_entry,append_resource_log_entry2,mark_order_completed_in_production_plan,init_production_plan_submodel, create_aas_for_holon, initialize_product_aas_and_submodels

# -------------------------------
# Order Class (Hierarchical)
# -------------------------------
class Order:
    def __init__(self, order_id, level, required_skill, process_time, suborders=None, product=None):
        self.order_id = order_id
        self.level = level
        self.required_skill = required_skill
        self.process_time = process_time
        self.steps_remaining = process_time
        self.suborders = suborders if suborders is not None else []
        self.product = product
        self.current_index = 0
        self.allocated = None
        self.processing = False

    def get_current_suborder(self):
        if self.suborders and self.current_index < len(self.suborders):
            return self.suborders[self.current_index]
        return None

    def advance(self):
        if self.current_index < len(self.suborders) - 1:
            self.current_index += 1
        else:
            self.current_index = None


class Product:
    def __init__(self, product_id, model, aas_id):
        self.product_id = product_id
        self.model = model
        self.aas_id = aas_id
        self.orders = []
        self.current_order_index = 0

    def add_order(self, order):
        self.orders.append(order)

    def mark_order_completed(self, order, location):
        mark_order_completed_in_production_plan(self, order, location)
    

    def get_next_target(self):
        def find_next(order):
            if order.level == "Robot" and order.steps_remaining > 0 and order.allocated:
                return order.allocated
            for sub in order.suborders:
                result = find_next(sub)
                if result:
                    return result
            return None

        for order in self.orders:
            result = find_next(order)
            if result:
                return result
        return None  # No more targets


# -------------------------------
# Base Holon (Processing Agent)
# -------------------------------
class Holon(Agent):
    def __init__(self, model, level, skills):
        super().__init__(model)
        self.level = level
        self.skills = skills
        self.order_backlog = []
        self.children = []
        self.aas_id = None

    def transfer_orders(self, suborders):
        if not suborders:
            self.model.log(f"{self.__class__.__name__} {self.unique_id} received empty suborder list. No transfers.")
            return

        for suborder in suborders:
            if suborder.allocated:
                suborder.allocated.incoming_orders.append(suborder)
                self.model.log(f"{self.__class__.__name__} received incoming Suborder {suborder.order_id} for processing.")
            else:
                self.model.log(f"WARNING: Suborder {suborder.order_id} has no allocated holon. Skipping transfer.")

    def step(self):
        self.transfer_orders([])

class FactoryHolon(Holon):
    def __init__(self, model):
        super().__init__(model, level="Factory", skills=["Factory"])
        self.created = False
        self.incoming_orders = []
        self.backlog = []
        self.products = []

    def initialize_aas(self, children_ids):
        self.aas_id = initialize_holon_aas(name="FactoryHolon_1", level="Factory", children=children_ids)

    def step(self):
        if not self.created:
            for product in self.products:
                top_order = product.orders[0]  # Factory order
                if top_order.suborders:
                    station_order = top_order.suborders[0]
                    station_order.allocated = self.model.station
                    self.model.station.incoming_orders.append(station_order)
                    self.model.log(f"FactoryHolon forwarded Product {product.product_id} with Station Order {station_order.order_id}")
            self.created = True

class StationHolon(Holon):
    def __init__(self, model):
        super().__init__(model, level="Station", skills=["frontend"])
        self.incoming_orders = []
        self.backlog = []
        self.products_spawned = set()

    def initialize_aas(self, children_ids):
        self.aas_id = initialize_holon_aas(name="StationHolon_1", level="Station", children=children_ids)

    def step(self):
        if not self.incoming_orders:
            self.model.log(f"StationHolon has no incoming orders to process.")
            return

        self.model.log(f"StationHolon processing {len(self.incoming_orders)} new orders.")
        suborders_to_transfer = []

        for order in self.incoming_orders:
            if order.required_skill == "frontend":
                self.model.log(f"Processing Order {order.order_id} at StationHolon.")
                first_robot = None
                if order.suborders:

                    ############# TODO: ADD greedy scheduling
                    for child in order.suborders:
                        child.product = order.product
                        if child.allocated is None:
                            candidates = [r for r in self.children if child.required_skill in r.skills]
                            if candidates:
                                chosen = candidates[0]
                                child.allocated = chosen
                                self.model.log(f"Order {child.order_id} allocated to RobotHolon {chosen.robot_id}.")
                                if first_robot is None:
                                    first_robot = chosen
                    suborders_to_transfer.extend(order.suborders)
                self.backlog.append(order)
                if first_robot and order.order_id not in self.products_spawned:
                    if first_robot.add_product(order.product):
                        order.product.current_location = first_robot
                        self.products_spawned.add(order.order_id)
                        self.model.log(f"Product {order.order_id} spawned at RobotHolon {first_robot.robot_id}.")
        self.transfer_orders(suborders_to_transfer)
        ############# TODO: Also send reference trajectories
        self.incoming_orders.clear()

class RobotHolon(Holon):
    def __init__(self, model, robot_id, pos, capacity=100, skills=None, strict_order_matching=False):
        super().__init__(model, level="Robot", skills=skills if skills else [])
        self.robot_id = robot_id
        self.pos = pos
        self.capacity = capacity
        self.products = []
        self.backlog = []
        self.incoming_orders = []
        self.strict_order_matching = strict_order_matching
        
        #robot.strict_order_matching = False  # for flexible

    def initialize_aas(self):
        self.aas_id = initialize_holon_aas(name=f"RobotHolon_{self.robot_id}", level="Robot", children=[])

    def receive_product(self, product):
        self.products.append(product)
        self.model.log(f"RobotHolon {self.robot_id} received Product {product.product_id}")


        
    def add_product(self, product):
        if len(self.products) >= self.capacity:
            self.model.log(f"RobotHolon {self.robot_id} at capacity.")
            return False
        self.products.append(product)
        product.current_location = self
        self.model.log(f"RobotHolon {self.robot_id} received Product {product.product_id}.")
        return True

    def step(self):
        if self.incoming_orders:
            self.backlog.extend(self.incoming_orders)
            self.incoming_orders.clear()

        if not self.products:
            self.model.log(f"RobotHolon {self.robot_id} waiting for product.")
            return
        if not self.backlog:
            self.model.log(f"RobotHolon {self.robot_id} idle.")
            return

        # Strict vs. FIFO
        if self.strict_order_matching:
            order = self.backlog[0]
            if order.product not in self.products:
                self.model.log(f"Product {order.product.product_id} not yet delivered for Order {order.order_id}.")
                return
        else:
            order = None
            for o in self.backlog:
                if o.product in self.products:
                    order = o
                    break
            if not order:
                self.model.log(f" No matching product found for any order at RobotHolon {self.robot_id}.")
                return

        # Process
        order.steps_remaining -= 1
        self.model.log(f"RobotHolon {self.robot_id} processing Order {order.order_id} (steps: {order.steps_remaining}).")

        if order.steps_remaining <= 0:
            self.backlog.remove(order)
            self.products.remove(order.product)
            order.advance()
            self.model.log(f"Order {order.order_id} completed at RobotHolon {self.robot_id}.")

            append_resource_log_entry2(
                aas_id=self.aas_id,
                log_type="OrderCompleted",
                description=f"Order {order.order_id} completed at RobotHolon {self.robot_id}",
                order=order
            )

            order.product.mark_order_completed(order, self)
            if not any(o.product == order.product for o in self.backlog):
                next_holon = order.product.get_next_target()
                if next_holon:
                    self.model.log(f"AGV transferring Product {order.product.product_id} to RobotHolon {next_holon.robot_id}.")
                    self.model.request_product_transfer(order.product, next_holon)
                else:
                    self.model.log(f"Product {order.product.product_id} fully processed.")


class AGVAgent(Agent):
    def __init__(self, model, pos):
        super().__init__(model)
        self.pos = pos
        self.target_robot = None
        self.product = None
        self.route = []
        self.current_location = None  

    def step(self):
        if self.product:
            # get route
            if not self.route:
                next_target = self.product.get_next_target()
                if next_target:
                    self.route = self.calculate_route(self.current_location, next_target)
                    self.model.log(
                        f"AGV {self.unique_id} calculated route: " +
                        " → ".join([
                            f"{r.__class__.__name__}_{getattr(r, 'robot_id', getattr(r, 'aas_id', '?'))}"
                            for r in self.route if r is not None
                        ])
                    )

            # are you at first point already?
            if self.route and self.route[0] == self.current_location:
                if len(self.route) == 1:
                    
                    self.current_location = self.route[0]
                    if isinstance(self.current_location, RobotHolon):
                        self.current_location.receive_product(self.product)
                        self.model.log(f"AGV {self.unique_id} delivered Product {self.product.product_id} to RobotHolon {self.current_location.robot_id}")
                    self.product = None
                    self.route = []
                    return  
                else:
                    self.route.pop(0)

            # Step 3: step to target 1 by 1 
            if self.route:
                next_stop = self.route[0]
                self.move_toward(next_stop.pos)
                self.model.log(
                    f"AGV {self.unique_id} moving toward {next_stop.__class__.__name__} at {next_stop.pos} (current pos: {self.pos})"
                )

                if self.pos == next_stop.pos:
                    self.current_location = next_stop

                    if len(self.route) == 1:
                        if isinstance(self.current_location, RobotHolon):
                            self.current_location.receive_product(self.product)
                            self.model.log(f"AGV {self.unique_id} delivered Product {self.product.product_id} to RobotHolon {self.current_location.robot_id}")
                        self.product = None
                        self.route = []
                    else:
                        self.route.pop(0)

                    self.model.log(f"AGV {self.unique_id} arrived at {self.current_location.__class__.__name__}")



    def calculate_route(self, current, target):
        route = []
        if current == target:
            return [target]

        current_station = getattr(current, "parent", None)
        target_station = getattr(target, "parent", None)

        if current_station == target_station:
            route = [current, target]
        else:
            # tbd. for stationb to station
            if current_station:
                route = [current, current_station]
            else:
                route = [current]
        return route

    def move_toward(self, target_pos):
        x, y = self.pos
        tx, ty = target_pos
        dx = 1 if tx > x else -1 if tx < x else 0
        dy = 1 if ty > y else -1 if ty < y else 0

        new_pos = (x + dx, y + dy)
        self.model.grid.move_agent(self, new_pos)
        self.pos = new_pos

# -------------------------------
# MAIN CONFIG
# -------------------------------
class FlexibleJobShopModel(Model):
    def __init__(self, orders, robot_info, num_agv=4, seed=None):
        super().__init__(seed=seed)
        self.orders = orders
        self.robot_positions = {rid: info['pos'] for rid, info in robot_info.items()}
        self.transfer_requests = []
        self.log_messages = []
        self.grid = MultiGrid(10, 10, torus=False)
        self.datacollector = DataCollector()
        self.products = []



        for i in range(10):
           

            #aas_id = create_aas_for_holon(name=product_name, level="Product", children=[])
         
            product = Product(product_id=i, model=self, aas_id=None)
            product_name = f"Product_{product.product_id}"
            product.aas_id = f"urn:aas:{product_name}"  # match what's actually being created
            

            # Robot level orders (deepest level)
            welding = Order(order_id=f"{i}01", level="Robot", required_skill="welding", process_time=8, product=product)
            assembly = Order(order_id=f"{i}02", level="Robot", required_skill="assembly", process_time=8, product=product)
            painting = Order(order_id=f"{i}03", level="Robot", required_skill="painting", process_time=8, product=product)
            robot_orders = [welding, assembly, painting]

            # Station level
            station_order = Order(
                order_id=f"{i}00", level="Station", required_skill="frontend", process_time=5,
                suborders=robot_orders, product=product
            )

            # Factory level
            factory_order = Order(
                order_id=f"{i}F", level="Factory", required_skill="management", process_time=1,
                suborders=[station_order], product=product
            )

            product.orders.append(factory_order)

            product_name = f"Product_{product.product_id}"
            #aas_id = create_aas_for_holon(name=product_name, level="Product", children=[])
            #product.aas_id = f"https://template.smartfactory.de/shells/{product_name}"

            initialize_product_aas_and_submodels(product)

            self.products.append(product)
            self.log(f"Product {product.product_id} created with 3-level order hierarchy and AAS {product.aas_id}")


        self.robot_agents = []
        for rid, info in robot_info.items():
            robot = RobotHolon(model=self, robot_id=rid, pos=info['pos'], capacity=100, skills=info.get('skills', []))
            robot.strict_order_matching = False  # for strict
            robot.initialize_aas()
            self.grid.place_agent(robot, info['pos'])
            self.robot_agents.append(robot)

        self.station = StationHolon(model=self)
        self.station.children = self.robot_agents
        self.station.initialize_aas([f"RobotHolon_{r.robot_id}" for r in self.robot_agents])

        self.factory = FactoryHolon(model=self)
        self.factory.children = [self.station]
        self.factory.initialize_aas(["StationHolon_1"])
        self.factory.products = self.products

        self.agv = AGVAgent(model=self, pos=(0, 0))
        self.grid.place_agent(self.agv, (0, 0))
        self.robot_positions = {robot.robot_id: robot.pos for robot in self.robot_agents}
        self.custom_agents = [self.factory, self.station] + self.robot_agents + [self.agv]

    def log(self, message):
        print(message)
        self.log_messages.append(message)

    def request_product_transfer(self, product, next_holon):
        self.transfer_requests.append((product, next_holon))
        self.log(f"Request: Move Product {product.product_id} to {next_holon.__class__.__name__} {next_holon.robot_id}.")

    def step(self):
        self.factory.step()
        self.station.step()
        for agent in self.custom_agents:
            if isinstance(agent, AGVAgent) and agent.product is None and self.transfer_requests:
                product, target_robot = self.transfer_requests.pop(0)
                if isinstance(target_robot, RobotHolon):
                    agent.product = product
                    agent.target_robot = target_robot
                    product.current_location = "AGV"
                    self.log(f"AGV assigned to transport Product {product.product_id} to RobotHolon {target_robot.robot_id}.")
        for robot in self.robot_agents:
            robot.step()
        self.agv.step()

if __name__ == '__main__':
    clear_all_aas_from_registry()
    clear_all_submodels()

    robot_info = {
        1: {"pos": (2, 2), "skills": ["welding"]},
        2: {"pos": (5, 2), "skills": ["assembly"]},
        3: {"pos": (8, 2), "skills": ["painting"]}
    }
    model = FlexibleJobShopModel(orders=[], robot_info=robot_info, num_agv=4)
    for step in range(55):
        print(f"\n--- Step {step} ---")
        model.step()

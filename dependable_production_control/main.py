from mesa import Agent, Model
from mesa.space import MultiGrid
from datetime import datetime
import uuid
import pandas as pd
import matplotlib.pyplot as plt

from aas_initializer import (
    initialize_holon_aas,
    clear_all_aas_from_registry,
    clear_all_submodels,
    append_resource_log_entry2,
    mark_order_completed_in_production_plan,
    initialize_product_aas_and_submodels
)

class MessageBus:
    def __init__(self):
        self.subscribers = {}
        self.queue = []

    def subscribe(self, topic, agent):
        self.subscribers.setdefault(topic, []).append(agent)

    def publish(self, topic, message):
        self.queue.append((topic, message))

    def deliver(self):
        for topic, msg in self.queue:
            for agent in self.subscribers.get(topic, []):
                agent.inbox.append(msg)
        self.queue.clear()

class Order:
    def __init__(self, order_id, level, required_skill, process_time, suborders=None, product=None):
        self.order_id = order_id
        self.level = level
        self.required_skill = required_skill
        self.process_time = process_time
        self.steps_remaining = process_time
        self.suborders = suborders or []
        self.product = product
        self.current_index = 0
        self.allocated = None
        self.planned_start = None
        self.planned_end = None
        self.actual_start = None
        self.actual_end = None

    def advance(self):
        if self.suborders and self.current_index is not None and self.current_index < len(self.suborders) - 1:
            self.current_index += 1
        else:
            self.current_index = None

class Product:
    def __init__(self, product_id, model, aas_id):
        self.product_id = product_id
        self.model = model
        self.aas_id = aas_id
        self.orders = []
        self.current_location = None

    def mark_order_completed(self, order, location):
        mark_order_completed_in_production_plan(self, order, location)

    def get_next_target(self):
        def dfs(o):
            if o.level == "Robot" and o.steps_remaining > 0 and o.allocated:
                return o.allocated
            for so in o.suborders:
                res = dfs(so)
                if res:
                    return res
            return None
        for root in self.orders:
            tgt = dfs(root)
            if tgt:
                return tgt
        return None

class Holon(Agent):
    def __init__(self, model, level, skills, topic):
        super().__init__(model)
        self.level = level
        self.skills = skills
        self.topic = topic
        self.inbox = []
        self.aas_id = None
        self.reference_trajectory = []
        model.bus.subscribe(topic, self)

    def publish(self, receiver_topic, payload):
        msg = {
            'messageId': str(uuid.uuid4()),
            'timestamp': datetime.utcnow().isoformat() + 'Z',
            'senderAAS': self.aas_id,
            'receiverTopic': receiver_topic,
            'payload': payload
        }
        self.model.bus.publish(receiver_topic, msg)

    def process_inbox(self):
        raise NotImplementedError

    def step(self):
        self.process_inbox()
class FactoryHolon(Holon):
    def __init__(self, model, topic='i40/factory/1'):
        super().__init__(model, 'Factory', ['Factory'], topic)
        self.created = False
        self.products = []

    def initialize_aas(self, children):
        self.aas_id = initialize_holon_aas('FactoryHolon_1', 'Factory', children)

    def process_inbox(self):
        self.inbox.clear()

    def step(self):
        super().step()
        if not self.created:
            orders = []
            for p in self.products:
                s = p.orders[0].suborders[0]  # station-level order
                orders.append({'order_id': s.order_id, 'product_id': p.product_id})

                reference_trajectory = []
            step_size = 60
            for i, o in enumerate(orders):
                start = i * step_size
                end = start + step_size
                reference_trajectory.append((o['order_id'], start, end))
            self.publish(self.model.station.topic, {'type': 'ExecutionRequest', 'orders': orders})
            self.model.log("FactoryHolon sent initial ExecutionRequest to StationHolon")
            self.created = True

class StationHolon(Holon):
    def __init__(self, model, topic='i40/station/StationHolon_1'):
        super().__init__(model, 'Station', ['frontend'], topic)
        self.children = []
        self.availabilities = {}
        self.pos = None
        self.reference_trajectory = {}

    def initialize_aas(self, children):
        self.aas_id = initialize_holon_aas('StationHolon_1', 'Station', children)

    def process_inbox(self):
        for msg in self.inbox:
            typ = msg['payload'].get('type')
            if typ == 'ExecutionRequest':
                self.availabilities = {r: r.available_at for r in self.children}
                self.reference_trajectory = {r.robot_id: [] for r in self.children}

                all_robot_orders = []
                for data in msg['payload']['orders']:
                    p = self.model.products[data['product_id']]
                    station_order = self._find_order(p.orders[0], data['order_id'])
                    ordered = self._get_sequenced_robot_orders(station_order)
                    all_robot_orders.extend(ordered)

                self.schedule_all(all_robot_orders)

            elif typ == 'TaskCompleted':
                pd = msg['payload']
                p = self.model.products[pd['product_id']]
                s = self._find_order(p.orders[0], pd['order_id'])
                s.advance()

            elif typ == 'ProductionLogUpdate':
                log = msg['payload']['log']
                self.model.log(f"[StationHolon] Received log from Robot: {log}")
        self.inbox.clear()

    def _find_order(self, order, oid):
        if order.order_id == oid:
            return order
        for so in order.suborders:
            found = self._find_order(so, oid)
            if found:
                return found
        return None

    def _get_sequenced_robot_orders(self, station_order):
        """Extract robot orders in welding -> assembly -> painting order per product."""
        priority = {'welding': 0, 'assembly': 1, 'painting': 2}
        robot_orders = [o for o in station_order.suborders if o.level == 'Robot']
        return sorted(robot_orders, key=lambda x: priority.get(x.required_skill, 999))

    def schedule_all(self, robot_orders):
        scheduled_by_product = {}
        for child in robot_orders:
            if child.allocated:
                continue

            product_id = child.product.product_id
            if product_id not in scheduled_by_product:
                scheduled_by_product[product_id] = []

            prev_orders = scheduled_by_product[product_id]
            last_end = max((o.planned_end for o in prev_orders), default=self.model.current_step)

            eligible = [r for r in self.children if child.required_skill in r.skills]
            if not eligible:
                self.model.log(f"No robot for skill {child.required_skill}")
                continue

            best, best_offset = None, None
            for r in eligible:
                backlog_offset = self.availabilities[r]
                tt = abs(self.pos[0] - r.pos[0]) + abs(self.pos[1] - r.pos[1])
                start_offset = max(backlog_offset, tt, last_end - self.model.current_step)
                if best_offset is None or start_offset < best_offset:
                    best_offset, best = start_offset, r

            chosen = best
            start = self.model.current_step + best_offset
            finish = start + child.process_time
            self.availabilities[chosen] = best_offset + child.process_time
            child.allocated = chosen
            child.planned_start = start
            child.planned_end = finish
            scheduled_by_product[product_id].append(child)

            # If product hasn't been placed yet, place it at the first robot
            if child.product.current_location is None:
                child.product.current_location = chosen
                chosen.receive_product(child.product)

            self.model.log(f"Scheduled {child.order_id} (Product {child.product.product_id}) on Robot {chosen.robot_id} at [{start}, {finish})")

            self.model.task_schedule.append({
                'Robot': chosen.robot_id,
                'Product': child.product.product_id,
                'Order': child.order_id,
                'Start': start,
                'Finish': finish
            })

            self.reference_trajectory.setdefault(chosen.robot_id, []).append((child.order_id, start, finish))
            payload = {
                'type': 'ExecutionRequest',
                'orders': [{'order_id': child.order_id, 'product_id': child.product.product_id}],
                'reference_trajectory': self.reference_trajectory[chosen.robot_id]
            }
            self.publish(chosen.topic, payload)

        self._plot_schedule_gantt()

    def _plot_schedule_gantt(self):
        import matplotlib.pyplot as plt
        import matplotlib.patches as mpatches
        import pandas as pd
        df = pd.DataFrame(self.model.task_schedule)
        if df.empty:
            print("No tasks to plot yet.")
            return
        unique_products = df['Product'].unique()
        colors = {pid: f'C{idx % 10}' for idx, pid in enumerate(unique_products)}
        fig, ax = plt.subplots(figsize=(12, 5))
        for _, row in df.iterrows():
            ax.barh(y=row['Robot'], width=row['Finish'] - row['Start'], left=row['Start'],
                    color=colors[row['Product']], edgecolor='black')
            ax.text((row['Start'] + row['Finish']) / 2, row['Robot'], f"{row['Order']}",
                    ha='center', va='center', color='white', fontsize=8)
        ax.set_xlabel('Time')
        ax.set_ylabel('Robot ID')
        ax.set_yticks(sorted(df['Robot'].unique()))
        ax.set_title('Gantt Chart: Initial Schedule')
        ax.grid(True, axis='x', linestyle='--', alpha=0.5)
        patches = [mpatches.Patch(color=colors[pid], label=f'Product {pid}') for pid in unique_products]
        ax.legend(handles=patches, title='Product', bbox_to_anchor=(1.05, 1), loc='upper left')
        plt.tight_layout()
        plt.show()


class RobotHolon(Holon):
    def __init__(self, model, robot_id, pos, skills):
        super().__init__(model, 'Robot', skills, f"i40/robot/{robot_id}")
        self.robot_id = robot_id
        self.pos = pos
        self.backlog = []
        self.trajectory_real = []

    @property
    def available_at(self):
        return sum(o.steps_remaining for o in self.backlog)

    def initialize_aas(self):
        self.aas_id = initialize_holon_aas(f"RobotHolon_{self.robot_id}", 'Robot', [])

    def receive_product(self, product):
        self.model.log(f"RobotHolon {self.robot_id} received Product {product.product_id}")

    def _find_order(self, order, oid):
        if order.order_id == oid:
            return order
        for so in order.suborders:
            found = self._find_order(so, oid)
            if found:
                return found
        return None

    def process_inbox(self):
        for msg in self.inbox:
            if msg['payload']['type'] == 'ExecutionRequest':
                for od in msg['payload']['orders']:
                    p = self.model.products[od['product_id']]
                    order = self._find_order(p.orders[0], od['order_id'])
                    self.backlog.append(order)
                    self.model.log(f"Enqueued {order.order_id} on Robot {self.robot_id}")
        
            # Store reference trajectory
            if 'reference_trajectory' in msg['payload']:
                self.trajectory_reference = msg['payload']['reference_trajectory']
        self.inbox.clear()

    def step(self):
        super().step()
        if not self.backlog:
            return

        order = self.backlog[0]

        if order.product.current_location is not self:
            return

        if order.actual_start is None:
            order.actual_start = self.model.current_step
            self.model.log(
                f"[Start]    Robot {self.robot_id} | Product {order.product.product_id:02} | "
                f"Order {order.order_id} ({order.required_skill}) @ step {order.actual_start}"
            )
        else:
            self.model.log(
                f"[Working]  Robot {self.robot_id} | Order {order.order_id} | "
                f"Remaining: {order.steps_remaining} steps"
            )
        order.steps_remaining -= 1

        if order.steps_remaining <= 0:
            order.actual_end = self.model.current_step + 1
            self.trajectory_real.append((order.order_id, order.actual_start, order.actual_end))
            self.backlog.pop(0)
            self.model.log(f"Completed {order.order_id} on Robot {self.robot_id}")

            append_resource_log_entry2(self.aas_id, 'OrderCompleted', f"{order.order_id} done", order)
            order.product.mark_order_completed(order, self)

            root = order.product.orders[0]
            parent = next((so for so in root.suborders if order in so.suborders), None)

            # Send TaskCompleted back to station if this is part of a station order
            if parent:
                self.publish(self.model.station.topic, {'type': 'TaskCompleted', 'order_id': parent.order_id, 'product_id': order.product.product_id})

            # Log update for dashboard
            log_entry = {
                'LogType': 'OrderCompleted',
                'Description': f"{order.order_id} done",
                'OrderId': order.order_id,
                'Skill': order.required_skill,
                'ProcessTime': order.process_time,
                'Timestamp': datetime.utcnow().isoformat() + 'Z'
            }
            self.publish(self.model.station.topic, {
                'type': 'ProductionLogUpdate',
                'log': log_entry
            })

            # 🚗 AGV transfer for next step
            next_target = order.product.get_next_target()
            if next_target and next_target is not self:
                self.model.request_product_transfer(order.product, next_target.topic)

                                                
# AGVAgent unchang
class AGVAgent(Agent):
    def __init__(self, model, pos=(0,0)):
        super().__init__(model)
        self.pos = pos
        self.product = None
        self.target = None
        self.route = []

    def step(self):
        if self.product is None and self.model.transfer_requests:
            prod, recv = self.model.transfer_requests.pop(0)
            self.target = next(a for a in self.model.schedule if getattr(a,'topic',None)==recv)
            self.product = prod
            self.route=[self.target]
        if self.product and self.route:
            hop = self.route[0]
            self.move_toward(hop.pos)
            if self.pos == hop.pos:
                hop.receive_product(self.product)
                self.product.current_location = hop
                self.product = None
                self.route = []

    def move_toward(self, tpos):
        x,y=self.pos; tx,ty=tpos
        dx=1 if tx>x else -1 if tx<x else 0
        dy=1 if ty>y else -1 if ty<y else 0
        self.model.grid.move_agent(self, (x+dx, y+dy))
        self.pos = (x+dx, y+dy)
        self.model.log(f"[AGV] Moving to {self.pos} → Target: {tpos}")
class FlexibleJobShopModel(Model):
    def __init__(self, robot_info, num_products=10):
        super().__init__()
        clear_all_aas_from_registry()
        clear_all_submodels()
        self.bus = MessageBus()
        self.grid = MultiGrid(30, 30, False)
        self.transfer_requests = []
        self.products = []
        self.schedule = []
        self.current_step = 0
        self.task_schedule = []

        for i in range(num_products):
            p = Product(i, self, None)
            w = Order(f"{i}01", 'Robot', 'welding', 8, [], p)
            a = Order(f"{i}02", 'Robot', 'assembly', 8, [], p)
            p_ = Order(f"{i}03", 'Robot', 'painting', 8, [], p)
            s = Order(f"{i}00", 'Station', 'frontend', 5, [w, a, p_], p)
            f_ = Order(f"{i}F", 'Factory', 'management', 1, [s], p)
            p.orders = [f_]
            initialize_product_aas_and_submodels(p)
            self.products.append(p)

        self.factory = FactoryHolon(self)
        self.factory.initialize_aas(["i40/station/StationHolon_1"])
        self.factory.products = self.products
        self.schedule.append(self.factory)

        self.station = StationHolon(self)
        self.station.initialize_aas([f"i40/robot/{rid}" for rid in robot_info])
        self.station.pos = (0, 0)
        self.grid.place_agent(self.station, self.station.pos)
        self.schedule.append(self.station)

        for rid, info in robot_info.items():
            r = RobotHolon(self, rid, info['pos'], info.get('skills', []))
            r.initialize_aas()
            self.grid.place_agent(r, info['pos'])
            self.schedule.append(r)
            self.station.children.append(r)

        self.agv = AGVAgent(self)
        self.grid.place_agent(self.agv, (0, 0))
        self.schedule.append(self.agv)

    def request_product_transfer(self, product, recv):
        self.transfer_requests.append((product, recv))

    def log(self, msg):
        print(msg)

    def step(self):
        self.bus.deliver()
        for agent in list(self.schedule):
            agent.step()
        self.current_step += 1


if __name__ == '__main__':
    robot_info = {
        1: {'pos': (2, 2), 'skills': ['welding', 'assembly']},
        2: {'pos': (5, 2), 'skills': ['assembly', 'painting']},
        3: {'pos': (8, 2), 'skills': ['welding', 'painting']}
    }
    num_products = 10
    model = FlexibleJobShopModel(robot_info, num_products=num_products)
    for _ in range(200):
        model.step()

    # Build DataFrames
    ref_df = pd.DataFrame(model.task_schedule)
    real_records = []
    for agent in model.schedule:
        if hasattr(agent, 'trajectory_real'):
            for oid, st, fin in agent.trajectory_real:
                real_records.append({'Robot': agent.robot_id, 'Start': st, 'Finish': fin})
    real_df = pd.DataFrame(real_records)

    # Subplot per robot: compare reference vs real throughput
    robots = sorted(set(ref_df['Robot'].unique()).intersection(real_df['Robot'].unique()))
    max_t = max(ref_df['Finish'].max(), real_df['Finish'].max())
    time_idx = range(max_t + 1)
    # prepare counts
    fig, axes = plt.subplots(len(robots), 1, figsize=(12, 4*len(robots)), sharex=True)
    if len(robots) == 1:
        axes = [axes]
    for ax, r in zip(axes, robots):
        # reference
        ref_series = pd.Series(0, index=time_idx)
        for f in ref_df[ref_df['Robot'] == r]['Finish']:
            ref_series.at[f] += 1
        ref_counts = ref_series.rolling(window=60, min_periods=1).sum()
        # real
        real_series = pd.Series(0, index=time_idx)
        for f in real_df[real_df['Robot'] == r]['Finish']:
            real_series.at[f] += 1
        real_counts = real_series.rolling(window=60, min_periods=1).sum()
        ax.plot(ref_counts.index, ref_counts.values, label='Reference')
        ax.plot(real_counts.index, real_counts.values, '--', label='Real')
        ax.set_title(f'Robot {r} Throughput (60-step MA)')
        ax.set_ylabel('Orders/60 steps')
        ax.legend()
    axes[-1].set_xlabel('Time Step')
    plt.tight_layout()
    plt.show()

    # Send throughput updates
    for r in robots:
        payload = {
            'type': 'ThroughputUpdate',
            'ref_counts': ref_counts.tolist(),
            'real_counts': real_counts.tolist()
        }
        topic = f"i40/robot/{r}"
        model.bus.publish(topic, {
            'messageId': str(uuid.uuid4()),
            'timestamp': datetime.utcnow().isoformat() + 'Z',
            'senderAAS': None,
            'receiverTopic': topic,
            'payload': payload
        })
    model.bus.deliver()

# Flexible Job Shop Simulation with AAS Integration

This repository contains a **flexible job shop simulation** featuring different types of holonic agents (Factory, Station, Robot), **AGVs** (Automated Guided Vehicles), and **Products** guided by a hierarchical production plan. The simulation integrates with the **Asset Administration Shell (AAS)** using submodels such as `ProductionPlan` and `ProductionLog`.

---

## Project Structure

```
.
├── main.py                      # Main simulation logic using Mesa
├── aas_initializer.py          # Utility functions for AAS setup & submodel operations
├── Submodel_ProductionPlan.json
├── Submodel_ProductionLog.json
├── README.md
├──AAS/
│   └──docker-compose.yaml
│   └──README.md
│   └──aas/  ##empty
│   └──basyx/
│	   └──aas config files
│   └──mosquitto/
│	   └──config/
│ 		 	└──mosquitto.conf

```

---

## Requirements

- Python 3.9+

- Dependencies:

  ```bash
  pip install mesa matplotlib requests
  ```

- AAS environment (s. below for setup)

---

## AAS Environment (via Docker Compose from BaSyx website)

Launch a full BaSyx-based AAS environment using Docker on these ports:

- AAS Server (`8081`)
- AAS Registry (`8082`)
- Submodel Registry (`8083`)
- AAS Discovery (`8084`)
- Dashboard API (`8085`)
- Web UI (`3000`)

### Run the Environment

```bash
docker-compose up -d
```

### This launches the following services

- AAS Environment: [http://localhost:8081](http://localhost:8081)
- AAS Registry: [http://localhost:8082](http://localhost:8082)
- Submodel Registry: [http://localhost:8083](http://localhost:8083)
- Web UI: [http://localhost:3000](http://localhost:3000)


---

## Run the Simulation

```bash
python main.py
```

This will:

1. Clear existing AAS and submodels from the registry.
2. Create new AAS instances for factory, stations, robots, and products.
3. Upload submodels (`ProductionPlan` and `ProductionLog`).
4. Run the simulation for 55 steps.

---

## Core Components

| Component            | Description                                                           |
| -------------------- | --------------------------------------------------------------------- |
| `Product`            | Represents a unit in production, containing a hierarchical order tree |
| `Order`              | Supports suborders to form multilevel production plans                |
| `RobotHolon`         | Executes robot-level tasks based on required skillsets                |
| `StationHolon`       | Distributes suborders to appropriate robots                           |
| `FactoryHolon`       | Initializes orders and forwards them to stations                      |
| `AGVAgent`           | Manages product transport across robot holons                         |
| `aas_initializer.py` | Manages AAS creation, submodel uploads, and dynamic updates           |

---

## Submodels

| Submodel              | Purpose                                                              |
| --------------------- | -------------------------------------------------------------------- |
| `ProductionPlan.json` | Defines the sequence of steps and resources for each product         |
| `ProductionLog.json`  | Logs activities, resource usage, faults, and events during execution |

---

##  Console Output

```
AGV 1 moving toward RobotHolon_3 at (8, 2)
RobotHolon 3 processing Order 003 (steps: 3)
Log entry added to ProductionLog
```

---

# Distributed Monitoring System as Multi-Agent System for Fault Detection

##  Quick Installation Guide

This MAS (Multi-Agent System) is built using the [SARL language](http://www.sarl.io), which is based on Java.

###  Prerequisites

1. Install **Java** – preferably version 11. [OpenJDK Downloads](https://openjdk.org/install/)
2. Install the **SARL IDE** from [sarl.io](http://www.sarl.io/download/index.html)
3. Clone this project to your local machine
4. In SARL IDE:  
   `File → Import → Maven → Existing Maven Project`

### Common Issues & Fixes

**Compilation errors?**

- Ensure you're using **Java 11**.
- Switch Java version via:  
  _Right-click project → Build Path → Configure Build Path → Libraries → Edit... → Select Java 11_

**Missing dependencies (e.g. BaSyx client)?**

- This project depends on the [BaSyx v3 client libraries](https://github.com/dfkibasys/basyx-v3-client-libraries/tree/master)
- Fix it by:
  - Running a Maven update:  
    _Right click on project → Maven → Update Project_
  - Or manually installing the BaSyx library into your local `.m2` repository


## AAS Environment Setup (via Docker)

This system interacts with BaSyx Asset Administration Shells (AAS).  
You can spin up a full BaSyx environment using Docker Compose.

### Start the Environment

Make sure Docker is installed. Then run:

```bash
docker-compose up -d
```

### Default Services and Ports

Once started, these services are available:

- AAS Server → http://localhost:8081  
- AAS Registry → http://localhost:8082  
- Submodel Registry → http://localhost:8083  
- AAS Discovery → http://localhost:8084  
- Dashboard API → http://localhost:8085  
- Web UI → http://localhost:3000

### Load the AAS into the Environment

After starting the AAS environment, you need to upload the required AAS from the provided folder in this repository to the running BaSyx AAS Server at `http://localhost:8081`.

You can use the BaSyx Web UI or API endpoints to upload and register the AAS.

## System Configuration

You can switch the fault detection mode in `ResourceAgent`:

```java
var detectionMode : FaultDetectionMode = FaultDetectionMode.HETERARCHICAL;
```

Available options:

- `HETERARCHICAL` – Agents exchange and classify faults themselves
- `HIERARCHICAL` – Agents report to a central agent which classifies the faults


## Running the MAS

1. Open the `AgentSpawner` class
2. Right-click → **Run As → SARL Agent**

The system will automatically spawn agents and begin processing.

---
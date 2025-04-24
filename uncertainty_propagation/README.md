# Distributed Monitoring System as Multi-Agent System for Uncertainty 

This Multi-Agent System defined in Janus SARL belongs to the paper "A. Bernhard et al. Towards exchanging monitoring data in flexible and
distributed factory organization structures". It is used as open source use case, which fuses uncertainties throughout a multi-agent system.

Briefly described a product holon wants to produce a sample product (battery pack) and requests the fused uncertainty to produce this battery pack. The factory which should produce this battery pack is represented by the resource holon. This holon is hierarchically split into three levels. On the top level the SFLab represents the factory or more precisely the research lab at SmartFactory-KL. This lab consists of a work station called assembly station and an AMR. At the beginning of the process, the AMR contains a battery case, drives to the assembly island and hands over the product to the assembly island to the transport system of the assembly island. This transport system transports the battery case to the assembly station which assembles batteries into the battery case and this sample process ends.

The product and the SFLab agents are spawned by an initial agent spawner. All other resource agents inside the SFLab are spawned by the containing parent agent based on the given bill-of-material inside the configuration of the parent agent.

The configuration of each agent is implemented by Asset Administration Shells (AAS). The shells contain certain submodels based on mostly standardized IDTA templates (e.g. nameplate, bill-of-material, capabilities) and newly in the paper described submodels (ProductionPlan and ProductionLog). In order to use AAS, this project uses a Basyx environment to register and host all AAS. For this paper, a local Basyx server and an open source Basyx client is used (see the installation below). 

## Quick Installation Guide

This MAS is implemented in the **[SARL language](http://www.sarl.io/)**, which is based on Java. To get started:

### 1. Install Java

Download and install **Java 11** (e.g., from the [OpenJDK website](https://openjdk.org/install/)).

### 2. Install SARL IDE

Download and install the [SARL IDE](http://www.sarl.io/download/index.html).

### 3. Clone the Project

```bash
git clone https://github.com/your-username/your-repo-name.git
```

### 4. Import the Project into SARL IDE

- Open **SARL IDE**
- Go to `File -> Import`
- Choose `SARL -> Existing SARL Maven Project`
- Select the cloned project directory

### 5. Set Java Version to 11 (if compilation errors occur)

If you see many compilation errors after import, it’s likely due to an incompatible Java version.

- Right-click on the project
- Navigate to:  
  `Build Path -> Configure Build Path -> Libraries`
- Select `JRE System Library` and click `Edit...`
- Choose a **Java 11** version from your installed JDKs

### 6. Import Basyx Client Dependency

A compilation error in `pom.xml` may appear due to a missing **Basyx client** library.

#### Option A: Automatic (Recommended)

If **Maven** is installed and configured:

- Right-click on the project  
- Go to `Maven -> Update Project`

#### Option B: Manual Installation

If Maven update doesn’t resolve the issue:

- Download the required Basyx client library from:  
  [https://github.com/dfkibasys/basyx-v3-client-libraries](https://github.com/dfkibasys/basyx-v3-client-libraries/tree/master)
- Manually install it into your local `.m2` Maven repository
- You can also try fetching it from **Maven Central**

### 7. Setup Basyx Server

Now the Basyx Client should print an exception that no Basyx Server can be found. You can either connect to your own Basyx server or use a preconfigured setup. All configuration details are provided below.

#### Option A: Use Your Own Basyx Server
If you have your own Basyx Server, follow these steps:
1. Ensure Version Compatibility: Make sure your server version is **newer than** `2.0.0-SNAPSHOT-2ddae04`
2. Required Server Components:
- **AAS Registry**
- **Submodel Registry**
- **AAS Repository**
3. Configure Client URLs:
Update `src/main/java/helper/AASHelper.java` and Modify the URLs at the top of the class. Replace these with the actual endpoints of your Basyx server.

```java
private final static String AASRegistryURL = "http://localhost:8082";
private final static String RepositoryURL = "http://localhost:8081";
private final static String SubmodelRegistryURL = "http://localhost:8083";
```

#### Option B: Set Up a Local Basyx Server
If you don't have a Basyx Server, you can configure your own local setup. Process:
1. Download the Server:
Go to the official Basyx getting started page:

👉 [https://basyx.org/get-started/introduction](https://basyx.org/get-started/introduction)

Download the ZIP file provided.

2. Include Required Services

During the setup process, **make sure to include** the following in the configuration:
- **AAS Registry**
- **Submodel Registry**

These services are essential for the client to function properly.

3. Edit Configuration

Once you’ve downloaded the server:

- Open the `docker-compose.yml` file from the extracted folder.
- Identify the ports and endpoints used for:
  - AAS Registry
  - AAS Repository
  - Submodel Registry

4. Update Java Client Configuration
Open the Java file: `src/main/java/helper/AASHelper.java`
Update the following URL values to match the configuration from your Docker Compose setup. Replace these with the correct ports/URLs defined in your compose file.

```java
private final static String AASRegistryURL = "http://localhost:8082";
private final static String RepositoryURL = "http://localhost:8081";
private final static String SubmodelRegistryURL = "http://localhost:8083";
```

5. Run the server
Make sure **Docker** is installed and running on your machine.

To start the Basyx Server, open a terminal in the directory where the `docker-compose.yml` file is located and run:

```bash
docker compose up
```

This will launch all configured services, including the AAS Registry, Submodel Registry, and Repository.

#### Option C: Use Preconfigured Basyx Docker Compose Setup
If you prefer a ready-to-use setup, the project includes a preconfigured Basyx server using Docker Compose.

1. Locate the Configuration Folder

Navigate to `basyx-config` in your project.

This folder contains a complete `docker-compose.yml` file with all required services:
- AAS Registry
- AAS Repository
- Submodel Registry

2. Start the Server

Ensure Docker is installed on your machine.

Run the following command inside the `basyx-config` folder. This command will launch the full Basyx stack using the predefined configuration.

```bash
docker compose up
```

3. Add AAS files
The server expects AAS files to be placed in `basyx-config/aas/`. In this preconfigured setup, the required AAS files for this use case are already included in the aas folder. If needed, you can add or replace files here.

Once the server is up and running, your Basyx Client should connect without any issues using the default endpoint configuration.

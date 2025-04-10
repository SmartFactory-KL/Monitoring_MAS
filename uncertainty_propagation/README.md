# Distributed Monitoring System as Multi-Agent System for Uncertainty 

This Multi-Agent System defined in Janus SARL belongs to the paper "A. Bernhard et al. Towards exchanging monitoring data in flexible and
distributed factory organization structures". It is used as open source use case, which fuses uncertainties throughout a multi-agent system.

Briefly described a product holon wants to produce a sample product (battery pack) and requests the fused uncertainty to produce this battery pack. The factory which should produce this battery pack is represented by the resource holon. This holon is hierarchically split into three levels. On the top level the SFLab represents the factory or more precisely the research lab at SmartFactory-KL. This lab consists of a work station called assembly station and an AMR. At the beginning of the process, the AMR contains a battery case, drives to the assembly island and hands over the product to the assembly island to the transport system of the assembly island. This transport system transports the battery case to the assembly station which assembles batteries into the battery case and this sample process ends.

The product and the SFLab agents are spawned by an initial agent spawner. All other resource agents inside the SFLab are spawned by the containing parent agent based on the given bill-of-material inside the configuration of the parent agent.

The configuration of each agent is implemented by Asset Administration Shells (AAS). The shells contain certain submodels based on mostly standardized IDTA templates (e.g. nameplate, bill-of-material, capabilities) and newly in the paper described submodels (ProductionPlan and ProductionLog). In order to use AAS, this project uses a Basyx environment to register and host all AAS. For this paper, a local Basyx server and an open source Basyx client is used (see the installation below). 

## Quick Installation Guide

This MAS is based on the SARL language. This language is based on java. So first you need to download download java (e.g. from the newest openjdk https://openjdk.org/install/). Then download and install the SARL IDE from http://www.sarl.io/download/index.html. Then you can clone this project into a personal repository. In SARL, you import the project from the file system (File -> Import) as SARL Maven Project.

If a lot of compilation errors occur, make sure to use java version 11 to run the project. You can switch the java version by right click on the project -> Build Path -> Configure Build Path -> Libraries -> {Your java version - e.g. JRE System Library} -> Edit... and then choose a java 11 version from the previously installed java package.

Next, a compilation error should appear in the pom.xml file. This is due to the missing Basyx client library. This project uses the open Basyx client from https://github.com/dfkibasys/basyx-v3-client-libraries/tree/master. If Maven is properly installed, a Maven update should already do the trick (Richt click on the project -> Maven -> Maven Update). Otherwise, you can manually install the library into your .m2 folder. It should be available at maven central.
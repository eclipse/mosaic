<a href="https://eclipse.org/mosaic"><p align="center"><img width="50%" src="https://raw.githubusercontent.com/eclipse/mosaic.website/main/static/img/logos/mosaic/EclipseMOSAIC-Logo-RGB-positiv.svg"></p></a>

# Eclipse MOSAIC Essentials - <br> The Simulation Suite for Connected and Automated Mobility

[![License](https://img.shields.io/badge/License-EPL%202.0-green.svg)](https://opensource.org/licenses/EPL-2.0)
![Made with java](https://img.shields.io/badge/Made%20with-Java-1f425f.svg) 
![Size](https://img.shields.io/github/repo-size/eclipse/mosaic.svg) <br>
[![Build](https://github.com/eclipse/mosaic/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/eclipse/mosaic/actions?query=branch%3Amain+workflow%3A%22Java+CI+with+Maven%22)
[![Jenkins Build](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.eclipse.org%2Fmosaic%2Fjob%2Fmosaic%2Fjob%2Fmain&label=Jenkins%20build)](https://ci.eclipse.org/mosaic/job/mosaic/)


[**Eclipse MOSAIC**](https://eclipse.org/mosaic) is a multi-scale simulation framework in the field of smart and connected mobility.
It allows coupling simulators from various domains towards a comprehensive simulation tool. 
Data exchange and time management is implemented by the Runtime Infrastructure (RTI), which is the heart of MOSAIC. Simulation models
are coupled to the RTI using HLA inspired interfaces; Each simulator is wrapped into a "Federate" object which is linked to an "Ambassador"
which is directly coupled with the RTI. Currently, the following simulators are coupled with the MOSAIC RTI:
  * MOSAIC Application (application simulation)
  * Eclipse SUMO (traffic simulation)
  * OMNeT++ (communication simulation)
  * ns-3 (communication simulation)
  * MOSAIC Cell Simulator (cellular communication simulation)
  * MOSAIC Simple Network Simulator (communication simulation)
  * MOSAIC Environment (environment and event simulation)  
  * MOSAIC Output Generator (evaluation and visualization)

The project is a Maven based multi-module project and has its child modules organized in three main categories:
  * MOSAIC RTI: modules providing the runtime infrastructure API and implementation
  * MOSAIC Libraries: mathematics, spatial, routing, utilities, communication models, and data exchange
  * MOSAIC Ambassadors: simulators and couplings which integrate simulation models to Eclipse MOSAIC

> View our website ([https://eclipse.org/mosaic](https://eclipse.org/mosaic)) for further documentation and tutorials.
## The Essential edition of Eclipse MOSAIC

This repository contains the *Essential* edition of Eclipse MOSAIC, that is, the runtime infrastructure, 
the core libraries, and various implementations of simulators or couplings to existing ones. All features 
included in this version of Eclipse MOSAIC are sufficient for most use-cases in the field of smart and connected mobility.
Additional simulators and assessment features are provided by [Fraunhofer FOKUS](https://www.fokus.fraunhofer.de/go/asct) on a commercial basis.

## Related repositories

* [Eclipse SUMO](https://github.com/eclipse/sumo) is coupled directly using the TraCI interface. We recommend using the SUMO release `1.9.2`.
* The coupling to [ns-3](https://www.nsnam.org) is realized by a federate implementation which can be found [in our MOSAIC Addons repository](https://github.com/mosaic-addons/ns3-federate). 
  We currently support ns-3 version `3.28`. 
* The coupling to [OMNeT++](https://omnetpp.org) is implemented in a very similar manner. The corresponding federate implementation can be found [in our MOSAIC Addons repository](https://github.com/mosaic-addons/omnetpp-federate). 
  We currently support OMNeT++ version `5.5` in combination with the INET framework in version `4.1`.  

## Contact

Any questions regarding Eclipse MOSAIC can be asked, discussed, and found in the [Discussions section](https://github.com/eclipse/mosaic/discussions) here at GitHub.

For further questions we are available via mosaic@fokus.fraunhofer.de

## Prerequisites

For a successful build you need the following software to be installed:

* **Maven 3.1.x** or higher.
* **Java 8 or 11** - We recommend using the [Adopt OpenJDK](https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot).
* **SUMO 1.9.2** - Additionally, the environment variable `SUMO_HOME` should be configured properly.

## Build

Eclipse MOSAIC is a Java base project using [Apache Maven](https://maven.apache.org/) for build and dependency management. 

Once installed, you can build Eclipse MOSAIC using the following command:

    mvn clean install
        
This command executes all tests as well. In order to skip test execution, the following command succeeds:

    mvn install -DskipTests
    
After building, a MOSAIC bundle including a start script and all necessary configurations is located in the `bundle\target` directory.

After extracting this bundle to an arbitrary path, Eclipse MOSAIC can be executed using:

    mosaic.sh -s HelloWorld
    mosaic.bat -s HelloWorld 
    
Besides, the simulation can also be started in your IDE using the main method in `org.eclipse.mosaic.starter.MosaicStarter`.

## License

Eclipse MOSAIC is licensed under the [Eclipse Public License Version 2](https://eclipse.org/legal/epl-v20.html).

## Contributing

Before starting with contributions, please see [CONTRIBUTING information](CONTRIBUTING.md).


# HytoraCloud

![img.png](img.png)
A minecraft CloudSystem based on the Node-Cluster-Principle using Netty-Framework that supports Multi-Root and Multi-Proxy

**This project is not fully released yet and might still contain bugs**.

## Structure

- **Node** (*as HeadNode*): <br>
  Manager of the cloud, commander of nodes 
  and is able to manage services itself<br>
<br>

- **Node** (*as SubNode*): <br>
 Only Responsible for starting & stopping of services (minecraft servers, bungee proxies), connects to the master. <br>

## Default Modules / Plugins

- see https://github.com/Lystx/HytoraCloud/tree/master/cloud-modules

## Features (✔ Completed)

- Logging API
- Screen API you can work with
- TPS of CloudSystem
- Networking with Netty
- Request/Query System
- EventSystem
- TemplateStorage download
- Node authentication
- auto start/stop services
- Lobby balancing
- Document API
- Module copy
- Node-To-Node-Logging 
- Node-To-Service-Logging & Backwards
- Setup API and implemented Setups (using Annotations)
- ChannelMessage System
- Service Deployments
- Service Screens
- Static and Dynamic services

## Planned (⚠ Not started)

- Access Screens from every Node
- Encryption for Sockets
- Template Caching on SubNodes
- Support for Sponge

## In Progress (🚧 working on)

- Node/Cluster
- Support for Velocity
- Multi-Proxy & Multi-Root
- Rest-API

## Installation
1. Download newest release (should be a .zip file)
2. Unzip the folder and execute the start file
3. Follow setup instructions
4. Create your first ServiceTasks by typing "task create"
5. Put the modules (if you want to use them) from the "modules" folder of the zip file into the generated modules folder under "local/modules/"
6. Restart the cloud if you put the modules inside the folder.
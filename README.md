
# HytoraCloud
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

- **Bridge**: Important for communication between Cloud <-> Services <br>
- **Permissions** (not finished): Allows the use of an integrated permission system <br>
- **Notify** (not finished): Allows the sending of service update messages to players <br>
- **Proxy** (not finished): Allows the use of proxy systems & configs <br>

## Features (✔ Completed)

- Logging API
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

##Planned (⚠ Not started)

- Access Screens from every Node
- Encryption for Sockets
- Template Caching on SubNodes
- ChannelMessage System
- Support for Sponge
- Service Deployments
- Signs Module
- NPC Module
- Service Screens

##In Progress (🚧 working on)

- Node/Cluster
- Support for Velocity
- Multi-Proxy & Multi-Root
- Rest-API
- Static services
               
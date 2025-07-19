
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

## Module-System

HytoraCloud uses its own ModuleSystem to make it as easy as possible for upcoming developers to extend HytoraCloud to their wishes.


<a href="https://github.com/Lystx/HytoraCloud/tree/master/cloud-modules">Klicke hier um zu den Modulen zu gelangen</a>.

## Features (✔ Completed)

    => Logging API
    => Screen API you can work with
    => TPS of CloudSystem
    => Networking with Netty
    => Request/Query System
    => EventSystem
    => TemplateStorage download
    => Node authentication
    => auto start/stop services
    => Lobby balancing
    => Document API
    => Module copy
    => Node-To-Node-Logging 
    => Node-To-Service-Logging & Backwards
    => Setup API and implemented Setups (using Annotations)
    => ChannelMessage System
    => Service Deployments
    => Service Screens
    => Static and Dynamic services

## Planned (⚠ Not started)

    => Access Screens from every Node
    => Encryption for Sockets
    => Template Caching on SubNodes
    => Support for Sponge

## In Progress (🚧 working on)

    => Node/Cluster
    => Support for Velocity
    => Multi-Proxy & Multi-Root
    => Rest-API

## Installation:
    1. Download newest release (should be a launcher.jar file)
    2. Execute the launcher.jar
    3. Follow setup instructions

##Permissions and what the do:
    
    => "cloud.full.join" - if cloud-network is full player can still join
    => "cloud.maintenance.bypass" - used to bypass proxy maintenance
    => "cloud.modules.notify.command.use" - used for module notify to togg
##Errors and what they mean:
    
    => #0x01 - ServiceScreen was null when stopping server (not system-breaking)

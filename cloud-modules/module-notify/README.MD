### Notify

The Notify-Module makes sure that you are up-to-date with the status of your services.<br>
It manages to send you notifications everytime a server starts/stop
If you don't like notifications you can

**Commands usage**
- notify (toggle) | toggles notifications for yourself

**Screenshots**

![StartMessage](https://i.imgur.com/A75p7yW.png "StartMessage")

![ReadyMessage](https://i.imgur.com/S4HVal9.png "ReadyMessage")

![StopMessage](https://i.imgur.com/s2DfoHS.png "StopMessage")
<br />

**Permissions:**
````
cloud.module.notify.command.use - for use of command
````

**Default config**
````json
{
  "displayPrefixInConsole": false,
  "displayInConsole": true,
  "enabledNotifications": [
    "82e8f5a2-4077-407b-af8b-e8325cad7191"
  ],
  "messages": {
    "prefix": "§8[§bH§fC§8]",
    "startMessage": "%prefix% §8'§b{task.node}§8' §7queued §a{server.name} §8| §bPort {server.port} §8| §bCapacity {server.capacity} §8| §bVersion {task.version} §8| §bState {server.state}",
    "stopMessage": "%prefix% §7The Service §c{server.name} §7has been stopped§8! §8[§bUptime§8: §f{server.uptime}§8]",
    "readyMessage": "%prefix% §7The Service §a{server.name} §7is now ready to use§8! §8[§bBootup§8: §f{server.uptimeDif}ms§8]"
  },
  "enabled": true,
  "showReadyMessage": true
}
````

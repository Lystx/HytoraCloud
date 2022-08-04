### Proxy

The Proxy-Module manages all of your proxies.<br>
It manages to display your MOTD(s) and TabList(s) and many more things
that are managed on your proxy.

**Commands usage**
- proxy (rl) | reloads the module and syncs it to the servers

**Compatible Softwares**
- BungeeCord
- Waterfall
<br>

**Screenshots**

![MotdNormalLayout](https://i.imgur.com/DcYBjnb.png "MotdNormalLayout")

![MotdMaintenanceLayOut](https://i.imgur.com/MQ5xXWN_d.webp?maxwidth=760&fidelity=grand "MotdMaintenanceLayOut")

![TabList](https://i.imgur.com/hiCWvqC_d.webp?maxwidth=760&fidelity=grand "TabList")
<br />

**Permissions:**
````
cloud.module.proxy.command.use - for use of command
````

**Default config**
````json
{
  "motd": {
    "defaults": [
      {
        "firstLine": "&8» &bHytoraCloud &8&l‴&7&l‴ &7your &bcloudSystem &8[&f1.8&7-&f1.18&8]",
        "secondLine": "&8» &3Status &8× §aOnline §8┃ §7Proxy &8× §3{proxy}",
        "protocolText": null,
        "playerInfo": []
      }
    ],
    "maintenances": [
      {
        "firstLine": "&8» &bHytoraCloud &8&l‴&7&l‴ &7your &bcloudSystem &8[&f1.8&7-&f1.18&8]",
        "secondLine": "&8» &3Status &8× §cMaintenance §8┃ §7Proxy &8× §3{proxy}",
        "protocolText": "&8» &c&oMaintenance",
        "playerInfo": [
          "§bHytoraCloud §7Information",
          "§8§m--------------------------",
          "§8",
          "&bVersion &8» &7SNAPSHOT-1.3",
          "&bThanks to &8» &7[Lystx]",
          "&bTwitter &8» &7@HytoraCloud",
          "&bDiscord &8» &7pazzqaGSVs",
          "§8",
          "§8§m--------------------------",
          "§8"
        ]
      }
    ]
  },
  "tablist": {
    "frames": [
      {
        "header": "&8\n&8        &8» &bHytoraCloud &8«        &8\n&8        &3Server &8» &7{service}     &8\n&8        &3Proxy &8» &7{proxy}        &8\n&8",
        "footer": "&8\n&8 &3Twitter &8» &7@HytoraCloud    &8┃ &3Developer &8» &7Lystx     &8\n&8 &3Online &8» &7{players.online} &8┃ &3Max &8» &7{players.max} &8\n&8"
      }
    ],
    "animationInterval": 1.0
  }
}
````
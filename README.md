# Essentials

cross-server essential-functionality |
Iteration. 2 | 2025

---

## Setup

This plugin requires the following dependencies:

- [EconomyAPI-Provider](https://cloud.einjojo.it/s/YK8WMIJgrPIycnH/download)
- [Wandoria-PlayerAPI](https://cloud.einjojo.it/s/cXeZeZXUEgsUPoP/download")

1. Put the jar from /build/libs into the paper server
2. Start the server-once
3. Configure the `/shared-connections.json`, located in the same folder as the `server.properties`
4. Restart the server

---

# Features

|              Name              | Description                                                                                                                                | 
|:------------------------------:|--------------------------------------------------------------------------------------------------------------------------------------------|
|   Cross Server Teleportation   | See TeleportExecutor. <br>Related Commands: <ul><li> /tp <li>/home <li>/back <li>/warp                                                     |
|         Economy Access         | Exposes commands which interact with the  api to modify the economic balance of an player <br/> /money                                     |
|          Chat System           | See ChatManager. <br>Related Commands: <ul><li> /msg <li>/reply <li>/r                                                                     |
|          Warp System           | Cross Server Warps, with permission support. <br/>Exposes bukkit events                                                                    |
|          Home System           | A player can own up to 20 _(constant defined in HomeManager)_ Homes. <br/>The amount is defined by permissions. <br/>Exposes bukkit events |
| *R*emote *C*ommand *E*xecution | Uses redis pub/sub to execute commands on other servers. <br/> Used for example in /gamemode.                                              |

---

## For Developers

A good point to start understanding this project is `commands/`

### Noteworthy

- PluginEnvironment can be implemented easily support different server types (CloudNet, SimpleCloud, etc).
- By default, it is expected that the _PlayerAPI_ is available
- Uses the incendo command framework
- You can start right out of the box with `gradle runServer`
- Listeners, named `*Listener.java`, are in the related feature package and not inside a `listeners` package.

### Singletons

Personally, I prefer to make classes singletons where I know that they will only be used once.
<br/>Therefore to access these classes, use their static getter methods.

- TeleportExecutor
- WarpManager
- PositionAccessor
- HomeManager
- EssentialsPlugin


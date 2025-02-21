# MultiMessageBridge

A plugin that bridges messages between Minecraft, Discord, Telegram, and more, creating a unified messaging system.

## Special Thanks
Special thanks to [Fairkor](https://discord.gg/fairkor) and MediocreSteam66 for sponsoring the development of this plugin!

---

## Requirements

Before installing the plugin, ensure you have the following:  

1. **Minecraft Server**  
   - A server running spigot/paper or any fork, version **1.19 or higher**.
2. **Discord Bot**  
   - A configured bot with its **token**. Create one via the [Discord Developer Portal](https://discord.com/developers/applications).  
3. **Telegram Bot**  
   - A configured bot with its **token**. Use [BotFather](https://core.telegram.org/bots#botfather) on Telegram to set up a new bot.  

---

## Installation

1. **Download**
    - Grab the latest version of the plugin from the [Versions](https://modrinth.com/plugin/multimessagebridge/versions).
2. **Add to Server**
    - Drop the downloaded `.jar` file into the `plugins` folder of your Minecraft server.
3. **Run the Server**
    - Start your Minecraft server to generate the default configuration files.
4. **Configure the Plugin**
    - Locate the configuration folder: `plugins/MultiMessageBridge/`.
    - Adjust settings in `config.yml` and other receiver-specific files (e.g., `Discord.yml`, `Telegram.yml`, etc.).
5. **Restart the Server**
    - Restart the server to apply your changes.
6. **Enjoy!**
    - Your messaging bridge is now ready to use.

---

## Feedback and Support

- **Bug Reports**  
  Encounter a bug? Open a new issue in the [GitHub Issues](https://github.com/Azimkin/MultiMessageBridge/issues) section.
- **Community Support**  
  Join the discussion and get support on my [Discord server](https://discord.gg/Z63mKkNgSS).

---

## Notes
- Ensure each messaging platform's API tokens and permissions are configured correctly in their respective configuration files.


## For developers

You can add plugin to your development environment from my repository

### Gradle kts
add repository:
```kotlin
maven {
    name = "azimkinRepoReleases"
    url = uri("https://repo.azimkin.top/releases")
}
```

and dependency
```kotlin
compileOnly("top.azimkin:MultiMessageBridge:0.3")
```

Good luck :)

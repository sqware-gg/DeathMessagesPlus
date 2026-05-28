# DeathMessagesPlus

**Get the plugin jar, setup help, and updates in the SQWARE Discord: [discord.sqware.gg](https://discord.sqware.gg).**

DeathMessagesPlus is a custom death messages plugin for Paper servers. It replaces vanilla player death messages with configurable colored messages and can show the dead player a return-to-location reminder.

Use it when you want DeathMessagesPrime-style control over death chat without a large dependency chain.

## Features

- Replaces vanilla player death messages.
- Configurable message templates.
- Color and formatting support.
- Optional permission-based broadcast audience.
- Return-to-death-location reminder for the dead player.
- Preview and reload commands.

## Requirements

- Paper `26.1.2+`
- Java `25+`
- Maven wrapper included

## Commands

```text
/deathmessagesplus status
/deathmessagesplus reload
/deathmessagesplus preview
```

Aliases: `/deathmessages`, `/deathsplus`, `/dmp`

## Permissions

```text
deathmessagesplus.admin  - admin commands, default op
deathmessagesplus.see    - receive broadcasts when audience is permission, default true
```

## Build

```powershell
.\mvnw.cmd package
```

The jar is written to `target/DeathMessagesPlus-0.1.0.jar`.

## License

DeathMessagesPlus is licensed under the Apache License, Version 2.0.

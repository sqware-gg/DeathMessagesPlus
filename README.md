# DeathMessagesPlus

DeathMessagesPlus is a Paper plugin that replaces vanilla player death messages with colored, configurable messages and sends the dead player a return-to-location warning.

## Compatibility

- Paper `26.1.2+`
- Java `25+`
- Maven

## Commands

```text
/deathmessagesplus status
/deathmessagesplus reload
/deathmessagesplus preview
```

Aliases:

```text
/deathmessages
/deathsplus
/dmp
```

## Permissions

```text
deathmessagesplus.admin  - use admin commands, default op
deathmessagesplus.see    - receive broadcasts when audience is permission, default true
```

## Build

```powershell
mvn package
```

The compiled jar is written to:

```text
target/DeathMessagesPlus-0.1.0.jar
```

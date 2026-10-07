# ClarityMarriage
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.x-green?style=for-the-badge&logo=minecraft)
![Paper](https://img.shields.io/badge/Server-Paper-blue?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21%20%7C%2025-orange?style=for-the-badge&logo=openjdk)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-1.0.0-brightgreen?style=for-the-badge)

A lightweight, modern, lag-free Minecraft marriage and relationship plugin built exclusively for Paper 1.21+ with Adventure MiniMessage and PlaceholderAPI support.

## Description

**ClarityMarriage** bridges the gap between social interaction and practical survival utility on modern Minecraft servers. Built from the ground up for high-performance servers, it features asynchronous data persistence (SQLite and YAML), safe item gifting, shared couple homes, private couple chat channels, combat-tagged partner teleportation, and customizable MiniMessage visual announcements.

---

## Features

- **Lag-Free & Asynchronous**: All database and disk operations run on isolated threads. Teleportation uses Paper's `teleportAsync` API for zero tick lag.
- **Dual Storage Engine**: Choose between high-performance **SQLite** (recommended for production) or human-readable **YAML** (`data.yml`).
- **Interactive Marriage Proposals**: Interactive chat buttons with hover effects allowing partners to accept or deny proposals with a single click.
- **Safe Item Gifting (`/marry gift`)**: Atomic, non-destructive inventory checks prevent item duplication or loss. Notifies players if the recipient's inventory is full.
- **Combat-Tagged Teleportation (`/marry tp`)**: Configurable warmup delays, movement and damage detection, and combat timers prevent abuse during PvP.
- **Shared Couple Homes (`/marry sethome` & `/marry home`)**: Create a unified couple home accessible exclusively by both partners.
- **Private Couple Chat Channel (`/marry chat`)**: Toggleable private chat routing regular messages directly between partners.
- **Joint Playtime Tracking**: Automatically monitors and records playtime spent together when both partners are online simultaneously.
- **Full Adventure MiniMessage**: Rich text formatting, hex gradients, click actions, and hover tooltips configurable via `language.yml`.
- **PlaceholderAPI Support**: Seamless integration for scoreboards, tablists, chat formatters, and custom menus.
- **Friendly-Fire Protection**: Configurable option to protect married couples from accidentally hurting each other.

---

## Requirements

- **Minecraft Version**: 1.21+ (1.21, 1.21.1, 1.21.3, 1.21.4+)
- **Server Software**: [Paper](https://papermc.io/), Purpur, or compatible forks
- **Java Version**: 21 or higher (fully compatible with Java 25)
- **Optional Dependencies**: [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)

---

## Installation

1. Download the latest `ClarityMarriage-1.0.0.jar` from the [Releases](https://github.com/harukiyu/ClarityMarriage/releases) tab.
2. Place the JAR file into your server's `plugins` folder.
3. Restart your server to generate the default configuration files.
4. Customize `config.yml` and `language.yml` located in `plugins/ClarityMarriage/`.
5. Reload configuration changes in-game using `/claritymarriage reload`.

---

## Configuration

### Default `config.yml`

```yaml
# ============================================================== #
#                    ClarityMarriage Configuration               #
# ============================================================== #

# Storage mechanism: "SQLITE" or "YAML"
# SQLITE is recommended for high performance and lag-free operation.
storage:
  type: "SQLITE"
  sqlite:
    file: "marriages.db"

# Marriage core settings
marriage:
  # Proposal request expiry time in seconds
  proposal-timeout: 60
  # Cooldown between sending proposals in seconds
  proposal-cooldown: 15
  # Broadcast wedding announcement to the entire server
  broadcast-on-marriage: true
  # Broadcast divorce announcement to the entire server
  broadcast-on-divorce: false
  # Allow or disallow PvP damage between married partners (false = protected from friendly fire)
  allow-pvp: false
  # Notify partner when their spouse logs in
  partner-join-notify: true

# Teleportation (/marry tp & /marry home)
teleport:
  # Warmup delay in seconds before teleporting (0 to disable)
  warmup-seconds: 3
  # Cooldown in seconds after teleporting
  cooldown-seconds: 30
  # Cancel teleport if player moves during warmup
  cancel-on-move: true
  # Cancel teleport if player takes damage during warmup
  cancel-on-damage: true
  # Seconds after taking/dealing damage during which teleport is blocked (Combat Tag)
  combat-tag-seconds: 10

# Couple Chat settings (/marry chat)
chat:
  # Format uses MiniMessage
  format: "<gradient:#ff758c:#ff7eb3>❤ [Couple] <sender>:</gradient> <white><message></white>"
  # Play chime sound when receiving a couple chat message
  play-sound: true
  sound: "BLOCK_NOTE_BLOCK_CHIME"

# Audio and visual effects
effects:
  play-sounds: true
  spawn-particles: true
  sounds:
    marriage-success: "UI_TOAST_CHALLENGE_COMPLETE"
    proposal-sent: "BLOCK_NOTE_BLOCK_BELL"
    proposal-received: "BLOCK_NOTE_BLOCK_BELL"
    proposal-denied: "ENTITY_VILLAGER_NO"
    teleport-start: "BLOCK_PORTAL_TRIGGER"
    teleport-success: "ENTITY_ENDERMAN_TELEPORT"
    gift-success: "ENTITY_PLAYER_LEVELUP"
    divorce: "BLOCK_BEACON_DEACTIVATE"
```

---

## Configuration Options

### Storage Settings

- **`storage.type: "SQLITE"` (Recommended)**: Stores marriages, stats, and homes in an optimized SQLite database (`marriages.db`). Zero tick lag with batch transaction handling.
- **`storage.type: "YAML"`**: Stores data in human-readable `data.yml`. Suitable for small private servers or manual file editing.

### Teleport & Combat Tagging

- **`warmup-seconds`**: Warmup delay before initiating teleport. During this window, any movement or incoming damage can abort the teleport.
- **`combat-tag-seconds`**: Prevents players from teleporting to their partner or home if they have dealt or received damage within this interval.

---

## PlaceholderAPI Placeholders

If [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) is installed on your server, the following placeholders are available:

| Placeholder | Description | Example Output |
| :--- | :--- | :--- |
| `%claritymarriage_is_married%` | Whether the player is currently married | `yes` / `no` |
| `%claritymarriage_status_icon%` | Heart icon if married, blank if single | `❤` |
| `%claritymarriage_partner%` | Partner's username | `Alex` |
| `%claritymarriage_partner_online%` | Partner's current connection status | `Online` / `Offline` |
| `%claritymarriage_wedding_date%` | Formatted timestamp of the wedding | `2026-10-07 09:20` |
| `%claritymarriage_married_duration%` | Time elapsed since marriage | `14d 6h 32m` |
| `%claritymarriage_playtime_together%`| Total joint online playtime | `8h 45m` |
| `%claritymarriage_has_home%` | Whether a shared home has been set | `yes` / `no` |

---

## Commands

### Player Commands (`/marry` or `/marriage`)

| Command | Arguments | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/marry` | `<player>` | Propose to another player | `claritymarriage.use` |
| `/marry` | `accept` | Accept an incoming proposal | `claritymarriage.use` |
| `/marry` | `deny` | Reject an incoming proposal | `claritymarriage.use` |
| `/marry` | `tp` | Teleport to your online partner | `claritymarriage.tp` |
| `/marry` | `sethome` | Set your shared couple home | `claritymarriage.home` |
| `/marry` | `home` | Teleport to your shared couple home | `claritymarriage.home` |
| `/marry` | `chat [message]` | Toggle couple chat or send private message | `claritymarriage.chat` |
| `/marry` | `stats [player]` | View your marriage profile & statistics | `claritymarriage.stats` |
| `/marry` | `gift` | Safely gift held item to your partner | `claritymarriage.gift` |
| `/marry` | `divorce [confirm]` | End your current marriage (safe confirmation) | `claritymarriage.use` |
| `/marry` | `help` | View player help menu | `claritymarriage.use` |

### Admin Commands (`/claritymarriage` or `/claritymarry`)

| Command | Arguments | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/cmarry` | `reload` | Reload configuration and language files | `claritymarriage.admin` |
| `/cmarry` | `forcemarry <p1> <p2>` | Administratively force two players to marry | `claritymarriage.admin` |
| `/cmarry` | `forcedivorce <player>` | Administratively dissolve a marriage | `claritymarriage.admin` |
| `/cmarry` | `info <player>` | Inspect a player's marriage profile | `claritymarriage.admin` |
| `/cmarry` | `help` | View administrative help menu | `claritymarriage.admin` |

---

## Permissions

| Permission | Description | Default |
| :--- | :--- | :--- |
| `claritymarriage.use` | Allows proposing, accepting, denying, and divorcing | `true` |
| `claritymarriage.tp` | Allows teleporting to partner | `true` |
| `claritymarriage.tp.bypass` | Bypasses teleport warmup, cooldown, and combat tags | `op` |
| `claritymarriage.home` | Allows setting and teleporting to shared couple home | `true` |
| `claritymarriage.chat` | Allows using private couple chat | `true` |
| `claritymarriage.gift` | Allows gifting held items to partner | `true` |
| `claritymarriage.stats` | Allows viewing marriage profiles | `true` |
| `claritymarriage.admin` | Full administrative command access | `op` |

---

## How It Works

1. **Proposing (`/marry <player>`)**:
   - The proposer sends a proposal. The target receives an interactive message containing clickable `[ACCEPT]` and `[DECLINE]` options.
   - If accepted within the timeout window, heart particles and fanfare sounds trigger, and a server-wide wedding broadcast is dispatched.

2. **Gifting Items (`/marry gift`)**:
   - The sender holds an item in their main hand and runs `/marry gift`.
   - The plugin runs a non-destructive space simulation on the partner's inventory. If the recipient's inventory is full, the transfer is cancelled, preventing item drop loss or duplication glitches.
   - If space is available, the item stack is safely transferred and both players receive particle and sound notifications.

3. **Teleportation (`/marry tp` & `/marry home`)**:
   - Initiates a countdown warmup. If the player moves their feet or takes damage, the warmup aborts.
   - Destination safety checks ensure players never land in lava, void, fire, or suffocating blocks.

4. **Private Couple Chat (`/marry chat`)**:
   - Toggling couple chat intercepts standard player chat in `AsyncChatEvent` and routes it exclusively to the spouse with a distinct prefix and sound notification.

5. **Divorce (`/marry divorce`)**:
   - Players must confirm the divorce within 15 seconds by clicking the confirmation prompt or typing `/marry divorce confirm`, eliminating accidental dissolutions.

---

## Building from Source

### Prerequisites

- **Java Development Kit (JDK)**: Version 21 or higher (JDK 21–25)
- **Apache Maven**: Version 3.9.0 or higher

### Build Steps

```bash
# Clone the repository
git clone https://github.com/harukiyu/ClarityMarriage.git
cd ClarityMarriage

# Build with Maven
mvn clean package

# Compiled JAR will be located at:
# target/ClarityMarriage-1.0.0.jar
```

---

## Support

For bug reports, feature requests, or technical questions:
- Check existing [GitHub Issues](https://github.com/harukiyu/ClarityMarriage/issues).
- Open a new issue with complete server version, Java version, and console logs.

---

## License

This project is licensed under the [MIT License](LICENSE).

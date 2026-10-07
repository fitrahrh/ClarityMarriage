# ClarityMarriage
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.x-green?style=for-the-badge&logo=minecraft)
![Paper](https://img.shields.io/badge/Server-Paper-blue?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21%20%7C%2025-orange?style=for-the-badge&logo=openjdk)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-1.1.0-brightgreen?style=for-the-badge)

A lightweight, modern, lag-free Minecraft marriage and relationship plugin built exclusively for Paper 1.21+ featuring Vault economy couple banks, EXP Love progression, Adventure MiniMessage, and PlaceholderAPI support.

## Description

**ClarityMarriage** bridges the gap between social interaction and practical survival utility on modern Minecraft servers. Built from the ground up for high-performance servers, it features asynchronous data persistence (SQLite and YAML), interactive Vault couple bank GUIs, unlimited deposit limit upgrades powered by **EXP Love**, safe item gifting, shared couple homes, private couple chat channels, combat-tagged partner teleportation, and customizable MiniMessage visual announcements.

---

## Features

- **Lag-Free & Asynchronous**: All database and disk operations run on isolated background threads. Teleportation uses Paper's `teleportAsync` API for zero tick lag.
- **Dual Storage Engine**: Choose between high-performance **SQLite** (recommended for production) or human-readable **YAML** (`data.yml`).
- **Gender Identity & Icons (`/marry gender`)**: Choose male (`♂`) or female (`♀`) with custom symbols and colors. Requires gender selection before marriage, with full support for both opposite-sex and same-sex unions.
- **Divorce Cooldown**: Configurable cooldown (e.g. 24h) after a divorce to prevent spam proposals and instant remarrying.
- **Player Marriage & Status Checker (`/marry check <player>`)**: Check any online or offline player's marriage status (Single or Married), their spouse, wedding date, joint EXP Love, and divorce cooldown.
- **Interactive Couple Bank GUI (`/marry bank`)**: Shared couple bank account integrated with Vault. Deposit, withdraw, and manage couple funds with intuitive quick-buttons or custom chat input.
- **EXP Love & Unlimited Upgrades**: Couples earn shared **EXP Love** by staying close to each other, hunting mobs together, mining, fishing, and kissing. Use EXP Love to upgrade bank deposit limits without any ceiling!
- **Interactive Marriage Proposals**: Interactive chat buttons with hover effects allowing partners to accept or deny proposals with a single click.
- **Safe Item Gifting (`/marry gift`)**: Atomic, non-destructive inventory checks prevent item duplication or loss. Warns players if the recipient's inventory is full.
- **Combat-Tagged Teleportation (`/marry tp`)**: Configurable warmup delays, movement and damage detection, and combat timers prevent abuse during PvP.
- **Shared Couple Homes (`/marry sethome` & `/marry home`)**: Create a unified couple home accessible exclusively by both partners.
- **Private Couple Chat Channel (`/marry chat`)**: Toggleable private chat routing regular messages directly between partners.
- **Joint Playtime Tracking**: Automatically monitors and records playtime spent together when both partners are online simultaneously.
- **Full Adventure MiniMessage**: Rich text formatting, hex gradients, click actions, and hover tooltips configurable via `language.yml`.
- **PlaceholderAPI Support**: Seamless integration for scoreboards, tablists, chat formatters, and custom menus with gender and status placeholders.
- **Friendly-Fire Protection**: Configurable option to protect married couples from accidentally hurting each other.

---

## Requirements

- **Minecraft Version**: 1.21+ (1.21, 1.21.1, 1.21.3, 1.21.4+)
- **Server Software**: [Paper](https://papermc.io/), Purpur, or compatible forks
- **Java Version**: 21 or higher (fully compatible with Java 25)
- **Optional Dependencies**:
  - [Vault](https://www.spigotmc.org/resources/vault.34315/) + Economy provider (EssentialsX, CMI, etc.) for Couple Bank
  - [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)

---

## Installation

1. Download the latest `ClarityMarriage-1.1.0.jar` from the [Releases](https://github.com/harukiyu/ClarityMarriage/releases) tab.
2. Place the JAR file into your server's `plugins` folder.
3. Ensure [Vault](https://www.spigotmc.org/resources/vault.34315/) and an economy provider are installed if you wish to use the Couple Bank feature.
4. Restart your server to generate the default configuration files.
5. Customize `config.yml` and `language.yml` located in `plugins/ClarityMarriage/`.
6. Reload configuration changes in-game using `/claritymarriage reload`.

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
    bank-upgrade: "UI_TOAST_CHALLENGE_COMPLETE"
    love-exp-gain: "ENTITY_EXPERIENCE_ORB_PICKUP"
    kiss: "ENTITY_VILLAGER_YES"

# Marriage Bank & Vault Settings (/marry bank)
bank:
  enabled: true
  # Initial/Default deposit limit (500,000)
  default-limit: 500000.0
  # Increment added to deposit limit on every upgrade (500,000)
  limit-per-level: 500000.0
  # Base EXP Love required for first upgrade
  upgrade-cost-base: 100
  # Additional EXP Love required per bank level
  upgrade-cost-per-level: 50

# EXP Love System
love:
  # Proximity distance in blocks (default: 5 blocks)
  proximity-distance: 5.0
  # Playtime EXP Love: Awarded when partners stay within 5 blocks
  playtime:
    # Interval in seconds (default: 600 seconds = 10 minutes)
    interval-seconds: 600
    exp: 50
  # Mob kills within proximity
  mob-kill:
    enabled: true
    exp: 3
    boss-exp: 50
  # Additional recommended ways to earn EXP Love
  mining:
    enabled: true
    exp: 2
  fishing:
    enabled: true
    exp: 2
  sleeping:
    enabled: true
    exp: 25
  kiss:
    enabled: true
    exp: 10
    cooldown-seconds: 1800 # 30 minutes
```

---

## 💖 EXP Love & Bank Upgrade System

EXP Love represents the strength of a couple's bond. Couples earn EXP Love by playing together within a close proximity (**5 blocks**):

| Activity | Condition | EXP Love Gain |
| :--- | :--- | :--- |
| **Proximity Playtime** | Playing within 5 blocks of each other for 10 minutes | **+50 EXP Love** |
| **Mob Hunting** | Killing any mob while partner is within 5 blocks | **+3 EXP Love** |
| **Boss Slaying** | Defeating Ender Dragon, Wither, Warden, or Elder Guardian | **+50 EXP Love** |
| **Mining Together** | Mining valuable ores while partner is within 5 blocks | **+2 EXP Love** |
| **Fishing Together** | Catching fish within 5 blocks of partner | **+2 EXP Love** |
| **Passing the Night** | Sleeping in adjacent beds together | **+25 EXP Love** |
| **Sweet Kiss / Hug** | Shift + Right-clicking your partner (30 min cooldown) | **+10 EXP Love** |

### Bank Limit Progression
- **Initial Limit (Level 1)**: $500,000
- **Upgrade Increment**: +$500,000 per level (e.g. Level 2 = $1,000,000, Level 3 = $1,500,000...)
- **No Maximum Limit**: Unlimited upgrades! Couples can continuously expand their bank capacity as long as they earn EXP Love together.

Players can type `/marry love` or `/marry love tips` in-game at any time to inspect their current EXP Love and view all ways to earn points.

---

## PlaceholderAPI Placeholders

| Placeholder | Description | Example Output |
| :--- | :--- | :--- |
| `%claritymarriage_is_married%` | Whether the player is currently married | `yes` / `no` |
| `%claritymarriage_status%` | Marriage status text | `Married` / `Single` |
| `%claritymarriage_status_icon%` | Heart icon if married, blank if single | `❤` |
| `%claritymarriage_gender%` | Player's chosen gender | `Male` / `Female` / `None` |
| `%claritymarriage_gender_icon%` | Player's gender symbol | `♂` / `♀` |
| `%claritymarriage_partner%` | Partner's username | `Alex` |
| `%claritymarriage_partner_gender%` | Partner's gender name | `Female` |
| `%claritymarriage_partner_gender_icon%` | Partner's gender symbol | `♀` |
| `%claritymarriage_partner_online%` | Partner's current connection status | `Online` / `Offline` |
| `%claritymarriage_divorce_cooldown%` | Formatted remaining divorce cooldown | `23h 45m` / `0s` |
| `%claritymarriage_divorce_cooldown_seconds%` | Raw remaining divorce cooldown | `85500` / `0` |
| `%claritymarriage_wedding_date%` | Formatted timestamp of the wedding | `2026-10-07 09:20` |
| `%claritymarriage_married_duration%` | Time elapsed since marriage | `14d 6h 32m` |
| `%claritymarriage_playtime_together%`| Total joint online playtime | `8h 45m` |
| `%claritymarriage_has_home%` | Whether a shared home has been set | `yes` / `no` |
| `%claritymarriage_bank_balance%` | Raw couple bank balance | `250000.0` |
| `%claritymarriage_bank_balance_formatted%` | Formatted couple bank balance | `$250,000.00` |
| `%claritymarriage_bank_limit%` | Raw bank deposit limit | `500000.0` |
| `%claritymarriage_bank_limit_formatted%` | Formatted bank deposit limit | `$500,000.00` |
| `%claritymarriage_bank_level%` | Current couple bank level | `1` |
| `%claritymarriage_love_exp%` | Current shared EXP Love | `150` |

---

## Commands

### Player Commands (`/marry` or `/marriage`)

| Command | Arguments | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/marry` | `<player>` | Propose to another player | `claritymarriage.use` |
| `/marry` | `accept` | Accept an incoming proposal | `claritymarriage.use` |
| `/marry` | `deny` | Reject an incoming proposal | `claritymarriage.use` |
| `/marry` | `gender <male\|female>` | Select or change your gender identity | `claritymarriage.gender` |
| `/marry` | `check [player]` | View any player's marriage status & profile | `claritymarriage.check` |
| `/marry` | `bank` | Open interactive Couple Bank & Vault GUI | `claritymarriage.bank` |
| `/marry` | `deposit <amount>` | Directly deposit money into couple bank | `claritymarriage.bank` |
| `/marry` | `withdraw <amount>`| Directly withdraw money from couple bank | `claritymarriage.bank` |
| `/marry` | `upgrade` | Upgrade bank deposit limit using EXP Love | `claritymarriage.bank.upgrade` |
| `/marry` | `love [tips]` | View EXP Love guide, tips, and current stats | `claritymarriage.love` |
| `/marry` | `tp` | Teleport to your online partner | `claritymarriage.tp` |
| `/marry` | `sethome` | Set your shared couple home | `claritymarriage.home` |
| `/marry` | `home` | Teleport to your shared couple home | `claritymarriage.home` |
| `/marry` | `chat [message]` | Toggle couple chat or send private message | `claritymarriage.chat` |
| `/marry` | `stats [player]` | View your marriage profile & statistics | `claritymarriage.stats` |
| `/marry` | `gift` | Safely gift held item to your partner | `claritymarriage.gift` |
| `/marry` | `divorce [confirm]` | End your current marriage (with divorce cooldown) | `claritymarriage.use` |
| `/marry` | `version` | View plugin version and system diagnostics | `claritymarriage.use` |
| `/marry` | `help` | View player help menu | `claritymarriage.use` |

### Admin Commands (`/claritymarriage` or `/claritymarry`)

| Command | Arguments | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/cmarry` | `reload` | Reload configuration and language files | `claritymarriage.admin` |
| `/cmarry` | `forcemarry <p1> <p2>` | Administratively force two players to marry | `claritymarriage.admin` |
| `/cmarry` | `forcedivorce <player>` | Administratively dissolve a marriage | `claritymarriage.admin` |
| `/cmarry` | `cleardivorce <player>` | Reset a player's active divorce cooldown | `claritymarriage.admin` |
| `/cmarry` | `setgender <player> <gender>` | Set player gender (`male`/`female`/`none`) | `claritymarriage.admin` |
| `/cmarry` | `info <player>` | Inspect a player's marriage profile | `claritymarriage.admin` |
| `/cmarry` | `help` | View administrative help menu | `claritymarriage.admin` |

---

## Permissions

| Permission | Description | Default |
| :--- | :--- | :--- |
| `claritymarriage.use` | Allows proposing, accepting, denying, and divorcing | `true` |
| `claritymarriage.gender` | Allows choosing player gender | `true` |
| `claritymarriage.check` | Allows checking any player's marriage status | `true` |
| `claritymarriage.bank` | Allows accessing couple bank GUI, deposits & withdrawals | `true` |
| `claritymarriage.bank.upgrade` | Allows upgrading bank deposit limit with EXP Love | `true` |
| `claritymarriage.love` | Allows viewing EXP Love guides and statistics | `true` |
| `claritymarriage.tp` | Allows teleporting to partner | `true` |
| `claritymarriage.tp.bypass` | Bypasses teleport warmup, cooldown, and combat tags | `op` |
| `claritymarriage.home` | Allows setting and teleporting to shared couple home | `true` |
| `claritymarriage.chat` | Allows using private couple chat | `true` |
| `claritymarriage.gift` | Allows gifting held items to partner | `true` |
| `claritymarriage.stats` | Allows viewing marriage profiles | `true` |
| `claritymarriage.admin` | Full administrative command access | `op` |

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
# target/ClarityMarriage-1.1.0.jar
```

---

## Support

For bug reports, feature requests, or technical questions:
- Check existing [GitHub Issues](https://github.com/harukiyu/ClarityMarriage/issues).
- Open a new issue with complete server version, Java version, and console logs.

---

## License

This project is licensed under the [MIT License](LICENSE).

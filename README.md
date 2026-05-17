# InvestPlugin — Paper 1.21.1

A fully-featured investment GUI plugin for Paper 1.21.1 servers.

---

## Features

- `/invest` opens an interactive chest GUI
- Players can **invest money**, **collect earnings**, **delete investments**, and toggle **auto-collect**
- Earnings calculated as: `invested_amount × rate` per second
- Fully configurable max investment cap and rate
- Confirmation screen before committing an investment
- Data persists between restarts via `data.yml`

---

## Requirements

| Dependency | Where to get it |
|---|---|
| **Paper 1.21.1** | https://papermc.io/downloads |
| **Vault** | https://www.spigotmc.org/resources/vault.34315/ |
| **Economy plugin** | EssentialsX recommended: https://essentialsx.net |
| **Java 21+** | For building only |
| **Maven 3.6+** | For building only |

---

## Building

```bash
chmod +x build.sh
./build.sh
```

The compiled jar will be at `target/InvestPlugin-1.0.0.jar`.

---

## Installation

1. Place `InvestPlugin-1.0.0.jar` in your server's `plugins/` folder
2. Ensure Vault and an economy plugin are installed
3. Start/restart the server
4. Configure `plugins/InvestPlugin/config.yml`

---

## Configuration (`config.yml`)

```yaml
# Max money a player can have invested at once (0 = unlimited)
max-invest: 1000000.0

# Earnings multiplier per second
# invested_amount × invest-rate = earnings per second
# Example: 1000 invested × 0.001 rate = $1.00/sec
invest-rate: 0.001

# How often payouts are processed (20 ticks = 1 second)
payout-interval: 20
```

---

## Commands & Permissions

| Command | Description | Permission |
|---|---|---|
| `/invest` | Open the investment GUI | `investplugin.invest` (default: all players) |
| `/invest reload` | Reload the config | `investplugin.admin` (default: ops) |

---

## GUI Layout

### Main Menu (`/invest`)

```
[  ] [  ] [  ] [  ] [  ] [  ] [  ] [  ] [  ]   ← Top row (glass border)
[  ] [🔴] [  ] [  ] [📄] [  ] [  ] [📦] [  ]   ← Middle row
[  ] [  ] [  ] [  ] [  ] [  ] [  ] [🟢] [  ]   ← Bottom row
```

- **Slot 2 (🔴 Redstone Block)** — Delete: wipes your investment (no refund)
- **Slot 5 (📄 Paper)** — Info: shows invested amount and earnings/sec; click to invest more
- **Slot 8 (📦 Chest)** — Collect: claim your available earnings
- **Below Slot 8 (🟢/🔴 Glass Pane)** — Auto-Collect toggle (green = on, red = off)

### Confirm Menu (after typing an amount)

```
[  ] [🔴] [  ] [  ] [📄] [  ] [  ] [🟢] [  ]   ← Middle row
```

- **Slot 2 (🔴 Red Glass)** — Cancel
- **Slot 5 (📄 Paper)** — Shows the amount being invested
- **Slot 8 (🟢 Green Glass)** — Confirm

---

## Data Storage

Player data is stored in `plugins/InvestPlugin/data.yml` and auto-saves on:
- Investment changes
- Collection
- Server shutdown

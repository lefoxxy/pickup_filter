# Pocket Filter

**Pocket Filter** is a lightweight Forge 1.20.1 utility mod for players who are tired of their inventory filling with junk every time they mine, farm, explore, or stand near a mob grinder.

Instead of constantly opening your inventory to throw away cobblestone, rotten flesh, seeds, arrows, or whatever else the world keeps shoving into your pockets, Pocket Filter lets you decide what automatic pickup should actually mean.

Want to collect only diamonds, ores, and rare drops while mining? Use whitelist mode.

Want to pick up everything except clutter? Use blacklist mode.

Want ignored items to stay safely on the ground instead of being deleted? That is exactly how it works.

## Why You Want It

Minecraft throws a lot of items at you. Some are valuable. Some are useful later. Some are just there to slowly turn your inventory into a junk drawer.

Pocket Filter gives you a simple, per-player filter with 15 reference slots. Drop an item reference into the filter GUI, choose whitelist or blacklist, and keep playing. No complicated setup. No extra item required. No weird storage tricks.

It is built for the moments where inventory clutter breaks the flow:

- Long mining sessions
- Mob farms
- Tree farms
- Exploration
- Building projects
- Early-game resource gathering
- Late-game automation cleanup

## Features

- Per-player pickup filters
- Whitelist and blacklist modes
- 15 one-item reference slots
- Simple GUI opened from the player inventory
- Optional keybind fallback
- Optional actionbar feedback when an item is ignored
- Ignored items remain on the ground
- Filter data persists after relogging
- Server-authoritative syncing for multiplayer
- Lightweight Forge implementation

## How It Works

Open your inventory and click the paper icon on the right side of the inventory screen.

Inside the Pocket Filter GUI, choose a mode:

**Whitelist**

Only items in the filter slots will be picked up automatically. If the whitelist is empty, nothing is picked up automatically.

**Blacklist**

Everything is picked up automatically except items in the filter slots. If the blacklist is empty, everything works like vanilla pickup.

The filter slots are reference slots only. They do not store real items, consume items, or duplicate items. They simply remember the item type.

## Feedback Messages

The `Msg` button controls whether Pocket Filter shows a small actionbar message when it ignores an item.

Turn it on if you want confirmation that the filter is working.

Turn it off if you prefer silence.

## Multiplayer Friendly

Pocket Filter is designed with multiplayer in mind. The server validates filter changes, keeps the real data, and syncs the GUI back to the client. Clients cannot create real items through the filter screen because the slots are only visual references.

## Configuration

Server/common config options include:

- Default filter mode
- Default feedback setting
- Feedback cooldown ticks
- Enable or disable the inventory button
- Enable or disable the keybind fallback

## Mod Info

- Minecraft: 1.20.1
- Loader: Forge
- Mod ID: `pocketfilter`
- License: MIT License

## Credits

Pocket Filter by GOGLEO (GitHub: lefoxxy)

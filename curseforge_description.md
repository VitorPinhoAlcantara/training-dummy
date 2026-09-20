# Training Dummy

A stationary, fully-equippable combat training dummy for testing your gear and weapons.

## Features

- **Craftable spawner**: combine an Armor Stand and a Hay Block to craft the Training Dummy item, then place it anywhere.
- **Damage readout**: every hit shows the damage you dealt, configurable to appear on your screen (above the XP bar) or in your chat, as either a running total or DPS. The total resets after a configurable idle interval, so back-to-back hits stack into one combo instead of spamming the screen.
- **Full gear**: right-click with a Stick to open the dummy's inventory — helmet, chestplate, leggings, boots, shield/offhand, main hand, and (if [Curios API](https://www.curseforge.com/minecraft/mc-mods/curios) is installed) accessory slots, complete with vanilla-style icons and hover tooltips. Works with any mod that hooks into Curios, including Artifacts and Relics.
- **Wears real skins**: rename the dummy with a Name Tag, and it fetches and wears that Minecraft account's actual skin (including slim/wide arm support).
- **Lure Bait**: craft this TNT-shaped item (any food item + Bone Meal) and place it in the dummy's main hand to make nearby mobs notice and attack it — great for testing mob AI or filming combat footage.
- **Armor works properly**: equipped armor and a raised shield actually reduce incoming damage, same as on a player.
- **Only removable on purpose**: hit it with a Stick to break it (drops all its gear); nothing else can destroy it.

## Configuration

- **Client config**: damage display location (screen/chat), metric (total/DPS), hit-reset interval, and display duration.
- **Common config**: Lure Bait radius.

Both are editable in-game or via the [Configured](https://www.curseforge.com/minecraft/mc-mods/configured) mod.

## Requirements

- **NeoForge 1.21.1**
- **Curios API** (optional — enables the accessory slots and Artifacts/Relics compatibility)

## Source

Source code: https://github.com/VitorPinhoAlcantara/training-dummy

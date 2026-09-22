# Training Dummy

A stationary, fully-equippable combat training dummy for testing your gear and weapons.

<br>

## Features

- **Craftable spawner**: combine an Armor Stand and a Hay Block to craft the Training Dummy item, then place it anywhere.
- **Damage readout**: every hit shows the damage you dealt, configurable to appear on your screen (above the XP bar) or in your chat, as either a running total, DPS or per hit. The total resets after a configurable idle interval, so back-to-back hits stack into one combo instead of spamming the screen.
- **Full gear**: right-click with a Stick to open the dummy's inventory, helmet, chestplate, leggings, boots, shield/offhand, main hand, and (if [Curios API](https://www.curseforge.com/minecraft/mc-mods/curios) is installed) accessory slots.
- **Wears real skins**: rename the dummy with a Name Tag, and it fetches and wears that Minecraft account's actual skin (including slim/wide arm support).
- **Lure Bait**: craft this TNT-shaped item (any food item + Bone Meal) and place it in the dummy's main hand to make nearby mobs notice and attack it, great for testing mob AI or filming combat footage.
- **Only removable on purpose**: hit it with a Stick to break it (drops all its gear); nothing else can destroy it.
- **Scoreboard Dummy**: craft one from a Dummy Spawner + Diamond. Instead of a running total, it tracks the single biggest hit landed on it and reports it to a leaderboard, a local one for this server, and an optional global one shared across servers. Right-click with a Stick to view the leaderboard; the #1 player's real skin renders next to their name, and you can click-drag it to spin it around.
- **In-game guide**: if [GuideMe](https://www.curseforge.com/minecraft/mc-mods/guideme) is installed, a Training Dummy Guide item is added with a full walkthrough of the mod.

<br>

## Configuration

- **Client config**: damage display location (screen/chat), metric (total/DPS), hit-reset interval, and display duration.
- **Common config**: Lure Bait radius, scoreboard leaderboard settings (modpack ID, worker URL/API key, refresh/debounce timing), a global-leaderboard on/off toggle (on by default), and a weapon blacklist for the leaderboard (some entries, like Avaritia/Modern Industrialization items and the Morgan Sword, are always blocked and can't be overridden).



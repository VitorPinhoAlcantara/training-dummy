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
- **Scoreboard Dummy**: craft one from a Dummy Spawner + Diamond. Instead of a running total, it tracks the single biggest hit landed on it and reports it to a leaderboard, a local one for this server, and an optional global one shared across servers (opt-in, off by default). Right-click with a Stick to view the leaderboard; the #1 player's real skin renders next to their name, and you can click-drag it to spin it around.
- **In-game guide**: if [GuideMe](https://www.curseforge.com/minecraft/mc-mods/guideme) is installed, a Training Dummy Guide item is added with a full walkthrough of the mod.

<br>

## Configuration

- **Client config**: damage display location (screen/chat), metric (total/DPS), hit-reset interval, and display duration.
- **Common config**: Lure Bait radius, scoreboard leaderboard settings (modpack ID, worker URL/API key, refresh/debounce timing), a global-leaderboard on/off toggle (**off by default**, see Privacy), and a weapon blacklist for the leaderboard (some entries, like Avaritia/Modern Industrialization items and the Morgan Sword, are always blocked and can't be overridden).

<br>

## Privacy

The scoreboard dummy's **global (cross-server) leaderboard is off by default**. With it off, the mod makes no network requests at all; the local (this server only) leaderboard works either way.

If a server owner turns it on (`globalLeaderboardEnabled = true` in the common config), this is everything that is sent, and when:

- **What:** the player's Minecraft **nickname** and the **damage** of a record-breaking hit, plus the leaderboard's `modpackId`.
- **When:** only when a hit qualifies for the top 10, after `recordDebounceSeconds` (default 20s) without an even bigger hit from the same player. One request per record.
- **Where:** the worker at `workerBaseUrl` (the mod's built-in worker if left blank). Point it at your own endpoint to keep everything in-house.
- **Reading:** the server fetches the top 10 only when a player opens the scoreboard dummy's screen and its cached copy is older than `leaderboardRefreshSeconds` (default 600s). Nobody opening the screen means no requests.
- **Performance:** every request is asynchronous, a slow or offline worker never blocks the server tick; failures just show up as one (rate-limited) warning in the log.

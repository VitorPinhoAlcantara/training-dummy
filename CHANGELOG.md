# Changelog

All notable changes to this mod are documented here.

## [1.7] - 2026-09-21

### New
- Added a Scoreboard Dummy: craft one from a Dummy Spawner + Diamond, and it tracks the single biggest hit landed on it instead of a running total, with both a local (this server) leaderboard and an optional global (cross-server) leaderboard.
- The #1 player on whichever leaderboard is showing gets their real skin rendered next to their name, click and drag it to spin it around.
- Global leaderboard can be turned off entirely from config (globalLeaderboardEnabled, on by default); the local leaderboard keeps working either way.
- A weapon blacklist keeps specific items from ever scoring a leaderboard hit, by exact item ID, by whole mod ("modid:*"), or by a name fragment matched against any item's ID. Every Avaritia and Modern Industrialization item, the Morgan Sword, and anything with "infinity" or "quantum" in its ID are banned by default and can't be overridden; servers can ban more on top from config.
- In-game guide integration: if GuideMe is installed, a Training Dummy Guide item is added with a full walkthrough of the mod, getting started, damage display modes, and Lure Bait, readable straight from GuideMe's book.

## [1.6] - 2026-09-18

### New
- Fire damage now actually affects dummies, they used to be fire-immune, which silently blocked any attack tagged as fire damage too, not just environmental fire.
- The Per Hit display now shows the damage type (Fire/Freezing/Explosive/Lightning/Magic) whenever a hit isn't plain physical damage, including a full breakdown when a single swing lands more than one type at once.
- Damage that can't be attributed to a specific attacking player (some magic mods' indirect/summoned spells never do) is now shown to everyone who's actually hit that dummy in the last 30 seconds, instead of being silently dropped.
- Dummy Spawner and Lure Bait now stack up to 64 (was 16).
- A couple more hidden nickname easter eggs.

## [1.5] - 2026-09-15

Port of v1.5 (26.1.2 branch) to Minecraft 1.21.1, plus a few small fixes found along the way.

## [1.2] - 2026-08-14

### New
- Dummy max health is now configurable (including per individual dummy, right from its inventory).
- On-screen damage text now has configurable position, color, and formatting (thousands separator).
- Buttons in the dummy's inventory to quickly switch between Total/DPS damage and Chat/Screen display, without needing to touch a config file.

### Fixed
- Mobs now actually stop attacking when you take the bait out of the dummy's hand.
- The damage-per-second (DPS) number no longer flickers non-stop on screen, it now only updates when a new hit lands.

### Changed
- Default max health is now 20 (used to be a very high value).
- Damage display now defaults to chat (used to be on-screen).

## [1.1.0] - 2026-08-13

### Fixed
- Multiplayer: joining a server that doesn't have this mod installed no longer fails the connection handshake. The mod's network channels are now marked optional, so you connect fine and simply lose access to the mod's features on that server.
- Removing the dummy (hitting it with a Stick) no longer deletes its equipped items. Armor, held items, and Curios accessories are now dropped on the ground, along with a Dummy spawner item for the dummy itself.
- Shift-clicking an item from your inventory into the dummy's screen now prioritizes Curios slots first, then armor, then hands, instead of always preferring the main hand.
- Fixed a crash (NPE from the Relics mod's Piglin Mask) when removing an item from the dummy's hand, caused by the dummy missing several attributes (attack speed, luck, mining-related, etc.) that other mods assume any living entity has. The dummy's attribute set now mirrors a real player's.
- Fixed a Curios inventory crash ("Slot X not in valid range") that could happen after a relic granting extra Curios slots was removed mid-session.
- Lure Bait no longer attracts naturally passive/neutral mobs (bees, wolves, foxes), only genuinely hostile mobs are affected now.
- Lure Bait now works reliably against "brain"-based hostile mobs (Piglins, Piglin Brutes, Breezes, etc.), which mostly ignored the old targeting logic. Piglins/Piglin Brutes specifically no longer target-then-immediately-forget the dummy in a loop, they're now made "angry" at it the same way vanilla does when they're actually hurt, which is the only channel they honor outside their normal target whitelist.
  - Note: Phantoms remain untargetable by the bait, their attack AI is hardcoded in vanilla to only ever go after a real Player, with no clean way to override it.
- Installed the correct NeoForge build of JEI for dev testing (was accidentally pulling the Fabric build via an ambiguous Modrinth version number).

### Added
- Dedicated "Training Dummy" creative-mode tab for the mod's items, instead of dumping them into the vanilla Combat tab.
- Tooltip on Lure Bait explaining what it's for (matching the existing Dummy spawner tooltip).

### Changed
- JEI added as a dev-only test dependency so item/recipe tooltips can be checked during development.

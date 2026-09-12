# Changelog

All notable changes to this mod are documented here.

## [1.5] - 2026-09-12

### New
- Added a few hidden easter eggs — some dummies will play a special hurt sound instead of the usual one.

## [1.4] - 2026-09-11

### New
- New "Per Hit" display option, alongside Total and DPS — shows just the damage of the last hit, without summing or averaging.
- Damage display (Total/DPS/Per Hit) is now a setting on each individual dummy instead of a shared client setting — different dummies can show different metrics, set from that dummy's own inventory screen.
- The loaded spawner item's tooltip now shows a Ctrl-held breakdown of what's actually customized on that dummy (health, damage display, armor, curios).

### Fixed
- Hitting two dummies back and forth no longer mixes their damage totals into one streak — each dummy now tracks its own hits independently.

### Changed
- Removed the global "Display metric" client config option, since it's now set per dummy instead.

## [1.3] - 2026-08-19

### New
- Breaking a dummy now saves its name, configured health, armor and curios onto its own spawner item - placing it back down recreates the dummy just as it was (negative effects are not saved).
- The spawner item can be renamed in an anvil to set the name of the dummy it creates.
- New button in the dummy's inventory to remove negative effects on the spot, instead of waiting for them to run out.
- Lure bait now takes priority over the player - mobs will prefer attacking the dummy even if they're already fighting you.

### Fixed
- The lure bait recipe was broken and didn't work at all.
- Fixed a crash/disconnect that could happen when breaking a dummy that had Curios items equipped.
- The DPS number no longer shows absurd values on the first hit of a streak.
- Multiple hits landing together (a crit plus a bonus hit, for example) now show as a single number instead of several separate lines.

### Changed
- The dummy's hurt sound is now its own, instead of the game's generic one.
- Updated the spawner and bait item icons.

## [1.2] - 2026-08-14

### Novidades
- Suporte à versão 26.1.2 do Minecraft/NeoForge.
- Vida máxima do dummy agora é configurável (inclusive por dummy individual, direto no inventário dele).
- Texto de dano na tela agora tem posição, cor e formatação (separador de milhar) configuráveis.
- Botões no inventário do dummy pra trocar rapidamente entre dano Total/DPS e exibição em Chat/Tela, sem precisar mexer em arquivo de config.

### Correções
- Mobs agora realmente param de atacar quando você tira a isca da mão do dummy.

### Mudanças
- Vida máxima padrão agora é 20 (antes era um valor bem alto).
- Exibição de dano por padrão agora é no chat (antes era na tela).

## [1.1.0] - 2026-08-13

### Fixed
- Multiplayer: joining a server that doesn't have this mod installed no longer fails the connection handshake. The mod's network channels are now marked optional, so you connect fine and simply lose access to the mod's features on that server.
- Removing the dummy (hitting it with a Stick) no longer deletes its equipped items. Armor, held items, and Curios accessories are now dropped on the ground, along with a Dummy spawner item for the dummy itself.
- Shift-clicking an item from your inventory into the dummy's screen now prioritizes Curios slots first, then armor, then hands — instead of always preferring the main hand.
- Fixed a crash (NPE from the Relics mod's Piglin Mask) when removing an item from the dummy's hand, caused by the dummy missing several attributes (attack speed, luck, mining-related, etc.) that other mods assume any living entity has. The dummy's attribute set now mirrors a real player's.
- Fixed a Curios inventory crash ("Slot X not in valid range") that could happen after a relic granting extra Curios slots was removed mid-session.
- Lure Bait no longer attracts naturally passive/neutral mobs (bees, wolves, foxes) — only genuinely hostile mobs are affected now.
- Lure Bait now works reliably against "brain"-based hostile mobs (Piglins, Piglin Brutes, Breezes, etc.), which mostly ignored the old targeting logic. Piglins/Piglin Brutes specifically no longer target-then-immediately-forget the dummy in a loop — they're now made "angry" at it the same way vanilla does when they're actually hurt, which is the only channel they honor outside their normal target whitelist.
  - Note: Phantoms remain untargetable by the bait — their attack AI is hardcoded in vanilla to only ever go after a real Player, with no clean way to override it.
- Installed the correct NeoForge build of JEI for dev testing (was accidentally pulling the Fabric build via an ambiguous Modrinth version number).

### Added
- Dedicated "Training Dummy" creative-mode tab for the mod's items, instead of dumping them into the vanilla Combat tab.
- Tooltip on Lure Bait explaining what it's for (matching the existing Dummy spawner tooltip).

### Changed
- JEI added as a dev-only test dependency so item/recipe tooltips can be checked during development.

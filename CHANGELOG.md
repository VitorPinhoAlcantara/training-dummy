# Changelog

All notable changes to this mod are documented here.

## [1.6] - 2026-09-18

### New
- Fire damage now actually affects dummies - they used to be fire-immune, which silently blocked any attack tagged as fire damage too, not just environmental fire.
- The Per Hit display now shows the damage type (Fire/Freezing/Explosive/Lightning/Magic) whenever a hit isn't plain physical damage, including a full breakdown when a single swing lands more than one type at once.
- Damage that can't be attributed to a specific attacking player (some magic mods' indirect/summoned spells never do) is now shown to everyone who's actually hit that dummy in the last 30 seconds, instead of being silently dropped.
- Dummy Spawner and Lure Bait now stack up to 64 (was 16).
- A couple more hidden nickname easter eggs.

## [1.5] - 2026-09-15

Port of v1.5 (26.1.2 branch) to Minecraft 1.21.1, plus a few small fixes found along the way.

## [1.2] - 2026-08-14

### Novidades
- Vida máxima do dummy agora é configurável (inclusive por dummy individual, direto no inventário dele).
- Texto de dano na tela agora tem posição, cor e formatação (separador de milhar) configuráveis.
- Botões no inventário do dummy pra trocar rapidamente entre dano Total/DPS e exibição em Chat/Tela, sem precisar mexer em arquivo de config.

### Correções
- Mobs agora realmente param de atacar quando você tira a isca da mão do dummy.
- O número de dano por segundo (DPS) não fica mais oscilando sem parar na tela — agora só atualiza quando um novo acerto acontece.

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

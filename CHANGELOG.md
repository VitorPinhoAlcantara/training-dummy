# Changelog

All notable changes to this mod are documented here.

## [1.5] - 2026-09-15

Porte das versões 1.3, 1.4 e 1.5 (já lançadas no branch 26.1.2) para o Minecraft 1.21.1, mais algumas mudanças pequenas feitas durante esse porte.

### Novidades
- Quebrar um dummy agora guarda seu nome, vida configurada, armadura e curios no próprio item spawner — colocá-lo de volta recria o dummy exatamente como estava.
- O item spawner pode ser renomeado numa bigorna para definir o nome do dummy que ele cria.
- Isca de atração agora tem prioridade sobre o jogador como alvo, mesmo que o mob já esteja te atacando.
- Nova opção de exibição de dano "Por Acerto", além de Total e DPS.
- A métrica de exibição (Total/DPS/Por Acerto) agora é configurada por dummy individualmente, em vez de ser uma config global do cliente.
- O tooltip do spawner carregado mostra, segurando Ctrl, o que foi de fato personalizado naquele dummy (vida, dano, armadura, curios).
- Alguns apelidos específicos tocam um som de hit próprio (easter egg).
- Colocar um único Dummy Spawner carregado, sozinho, em qualquer grade de crafting agora o "reseta": mantém armadura/curios guardados, mas limpa vida customizada, métrica de dano e nome.

### Correções
- A receita da isca de atração, que estava quebrada.
- Um crash/desconexão ao quebrar um dummy com itens do Curios equipados.
- O número de DPS não mostra mais valores absurdos no primeiro acerto de uma sequência.
- Vários acertos no mesmo tick agora aparecem como um número só, em vez de várias linhas.
- Layout do inventário do dummy, que sobrepunha o rótulo "Inventory" e os primeiros slots.

### Mudanças
- O botão "Limpar Efeitos Negativos" virou "Clear Effects" e agora remove todos os efeitos ativos (bons e ruins), não só os negativos.
- Removida a config global de métrica de exibição, já que agora é definida por dummy.

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

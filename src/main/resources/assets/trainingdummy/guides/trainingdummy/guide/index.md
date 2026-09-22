---
navigation:
  title: Training Dummy
  position: 0
item_ids:
  - trainingdummy:dummy_spawner
---

# Training Dummy

A configurable combat dummy for testing damage, gear and mods - drop it down, hit it, and read
the numbers.

---

## Getting Started

<div alignItems="center" fullWidth={true}>
<ItemImage id="trainingdummy:dummy_spawner" scale="2.5" />
<GameScene zoom="4">
  <Entity id="trainingdummy:dummy" />
</GameScene>
</div>

- **Place** a Dummy Spawner on the ground to summon a dummy.

- **Hit it** with anything to see damage numbers, shown as chat or on-screen (configurable).

- **Right-click with a Stick** to open its inventory (armor, curios, health, display settings).

- **Hit it with a Stick** to remove it - it drops a spawner you can place back down.

- **Rename it** (in an anvil, with a Name Tag) to give it a real Minecraft player's skin.

- **Placing a single loaded Dummy Spawner alone** in any crafting grid resets its health/name/
  display setting, but keeps whatever gear it remembers.

---

## Damage Display

Each dummy can show its incoming damage as **Total**, **DPS**, or **Per Hit** - set per dummy,
not globally.

Per Hit also labels the damage type (Fire, Freezing, Explosive, Lightning, Magic) whenever it
isn't plain physical damage.

---

## Lure Bait

Hold Lure Bait in a dummy's main hand to make nearby hostile mobs target it instead of you.

<Column alignItems="center" fullWidth={true}>
<ItemImage id="trainingdummy:lure_bait" scale="2.5" />
</Column>

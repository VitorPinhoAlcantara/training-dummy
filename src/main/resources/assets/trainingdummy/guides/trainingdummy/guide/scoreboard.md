---
navigation:
  title: Scoreboard Dummy
  parent: index.md
  position: 2
item_ids:
  - trainingdummy:scoreboard_dummy_spawner
---

# Scoreboard Dummy

A second dummy variant that tracks the single biggest hit landed on it, instead of a running
total, and reports it to a leaderboard.

---

## Getting Started

<div alignItems="center" fullWidth={true}>
<ItemImage id="trainingdummy:scoreboard_dummy_spawner" scale="2.5" />
</div>

- **Craft** one with a Dummy Spawner and a Diamond.

- **Place** it down like a regular dummy.

- **Hit it** - if it's big enough to make the top 10, it's added to the leaderboard.

- **Right-click with a Stick** to open the leaderboard and see where you rank.

- **Hit it with a Stick** to remove it - it drops a spawner you can place back down.

---

## Local vs Global

Every server keeps its own **local** leaderboard, top 10 hits only.

If the server owner has it set up, there's also a **global** leaderboard shared across every
server. The global leaderboard resets every week; the local one never does.

The #1 player on whichever board is showing gets their skin rendered next to their name; click and drag
it to spin it around.

---

## Weapon Blacklist

Server owners can block specific weapons (or whole mods) from ever scoring a hit. If a hit with
something like that doesn't show up on the board, that's why.

The following are always blocked, on every server:

- Morgan Sword (Mahou Tsukai)
- Any item from Avaritia
- Any item from Modern Industrialization
- Any item with "Infinity" in its name
- Any item with "Quantum" in its name

Server owners can block more weapons on top of this list.

<p align="center"><img src="publish/icon.png" width="128" alt="InvFull & Durability Alerts icon"></p>

<h1 align="center">InvFull & Durability Alerts</h1>

<p align="center">
Neat, vanilla-styled notifications in the bottom-right corner — with a sound —<br>
when your <b>inventory is full</b> or the <b>item in your hand is about to break</b>.
</p>

<p align="center">
<a href="https://modrinth.com/mod/invfull-durability-alerts">Modrinth</a> ·
<a href="https://www.curseforge.com/minecraft/mc-mods/invfull-durability-alerts">CurseForge</a> ·
<a href="https://github.com/Vivegass/invfull-durability-alerts/issues">Issues</a>
</p>

---

## Features

- **Inventory full** — a small notice slides in at the bottom-right corner the moment your last free slot is taken. It slides away on its own, or as soon as you free a slot.
- **Low durability** — warns once when the tool, weapon or other item in your main hand (and optionally offhand) wears down to a set percentage (10% by default). Shows the item, its name and the uses left.
- **Looks like the game** — notices use the vanilla tooltip frame, so they don't stand out from the HUD. Several notices stack neatly.
- **Clearly audible sounds** — a note-block pling for a full inventory and a light anvil clink for low durability.
- **Your own sounds** — pick any sound from the game, from another mod or from a resource pack; adjust volume and pitch.
- **No spam** — each alert fires once per event, not every tick; nothing fires on world join, in Creative or in Spectator.
- **Client-side only** — works on any server, vanilla or modded.

## Requirements

| | |
|---|---|
| Minecraft | 26.3 |
| Loader | Fabric Loader ≥ 0.20.1 |
| Required | [Fabric API](https://modrinth.com/mod/fabric-api) |
| Optional | [Mod Menu](https://modrinth.com/mod/modmenu) + [Cloth Config](https://modrinth.com/mod/cloth-config) — in-game settings screen |

## Configuration

**In game:** Mod Menu → InvFull & Durability Alerts → settings button.

**By hand:** `config/invfull.json`, then run `/invfull reload`.

```json
{
  "inventoryFull": {
    "enabled": true,
    "showToast": true,
    "playSound": true,
    "sound": "minecraft:block.note_block.pling",
    "volume": 0.8,
    "pitch": 1.2
  },
  "durability": {
    "enabled": true,
    "showToast": true,
    "playSound": true,
    "sound": "minecraft:block.anvil.land",
    "volume": 0.45,
    "pitch": 1.6
  },
  "durabilityPercent": 10,
  "checkOffhand": true,
  "toastSeconds": 4
}
```

### Commands

| Command | What it does |
|---|---|
| `/invfull test full` | Preview the inventory-full notice and sound |
| `/invfull test durability` | Preview the low-durability notice and sound |
| `/invfull reload` | Reload `config/invfull.json` |

### Using your own sound file

1. Make a resource pack with `assets/mysounds/sounds.json`:
   ```json
   { "alert": { "sounds": ["mysounds:alert"] } }
   ```
2. Put your sound at `assets/mysounds/sounds/alert.ogg` (OGG Vorbis, mono is best).
3. Enable the pack and set the sound to `mysounds:alert`.

## Building

Requires JDK 25.

```sh
./gradlew build
```

The jar is written to `build/libs/`.

## License

[MIT](LICENSE)

---

<details>
<summary><b>По-русски</b></summary>

Аккуратные уведомления в правом нижнем углу со звуком, когда **инвентарь заполнен** или **предмет в руке скоро сломается**.

- Плашки в стиле ванильной подсказки, не выбиваются из игры; несколько уведомлений встают стопкой.
- Предупреждение о прочности — один раз, когда предмет в правой (и, по желанию, левой) руке износился до порога (по умолчанию 10%).
- Звуки настраиваются: любой звук игры, другого мода или ресурспака; громкость и высота тона.
- Настройки — через Mod Menu (нужен Cloth Config) или в `config/invfull.json` + `/invfull reload`.
- Проверка: `/invfull test full` и `/invfull test durability`.
- Только клиент, работает на любом сервере.

</details>

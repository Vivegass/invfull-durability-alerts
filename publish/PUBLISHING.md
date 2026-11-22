# Публикация на Modrinth и CurseForge

Готовый файл: `build/libs/invfull-durability-alerts-1.0.0+26.3.jar` (собрать: `./gradlew build`).
Не загружайте `-sources.jar` как основной файл.

## Общие поля

| Поле | Значение |
|---|---|
| Название | InvFull & Durability Alerts |
| Slug / URL | `invfull-durability-alerts` |
| Краткое описание (summary) | Neat bottom-right notifications with a sound when your inventory is full or your held item is about to break. |
| Описание | содержимое `publish/description.md` |
| Иконка | `publish/icon.png` |
| Лицензия | MIT |
| Исходники | https://github.com/Vivegass/invfull-durability-alerts |
| Баг-трекер | https://github.com/Vivegass/invfull-durability-alerts/issues |
| Версия | `1.0.0+26.3`, канал Release |
| Changelog | раздел из `CHANGELOG.md` |
| Версия игры | 26.3 |
| Загрузчик | Fabric |

## Modrinth

- Project type: **Mod**
- Categories: **Utility** (+ по желанию **Game Mechanics**)
- Environment: **Client: required**, **Server: unsupported**
- Dependencies:
  - Fabric API — *required*
  - Mod Menu — *optional*
  - Cloth Config API — *optional*

## CurseForge

- Class: **Mods**, Main category: **Utility & QoL**, доп. категория **Map and Information**
- Environment: **Client**
- Relations:
  - Fabric API — *Required Dependency*
  - Mod Menu — *Optional Dependency*
  - Cloth Config API — *Optional Dependency*
- Java: 25

## Перед публикацией

- [x] Своя иконка
- [ ] 2–3 скриншота: уведомление «Инвентарь заполнен», «Предмет скоро сломается», экран настроек
- [ ] Проверить в игре `/invfull test full`, `/invfull test durability` и экран настроек
- [ ] На GitHub создать релиз `v1.0.0+26.3` с этим jar

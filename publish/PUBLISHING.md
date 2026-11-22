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
| Демо GIF | `publish/demo.gif` (640×360, 8 МБ, уже в description.md). Если CurseForge не покажет внешнюю ссылку — загрузите файл во вкладку Images проекта и вставьте оттуда |
| Баннер | `publish/banner.png` (уже вставлен в начало description.md) |
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
- [x] Релиз на GitHub `v1.0.0+26.3` создан

## Выкладка версий одной командой

Сборка — мультиверсионная ([Stonecutter](https://stonecutter.kikugie.dev/)): один код, jar на каждую версию Minecraft. Версии и зависимости — в `stonecutter.properties.toml`, ID проектов — в `gradle.properties`. Публикация — [mod-publish-plugin](https://modmuss50.github.io/mod-publish-plugin/), каждый jar уходит со своим списком версий игры.

Токены — только в переменных окружения (сохраните сами, никому не показывайте):

```powershell
setx MODRINTH_TOKEN "ваш_токен"
setx CURSEFORGE_TOKEN "ваш_токен"
```

### Каждый релиз

1. Поднимите `mod.version` в `stonecutter.properties.toml`, добавьте раздел `## <версия>` в `CHANGELOG.md`.
2. Пробный прогон (ничего не публикует, файлы в `versions/*/build/publishMods/`):
   ```powershell
   ./gradlew publishMods
   ```
3. Публикация всех версий:
   ```powershell
   ./gradlew publishMods -Prelease
   ```
   Одна версия: `./gradlew :1.21.11:publishMods -Prelease`.

### NeoForge (через Sinytra Connector)

Для 1.21.1, 26.1.2 и 26.2 дополнительно собирается `*-neoforge.jar` (тот же Fabric-jar + служебный файл, чтобы у него был свой хеш) и публикуется отдельной версией с загрузчиком **NeoForge** и обязательными зависимостями **Sinytra Connector** и **Forgified Fabric API**. Так мод находится в лаунчерах (Prism и др.) при фильтре NeoForge, и зависимости ставятся автоматически. Список версий с Connector — `hasConnector` в `build.gradle.kts`.

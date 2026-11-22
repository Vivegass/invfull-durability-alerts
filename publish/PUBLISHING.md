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

## Выкладка версии одной командой

Сборка подключена к [mod-publish-plugin](https://modmuss50.github.io/mod-publish-plugin/): jar, changelog (раздел текущей версии из `CHANGELOG.md`), версия игры, загрузчик и зависимости уходят на площадки автоматически.

### Один раз

1. Создайте проекты на сайтах (поля — в таблицах выше). После модерации возьмите ID:
   - **Modrinth:** страница проекта → ⋮ → *Copy ID* (8 символов).
   - **CurseForge:** справа на странице проекта, *Project ID* (число).
2. Впишите их в `gradle.properties`:
   ```properties
   modrinth_id=XXXXXXXX
   curseforge_id=1234567
   ```
3. Создайте токены и сохраните в переменные окружения (вводите сами, никому не показывайте):
   - Modrinth: https://modrinth.com/settings/pats — права *Create versions*, *Read versions*, *Write versions*.
   - CurseForge: https://legacy.curseforge.com/account/api-tokens
   ```powershell
   setx MODRINTH_TOKEN "ваш_токен"
   setx CURSEFORGE_TOKEN "ваш_токен"
   ```
   После `setx` перезапустите терминал/приложение.

### Каждый релиз

1. Поднимите `version` в `gradle.properties`, добавьте раздел в `CHANGELOG.md`.
2. Пробный прогон (ничего не публикует, файлы в `build/publishMods/`):
   ```powershell
   ./gradlew publishMods
   ```
3. Публикация:
   ```powershell
   $env:GITHUB_TOKEN = gh auth token   # если нужен и релиз на GitHub
   ./gradlew publishMods -Prelease
   ```

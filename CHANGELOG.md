# CHANGELOG

## [2.2.1]
- Most of the machines now supports `null` as input value for GUI support

## [2.1.3]
- Fixed a bug where inputs with NBT tags were ignored, which could result in recipes being treated as duplicates.

## [2.1.2]
- Hot-fix the adding recipes problems from the previous path (ignoring item quantities, wrong items showing at JEI)

## [2.1.1]
- Registered machines recipes now can be dumped using the `/fet dump [machineName|all]` command
- Fixed a bug when adding custom recipes would not work in modpacks with compatibility mods (e.g. '[JAOPCA](https://www.curseforge.com/minecraft/mc-mods/jaopca)')

## [2.0.3]
- Fixed Combustion Generator recipes could not be removed (and therefore the addition could not be canceled)
- Changes for future FET-GUI accessibility
- Other minor code improvements

## [2.0.2]
- Migrated build tooling from ForgeGradle 2.3 to RetroFuturaGradle 2.0.2
- Fixed tank double output add bug
- Fixed CombustionGenRecipes error message
- Fixed Vat & SliceNSplice recipe conflict check
- ...Minor code design changes...

## [2.0.1]
- Initial public release
- Updated compatibility with EnderIO 5.3.72
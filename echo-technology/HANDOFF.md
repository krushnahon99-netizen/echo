# Echo Technology — Handoff for the next agent

You are continuing an unfinished Fabric mod. Read this file first, then `ALL_SOURCES.txt`
(every source file so far, concatenated, each preceded by a `===== FILE: path =====` header).

## Goal (unchanged from the original brief)
Dark, sculk-inspired tech mod. Reactor burns fuel -> energy -> conduits -> machines.
Fabricator crafts items with energy. Tools + upgrades. Final deliverable: a working `.jar`
(the user has Minecraft Java Edition but no PC to build; the agent must build the jar).

## Fixed configuration
- Mod ID `echo_technology`, package `com.echotechnology`
- Minecraft 1.21.1, Fabric, Java 21, Gradle + Fabric Loom, Yarn mappings
- Exact versions: see `gradle.properties` (Fabric API 0.116.7+1.21.1, Yarn 1.21.1+build.3)
- Gradle wrapper 8.14.3 is included (`gradlew`, `gradle/wrapper/`)
- Client code lives in `src/client/java` (Loom split source sets)

## Environment notes (v0 sandbox)
No Java was preinstalled. The previous agent did:
```
mkdir -p /vercel/share/jdk && cd /vercel/share/jdk
curl -sL -o jdk.tar.gz "https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk/hotspot/normal/eclipse"
tar xzf jdk.tar.gz && rm jdk.tar.gz
export JAVA_HOME=$(ls -d /vercel/share/jdk/jdk-21*) PATH=$JAVA_HOME/bin:$PATH
cd <project>/echo-technology && ./gradlew build --console=plain
```
Run the build in the background; the first run downloads/remaps Minecraft (several minutes).
Verify Yarn signatures with `javap` against the mapped jar in
`.gradle/loom-cache/minecraftMaven/...minecraft-common...jar` instead of guessing.

## Status
| Stage | State |
|---|---|
| Skeleton mod (entrypoint, fabric.mod.json, build.gradle) | Built successfully once |
| `energy/` EchoEnergyStorage, EnergyTransfer, EnergyNode, EnergyDistribution | Written, not compiled since |
| `upgrade/` UpgradeType, UpgradeModifiers | Written |
| `recipe/` FabricatorRecipe, FabricatorRecipes | Written |
| `machine/` ReactorCore, FabricatorCore (pure-Java logic, unit-testable) | Written |
| `config/` EchoConfig (JSON config) | Written |
| `registry/` ModBlocks, ModItems, ModBlockEntities, ModScreenHandlers, ModComponents, ModItemGroups | Written, reference classes that DO NOT EXIST yet |
| `block/` MachineBlock, EchoReactorBlock, EchoFabricatorBlock, EchoChargerBlock, EchoConduitBlock | Written |
| `block/entity/` MachineBlockEntity (base: energy, NBT, sync) | Written |
| **Current build** | **Broken** — missing referenced classes (expected) |

## Remaining work, in priority order
1. Grep the registries/blocks for every referenced class that does not exist and create it:
   reactor / fabricator / charger block entities, screen handlers, item classes.
2. Screens (client): reactor, fabricator, charger. Register via `HandledScreens.register` in
   `EchoTechnologyClient`. Show live energy/progress from synced properties (`SyncedInts`).
3. Items: Echo Mining Tool (energy-based area/fast mining with cooldown), Echo Scanner,
   Efficiency + Capacity upgrade items (must change real machine logic via UpgradeModifiers).
4. Resources: blockstates, block/item models, 16x16 textures (dark metal + cyan),
   crafting recipes JSON (1.21.1 uses `data/echo_technology/recipe/` — singular folder),
   loot tables (`data/echo_technology/loot_table/blocks/`), mineable tags,
   `assets/echo_technology/lang/en_us.json`, mod icon.
5. JUnit tests for energy limits, transfer, recipes, insufficient energy, output space, upgrades
   (pure-Java classes in `energy/`, `machine/`, `recipe/`, `upgrade/` are designed for this).
6. `./gradlew build`, fix errors, then COPY the jar out of `build/libs/` into a tracked
   folder (e.g. `echo-technology/release/`) because `build/` is gitignored and will not appear
   in the user's "Download ZIP".
7. README with install steps + manual test checklist.

If budget is tight: drop Charger and Scanner first; keep Reactor -> Conduit -> Fabricator,
Mining Tool, two upgrades, textures, and a successful jar.

## Yarn 1.21.1 API reminders (verify with javap)
- `BlockWithEntity` requires `getCodec()` returning `MapCodec`; tickers via `validateTicker`.
- `onUse(BlockState, World, BlockPos, PlayerEntity, BlockHitResult)` returns `ActionResult`.
- BlockEntity NBT: `writeNbt/readNbt(NbtCompound, RegistryWrapper.WrapperLookup)`.
- Screens with block position: Fabric `ExtendedScreenHandlerType` with a `PacketCodec`.
- Items with data use data components (`ModComponents`), not raw item NBT.

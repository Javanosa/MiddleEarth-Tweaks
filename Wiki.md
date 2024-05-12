## MiddleEarth-Tweaks Wiki - Press CTRL + F to search

### Troubleshooting
- Set `Config > General > DebugLVL = 3` to enable Log-output `0 = None, 1 = Minor, 2 = Middle, 3 = High`
- Disable/Enable core modifications in ASM config file `/config/metweaks-ASM.properties`
- Please contact me if there is any question or problem.

### Vertical Slabs
- Press Keybind V to toggle while holding a Slab
- Sneak to use view direction while placement
- Just one item and no extra crafting needed
- Mirror vertical slab when placed on the side of another VerticalSlab `config > Blocks > Mirror VerticalSlabs = False`
- You can disable this feature completely in `config > Blocks > VerticalSlabs = False`
- Synchronized with server

### Ally Conquest Spawns
- THis mod fixes a bug where conquest cant be decreased by fighting allies spawned from conquest aid
- Option to disable Allies spawning from conquest too `Config > Misc > AllyConquest Spawns`

### Mount Speeds
- Speed values can be reverted and changes anytime, they dont affect the world itself.
- Primarly supposed to prevent server lag and ensure fairplay between a group
- Global speed value can be set in `Config > Mountspeed > Global Mountspeed Min/Max` and `Use Global Mountspeed` needs to be enabled
- To use entity specific speeds, you can disable `Use Global Mountspeed` and take a look at `Config > Individual Mountspeeds`
- The following Value can be used as a reference:

| Activity   | Speed |
| --------- | ------- |
| Boat | 0.17   |
| Minecart | 0.20   |
| Average Horse | 0.20   |
| SprintJump | 0.17   |
| Sprint | 0.13   |
| Walk | 0.10   |
| Swim | 0.05   |
| FlyCreative | 0.25   |

- *Note that speed values shown by the HUD on other entities than mounts are handled differently*

### Trapdoor / Fencegate Placement
- Now takes view direction into account, so you dont need an adjacent block to place a specific rotation
- Fencegates dont anymore need a block below them
- If its not present on the server you play on, i suggest disabling `metweaks-ASM.properties > trapdoorPlacement`

### Potion IDs
- Supposed to relieve issues with other Mods using the same ID
- Will be synced with server
- Option can be found in `config > Misc > KillPotionID`
- Free IDs usually are: `0, 24, 25, 26, 27, 28, 29, 31`

### Bark Blocks
- Can be crafted of 4 logs
- Does only add barked variants, no new block IDs
- Can be disabled in `config > Blocks > BarkBlocks`

### Configurations
- The config files are:
- `/config/metweaks.cfg`
- `/config/metweaks-ASM.properties`
- In case of major config changes, a backup file will be created automatically. There you can find your old settings.

### LOTR Compass
- use the options in `config > HUD Elements > Compass...` to move it around.
- If `Compass Right` is turned off, then the Compass will appear on the left side.
- If `Compass Bottom` is turned off, then the Compass will appear on the top.
- To move the compass only slightly, you can modify `Compass X / Y`. The effect of the values move to the left or right / top or bottom depending on `Compass Right / Bottom`
- If you want to move the compass partically out of the screen or very close to the edge, disable `Auto Adjust Values`

### ME-Tweaks Status Message
- Displays Messages like Healthbar ON / OFF or Vertical Slabs ON / OFF
- Modify `config > HUD Elements > ME-Tweaks Status Message Offset-Y` to move
- Can be accessed from API as method actionBar(...);

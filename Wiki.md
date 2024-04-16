## MiddleEarth-Tweaks Wiki

### Troubleshooting
- Set `Config > General > DebugLVL = 3` to enable Log-output `0 = None, 1 = Minor, 2 = Middle, 3 = High`
- Disable/Enable core modifications in ASM config file `/config/metweaks-ASM.properties`
- Please contact me if there is any question or problem.

### Vertical Slabs
- Press Keybind V to toggle while holding a Slab
- Sneak to use facing direction while placement
- You can disable this feature completely in `config > General > VerticalSlabs = False`
- Synchronized with server

### Potion IDs
- Supposed to relieve issues with other Mods using the same ID
- Will be synced with server
- Option can be found in `config > General > KillPotionID`
- Free IDs usually are: `0, 24, 25, 26, 27, 28, 29, 31`

### Mount Speeds
- Speed values can be reverted and changes anytime, they dont affect the world itself.
- Primarly supposed to prevent server lag and ensure fairplay between a group
- Global speed value can be set in `Config > General > GlobalMountspeedMin/Max` and `UseGlobalMountSpeed` needs to be enabled
- To use entity specific speeds, you can disable `UseGlobalMountSpeed` and take a look at `Config > Customize Mountspeeds`
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
  



## MiddleEarth Tweaks - The Lord of the Rings Addon
QoL Mod to enhance and expand feature of the LOTR Mod by Mevans
## Download: [Curseforge](https://www.curseforge.com/minecraft/mc-mods/lotr-middleearth-tweaks) - [Modrinth](https://modrinth.com/mod/middleearth-tweaks)
## [Wiki](https://github.com/Javanosa/MiddleEarth-Tweaks/blob/main/Wiki.md)
## [Developer API](https://github.com/Javanosa/MiddleEarth-Tweaks/blob/main/MeTweaksAPI.java)
## [Discord](https://discord.gg/maHfVhGaah)

### Known Issues: *(I currently cannot push updates)*
1. Crash with ET Futurum > 2.6.1. Solutions: 
   - Use ET Futurum up to 2.6.0
   - Remove `setHardness()` in constructor of `metweaks.block.VerticalSlab` and add a method to class:

  ```java
public float getBlockHardness(World world, int x, int y, int z) {
    return slab.getBlockHardness(world, x, y, z);
}
```

2. Error when using guards advanced settings. Solutions:
   - Use MiddeEarth Tweaks 1.4.2 without unit transfer feature
   - correct last two packet ids from 2,3 to 7,8 in `metweaks.network.NetworkHandler`

3. Crash when Rider out of height limit bounds, Solutions:
  - This is server side only
  - Turn off `Config > Misc > Riders Avoid Suffocating in Blocks`
  - In class `metweaks.guards.DontSuffocateAI` and method `isSuffocating` add after definition of y local variable:

   ```java
if(y < 0 || y > 256) return false;
```

*You may use Recaf or recompile the classes and replace them like in a zip archive. See bytecode-patches.txt for more info*

## Features:
- Vertical Slabs (can be disabled)
- Bark Blocks (no new block id) 
- Customize Mountspeeds
- Healthbar and Attribute Info 
- Advanced Guardmode 
- Toggle-Guardmode-Horn
- Fix Mounted NPCs getting lost 
- Riders dont suffocate in Blocks anymore
- /conquestdecay to control/disable conquest decrease 
- Fix Ally Aid Conquest unclearable
  
&nbsp;
- Upgradeable Ranged Weapons for Hireds
- Unit Overview (U Key) for mass assignment, Summary and Inventory management
- Hoe's can now harvest hay, thatch, reed and wattle&daub blocks
- Fix Npc's offering a quest but not showing questoffer-mark
- Fix Bombardiers destroying blocks within protection
- Foods like Berries can be consumed faster to balance their low saturation
  
&nbsp;
- Option to prevent ring writing overflowing the edge
- Ranged AI Improvements
- Randomly Enchant Ranged NPC Weapons now too
- NPC's are now able to use plates, spears, termites as ranged weapons
- Transfer Hired Units to other Players
- AI Conquest (NPCs conquest on their own, default disabled)
- New Commands with lotr specific Features:
  - /entitykill
  - /entitytp
  - /entityeffect
  - /entitystack
- Potion ID Config 
- Improved Trapdoor and Fencegate placement  
  
&nbsp;
- Very Customizable 
- Every Feature can be disabled, more in metweaks-ASM.properties
  

## More Features:

- Fix Horse Chest Performance 
- Attacks from NPCs of the Harad Continent are not sluggish anymore
- Melon Blocks can be harvested with an axe now
- Foods like Berries can be consumed faster to balance their low saturation
- Tauredain Blowgunners have a low chance of shooting poisened darts
- Always set Bedspawn on attempt 
- Bed: Enemies nearby - in MiddleEarth 
- Fix Spawners causing Inventory lag
- Fix Turkish Language File Issue 
- /conqClear command to clear up specific faction conquest
- Option to disable Structures
  

Work in Progress - Many more features comming! 
 
My vision is a mod that suits everyone with its customization. It adds features for the LOTR Mod by Mevans, just plain Vanilla and other great 1.7.10 Mods

 
All rights for the Lord of the Rings Mod go to Mevans

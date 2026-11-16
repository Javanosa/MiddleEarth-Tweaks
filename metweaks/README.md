### Source Code MiddleEarth-Tweaks 1.6.22

**Libraries to build (into the libs folder):**
- LOTRMod-v36.15-dev
- fastutil
- Chisel-2.9.5.11-dev
- libs.jar contains:
	- astikoor-1.7.10-1.0.0-dev
	- CustomNPC-Plus-1.9.3-dev
	- MorePlayerModels-Plus-4.2-dev
	- etfuturum-2.6.2-dev
	- angelica-1.0.0-beta68a-dev
	- angelica 2.0.0-dev / embed-org.embeddedt.celeritas:celeritas-common-2.5.4-GTNH-dev
	
See [License](https://github.com/Javanosa/MiddleEarth-Tweaks/blob/main/License.md) for more info

To Build Jar:
- just once if you havnt used forgegralde 1.7.10 before:
	- `/gradlew setupDecompWorkspace`
	- `/gradlew eclipse`
- `/gradlew build`

To Run Dev Environment:
- `/gradlew runClient`

If you have not installed forgegradle 1.7.10 prior to 2022, you may need to use a forgegradle alternative.

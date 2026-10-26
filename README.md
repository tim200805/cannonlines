# Cannon Lines (Fabric, Minecraft 1.21.11, client only)

Draws a glowing beam (64 blocks) out of the BACK of every chest near you, which is the way a cannon fires.

STATUS: first version, written but NOT compiled or tested yet.

Command (client side only): /cannonlines  toggles it on/off.

## Build the jar
Option A, nothing to install: create a free GitHub repo, upload everything in this folder (including the
hidden .github folder), open the Actions tab, run "build", download the "cannonlines-jar" artifact.
Use the file WITHOUT "-sources" in its name.

Option B, on your PC: install JDK 21, then in this folder run `gradlew build` (Windows) or `./gradlew build`.
The jar ends up in build/libs/.

Put the jar in your mods folder next to Fabric API.

## Note
Check lifesteal.net's rules before using any overlay on the server.

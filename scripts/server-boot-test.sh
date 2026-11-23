#!/usr/bin/env bash
# Boots a real Forge 1.20.1 dedicated server with one built Pinkified Minecraft jar in its mods folder, lets the world
# run for 20 seconds and stops. Pinkified is client-only (clientSideOnly=true in mods.toml), so a server that has the
# jar installed by mistake, for example from a shared modpack, must still start cleanly. Fails if the server crashes,
# never finishes starting, or its log has an error that names the mod.
#   usage: scripts/server-boot-test.sh <path to the mod jar>
set -u
JAR="$(cd "$(dirname "$1")" && pwd)/$(basename "$1")"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MC=$(grep '^minecraft_version=' "$ROOT/1.20.1/gradle.properties" | cut -d= -f2)
FORGE=$(grep '^forge_version=' "$ROOT/1.20.1/gradle.properties" | cut -d= -f2)
SERVER="$ROOT/build/boot-test-server"
rm -rf "$SERVER"; mkdir -p "$SERVER/mods"
cd "$SERVER"

curl -fsSL -o installer.jar "https://maven.minecraftforge.net/net/minecraftforge/forge/$MC-$FORGE/forge-$MC-$FORGE-installer.jar"
java -jar installer.jar --installServer > install.log 2>&1 || { tail -40 install.log; exit 1; }
cp "$JAR" mods/

echo "eula=true" > eula.txt
cat > server.properties <<PROPS
online-mode=false
function-permission-level=4
spawn-protection=0
view-distance=4
simulation-distance=4
level-type=minecraft\:flat
level-seed=20261123
initial-enabled-packs=vanilla,file/pink_boot
PROPS
PACK="world/datapacks/pink_boot"
mkdir -p "$PACK/data/pink_boot/functions" "$PACK/data/minecraft/tags/functions"
echo '{"pack":{"pack_format":15,"description":"Pinkified Minecraft boot test"}}' > "$PACK/pack.mcmeta"
echo '{"values":["pink_boot:start"]}' > "$PACK/data/minecraft/tags/functions/load.json"
printf 'say PINK_BOOT_STARTED\nschedule function pink_boot:finish 400t\n' > "$PACK/data/pink_boot/functions/start.mcfunction"
printf 'say PINK_BOOT_OK\nstop\n' > "$PACK/data/pink_boot/functions/finish.mcfunction"

LOG="$SERVER/boot.log"
bash run.sh nogui < /dev/null > "$LOG" 2>&1 &
PID=$!
result=timeout
for _ in $(seq 1 120); do
  sleep 5
  if grep -q "PINK_BOOT_OK" "$LOG"; then result=ok; break; fi
  if grep -q -E "This crash report has been saved|Encountered an unexpected exception|Failed to start the minecraft server|Failed to load function pink_boot" "$LOG"; then result=crash; break; fi
  if ! kill -0 $PID 2>/dev/null; then result=exited; break; fi
done
sleep 10
pkill -P $PID 2>/dev/null; kill $PID 2>/dev/null; wait $PID 2>/dev/null

grep -n -i -E "PINK_BOOT|pinkified|Pinkified|clientSideOnly|client side|Done \(" "$LOG" logs/debug.log 2>/dev/null | head -40
for f in crash-reports/*.txt; do [ -f "$f" ] && { echo "=== $f"; head -80 "$f"; }; done
grep -n -E "ERROR|Exception|Caused by" "$LOG" | head -60
echo "Result: $result"
[ "$result" = ok ] || exit 1
grep -q "This crash report has been saved" "$LOG" && exit 1
grep -q -E "Done \([0-9.]+s\)!" "$LOG" || { echo "The server never finished starting"; exit 1; }
if grep -E "ERROR|Exception|Caused by" "$LOG" | grep -q -E 'com.sugarplumstudios|pinkified([^-.a-z]|$)'; then echo "The log has errors naming Pinkified"; exit 1; fi
echo "Server boot test passed"

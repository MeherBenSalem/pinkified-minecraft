#!/usr/bin/env bash
# Starts a dev server for one Minecraft version and loader with Pinkified Minecraft on the classpath, lets the world
# run for 20 seconds after it finished starting, and stops it. Pinkified is client-only, so a server with the mod
# installed (for example from a shared modpack) must still start cleanly. Fails if the server crashes, a mixin
# fails, it never finishes starting, or its log has an error that names the mod.
# Based on the-witch-hut's scripts/server-smoke-test.sh.
#   usage: scripts/server-smoke-test.sh <minecraft version folder> <fabric|forge|neoforge>
set -u
VERSION="$1"; LOADER="$2"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/$VERSION"
RUN="$LOADER/runs/server"
rm -rf "$RUN/world" "$RUN/crash-reports" "$RUN/logs"
mkdir -p "$RUN"
echo "eula=true" > "$RUN/eula.txt"
cat > "$RUN/server.properties" <<PROPS
online-mode=false
spawn-protection=0
view-distance=4
simulation-distance=4
level-type=minecraft\:flat
level-seed=20261123
PROPS
if [ "$VERSION" = 1.20.1 ] && [ "$LOADER" = forge ]; then
  # In the Forge 1.20.1 dev environment the LAN advertiser loads a client-only class and the server stops.
  mkdir -p "$RUN/defaultconfigs"
  printf '[server]\n\tadvertiseDedicatedServerToLan = false\n' > "$RUN/defaultconfigs/forge-server.toml"
fi

LOG="$ROOT/server-smoke-$VERSION-$LOADER.log"
./gradlew ":$LOADER:runServer" --stacktrace \
  -Porg.gradle.java.installations.fromEnv=JAVA_HOME_17_X64,JAVA_HOME_21_X64,JAVA_HOME_25_X64 < /dev/null > "$LOG" 2>&1 &
PID=$!
result=timeout
for _ in $(seq 1 240); do
  sleep 5
  if grep -q -E "Done \([0-9.,]+m?s\)!" "$LOG"; then result=started; break; fi
  if grep -q -E "This crash report has been saved|Encountered an unexpected exception|Failed to start the minecraft server|Mixin apply.*failed|InvalidInjectionException|BUILD FAILED" "$LOG"; then result=crash; break; fi
  if ! kill -0 $PID 2>/dev/null; then result=exited; break; fi
done
if [ "$result" = started ]; then
  sleep 20
  if grep -q -E "This crash report has been saved|Exception in server tick loop|InvalidInjectionException" "$LOG"; then result=crash; else result=ok; fi
fi
pkill -P $PID 2>/dev/null; kill $PID 2>/dev/null
pkill -f "runs/server" 2>/dev/null
wait $PID 2>/dev/null

grep -n -i -E "pinkified|Done \(|Loading [0-9]+ mods" "$LOG" | grep -v -E "Download|Task :" | head -40
for f in "$RUN"/crash-reports/*.txt; do [ -f "$f" ] && { echo "=== $f"; head -80 "$f"; }; done
grep -n -E "ERROR|Exception|Caused by|[Mm]ixin.*(fail|error)" "$LOG" | head -60
[ "$result" = ok ] || tail -60 "$LOG"
echo "Result: $result"
[ "$result" = ok ] || exit 1
if grep -E "ERROR|Exception|Caused by" "$LOG" | grep -q -E 'com.sugarplumstudios|pinkified([^-.a-z]|$)'; then echo "The log has errors naming Pinkified"; exit 1; fi
echo "Server smoke test passed"

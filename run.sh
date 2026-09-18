#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

if [ -f ".env" ]; then
  source .env
fi

JAR="target/cli-task-tracker-1.0.0.jar"
# Recompila si el jar no existe o si hay cambios sin empaquetar
# (algún archivo bajo src/ o pom.xml es más reciente que el jar).
if [ ! -f "$JAR" ] || [ -n "$(find src pom.xml -newer "$JAR" -print -quit)" ]; then
  echo "==> Compilando..."
  mvn package -q -DskipTests
fi

echo "==> Levantando CLI Task Tracker..."
java -jar "$JAR"

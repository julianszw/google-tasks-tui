# Refresh del jar en cada lanzamiento de `taskmaster`

## Why

El comando global `taskmaster` (en `~/.local/bin/taskmaster`) delega en `run.sh`,
que solo recompila el jar si este **no existe**:

```bash
if [ ! -f "$JAR" ]; then
  mvn package -q -DskipTests
fi
```

Si `target/cli-task-tracker-1.0.0.jar` ya existe, `run.sh` lo ejecuta tal cual,
ignorando cambios recientes en el código. Esto ya causó un incidente real: un fix
commiteado (re-autenticación ante token expirado, change `037`) no se reflejó al
ejecutar `taskmaster` porque el jar en `target/` seguía desactualizado.

El objetivo es que `taskmaster` corra **siempre** el código más reciente, sin
quedar atrapado en un binario viejo.

## What Changes

- Modificar `run.sh` para recompilar/empaquetar siempre que el código fuente sea
  más reciente que el jar, en lugar de compilar solo si el jar falta.

  Enfoque elegido — **(A) recompilar por comparación de timestamps**:

  ```bash
  JAR="target/cli-task-tracker-1.0.0.jar"
  if [ ! -f "$JAR" ] || [ -n "$(find src pom.xml -newer "$JAR" -print -quit)" ]; then
    echo "==> Compilando..."
    mvn package -q -DskipTests
  fi
  ```

  La condición dispara el build cuando:
  - el jar no existe, o
  - algún archivo bajo `src/` o el propio `pom.xml` tiene timestamp posterior al
    jar (es decir, hay cambios no empaquetados).

  Incluir `pom.xml` cubre cambios de dependencias/plugins que requieren
  reempaquetar el shade jar.

- Mantener el resto del flujo de `run.sh` intacto (carga de `.env`, ejecución
  con `java -jar`).

### Enfoques considerados y descartados

- **(B) Recompilar siempre**: garantía total, pero añade varios segundos de
  `mvn package` (con shade) a *cada* arranque, penalizando el uso frecuente de la
  CLI. Descartado por latencia de arranque.
- **(C) Compilar en caliente y correr desde `target/classes` + classpath** (como
  `rebuild-run.sh`): elimina el shade jar, rompe el modelo de artefacto único y
  sigue forzando compilación en cada arranque. Descartado por cambiar el modelo
  de distribución.

**(A)** es robusto (no deja jar desactualizado) y simple, con arranque rápido
cuando no hubo cambios.

## Impact

- **Scripts**: `run.sh` (único archivo funcional modificado). `rebuild-run.sh` y
  `~/.local/bin/taskmaster` no cambian.
- **Capacidades/specs**: sin delta de spec. Es un cambio de tooling de
  build/lanzamiento, no modifica ninguna capacidad documentada en
  `openspec/specs/`.
- **Tests**: no se agregan tests unitarios; la verificación es manual
  (ver `tasks.md`).
- **Usuarios**: `taskmaster` pasará a reflejar los cambios de código sin pasos
  manuales extra. Primer arranque tras un cambio tardará lo que tarda `mvn
  package`; los arranques posteriores sin cambios serán instantáneos.

# Tareas

- [x] Modificar `run.sh` para recompilar cuando el jar no exista **o** el código fuente (`src/`, `pom.xml`) sea más reciente que el jar (comparación de timestamps con `find ... -newer`)
- [x] Mantener la condición en una sola línea robusta (soportar paths con espacios y `set -e` sin efectos colaterales)
- [x] Verificar manualmente el caso "jar desactualizado": tocar un archivo de `src/` o `pom.xml`, ejecutar `run.sh` y confirmar que recompila
- [x] Verificar manualmente el caso "jar al día": ejecutar `run.sh` dos veces seguidas y confirmar que la segunda no recompila
- [x] Verificar manualmente el caso "sin jar": borrar `target/cli-task-tracker-1.0.0.jar` y confirmar que recompila
- [x] Confirmar que `taskmaster` (wrapper global) refleja el fix end-to-end tras modificar código y relanzar

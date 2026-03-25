# Matriz de Versoes

Data de referencia desta matriz: `25 de marco de 2026`.

## Releases oficiais mais recentes do Minecraft

- `26.1` lancada em `24 de marco de 2026`
- `1.21.11` lancada em `9 de dezembro de 2025`
- `1.21.10` lancada em `7 de outubro de 2025`
- `1.21.9` lancada em `30 de setembro de 2025`
- `1.21.8` lancada em `17 de julho de 2025`
- `1.21.7` lancada em `30 de junho de 2025`

## Estado atual do repositorio

| Modulo | Loader | Status | Validacao local | JVM recomendada |
| --- | --- | --- | --- | --- |
| `versions/forge-1.7.10` | Forge `10.13.4.1614-1.7.10` | Implementado com fallback legacy | `build` | JDK `8` |
| `versions/forge-1.12.2` | Forge `14.23.5.2864` | Implementado com fallback legacy | `build` | JDK `8` |
| `versions/forge-1.16.5` | Forge `36.2.42` | Implementado com fallback legacy | `build` | JDK `21` para Gradle, target Java `8` |
| `versions/forge-1.19.4` | Forge `45.4.3` | Implementado | `build` | JDK `21` para Gradle, target Java `17` |
| `versions/forge-1.20.1` | Forge `47.4.18` | Implementado | `build` e `runServer` | JDK `21` para Gradle, target Java `17` |
| `versions/forge-1.21` | Forge `51.0.33` | Implementado | `build` | JDK `21` |
| `versions/forge-1.21.1` | Forge `52.1.14` | Implementado | `build` e `runServer` | JDK `21` |
| `versions/forge-1.21.3` | Forge `53.1.10` | Implementado | `build` | JDK `21` |
| `versions/forge-1.21.4` | Forge `54.1.16` | Implementado | `build` | JDK `21` |
| `versions/forge-1.21.5` | Forge `55.1.10` | Implementado | `build` | JDK `21` |
| `versions/forge-1.21.6` | Forge `56.0.9` | Implementado | `build` | JDK `21` |
| `versions/fabric-1.20.1` | Fabric Loader `0.18.5` | Implementado | `build` | JDK `21` |
| `versions/fabric-1.21` | Fabric Loader `0.18.5` | Implementado | `build` | JDK `21` |
| `versions/fabric-1.21.1` | Fabric Loader `0.18.5` | Implementado | `build` | JDK `21` |
| `versions/fabric-1.21.2` | Fabric Loader `0.18.5` | Implementado | `build` | JDK `21` |
| `versions/fabric-1.21.3` | Fabric Loader `0.18.5` | Implementado | `build` | JDK `21` |
| `versions/fabric-1.21.4` | Fabric Loader `0.18.5` | Implementado | `build` | JDK `21` |
| `versions/fabric-1.21.5` | Fabric Loader `0.18.5` | Implementado | `build` | JDK `21` |
| `versions/fabric-1.21.6` | Fabric Loader `0.18.5` | Implementado | `build` | JDK `21` |

## Observacoes importantes

- `Forge 1.21.2` nao entrou na matriz porque a promocao oficial do Forge para essa patch version nao estava disponivel durante a montagem do repositorio.
- Em `1.16.5`, `1.12.2` e `1.7.10` o bioma `Lush Caves` nao existe no jogo base, entao esses ports ficaram com foco na mecanica central de regrowth e duplicacao; a geracao natural foi tratada como fallback legacy.
- As linhas `1.12.2` e `1.7.10` exigem JDK `8` para build local por causa do tooling antigo do ForgeGradle.

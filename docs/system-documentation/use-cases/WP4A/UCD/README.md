Use Case Diagram — WP4A
======================

Este ficheiro contém o diagrama de casos de uso para o sub-bloco WP4A (manutenção).

Ficheiros:
- `usecase_wp4a.puml` – diagrama PlantUML com atores e principais use cases:
  - Create Maintenance Template (criar templates aplicáveis a modelos de aeronave)
  - Register Maintenance Part (registar peças de manutenção)
  - Create Maintenance Record (criar registos de manutenção)
  - Update Record Status & Notes (atualizar estado e notas; usa ETag / If-Match)
  - List / Search Maintenance Records
  - Get Maintenance Totals / Reports

Actores identificados no diagrama:
- Maintenance Manager — gere templates e peças; consulta relatórios
- Technician — cria e atualiza registos de manutenção
- Scheduler / System — representa triggers automáticos (e.g., agendamento por horas de voo)

Observações técnicas:
- No código actual o `CreateMaintenanceTemplate` aceita nomes de modelos (strings) e o use-case resolve nomes para entidades `AircraftModel`.
- As actualizações de registo aplicam locking optimista (verificação de versão via cabeçalho `If-Match`).

Gerar SVG
---------
Para gerar o SVG a partir do `.puml`, execute o script de projeto `generate-svg.sh` na raiz do repositório (por exemplo em Git Bash ou WSL):

```bash
bash ./generate-svg.sh
```

Se preferir gerar apenas este ficheiro com PlantUML localmente e tiver plantuml.jar:

```bash
java -jar plantuml.jar usecase_wp4a.puml
```

Path neste repositório:
`docs/system-documentation/use-cases/WP4/WP4A/`


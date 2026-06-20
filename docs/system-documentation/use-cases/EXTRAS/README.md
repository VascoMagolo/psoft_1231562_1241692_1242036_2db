# EXTRAS - Extra Use Cases

This package documents additional use cases that were implemented but are not part of the main project deliverables.

## Covered use cases

- `UpdateAircraftUseCase` - Update aircraft details (`PATCH /api/aircrafts/{registration}`)
- `DeleteAircraftUseCase` - Delete an aircraft (`DELETE /api/aircrafts/{registration}`)
- `DeleteAircraftModelUseCase` - Delete an aircraft model (`DELETE /api/aircraftModels/{modelName}`)
- `UpdateMaintenanceComponentsUseCase` - Update maintenance parts and templates (`PATCH /api/maintenance/parts/{partNumber}`, `PATCH /api/maintenance/templates/{name}`)

## Notes

- All diagrams are stored as PlantUML source in each use case folder under `puml/`.

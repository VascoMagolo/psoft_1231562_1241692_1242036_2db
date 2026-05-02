# US226 - Maintenance Part relationships

## Question

Hello,

Following up on a previous clarification regarding US226, where we confirmed the base attributes for a maintenance part (as discussed in this thread), we have a follow-up question regarding its relationships.

Currently, our domain model has MaintenancePart as an isolated entity since the requirements focus primarily on tracking stocks and alerts.
Should MaintenancePart be related to other entities in the system? For example:

1. Should it be linked to a MaintenanceRecord to track which parts were actually consumed during a maintenance task (and thus automatically deduct from the stock)?
2. Should it be linked to an AircraftModel to define which parts are compatible with which models?

Or should it remain a standalone inventory catalog for this phase of the project?

Thank you.

## Answer

What is a "Domain Model" ? I know about Flights, Airports, and Airplanes, sorry.

### Sub-question

Let me rephrase my question.

Regarding the tracking of maintenance parts inventory (US226), we want to make sure the system works exactly how your mechanics and supervisors expect:
1. When a mechanic performs maintenance on an airplane, do they need to log exactly which parts were used so the system can automatically deduct them from the inventory? Or is the inventory updated manually by a supervisor later?
2. Are these maintenance parts universal, or do we need the system to restrict certain parts so they can only be used on specific airplane models?

### Sub-answer

1. For this version of the system, we want to keep it simple. The inventory is managed directly by the Maintenance Supervisor (e.g., he manually edits part quantities). The system simply needs to alert when stock falls below a defined minimum threshold.
2. For this version of the system, we will assume there is no need to track restrictions regarding parts and specific aircraft models.

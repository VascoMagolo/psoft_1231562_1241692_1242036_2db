# LETI-PSOFT Project

# 1. Team Members

The team consists of the students listed in the table below.

| Student Number | Name            |
| -------------- | --------------- |
| **1231562**    | Vasco Magolo    |
| **1241692**    | Diogo Nogueira  |
| **1242036**    | Pedro Guilherme |

# 2. Task Distribution

Throughout the project development period, the distribution of tasks, requirements, and features among team members was
organized as shown in the table below.

Please note that in every phase there are:

- Tasks that **must be** performed collaboratively by all team members. These are considered team responsibilities, and
  each member is expected to actively contribute to their successful completion;
- Tasks related to Software Development Process (SDP) activities within the scope of a single User Story (US), which are
  primarily assigned to individual team members. In other words, each US-related task is the responsibility of a
  specific team member.

While responsibilities may be distributed among team members, **effective collaboration and mutual support are essential**.
Additionally, all members should maintain a comprehensive understanding of the entire project, beyond their assigned tasks.

Finally, keep in mind that each User Story is not isolated -- it must be integrated with the others. Once again,
**integration is a shared team responsibility**.

**The following table must always be kept up to date.**

| Task                                                                                                          | Vasco Magolo | Diogo Nogueira | Pedro Guilherme |
|:--------------------------------------------------------------------------------------------------------------|:------------:|:--------------:|:---------------:|
| **Shared Artifacts & Setup**                                                                                  |              |                |                 |
| [WP #0 - Glossary](system-documentation/global-artifacts/glossary.md)                                         |      x       |       x        |        x        |
| [WP #0 - Domain Model](system-documentation/global-artifacts/puml/DM.puml)                                    |      x       |       x        |        x        |
| WP #0A - Bootstrap Admins & Initial Data                                                                      |      x       |       x        |        x        |
| WP #0B - Data Bootstrapping Phase 2                                                                            |      x       |       x        |        x        |
| **WP #1A - Aircraft Management**                                                                              |              |                |                 |
| [US101 - Register aircraft model](system-documentation/use-cases/WP1A/US101)                                  |      x       |                |                 |
| [US102 - Register specific aircraft instance](system-documentation/use-cases/WP1A/US102)                      |      x       |                |                 |
| [US103 - View aircraft details](system-documentation/use-cases/WP1A/US103)                                    |      x       |                |                 |
| [US104 - Search for aircraft](system-documentation/use-cases/WP1A/US104)                                      |      x       |                |                 |
| [US105 - Update aircraft operational status](system-documentation/use-cases/WP1A/US105)                       |      x       |                |                 |
| **WP #1B - Aircraft Analytics**                                                                               |              |                |                 |
| [US201 - Update aircraft model specs](system-documentation/use-cases/WP1B/US201)                              |      x       |                |                 |
| [US202 - Register aircraft model w/ image](system-documentation/use-cases/WP1B/US202)                         |      x       |                |                 |
| [US203 - View compatible routes for aircraft](system-documentation/use-cases/WP1B/US203)                      |      x       |                |                 |
| [US204 - Top 5 utilized aircraft models](system-documentation/use-cases/WP1B/US204)                           |      x       |                |                 |
| [US205 - View fleet status](system-documentation/use-cases/WP1B/US205)                                        |      x       |                |                 |
| [US206 - Total operational hours per aircraft](system-documentation/use-cases/WP1B/US206)                     |      x       |                |                 |
| **WP #2A - Airports (Core)**                                                                                  |              |                |                 |
| [US106 - Register an airport](system-documentation/use-cases/WP2A/US106)                                      |              |       x        |                 |
| [US106a - Add aircraft certification to airport](system-documentation/use-cases/WP2A/US106a)                  |              |       x        |                 |
| [US107 - View airport details](system-documentation/use-cases/WP2A/US107)                                     |              |       x        |                 |
| [US108 - Search for airports](system-documentation/use-cases/WP2A/US108)                                      |              |       x        |                 |
| [US109 - Update airport operational status](system-documentation/use-cases/WP2A/US109)                        |              |       x        |                 |
| **WP #2B - Airports (Extended)**                                                                              |              |                |                 |
| [US207 - Register airport w/ detailed facilities](system-documentation/use-cases/WP2B/US207)                  |              |       x        |                 |
| [US208 - Update airport details (hours/contact)](system-documentation/use-cases/WP2B/US208)                   |              |       x        |                 |
| [US209 - View routes to/from specific airport](system-documentation/use-cases/WP2B/US209)                     |              |       x        |                 |
| [US210 - Stats on busiest airports](system-documentation/use-cases/WP2B/US210)                                |              |       x        |                 |
| [US211 - View airports grouped by region](system-documentation/use-cases/WP2B/US211)                          |              |       x        |                 |
| **WP #3A - Flight Routes**                                                                                    |              |                |                 |
| [US110 - Create a flight route](system-documentation/use-cases/WP3A/US110)                                    |              |                |        x        |
| [US111 - Keep track of route history](system-documentation/use-cases/WP3A/US111)                              |              |                |        x        |
| [US112 - Update or deactivate a route](system-documentation/use-cases/WP3A/US112)                             |              |                |        x        |
| [US113 - View routes from airport / details by ID](system-documentation/use-cases/WP3A/US113)                 |              |                |        x        |
| [US114 - Search for routes](system-documentation/use-cases/WP3A/US114)                                        |              |                |        x        |
| **WP #3B - Flight Scheduling & Network**                                                                      |              |                |                 |
| [US212 - Assign aircraft to route (scheduled flight)](system-documentation/use-cases/WP3B/US212)              |              |                |        x        |
| [US213 - View scheduled flights for specific aircraft](system-documentation/use-cases/WP3B/US213)             |              |                |        x        |
| [US214 - List active routes by popularity/distance](system-documentation/use-cases/WP3B/US214)                |              |                |        x        |
| [US215 - Calculate total distance in network](system-documentation/use-cases/WP3B/US215)                      |              |                |        x        |
| [US216 - Search for alternative routes](system-documentation/use-cases/WP3B/US216)                            |              |                |        x        |
| **WP #4A - Maintenance Records (Core)**                                                                       |              |                |                 |
| [US115a - Create a maintenance record](system-documentation/use-cases/WP4A/US115a)                            |      x       |       x        |        x        |
| [US115b - Create maintenance templates](system-documentation/use-cases/WP4A/US115b)                           |      x       |       x        |        x        |
| [US116 - View all maintenance records for aircraft](system-documentation/use-cases/WP4A/US116)                |      x       |       x        |        x        |
| [US117 - View total maintenance hours for fleet](system-documentation/use-cases/WP4A/US117)                   |      x       |       x        |        x        |
| [US119 - Update maintenance record (mark completed)](system-documentation/use-cases/WP4A/US119)               |      x       |       x        |        x        |
| [US222 - Maintenance due alerts](system-documentation/use-cases/WP4A/US222)                                   |      x       |       x        |        x        |
| [US226 - Track maintenance parts inventory (also BONUS)](system-documentation/use-cases/WP4A/US226)            |      x       |       x        |        x        |
| **WP #4B - Maintenance Analytics**                                                                            |              |                |                 |
| [US217 - Categorize maintenance records by component](system-documentation/use-cases/WP4B/US217)              |      x       |       x        |        x        |
| [US218 - Search maintenance records](system-documentation/use-cases/WP4B/US218)                               |      x       |       x        |        x        |
| [US219 - View ongoing maintenance activities](system-documentation/use-cases/WP4B/US219)                      |      x       |       x        |        x        |
| [US220 - Generate reports on maintenance costs](system-documentation/use-cases/WP4B/US220)                    |      x       |       x        |        x        |
| [US221 - View avg maintenance turnaround time](system-documentation/use-cases/WP4B/US221)                     |      x       |       x        |        x        |
| **Bonus/Optional Features (Team)**                                                                            |              |                |                 |
| [US223 - Aircraft utilization rates graphs](system-documentation/use-cases/BONUS/US223)                       |      x       |       x        |        x        |
| [US224 - Search aircraft by specific features (merged with US104)](system-documentation/use-cases/WP1A/US104) |      x       |       x        |        x        |
| [US225 - Import bulk airport data (CSV)](system-documentation/use-cases/BONUS/US225)                          |      x       |       x        |        x        |
| [US227 - Fuel efficiency metrics](system-documentation/use-cases/BONUS/US227)                                 |      x       |       x        |        x        |
| [US228 - Export route network (GeoJSON, KML)](system-documentation/use-cases/BONUS/US228)                     |      x       |       x        |        x        |
| [US229 - Generate flight utilization reports](system-documentation/use-cases/BONUS/US229)                     |      x       |       x        |        x        |

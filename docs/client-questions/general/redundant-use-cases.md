# General - Clarification on redundant use cases

## Question

Hello,

We noticed a direct overlap between the new enhanced requirements and the core functionalities we already successfully delivered in Phase 1.
Specifically, we noticed that:

- US224 (search by specific features) is a natural filter extension of the search endpoint we built for US104.
- US205 (view real-time status) overlaps with the aircraft details endpoint already established in US103.
- US202 (register model with image) is essentially an optional field addition to the base registration payload from US101.

Since our goal is to provide a robust, clean, and scalable REST API, our architectural proposal is to upgrade and enrich our existing Phase 1 endpoints to fulfill these Phase 2 requirements, rather than developing completely new and redundant endpoints.

## Answer

Hi. A "clean and scalable REST API" that offers the requested functionality sounds good. Thanks.
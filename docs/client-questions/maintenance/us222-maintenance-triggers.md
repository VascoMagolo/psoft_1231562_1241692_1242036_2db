# US222 - Scheduled maintenance triggers and intervals

## Question

Hello,

Regarding US222, the system needs to trigger alerts when an aircraft is due for scheduled maintenance based on flight hours or calendar days.

To calculate when an aircraft is "due," where should the system look for the defined intervals/limits (e.g., "requires maintenance every 500 hours or 180 days")?

1. Should these intervals be attributes of a Maintenance Template (meaning a specific template is triggered periodically)?
2. Should these intervals be defined directly on the Aircraft Model?
3. Or is there another way the maintenance schedule is defined?

Thank you.

## Answer

Hi.

Seems like a good idea to have the interval defined as part of the Maintenance Template. Unclear to me what you mean with "(meaning a specific template is triggered periodically)".

### Sub-question

Hello,

Apologies for the confusing wording.

By "triggered periodically", I simply meant that the system will look at the limits set in a specific template (for example, a "100-hour inspection" template set to 100 flight hours) and automatically send an alert to the team every time an aircraft reaches that specific limit.
We will go ahead and add the maintenance intervals (flight hours and calendar days) to the Maintenance template as you suggested.

### Sub-answer

Hi. Apologies for the delay, I missed this one.

Let's say the trigger is manual for now. That is, there is some option the user can select to trigger the check.

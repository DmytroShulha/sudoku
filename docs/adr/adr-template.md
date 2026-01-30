# ADR [NNN]: [TITLE]

## Metadata
* **Status:** [DRAFT | PROPOSED | ACCEPTED | REJECTED | SUPERSEDED by ADR-XXX | DEPRECATED]
* **Date:** YYYY-MM-DD
* **Deciders:** [Name of Architect, Lead Dev, Product Owner]
* **Stakeholders Consulted:** [e.g., Security Team, DevOps, Frontend Guild]

## 1. Context (The Forces)
*Describe the situation and the **Architecturally Significant Requirements (ASRs)** driving this.*

* **Business:** (e.g., Need to launch in 3 months; budget is fixed.)
* **Technological:** (e.g., Existing Java 17 stack; high latency in current DB.)
* **Political/Social:** (e.g., Team prefers Kotlin; company policy mandates AWS.)
* **Project-local:** (e.g., This specific module handles high-security PII.)

## 2. Options Considered
*Record the "Road Not Taken" here. This prevents "Blind Reversal" later.*

* **Option 1: [Name]**
  * **Pros/Cons:** Brief technical trade-offs.
* **Option 2: [Name]**
  * **Pros/Cons:** Brief technical trade-offs.

## 3. Decision Outcome
**Chosen Option: [Name]**

* **Rationale:** Why was this chosen? (e.g., "Matches team expertise (Social Force) despite the slightly higher licensing cost (Business Force).")
* **Rejected Options:** Specifically state why the others were **Rejected**. (e.g., "Rejected Option 2 because it introduced a vendor lock-in we aren't ready for.")

## 4. Consequences
*What is the new reality?*

* **{+} Positive:** (e.g., Faster development velocity, better testability.)
* **{-} Negative:** (e.g., Higher memory usage, requires manual boilerplate.)
* **{!} Risks/Tech Debt:** (e.g., "We are accepting a lack of scalability for now; we must revisit if we hit 10k concurrent users.")

## 5. Validation & Compliance
*How will we know this is being followed?*

* (e.g., "Automated check via ArchUnit," "Code review checklist item," or "Fitness Function.")

## 6. Links & References
* Links to Jira tickets, RFCs(Request for Comments), or previous ADRs.
* 
Back to [Log](../adl.md)
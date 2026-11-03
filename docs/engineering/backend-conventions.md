# CapsuleAI — Backend Conventions

Rules that apply to all backend code and every sprint implementation guide from Sprint 2 onward. Sprint 1 already follows them. Each rule states what to do, why, and how to tell when it does not apply.

## 1. Controllers group a cohesive API

**Rule.** Put related endpoints in the same controller when they share a **bounded context**, a **resource responsibility** and a **common API base path**. Do not create one controller class per endpoint or per user story.

A controller represents one cohesive API responsibility. The class-level `@RequestMapping` names the base path once; each endpoint is a handler method.

| API group (example)                  | Controller               | Base path (illustrative)     |
| ------------------------------------ | ------------------------ | ---------------------------- |
| Registration and session lifecycle   | `AuthController`         | `/api/v1/auth`               |
| Wardrobe item operations             | `WardrobeItemController` | `/api/v1/wardrobe/items`     |
| Outfit advice operations             | `OutfitAdviceController` | `/api/v1/outfit-advice`      |

Only `AuthController` exists today. The other paths are examples; the real ones come from the OpenAPI contract when those stories are designed.

**Keep the controller thin.** Grouping endpoints does not merge anything below the web adapter:

- Each handler translates HTTP to one command, calls **its own** application service, and translates the result back. No business rules, no transactions, no repository calls in the controller.
- Application services, domain rules and persistence adapters stay separate per use case.
- Who may call a handler is decided by the module's security configuration, not by annotations scattered across controllers.
- Error translation stays in the module's `@RestControllerAdvice`, scoped to its web package.

**Do not over-group.** One module may own several resources. Give each resource its own controller even if they live in the same module and package, e.g. a future profile resource does not join `AuthController` just because both belong to Identity. Split a controller when its handlers no longer share a resource or base path, or when it starts to need services that only some handlers use.

**In implementation guides.** The first story that introduces an API group creates its controller (`NEW`). Later stories in the same group **modify** that controller and add one handler (`MODIFY`, complete resulting file), rather than adding a new class. Name the controller after the resource, not the story.

**Why.** One class per API keeps that surface reviewable in one place and prevents duplicate base paths and request mappings from drifting apart. Thin handlers that delegate to separate services keep the use cases as independent as they were with one class each.

**Reference.** Sprint 1 applies this in [shared architecture §3.2 and TD-27](../06-scrum/sprints/sprint-01/implementation-guides/shared-architecture.md#32-final-package-map).

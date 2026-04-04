# Viva Prep Notes

## Architecture

- Clean layered backend: Controller -> Service -> Repository -> Entity
- Modular packages:
  - `auth`, `user`, `qa`, `lostfound`, `config`, `common`, `enums`
- DTO-first API responses: entities are not returned directly
- Centralized error shape via `ApiResponse` and `GlobalExceptionHandler`

## JWT Authentication (Backend)

- Stateless security: `SessionCreationPolicy.STATELESS`, CSRF disabled for API
- JWT filter:
  - extracts `Authorization: Bearer <token>`
  - validates signature/expiration
  - sets authentication in SecurityContext
- Secret validation:
  - startup fails if secret is missing/too short
  - for local demo, a safe default is provided in `application.yml`

## Q&A Module

- Entities: `Question`, `Answer`, `Tag`
- Tags are unique (DB constraint), normalized (trim + lowercase), de-duplicated per question
- Pagination:
  - uses DB-backed `Pageable` in repository layer (no in-memory slicing)
- N+1 risks:
  - repository uses `@EntityGraph` / fetch queries for authors where needed
  - service read methods are transactional to keep lazy loads safe (and DTO mapping avoids entity serialization)

## Voting System (Sprint 3)

- Entity: `Vote(answer, user, voteType)`
- DB uniqueness: `(answer_id, user_id)` prevents duplicate votes
- Voting behavior:
  - no vote -> create
  - same vote again -> remove (toggle off)
  - opposite vote -> update type
- Reputation rules:
  - upvote: +10
  - downvote: -2
  - toggle off reverses effect
  - accept answer: +15 (switching acceptance moves +15)

## Accepted Answer

- Only question owner can accept
- Only one accepted answer per question
- Switch is atomic and consistent (uses transactional boundary + row-level lock)

## Lost & Found + Claim Workflow (Sprint 4/5)

- `LostFoundPost`:
  - `type`: LOST / FOUND
  - `status`: OPEN / CLAIMED / RESOLVED (future-compatible)
- Claim Request:
  - only for FOUND + OPEN posts
  - cannot claim your own post
  - only one PENDING claim per user per post
- Approval:
  - approved claim -> APPROVED
  - all other pending claims -> REJECTED
  - post -> RESOLVED

## Concurrency / Race Conditions (how to explain)

- Voting:
  - DB uniqueness stops duplicates
  - transactional service updates reputation consistently
- Claims:
  - uses row-level locking on the post during claim creation/approval to serialize concurrent attempts
  - service-level validation prevents duplicate PENDING claims


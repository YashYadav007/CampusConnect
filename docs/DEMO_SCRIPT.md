# CampusConnect Demo Script (3–5 minutes)

Goal: show a complete story across Q&A + Lost & Found + Claim workflow without getting stuck.

## Setup (before presenting)

1. Start backend + frontend.
2. Confirm demo data is seeded (Home shows latest content).
3. Have these credentials ready:
   - `aarav@demo.com` / `password123` (Owner)
   - `kabir@demo.com` / `password123` (Voter/Claimer)
   - `diya@demo.com` / `password123` (Helper)

## Flow A: Q&A (2 minutes)

1. Login as `kabir@demo.com`
2. Go to **Q&A**
3. Open a seeded question (shows answers, votes, accepted answer highlight)
4. Vote on an answer:
   - click **Upvote**
   - click again to toggle off
   - click **Downvote** to switch
5. (Optional) Add an answer at the bottom

Viva hook:
- voting is transactional; uniqueness enforced per (answer_id, user_id)
- score = upvotes - downvotes

## Flow B: Accepted Answer + Reputation (1 minute)

1. Login as `aarav@demo.com` (question owner for at least one seeded question)
2. Open that question
3. Click **Accept** on an answer (only visible for owner)
4. Mention reputation:
   - accepted answer gives +15 to answer author

Viva hook:
- only one accepted answer per question, switch is atomic

## Flow C: Lost & Found + Claim (2 minutes)

1. Login as `kabir@demo.com`
2. Go to **Lost & Found**
3. Open a FOUND + OPEN post
4. Click **Claim this item**, submit a message
5. Login as `aarav@demo.com` (owner of that FOUND post)
6. Open the same post
7. In **Claim Requests**, approve the pending claim
8. Observe:
   - claim becomes APPROVED
   - post status becomes RESOLVED
   - other pending claims auto-rejected (if any)

Viva hook:
- claim flow uses transactional consistency and row-level locking to prevent races
- post owner authorization is enforced


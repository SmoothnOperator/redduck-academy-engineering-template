# Solution

Answer all three problems. For each one, give your verdict, state the exact constraint in words, and
explain your reasoning. When you claim a bypass, show how it makes the constraint irrelevant.

## Problem 1

**Verdict:** bypass exists

**The exact constraint:** To know the running total, the system must scan and sum all records on every read.

**Reasoning:** The outcome only requires the scalar sum of the values, not the inspection of the hundred thousand historical records that produced it. Because addition is associative and commutative, we do not need to re-read history to know the new sum; we only need to account for deltas as they occur. The constraint of reading all records belongs entirely to the naive "calculate on demand from raw data" method, rather than to the outcome itself.

**If a bypass exists, how it removes the constraint:** Maintain a running total incrementally at write time. When a record is created, updated, or deleted, an atomic database trigger, transaction, or materialized view updates a single stored total accumulator by the delta (`total += new_value - old_value`). When the dashboard queries the running total, it performs an O(1) read of that single accumulated value. The query path never reads any of the hundred thousand records, eliminating the read-all-records constraint completely rather than merely reading fewer records or indexing them.

## Problem 2

**Verdict:** no bypass

**The exact constraint:** Delivery certainty can be achieved by exchanging confirmation messages over an unreliable channel that can silently drop any transmission.

**Reasoning:** This is the Two Generals' Problem, proven unsolvable in distributed computing. The outcome requires two independent parties to reach common knowledge (certainty of delivery) across a channel that may silently drop any message, including acknowledgements. If sender A sends message M, A cannot distinguish whether M was dropped or if recipient B's acknowledgment Ack1 was dropped. If B sends Ack1, B cannot know whether A received Ack1 without an Ack2 from A, which in turn requires an Ack3, creating an infinite regress. Because the final transmission in any finite protocol is unacknowledged and can be silently lost, absolute certainty can never be guaranteed. The constraint belongs intrinsically to the physics and semantics of the required outcome over an unreliable medium; no protocol, architecture, or algorithmic restructuring can eliminate it.

**If a bypass exists, how it removes the constraint:** None exists. A design can only achieve probabilistic confidence (such as retries with acknowledgements) or alter the problem definition (such as using an independent reliable channel or shared medium), but within the stated constraints, no design can bypass the impossibility of guaranteed certainty over a lossy channel.

## Problem 3

**Verdict:** bypass exists

**The exact constraint:** You must detect and intercept duplicate submissions at the moment they happen before they can be processed.

**Reasoning:** Catching a duplicate "as it happens" treats the problem as a real-time race condition at the interaction boundary (such as disabling buttons or timing UI clicks), which is inherently fragile against network lag, double-taps, client retries, or concurrent requests. However, the true outcome required is not "block rapid clicks," but rather "ensure the customer is charged exactly once for a single purchase intent." The constraint of intercepting the second click in real time belongs solely to client-side timing and event-filtering methods, not to the financial outcome.

**If a bypass exists, how it removes the constraint:** Use server-side idempotency keys. When the payment form loads, generate a unique client-side idempotency key (such as a UUID) representing that specific checkout attempt. When the customer submits—even if double-clicked simultaneously—both requests carry the exact same idempotency key. The backend or payment gateway uses an atomic check/unique constraint on this key: the first request acquires the lock and executes the payment, while the second request is recognized as an identical replay and returns the result of the first transaction without executing a second charge. The timing and frequency of user clicks become entirely irrelevant; the system no longer needs to catch or block duplicates as they happen.

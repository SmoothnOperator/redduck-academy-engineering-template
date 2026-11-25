# Solution

## Language

language: "typescript"

## The attack

A rational adversary exercises a non-positive transfer amount through the public `transfer(from, to, amount)` method to extract value and drain a victim's account.

### Concrete steps:
1. **Starting balances:** `attacker: 0`, `victim: 100`.
2. **The exact call made:**
   ```ts
   ledger.transfer("attacker", "victim", -100);
   ```
3. **Execution inside the service:**
   - The ledger computes available balance for `"attacker"`: `available = balanceOf("attacker") = 0`.
   - It checks whether balance is sufficient: `if (available < amount)`. Because `0 < -100` evaluates to **false**, the check passes and no error is thrown.
   - The ledger updates balances using arithmetic on the negative amount:
     - `balances.set("attacker", available - amount)` => `0 - (-100) = +100` (credits the attacker).
     - `balances.set("victim", balanceOf("victim") + amount)` => `100 + (-100) = 0` (drains the victim).
4. **Ending balances:** `attacker: 100`, `victim: 0`. The victim is fully drained, and the attacker holds 100 points they were never granted.

*(In addition, because the API also exposes `from`, an adversary could alternatively call `ledger.transfer("victim", "attacker", 100)` to debit the victim directly without authorization).*


## Why it works at the design level

At the design level, the system exposes a critical architectural flaw: **the caller controls the `from` (the debited sender account) parameter, which the system should have controlled itself.**

In any sound ledger design, identity and debit authorization must be established by the system's execution context (e.g., an authenticated session principal, a cryptographic signature, or `msg.sender`). A caller should only be permitted to specify the recipient and amount (`transfer(to, amount)`).

By exposing `from` directly in the public interface, the ledger grants ambient, unauthenticated authority over every balance in the system. The service verifies that the *source account* holds enough points (`available < amount`), but never establishes whether the *caller* owns or has authority over that account. 

(Furthermore, the design allows the caller to supply signed numerical values for `amount` without a positivity constraint, allowing negative transfers to invert the debit/credit relationship).

## Patch or elimination?

The proper fix is an **elimination** — it removes the condition that made the bad input possible to express, rather than merely patching it at runtime:

- **A patch** keeps the flawed `transfer(from, to, amount)` interface and adds runtime guards:
  ```ts
  if (caller.identity !== from) throw new Error("unauthorized");
  if (amount <= 0) throw new Error("invalid amount");
  ```
  While this blocks the immediate exploit, it keeps the dangerous parameter in the contract. Any downstream endpoint, proxy, or future caller that fails to enforce the guard reopens the vulnerability.
- **Elimination** restructures the API so the invalid condition cannot even be represented:
  1. Remove `from` entirely from the public method signature: `transfer(to: string, amount: number)`. Inside the method, the system binds `from` strictly and automatically to the authenticated caller identity (`const from = authContext.getCurrentUser()`). The caller cannot specify a victim's account because no such parameter exists.
  2. Enforce positive amounts at the type level (e.g., an unsigned integer `uint` in languages that support it, or a branded `PositiveNumber` type), making negative amounts unrepresentable at compile time.

Removing the condition is fundamentally stronger than guarding it because it removes the attack vector from the domain model entirely. A capability that does not exist in the interface cannot be exploited, cannot be misconfigured, and requires no defensive runtime checks across callers.


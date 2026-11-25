# Solution

## Language

language: "typescript"

## Why derive the identifier from content

A registry establishes identity through arbitrary assignment: a central authority or database assigns an ID (such as a sequence number or UUID) and maps it to an object. Trusting a registry forces every participant to trust three fragile assumptions:
1. **The central authority's honesty and correctness:** The operator can silently swap, tamper with, or reassign the underlying content while keeping the identifier unchanged.
2. **Channel and registry integrity:** Callers must trust the network path to the registry, cache coherency, and access controls against man-in-the-middle attacks.
3. **Availability:** If the registry is offline, partitioned, or deprecated, verification is completely blocked.

Recomputing the identifier directly from the content removes the registry entirely. The identifier is a deterministic, cryptographic digest of the canonicalized content itself ($H(\text{content}) = \text{id}$). Anyone holding the content can compute the digest independently and verify integrity locally in $O(1)$ time with zero network requests, zero shared state, and zero third-party trust. Verification becomes a self-evident mathematical proof rather than reliance on an external authority.

## What changes when the content changes

### Why it is a feature (Tamper-Evidence and Substitution Detection)
Because the identifier is mathematically bound to the content, any modification—whether an adversarial alteration, an accidental corruption, or a single altered character (as demonstrated in the demo when `"Revenue held steady."` becomes `"Revenue tripled."`)—radically changes the resulting hash (the avalanche effect). A receiver holding an expected identifier can detect any tampering or substitution immediately without consulting the sender or a registry. The content authenticates itself.

### Why it is a constraint (Strict Immutability and Identity Coupling)
The trade-off of content-derived identity is that identity and state are permanently coupled. You cannot update or edit an object's content while preserving its identifier. Any change produces a completely new identifier representing a new object. Consequently, systems built on content addressing cannot perform in-place mutations; to model mutable entities, they must introduce an explicit indirection layer (such as mutable pointers, branch refs in Git, or DNS names over IPFS hashes) on top of the immutable content-addressed core.


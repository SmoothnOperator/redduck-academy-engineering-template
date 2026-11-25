// 03-core-periphery/src/Store.ts  —  TYPESCRIPT version.
// Only edit this file if you chose TypeScript, and complete just one language.
// Then declare it at the top of SOLUTION.md:  language: "typescript"
//
// Given (do not edit): ./store-data holds the UserRecord type, the seed users,
// and the hash helper. Done for you: verify, which returns only a boolean.
// Your job:
//   - add a hash-free public-profile lookup that returns username and role,
//   - rebuild report so it lists every user without ever including a hash.
// Full description in TASK.md.

import { SEED_USERS, hash } from "./store-data";
import type { UserRecord } from "./store-data";

export interface PublicProfile {
  username: string;
  role: string;
}

export class UserStore {
  private users: Record<string, UserRecord> = SEED_USERS;

  // The one legitimate core operation. It checks a password and returns only a boolean.
  verify(username: string, password: string): boolean {
    const user = this.users[username];
    if (!user) return false;
    return user.passwordHash === hash(password);
  }

  // Narrow, hash-free public-profile lookup (username and role).
  getProfile(username: string): PublicProfile | undefined {
    const user = this.users[username];
    if (!user) return undefined;
    return { username: user.username, role: user.role };
  }

  // Alias for public profile lookup
  getPublicProfile(username: string): PublicProfile | undefined {
    return this.getProfile(username);
  }

  // Rebuilt report on top of the narrow public-profile interface.
  // Lists every user without ever exposing a password hash.
  report(): string[] {
    return Object.keys(this.users).map((username) => {
      const profile = this.getProfile(username);
      return profile ? `${profile.username} (${profile.role})` : "";
    }).filter(Boolean);
  }
}

// Standalone periphery report helper on top of the narrow UserStore interface
export function report(store: UserStore): string[] {
  return store.report();
}


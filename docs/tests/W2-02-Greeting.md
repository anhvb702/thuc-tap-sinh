# W2-02 — Lời chào theo giờ địa phương

| Test | Input | Expected output | Mutation bắt |
| --- | --- | --- | --- |
| `greeting_tracksDayPeriods` | local hour 5, 12, 18, and invalid hours | Sáng, Chiều, Tối; invalid hour rejected | hardcode buổi chiều or shift boundary |
| `viewModel_refreshesCurrentLocalTime` | injected clock crosses local date/hour, then refresh | state day/hour use injected clock and timezone | use system clock or stale state |
| Resume refresh | app returns to foreground | ViewModel refreshes using injected clock | no lifecycle refresh |

`lifecycle-runtime-compose` is added because the app observes `ON_RESUME` to refresh the injected local clock and repository snapshot.

## Bằng chứng

Pending implementation and Gradle run.

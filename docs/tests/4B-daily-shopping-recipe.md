# 4B-DAILY — Daily shopping and custom recipe editing

- **Task ID & mục tiêu:** 4B-DAILY. Keep daily shopping independent of weekly-plan state, persist its checklist locally, support catalog/custom items and compartments, copy a Zalo checklist, and edit a Lite custom recipe title/ingredients.
- **Input:** Pantry catalog/state, persisted daily shopping rows, toggle/add/remove/compartment actions, stock action; existing custom `Recipe` and edited title/ingredient IDs.
- **Expected output:** Daily checklist is locally persisted and usable with no weekly plan. Daily stock uses selected CHILL/FREEZE compartment. Zalo text groups unchecked items by compartment. Editing a custom recipe updates its title and ingredient list while preserving its identity; delete remains available.
- **Boundaries/risks:** Process recreation, duplicate items, unknown custom item, empty title/list, and missing weekly plan.
- **Six mutations:** block daily tab with no weekly plan; drop persistence write; invert checked state in copied checklist; ignore compartment when stocking; save edits as new ID; omit ingredient update. Repository/UI tests assert outputs and persisted state.

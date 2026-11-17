# SEED-02 — Vietnamese ingredient and recipe bundles

## Contract

- **Task ID:** SEED-02
- **Goal:** Expand the shipped shelf catalog to 50–60 unique common Vietnamese ingredients and ship 25–30 valid family recipes using only catalog IDs.
- **Inputs:** `shelf_table.json`, `recipes.json`, the catalog supplied to `filterRecipesByDiet`, and each recipe's required/optional slots.
- **Expected output:** Both shelf catalog copies parse to the same unique IDs; each row uses a supported group and valid shelf duration. Every recipe has a unique nonblank ID/title, 1–8 nonblank steps, at least one required slot, duration 1–1440, and references catalog IDs only. Bundle contains meat, seafood, and vegetarian options. Added meat/seafood IDs are excluded by applicable diet restrictions.
- **Edges:** Duplicate IDs, IDs missing from catalog, empty required slot/list, out-of-range duration, and all-meat/all-seafood/vegetarian classification.

## Mutation checks

1. Remove one shelf row: count and recipe catalog-reference checks fail.
2. Duplicate a shelf ID: unique-ID check fails.
3. Change a shelf group to an unsupported group: ShelfTable parsing fails.
4. Change a meat or seafood ID to an unclassified ID/group: diet classification check fails.
5. Remove a required ingredient from catalog: shipped recipe reference check fails.
6. Change a recipe duration outside 1–1440 or remove all required slots: Recipe construction fails.

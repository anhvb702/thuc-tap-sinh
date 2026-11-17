# Phase 5 contract

## P0-1 flexible meal slots
- Task ID: PH5-P0-1
- Input: breakfast/lunch/dinner MealSlot values, planned dishes, per-day disabled MealSlotKey values.
- Expected: breakfast round-trips through repository/backup/cook-log and renders a breakfast label; disabled dishes are omitted from shopping quantities and restored when re-enabled. Presets set 1/2/3 slots and workweek schedule.
- Boundaries: existing LUNCH/DINNER semantics and existing backup formats remain valid; meal toggles must be day-specific.

## P0-2 freshness scheduling
- Task ID: PH5-P0-2
- Input: recipes whose ingredients have defaultShelfDays on either side of 3/5 and day indexes 0..6.
- Expected: early days rank short-shelf-life ingredients higher; late days rank longer shelf-life ingredients higher, without changing tie ordering otherwise.

## P1-1 weekend aisle grouping
- Task ID: PH5-P1-1
- Input: shopping items across rau-la/rau-cu/trai-cay, thit/ca, sua-trung/khac.
- Expected: three aisle headings in requested order, each item under its group heading.

## P1-2 shopping tab naming
- Task ID: PH5-P1-2
- Input: recipeTabSpecs.
- Expected: shopping tab label is "Giỏ đi chợ lẻ".

## P1-3 late-week cold storage suggestion
- Task ID: PH5-P1-3
- Input: meat/seafood required by enabled meals on dayIndex >= 3.
- Expected: default FREEZE suggestion; user selection can override to CHILL.

## Mutations to validate
1. Reverse short/long freshness preference.
2. Change early window boundary from day 2 to 3.
3. Include disabled meal ingredients in shopping count.
4. Render disabled planned dish.
5. Change aisle mapping for seafood to vegetable aisle.
6. Remove late-week FREEZE default.

# CHANGELOG
Ordered from the newest to the oldest versions

### v0.0.1
- Add `Redstone Selector` item
- Add right-click and left-click events
  - The game blocks all interactions with blocks when the player holds the `Redstone Selector` item
- The `Redstone Selector` has an animation while on cooldown which activates after placing a selection corner
- Add selection system with saving positions to item's nbt data
- Add rendering the selection using [Renderer](https://github.com/0x3C50/Renderer) package by [0x3C50](https://github.com/0x3C50)
  - The game renders the selection only when the client's player has the `Redstone Selector` item in the main hand
  - The selection consists of two parts:
    - The main part which is an outline-only block containing the whole selection
    - Two 1x1x1 outline-and-fill boxes colored red and blue positioned at two selection corners
  - Each selection part has a margin to avoid z-fighting with the block textures
- Add a feature when while the player is sneaking the selection renders through blocks
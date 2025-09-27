# Nice Singletons

Personally, I prefer to make classes singletons where I know that they will only be used once.
Therefore, the access of these classes is made through the static methods.

- TeleportExecutor
- WarpManager
- PositionAccessor
- HomeManager
- EssentialsPlugin (also exposes the classes above for better outside-dev experience using getters)

#

TODO: Commands
# Minecraft 1.21.11 controls restoration

Amecs Reborn 2.0.3 restores its controls search widget and optional integration with Controlling 29.0.1 and Searchables 1.0.4. The integration uses Controlling's Fabric events for key assignment, modifier recognition, individual/global reset, and edit-button selection. Neither optional mod is bundled or required to start Amecs.

The Amecs search bar appears on the vanilla controls screen; Controlling retains its own search UI. Search supports translated binding/category names, `=key` for keys, `=` for unbound bindings and `=%%` for conflicting bindings (`=%` remains accepted). Filtering retains the original entries and never sorts the game's keybinding array. It uses the 1.21.11 list replacement, category, render and input APIs.

Build and run actual client checks with Java 21:

```
./gradlew :1.21.11:build
./gradlew :1.21.11:runControlsTest
./gradlew :1.21.11:runControlsTest -PwithControlling
```

On headless Linux prefix the client commands with `xvfb-run -a`. The development test mod is a separate source set and is excluded from the release JAR. Successful runs print `AMECS_CONTROLS_TEST_PASS controlling=false` or `controlling=true`; a failure crashes the test client. Each run uses its own `run-controls-test` directory, never an existing player instance.

Checks exercise real controls screens, search/clear, key and conflict filters, keyboard focus, modifier capture, mouse capture, resets and options persistence. They do not simulate a human play session on a multiplayer server.

The branch release workflow builds the explicit 1.21.11 node, runs both client configurations, checks the packaged classes and publishes an immutable version tag, e.g. `v2.0.3-mc1.21.11`. It preserves the previous branch release.

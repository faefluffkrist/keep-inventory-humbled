
# Keep Inventory Humbled

Keep Inventory Humbled reworks Minecraft's Keep Inventory behavior by preserving a player's hotbar, offhand, and equipped armor while still giving death meaningful consequences.

The mod was originally created to fit into a modpack I am working on, but its behavior is highly configurable for use in other modpacks or standalone.

![Items dropped after death, including recoverable experience](https://cdn.modrinth.com/data/cached_images/ef7a9f7a796493fd9a58dcf4497d9079f3c8b34b_0.webp)

## Default Behavior

By default, Keep Inventory Humbled:

- Keeps your hotbar, offhand, and equipped armor while the rest of your inventory drops.
- Removes half of your experience levels along with your current experience-bar progress.
- Makes 10% of the lost experience recoverable at your death location. The dropped experience can only be picked up by its owner.
- Damages equipped armor by 20% of its maximum durability, regardless of the Unbreaking enchantment.
- Breaks armor if the durability penalty would leave it at 10% durability or below.
- Reduces the normal armor durability penalty to 25% for environmental deaths such as falling, drowning, ongoing fire damage, and freezing.
- Preserves vanilla Curse of Vanishing behavior, causing affected items to disappear on death.
- Preserves Curse of Binding across death. Non-durable wearable items with Curse of Binding, such as a Jack o' Lantern, are instead destroyed to prevent the player from becoming permanently stuck with them.

## Configuration

The mod allows the death penalties above to be customized or disabled.

Configuration options include the amount of experience lost and recovered, armor durability penalties, armor-breaking thresholds, environmental-death penalties, and other death behavior.

For clients, [Mod Menu](https://modrinth.com/mod/modmenu) by Terraformers is recommended for easier in-game configuration.

Changes made through the configuration screen require leaving and re-entering the world before they take effect.

Dedicated servers can configure the mod through its configuration file and must be restarted for changes to take effect.

## Download

Official releases are distributed through Modrinth:

[Keep Inventory Humbled on Modrinth](https://modrinth.com/mod/mod-faeflufkrist)

## Issues and Suggestions

If you encounter a bug, compatibility problem, or have a feature suggestion, please use the GitHub Issues page.

When reporting a bug, include your Minecraft version, mod version, Fabric Loader version, reproduction steps, and relevant logs whenever possible.

## Building from Source

Keep Inventory Humbled uses the included Gradle Wrapper.

### Windows

```shell
gradlew.bat build
```

### Linux / macOS

```shell
./gradlew build
```

Compiled JARs will be generated in:

```text
build/libs/
```

## Inspiration

Keep Inventory Humbled was inspired by nugrevan's [Keep Inventory Updated](https://modrinth.com/datapack/keep-inventory-updated).

Keep Inventory Humbled is a standalone implementation and is not affiliated with that project.

## License

Keep Inventory Humbled is licensed under the MIT License. See [LICENSE](LICENSE) for details.

You may include Keep Inventory Humbled in modpacks.
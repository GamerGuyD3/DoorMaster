package me.trueprotocol.doorMaster;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DisplayEntityStorage {

    private final DoorMaster plugin;
    private File dataFile;
    private FileConfiguration config;

    public DisplayEntityStorage(DoorMaster plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "displayEntities.yml");
        this.config = YamlConfiguration.loadConfiguration(dataFile);
    }

    // Saves display entities to displayEntities.yml
    public void saveDisplayEntities(Map<Location, UUID> displayEntities) {

        // Clear the config before saving to avoid duplicates
        config.set("displayEntities", null);

        for (Map.Entry<Location, UUID> entry : displayEntities.entrySet()) {
            Location loc = entry.getKey();
            UUID entityId = entry.getValue();

            // Save the UUID as the key and locString as the value
            String locString = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
            config.set("displayEntities." + entityId.toString(), locString);
        }

        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Loads display entities to memory
    public Map<Location, UUID> loadDisplayEntities() {
        Map<Location, UUID> loadedEntities = new HashMap<>();

        ConfigurationSection displayEntitiesSection = config.getConfigurationSection("displayEntities");

        if (displayEntitiesSection == null) {
            plugin.getLogger().warning("No displayEntities section found in the config.");
            return loadedEntities;
        }

        // Loop through each entry within "displayEntities"
        for (String entityIdString : displayEntitiesSection.getKeys(false)) {

            try {
                // Parse the UUID
                UUID entityId = UUID.fromString(entityIdString);

                // Get the locString for this UUID
                String locString = displayEntitiesSection.getString(entityIdString);
                String[] parts = locString.split(",");

                if (parts.length != 4) {
                    plugin.getLogger().warning("Invalid location string: " + locString);
                    continue;
                }

                Location loc = new Location(
                        Bukkit.getWorld(parts[0]),
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2]),
                        Double.parseDouble(parts[3])
                );

                // Add the entity to the map
                loadedEntities.put(loc, entityId);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return loadedEntities;
    }
}
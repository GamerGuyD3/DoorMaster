package me.trueprotocol.doorMaster;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DoorMaster extends JavaPlugin {

    public Map<Location, UUID> displayEntities = new HashMap<>();
    private DisplayEntityStorage storage;

    @Override
    public void onEnable() {
        // Plugin startup logic
        storage = new DisplayEntityStorage(this);
        displayEntities = storage.loadDisplayEntities();

        getLogger().info("Plugin Enabled");
        this.getServer().getPluginManager().registerEvents(new PlayerInteract(this, displayEntities), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        storage.saveDisplayEntities(displayEntities);
    }
}

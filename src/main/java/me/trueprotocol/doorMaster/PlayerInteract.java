package me.trueprotocol.doorMaster;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;

public class PlayerInteract implements Listener {

    private final DoorMaster plugin;
    private final Map<Location, UUID> displayEntities;

    public PlayerInteract(DoorMaster plugin, Map<Location, UUID> displayEntities) {
        this.plugin = plugin;
        this.displayEntities = displayEntities;
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clickedBlock = e.getClickedBlock();

        // Check if the clicked block is a trapdoor or a door
        if (clickedBlock.getType() == Material.OXIDIZED_COPPER_DOOR) {
            e.setCancelled(true);
            handleDoorInteraction(clickedBlock);
        } else if (clickedBlock.getType() == Material.OXIDIZED_COPPER_TRAPDOOR) {
            e.setCancelled(true);
            handleTrapdoorInteraction(clickedBlock);
        }
    }

    private void handleDoorInteraction(Block block) {
        Door door = (Door) block.getBlockData();
        BlockFace facing = door.getFacing();
        Door.Hinge hinge = door.getHinge();

        // Use the bottom half of the door as reference
        Location doorBottomLoc = getBottomHalf(block, door.getHalf());

        // Call the method that handles the rest of the door logic
        handleDoorLogic(doorBottomLoc, facing, hinge, door.isOpen());

        // Toggle the door's open state
        door.setOpen(!door.isOpen());
        block.setBlockData(door);
    }

    private void handleTrapdoorInteraction(Block trapdoorBlock) {
        // Find the nearby door
        Block doorBlock = findNearbyDoor(trapdoorBlock);

        if (doorBlock != null && doorBlock.getType() == Material.OXIDIZED_COPPER_DOOR) {
            // Remove the clicked trapdoor
            trapdoorBlock.setType(Material.AIR);

            // Handle door interaction (open/close the door)
            handleDoorInteraction(doorBlock);
        }
    }

    private Block findNearbyDoor(Block trapdoorBlock) {
        // Assuming the door is nearby, you can adjust the range based on your larger door setup
        Block[] nearbyBlocks = {
                trapdoorBlock.getRelative(BlockFace.NORTH),
                trapdoorBlock.getRelative(BlockFace.SOUTH),
                trapdoorBlock.getRelative(BlockFace.EAST),
                trapdoorBlock.getRelative(BlockFace.WEST),
                trapdoorBlock.getRelative(BlockFace.DOWN),

                // Corner checks
                trapdoorBlock.getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN),
                trapdoorBlock.getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN),
                trapdoorBlock.getRelative(BlockFace.EAST).getRelative(BlockFace.DOWN),
                trapdoorBlock.getRelative(BlockFace.WEST).getRelative(BlockFace.DOWN),
        };

        for (Block block : nearbyBlocks) {
            if (block.getType() == Material.OXIDIZED_COPPER_DOOR) {
                return block;
            }
        }
        return null;  // No door found nearby
    }

    private Location getBottomHalf(Block block, Bisected.Half half) {
        // Adjust the location to always refer to the bottom half of the door
        return half == Bisected.Half.TOP ? block.getLocation().add(0, -1, 0) : block.getLocation();
    }

    private void handleDoorLogic(Location doorBottomLoc, BlockFace facing, Door.Hinge hinge, boolean isOpen) {

        BlockFace doorFacing;
        int xOffset, zOffset, airXOffset, airZOffset;

        // Adjust trapdoor and air block positions based on door facing and hinge, relative to the door's bottom half
        switch (facing) {
            case WEST:
                xOffset = isOpen ? 0 : -1;
                if (hinge == Door.Hinge.RIGHT) {
                    doorFacing = isOpen ? BlockFace.WEST : BlockFace.SOUTH;
                    zOffset = isOpen ? 1 : 0;
                    airXOffset = isOpen ? -1 : 0;
                    airZOffset = isOpen ? 0 : 1;
                } else {
                    doorFacing = isOpen ? BlockFace.WEST : BlockFace.NORTH;
                    zOffset = isOpen ? -1 : 0;
                    airXOffset = isOpen ? -1 : 0;
                    airZOffset = isOpen ? 0 : -1;
                }
                break;
            case NORTH:
                zOffset = isOpen ? 0 : -1;
                if (hinge == Door.Hinge.RIGHT) {
                    doorFacing = isOpen ? BlockFace.NORTH : BlockFace.WEST;
                    xOffset = isOpen ? -1 : 0;
                    airZOffset = isOpen ? -1 : 0;
                    airXOffset = isOpen ? 0 : -1;
                } else {
                    doorFacing = isOpen ? BlockFace.NORTH : BlockFace.EAST;
                    xOffset = isOpen ? 1 : 0;
                    airZOffset = isOpen ? -1 : 0;
                    airXOffset = isOpen ? 0 : 1;
                }
                break;
            case EAST:
                xOffset = isOpen ? 0 : 1;
                if (hinge == Door.Hinge.RIGHT) {
                    doorFacing = isOpen ? BlockFace.EAST : BlockFace.NORTH;
                    zOffset = isOpen ? -1 : 0;
                    airXOffset = isOpen ? 1 : 0;
                    airZOffset = isOpen ? 0 : -1;
                } else {
                    doorFacing = isOpen ? BlockFace.EAST : BlockFace.SOUTH;
                    zOffset = isOpen ? 1 : 0;
                    airXOffset = isOpen ? 1 : 0;
                    airZOffset = isOpen ? 0 : 1;
                }
                break;
            case SOUTH:
                zOffset = isOpen ? 0 : 1;
                if (hinge == Door.Hinge.RIGHT) {
                    doorFacing = isOpen ? BlockFace.SOUTH : BlockFace.EAST;
                    xOffset = isOpen ? 1 : 0;
                    airZOffset = isOpen ? 1 : 0;
                    airXOffset = isOpen ? 0 : 1;
                } else {
                    doorFacing = isOpen ? BlockFace.SOUTH : BlockFace.WEST;
                    xOffset = isOpen ? -1 : 0;
                    airZOffset = isOpen ? 1 : 0;
                    airXOffset = isOpen ? 0 : -1;
                }
                break;
            default:
                return;
        }

        // Update trapdoors and air blocks relative to doorBottomLoc
        updateBigDoor(doorBottomLoc, xOffset, zOffset, airXOffset, airZOffset, doorFacing);

        // Handle block display entity logic
        createOrTeleportDisplayEntity(doorBottomLoc, facing, hinge, isOpen);
    }

    private void updateBigDoor(Location doorBottomLoc, int xOffset, int zOffset, int airXOffset, int airZOffset, BlockFace trapdoorFacing) {
        Location newLoc = doorBottomLoc.clone();
        Location oldLoc = doorBottomLoc.clone();

        // Move trapdoor relative to door position
        newLoc.add(xOffset, 0, zOffset);
        newLoc.getBlock().setType(Material.OXIDIZED_COPPER_TRAPDOOR);
        TrapDoor trapDoor = (TrapDoor) newLoc.getBlock().getBlockData();
        trapDoor.setFacing(trapdoorFacing);
        trapDoor.setOpen(true);

        // Set trapdoors for new location
        for (int i = 0; i < 3; i++) {
            newLoc.getBlock().setBlockData(trapDoor);
            newLoc.add(0, 1, 0);
        }

        doorBottomLoc.add(0, 2, 0);
        doorBottomLoc.getBlock().setBlockData(trapDoor);

        // Delete old trapdoors
        oldLoc.add(airXOffset, 0, airZOffset);
        for (int i = 0; i < 3; i++) {
            oldLoc.getBlock().setType(Material.AIR);
            oldLoc.add(0, 1, 0);
        }
    }

    private void createOrTeleportDisplayEntity(Location doorLocation, BlockFace facing, Door.Hinge hinge, boolean isOpen) {

        switch (facing) {
            case NORTH:
                if (hinge == Door.Hinge.RIGHT) {
                    doorLocation.add(0.90625, 0, 0.90625);
                } else {
                    doorLocation.add(0.09375, 0, 0.90625);
                }
                break;
            case EAST:
                if (hinge == Door.Hinge.RIGHT) {
                    doorLocation.add(0.09375, 0, 0.90625);
                } else {
                    doorLocation.add(0.09375, 0, 0.09375);
                }
                break;
            case SOUTH:
                if (hinge == Door.Hinge.RIGHT) {
                    doorLocation.add(0.09375, 0, 0.09375);
                } else {
                    doorLocation.add(0.90625, 0, 0.09375);
                }
                break;
            case WEST:
                if (hinge == Door.Hinge.RIGHT) {
                    doorLocation.add(0.90625, 0, 0.09375);
                } else {
                    doorLocation.add(0.90625, 0, 0.90625);
                }
                break;
            default:
                break;
        }

        UUID entityId = displayEntities.get(doorLocation);
        ItemDisplay existingEntity = null;

        // Get the existing ItemDisplay using the entityId
        if (entityId != null) {
            existingEntity = (ItemDisplay) doorLocation.getWorld().getEntity(entityId);
        }

        // Create a new ItemDisplay entity if one doesn't already exist
        if (existingEntity == null) {

            ItemDisplay displayEntity = (ItemDisplay) doorLocation.getWorld().spawnEntity(doorLocation, EntityType.ITEM_DISPLAY);

            ItemStack blockData = Material.OAK_DOOR.asItemType().createItemStack();
            displayEntity.setItemStack(blockData);

            // Store the new entity
            displayEntities.put(doorLocation, displayEntity.getUniqueId());

            // Set initial rotation based on the door's facing
            setItemDisplayRotation(displayEntity, facing, hinge, isOpen);

        } else {
            // If the display entity exists, teleport it and rotate based on door's facing
            existingEntity.teleport(doorLocation);
            setItemDisplayRotation(existingEntity, facing, hinge, isOpen);
        }
    }

    private void setItemDisplayRotation(ItemDisplay displayEntity, BlockFace facing, Door.Hinge hinge, boolean isOpen) {
        // Define the rotation quaternion based on the door's facing direction and whether it's open
        Vector3f hingeCenter = new Vector3f();
        Quaternionf rotationQuaternion = new Quaternionf();

        switch (facing) {
            case NORTH:
                if (hinge == Door.Hinge.RIGHT) {
                    hingeCenter.set(isOpen ? -1.40625f : 0.0f, -0.5f, isOpen ? 0.0f : -1.40625f);
                    rotationQuaternion.rotateY((float) Math.toRadians(isOpen ? 180 : 90));
                } else {
                    hingeCenter.set(isOpen ? 1.40625f : 0.0f, -0.5f, isOpen ? 0.0f : -1.40625f);
                    rotationQuaternion.rotateY((float) Math.toRadians(isOpen ? 0 : 90));
                }
                break;
            case EAST:
                if (hinge == Door.Hinge.RIGHT) {
                    hingeCenter.set(isOpen ? 0.0f : 1.40625f, -0.5f, isOpen ? -1.40625f : 0.0f);
                    rotationQuaternion.rotateY((float) Math.toRadians(isOpen ? 90 : 0));
                } else {
                    hingeCenter.set(isOpen ? 0.0f : 1.40625f, -0.5f, isOpen ? 1.40625f : 0.0f);
                    rotationQuaternion.rotateY((float) Math.toRadians(isOpen ? -90 : 0));
                }
                break;
            case SOUTH:
                if (hinge == Door.Hinge.RIGHT) {
                    hingeCenter.set(isOpen ? 1.40625f : 0.0f, -0.5f, isOpen ? 0.0f : 1.40625f);
                    rotationQuaternion.rotateY((float) Math.toRadians(isOpen ? 0 : -90));
                } else {
                    hingeCenter.set(isOpen ? -1.40625f : 0.0f, -0.5f, isOpen ? 0.0f : 1.40625f);
                    rotationQuaternion.rotateY((float) Math.toRadians(isOpen ? 180 : -90));
                }
                break;
            case WEST:
                if (hinge == Door.Hinge.RIGHT) {
                    hingeCenter.set(isOpen ? 0.0f : -1.40625f, -0.5f, isOpen ? 1.40625f : 0.0f);
                    rotationQuaternion.rotateY((float) Math.toRadians(isOpen ? -90 : 180));
                } else {
                    hingeCenter.set(isOpen ? 0.0f : -1.40625f, -0.5f, isOpen ? -1.40625f : 0.0f);
                    rotationQuaternion.rotateY((float) Math.toRadians(isOpen ? 90 : 180));
                }
                break;
            default:
                break;
        }

        // Create a transformation with translation, rotation, and scale
        Transformation transformation = new Transformation(
                hingeCenter,   // Translation (center of hinge)
                rotationQuaternion,               // Apply the rotation quaternion
                new Vector3f(3.0f, 3.0f, 3.0f),   // Scale
                new Quaternionf()
        );

        // Apply the transformation to the ItemDisplay
        displayEntity.setTransformation(transformation);
    }
}
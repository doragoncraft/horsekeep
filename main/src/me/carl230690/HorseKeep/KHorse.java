 package me.carl230690.HorseKeep;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Horse.Color;
 import org.bukkit.entity.Horse.Style;
 import org.bukkit.entity.Horse.Variant;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.HorseInventory;
 import org.bukkit.inventory.ItemStack;
 
 public class KHorse
 {
   private Configuration config;
   private HorseKeep plugin;
   private EntityType[] horseEntityTypes = { EntityType.HORSE };
   public static KHorse instance;
 
   public static KHorse getInstance()
   {
     if (null == instance) {
       instance = new KHorse(instance.plugin, instance.config);
     }
     return instance;
   }
 
   public KHorse(HorseKeep plugin, Configuration config)
   {
     this.plugin = plugin;
     this.config = config;
   }
 
   public boolean isHorse(Entity entity)
   {
     for (EntityType horseType : this.horseEntityTypes)
     {
       if (entity.getType() == horseType)
       {
         return true;
       }
     }
 
     return false;
   }
 
   public boolean isOwnedHorse(Entity horse)
   {
     return isOwnedHorse(horse.getUniqueId());
   }
 
   public boolean isOwnedHorse(UUID uuid)
   {
     if (this.config.isConfigurationSection("horses." + uuid)) return true;
 
     return false;
   }
 
   public boolean horseIdentifierExists(String horseIdentifier)
   {
     if (!this.config.isConfigurationSection("horses")) return false;
 
     ConfigurationSection horsesSection = this.config.getConfigurationSection("horses");
 
     for (String key : horsesSection.getKeys(false)) {
       if (this.config.isSet("horses." + key + ".identifier"))
       {
         if (this.config.getString("horses." + key + ".identifier").equalsIgnoreCase(horseIdentifier))
         {
           return true;
         }
       }
       else
       {
         this.plugin.getLogger().warning("Horse has no identifier set (UUID " + key + ") - Auto-repair");
         this.config.set("horses." + key + ".identifier", getNewHorseIdentifier());
         this.plugin.saveConfig();
       }
     }
 
     return false;
   }
 
   public String getHorseOwner(Entity entity)
   {
     return getHorseOwner(entity.getUniqueId());
   }
 
   public String getHorseOwner(String horseIdentifier)
   {
     return getHorseOwner(getHorseUUID(horseIdentifier));
   }
 
   public String getHorseOwner(UUID horseUUID)
   {
     return this.config.getString("horses." + horseUUID + ".owner");
   }
 
   public List<String> getHorseMembers(Entity horse)
   {
     return getHorseMembers(horse.getUniqueId());
   }
 
   public List<String> getHorseMembers(UUID horseUUID)
   {
     return this.config.getStringList("horses." + horseUUID.toString() + ".members");
   }
 
   public boolean canMountHorse(Player player, Entity horse)
   {
     return canMountHorse(player, horse.getUniqueId());
   }
 
   public boolean canMountHorse(Player player, UUID horseUUID)
   {
     String horseIdentifier = getHorseIdentifier(horseUUID);
 
     if ((isHorseMember(horseIdentifier, player.getName())) || (isHorseOwner(horseIdentifier, player.getName())))
     {
       return true;
     }
 
     return false;
   }
 
   public void addHorseMember(String horseIdentifier, String playerName)
   {
     List horseMembers = getHorseMembers(getHorseUUID(horseIdentifier));
 
     horseMembers.add(playerName);
 
     this.config.set("horses." + getHorseUUID(horseIdentifier) + ".members", horseMembers);
 
     this.plugin.saveConfig();
   }
 
   public void removeHorseMember(String horseIdentifier, String playerName)
   {
     List horseMembers = getHorseMembers(getHorseUUID(horseIdentifier));
 
     horseMembers.remove(playerName);
 
     this.config.set("horses." + getHorseUUID(horseIdentifier) + ".members", horseMembers);
 
     this.plugin.saveConfig();
   }
 
   public boolean isHorseMember(String horseIdentifier, String playerName)
   {
     List horseMembers = getHorseMembers(getHorseUUID(horseIdentifier));
 
     return horseMembers.contains(playerName);
   }
 
   public boolean isHorseOwner(Player player, Entity horse)
   {
     if (getHorseOwner(horse.getUniqueId()).equalsIgnoreCase(player.getName())) return true;
     return false;
   }
 
   public boolean isHorseOwner(String horseIdentifier, String playerName)
   {
     if (getHorseOwner(horseIdentifier).equalsIgnoreCase(playerName)) return true;
     return false;
   }
 
   public void removeHorse(String horseIdentifier)
   {
     removeHorse(getHorseUUID(horseIdentifier));
   }
 
   public void removeHorse(UUID horseUUID)
   {
     this.config.getConfigurationSection("horses").set(horseUUID.toString(), null);
 
     this.plugin.saveConfig();
   }
 
   public Integer getNewHorseIdentifier()
   {
     Integer identifierIncremental = Integer.valueOf(this.config.getInt("internalIncrementalIdentifier"));
 
     this.config.set("internalIncrementalIdentifier", Integer.valueOf(identifierIncremental.intValue() + 1));
 
     this.plugin.saveConfig();
 
     return identifierIncremental;
   }
 
   public void setHorseOwner(Player player, Entity horse)
   {
     this.config.set("horses." + horse.getUniqueId() + ".owner", player.getName());
 
     this.config.set("horses." + horse.getUniqueId() + ".identifier", getNewHorseIdentifier());
 
     this.config.set("horses." + horse.getUniqueId() + ".members", null);
 
     this.plugin.saveConfig();
   }
 
   public boolean isHorseIdentifierTaken(String identifier)
   {
     ConfigurationSection horsesSection = this.config.getConfigurationSection("horses");
 
     Boolean taken = Boolean.valueOf(false);
 
     for (String key : horsesSection.getKeys(false)) {
       if (this.config.getString("horses." + key + ".identifier").equalsIgnoreCase(identifier))
       {
         taken = Boolean.valueOf(true);
       }
     }
 
     return taken.booleanValue();
   }
 
   public UUID getHorseUUID(String identifier)
   {
     ConfigurationSection horsesSection = this.config.getConfigurationSection("horses");
 
     for (String key : horsesSection.getKeys(false)) {
       if (this.config.isSet("horses." + key + ".identifier"))
       {
         if (this.config.getString("horses." + key + ".identifier").equalsIgnoreCase(identifier))
         {
           return UUID.fromString(key);
         }
       }
       else
       {
         this.plugin.getLogger().warning("Horse has no identifier set (UUID " + key + ") - Auto-repair");
         this.config.set("horses." + key + ".identifier", getNewHorseIdentifier());
         this.plugin.saveConfig();
       }
     }
 
     return null;
   }
 
   public UUID getHorseUUID(Entity horse)
   {
     return horse.getUniqueId();
   }
 
   public Location getHorseLocationFromConfig(Entity horse)
   {
     return getHorseLocationFromConfig(horse.getUniqueId());
   }
 
   public Location getHorseLocationFromConfig(UUID horseUUID)
   {
     if (!this.config.isSet("horses." + horseUUID.toString() + ".lastpos")) return null;
 
     String locConfig = this.config.getString("horses." + horseUUID.toString() + ".lastpos");
 
     String[] locParams = locConfig.split(":");
 
     Location loc = new Location(Bukkit.getWorld(locParams[0]), Double.parseDouble(locParams[1]), Double.parseDouble(locParams[2]), Double.parseDouble(locParams[3]), Float.parseFloat(locParams[4]), Float.parseFloat(locParams[5]));
     return loc;
   }
 
   public boolean isOnHorse(Player player)
   {
     if (player.isInsideVehicle())
     {
       if (player.getVehicle().getType() == EntityType.HORSE)
       {
         return true;
       }
     }
     return false;
   }
 
   public void ejectFromHorse(Player player)
   {
     if (isOnHorse(player))
     {
       player.getVehicle().eject();
     }
   }
 
   public List<String> getOwnedHorses(Player player)
   {
     return getOwnedHorses(player.getName());
   }
 
   public List<String> getOwnedHorses(String playerName)
   {
     List ownedHorses = new ArrayList();
 
     if (!this.config.isConfigurationSection("horses")) return ownedHorses;
 
     ConfigurationSection horsesSection = this.config.getConfigurationSection("horses");
 
     for (String key : horsesSection.getKeys(false)) {
       if (this.config.isSet("horses." + key + ".owner"))
       {
         if (this.config.getString("horses." + key + ".owner").equalsIgnoreCase(playerName))
         {
           ownedHorses.add(key);
         }
       }
       else
       {
         this.plugin.getLogger().warning("Horse has no owner set (UUID " + key + ") - Removing from config");
         removeHorse(UUID.fromString(key));
       }
     }
 
     return ownedHorses;
   }
 
   public boolean hasHorseIdentifier(UUID horseUUID)
   {
     if (this.config.isSet("horses." + horseUUID + ".identifier")) return true;
     return false;
   }
 
   public String getHorseIdentifier(UUID horseUUID)
   {
     if (this.config.isSet("horses." + horseUUID.toString() + ".identifier"))
     {
       return this.config.getString("horses." + horseUUID.toString() + ".identifier");
     }
     return null;
   }
 
   public void store(Horse horse, String playerName)
   {
     this.config.set("horses." + horse.getUniqueId().toString() + ".stored", Boolean.valueOf(true));
     this.config.set("horses." + horse.getUniqueId().toString() + ".tamed", Boolean.valueOf(horse.isTamed()));
     this.config.set("horses." + horse.getUniqueId().toString() + ".variant", horse.getVariant().toString());
     this.config.set("horses." + horse.getUniqueId().toString() + ".style", horse.getStyle().toString());
     this.config.set("horses." + horse.getUniqueId().toString() + ".color", horse.getColor().toString());
     this.config.set("horses." + horse.getUniqueId().toString() + ".jumpstrength", Double.valueOf(horse.getJumpStrength()));
     this.config.set("horses." + horse.getUniqueId().toString() + ".maxhealth", Double.valueOf(horse.getMaxHealth()));
 
     if (horse.getCustomName() != null)
     {
       this.config.set("horses." + horse.getUniqueId().toString() + ".name", horse.getCustomName());
     }
     else this.config.set("horses." + horse.getUniqueId().toString() + ".name", null);
 
     this.config.set("horses." + horse.getUniqueId().toString() + ".lasthealth", Double.valueOf(horse.getHealth()));
     this.config.set("horses." + horse.getUniqueId().toString() + ".age", Integer.valueOf(horse.getAge()));
 
     if (horse.getInventory().getSaddle() != null)
     {
       this.config.set("horses." + horse.getUniqueId().toString() + ".saddled", Boolean.valueOf(true));
     }
     else this.config.set("horses." + horse.getUniqueId().toString() + ".saddled", null);
 
     if (horse.getInventory().getArmor() != null)
     {
       this.config.set("horses." + horse.getUniqueId().toString() + ".armor", horse.getInventory().getArmor());
     }
     else this.config.set("horses." + horse.getUniqueId().toString() + ".armor", null);
 
     if (horse.isCarryingChest())
     {
       this.config.set("horses." + horse.getUniqueId().toString() + ".chestcontent", horse.getInventory().getContents());
     }
     else this.config.set("horses." + horse.getUniqueId().toString() + ".chestcontent", null);
 
     this.plugin.saveConfig();
   }
 
   public boolean isStored(UUID horseUUID)
   {
     if (this.config.isSet("horses." + horseUUID + ".stored"))
     {
       return this.config.getBoolean("horses." + horseUUID + ".stored");
     }
     return false;
   }
 
   public void summon(String horseIdentifier, Location loc)
   {
     UUID horseUUID = getHorseUUID(horseIdentifier);
 
     ConfigurationSection horseCfgSection = this.config.getConfigurationSection("horses." + horseUUID.toString());
 
     Entity entity = loc.getWorld().spawnEntity(loc, EntityType.HORSE);
     Horse spawnedHorse = (Horse)entity;
 
     spawnedHorse.setVariant(Horse.Variant.valueOf(this.config.getString("horses." + horseUUID + ".variant")));
     spawnedHorse.setColor(Horse.Color.valueOf(this.config.getString("horses." + horseUUID + ".color")));
     spawnedHorse.setStyle(Horse.Style.valueOf(this.config.getString("horses." + horseUUID + ".style")));
     spawnedHorse.setCustomName(this.config.getString("horses." + horseUUID + ".name"));
     spawnedHorse.setMaxHealth(Double.parseDouble(this.config.getString("horses." + horseUUID + ".maxhealth")));
     spawnedHorse.setHealth(Double.parseDouble(this.config.getString("horses." + horseUUID + ".lasthealth")));
     spawnedHorse.setJumpStrength(Double.parseDouble(this.config.getString("horses." + horseUUID + ".jumpstrength")));
     spawnedHorse.setAge(Integer.parseInt(this.config.getString("horses." + horseUUID + ".age")));
 
     if (this.config.getBoolean("horses." + horseUUID + ".tamed"))
     {
       spawnedHorse.setTamed(this.config.getBoolean("horses." + horseUUID + ".tamed"));
     }
 
     if (this.config.getBoolean("horses." + horseUUID + ".saddled"))
     {
       spawnedHorse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
     }
 
     if (this.config.getString("horses." + horseUUID + ".armor") != null)
     {
       spawnedHorse.getInventory().setArmor(this.config.getItemStack("horses." + horseUUID + ".armor"));
     }
 
     if (this.config.getString("horses." + horseUUID + ".chestcontent") != null)
     {
       spawnedHorse.setCarryingChest(true);
       spawnedHorse.getInventory().setContents((ItemStack[])this.config.get("horses." + horseUUID + ".chestcontent"));
     }
 
     this.config.createSection("horses." + spawnedHorse.getUniqueId());
 
     this.config.set("horses." + spawnedHorse.getUniqueId(), horseCfgSection);
 
     this.config.set("horses." + spawnedHorse.getUniqueId() + ".stored", Boolean.valueOf(false));
 
     this.config.set("horses." + horseUUID.toString(), null);
 
     this.plugin.saveConfig();
   }
 
   public HorseTeleportResponse teleportHorse(UUID horseUUID, Location loc)
   {
     if (this.config.getBoolean("horses." + horseUUID + ".stored"))
     {
       return HorseTeleportResponse.NOT_TELEPORTED_STORED;
     }
 
     for (World w : this.plugin.getServer().getWorlds()) {
       for (LivingEntity e : w.getLivingEntities())
       {
         if (horseUUID.toString().equalsIgnoreCase(e.getUniqueId().toString()))
         {
           if (!e.getLocation().getChunk().isLoaded()) {
             e.getLocation().getChunk().load();
           }
 
           e.teleport(loc);
           return HorseTeleportResponse.TELEPORTED;
         }
       }
 
     }
 
     if (getHorseLocationFromConfig(horseUUID) != null)
     {
       Location horseLastLocation = getHorseLocationFromConfig(horseUUID);
       Chunk c = horseLastLocation.getChunk();
 
       if (!horseLastLocation.getWorld().getName().equalsIgnoreCase(loc.getWorld().getName()))
       {
         this.plugin.getLogger().warning("Tried to teleport horse in another world (" + horseLastLocation.getWorld().getName() + " to " + loc.getWorld().getName());
         return HorseTeleportResponse.NOT_TELEPORTED_WRONG_WORLD;
       }
 
       if (!c.isLoaded()) {
         c.load();
       }
 
       Entity[] entitiesChunkList = c.getEntities();
       for (Entity e : entitiesChunkList) {
         if (e.getUniqueId().toString().equalsIgnoreCase(horseUUID.toString()))
         {
           e.teleport(loc);
           return HorseTeleportResponse.TELEPORTED;
         }
       }
 
       return HorseTeleportResponse.NOT_TELEPORTED_ENTITY_DELETED;
     }
 
     return HorseTeleportResponse.NOT_TELEPORTED;
   }
 }
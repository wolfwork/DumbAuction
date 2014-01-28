package com.turt2live.dumbauction.rewards;

import com.turt2live.dumbauction.DumbAuction;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Represents an item store for offline players
 *
 * @author turt2live
 */
public class OfflineStore implements SavingStore, Listener {

    private Map<String, List<ItemStack>> queue = new HashMap<String, List<ItemStack>>();
    private File file;

    public OfflineStore(DumbAuction plugin) throws IOException, InvalidConfigurationException {
        this(plugin, "offline.yml");
    }

    protected OfflineStore(DumbAuction plugin, String fileName) throws IOException, InvalidConfigurationException {
        file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) file.createNewFile();

        FileConfiguration config = new YamlConfiguration();
        config.load(file);
        if (config.getKeys(false) != null) {
            for (String playerName : config.getKeys(false)) {
                List<ItemStack> items = new ArrayList<ItemStack>();
                for (String key : config.getConfigurationSection(playerName).getKeys(false)) {
                    items.add(config.getItemStack(playerName + "." + key));
                }
                queue.put(playerName, items);
            }
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void save() {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(file);
            Set<String> configKeys = config.getKeys(false);
            for (String key : configKeys) {
                config.set(key, null);
            }
            for (String key : queue.keySet()) {
                List<ItemStack> items = queue.get(key);
                for (int i = 0; i < items.size(); i++) {
                    config.set(key + "." + i, items.get(i));
                }
            }
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void store(String playerName, ItemStack item) {
        if (item == null || playerName == null) throw new IllegalArgumentException();
        List<ItemStack> stacks = queue.get(playerName);
        if (stacks == null) stacks = new ArrayList<ItemStack>();
        stacks.add(item);
        queue.put(playerName, stacks);
    }

    @Override
    public boolean distributeStore(String player, Player distributeTo) {
        if (distributeTo == null) distributeTo = DumbAuction.getInstance().getServer().getPlayerExact(player);
        List<ItemStack> queue = getStore(player);
        if (queue != null) {
            clearStore(player);
            Map<Integer, ItemStack> overflow = distributeTo.getInventory().addItem(queue.toArray(new ItemStack[0]));
            if (overflow != null && !overflow.isEmpty()) {
                Location drop = distributeTo.getLocation();
                for (ItemStack item : overflow.values()) {
                    drop.getWorld().dropItemNaturally(drop, item);
                }
                DumbAuction.getInstance().sendMessage(distributeTo, ChatColor.RED + "" + ChatColor.BOLD + "Some of your auctions went on the ground - Full inventory");
            }
            DumbAuction.getInstance().sendMessage(distributeTo, ChatColor.GREEN + "Winnings from auctions you bid on were given to you.");
            return true;
        }
        return false;
    }

    @Override
    public void store(String playerName, List<ItemStack> items) {
        if (items == null || playerName == null) throw new IllegalArgumentException();
        for (ItemStack stack : items) store(playerName, stack);
    }

    @Override
    public void clearStore(String playerName) {
        if (playerName == null) throw new IllegalArgumentException();
        queue.remove(playerName);
    }

    @Override
    public boolean isApplicable(String player) {
        return DumbAuction.getInstance().getServer().getPlayerExact(player) == null;
    }

    @Override
    public List<ItemStack> getStore(String playerName) {
        if (playerName == null) throw new IllegalArgumentException();
        return queue.get(playerName);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        distributeStore(event.getPlayer().getName(), event.getPlayer());
    }
}
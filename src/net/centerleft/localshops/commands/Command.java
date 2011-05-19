package net.centerleft.localshops.commands;

import java.util.Iterator;
import java.util.logging.Logger;

import net.centerleft.localshops.Config;
import net.centerleft.localshops.ItemInfo;
import net.centerleft.localshops.LocalShops;
import net.centerleft.localshops.PlayerData;
import net.centerleft.localshops.Search;
import net.centerleft.localshops.Shop;
import net.centerleft.localshops.ShopLocation;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.PrimitiveCuboid;

public abstract class Command {
    
    // Attributes
    protected LocalShops plugin = null;
    protected String commandLabel = null;
    protected CommandSender sender = null;
    protected String command = null;
    protected static String DECIMAL_REGEX = "(\\d+\\.\\d+)|(\\d+\\.)|(\\.\\d+)|(\\d+)";
    protected static final Logger log = Logger.getLogger("Minecraft");

    // Command Types Enum
    public static enum CommandTypes {
        ADMIN(0, new String[] { "localshops.admin" }),
        ADD(1, new String[] { "localshops.manager.add" }),
        BUY(2, new String[] { "localshops.user.buy" }),
        CREATE(3, new String[] { "localshops.manager.create" }),
        CREATE_FREE(4, new String[] { "localshops.free.create" }),
        DESTROY(5, new String[] { "localshops.manager.destroy" }),
        HELP(6, new String[] {}),
        BROWSE(7, new String[] { "localshops.user.browse" }),
        MOVE(8, new String[] { "localshops.manager.move" }),
        MOVE_FREE(9, new String[] { "localshops.free.move" }),
        REMOVE(10, new String[] { "localshops.manager.remove" }),
        SEARCH(11, new String[] {}),
        SELECT(12, new String[] { "localshops.manager.select" }),
        SELL(13, new String[] { "localshops.user.sell" }),
        SET_OWNER(14, new String[] { "localshops.manager.set.owner" }),
        SET(15, new String[] { "localshops.manager.set" });

        int id = -1;
        String[] permissions = null;

        CommandTypes(int id) {
            this.id = id;
        }

        CommandTypes(int id, String[] permissions) {
            this(id);
            this.permissions = permissions;
        }

        public int getId() {
            return id;
        }

        public String[] getPermissions() {
            return permissions;
        }
    }
    
    public Command(LocalShops plugin, String commandLabel, CommandSender sender, String command){
        this.plugin = plugin;
        this.commandLabel = commandLabel;
        this.sender = sender;
        this.command = command.trim();
    }
    
    public Command(LocalShops plugin, String commandLabel, CommandSender sender, String[] args) {
        this(plugin, commandLabel, sender, Search.join(args, " ").trim());
    }
    
    public String getCommand() {
        return command;
    }
    
    public boolean process() {
        // Does nothing and needs to be overloaded by subclasses
        return false;
    }
    
    protected boolean canUseCommand(CommandTypes type) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // check if admin first
            for (String permission : CommandTypes.ADMIN.getPermissions()) {
                if (plugin.getPermManager().hasPermission(player, permission)) {
                    return true;
                }
            }

            // fail back to provided permissions second
            for (String permission : type.getPermissions()) {
                if (!plugin.getPermManager().hasPermission(player, permission)) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }
    
    protected boolean canCreateShop(String playerName) {
        if (canUseCommand(CommandTypes.ADMIN)) {
            return true;
        } else if (( plugin.getShopData().numOwnedShops(playerName) < Config.PLAYER_MAX_SHOPS || Config.PLAYER_MAX_SHOPS < 0) && canUseCommand(CommandTypes.CREATE)) {
            return true;
        }

        return false;
    }
    
    protected boolean canModifyShop(Shop shop) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            // If owner, true
            if(shop.getOwner().equals(player.getName())) {
                return true;
            }
            // If manager, true
            if(shop.getManagers().contains(player.getName())) {
                return true;
            }
            // If admin, true
            if(canUseCommand(CommandTypes.ADMIN)) {
                return true;
            }
            return false;
        } else {
            // Console, true
            return true;
        }
    }
    
    protected boolean shopPositionOk(double[] xyzA, double[] xyzB, String worldName) {
        BookmarkedResult res = new BookmarkedResult();

        // make sure coords are in right order
        for (int i = 0; i < 3; i++) {
            if (xyzA[i] > xyzB[i]) {
                double temp = xyzA[i];
                xyzA[i] = xyzB[i];
                xyzB[i] = temp;
            }
        }

        // Need to test every position to account for variable shop sizes

        for (double x = xyzA[0]; x <= xyzB[0]; x++) {
            for (double z = xyzA[2]; z <= xyzB[2]; z++) {
                for (double y = xyzA[1]; y <= xyzB[1]; y++) {
                    res = LocalShops.getCuboidTree().relatedSearch(res.bookmark, x, y, z);
                    if (shopOverlaps(res, worldName))
                        return false;
                }
            }
        }
        return true;
    }

    protected boolean shopOverlaps(BookmarkedResult res, String worldName) {
        if (res.results.size() != 0) {
            for (PrimitiveCuboid cuboid : res.results) {
                if (cuboid.uuid != null) {
                    if (cuboid.world.equalsIgnoreCase(worldName)) {
                        Shop shop = plugin.getShopData().getShop(cuboid.uuid);
                        sender.sendMessage(LocalShops.CHAT_PREFIX + ChatColor.DARK_AQUA + "Could not create shop, it overlaps with " + ChatColor.WHITE + shop.getName());
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    protected void givePlayerItem(ItemStack item, int amount) {
        Player player = (Player) sender;

        int maxStackSize = 64;

        // fill all the existing stacks first
        for (int i : player.getInventory().all(item.getType()).keySet()) {
            if (amount == 0)
                continue;
            ItemStack thisStack = player.getInventory().getItem(i);
            if (thisStack.getType().equals(item.getType()) && thisStack.getDurability() == item.getDurability()) {
                if (thisStack.getAmount() < maxStackSize) {
                    int remainder = maxStackSize - thisStack.getAmount();
                    if (remainder <= amount) {
                        amount -= remainder;
                        thisStack.setAmount(maxStackSize);
                    } else {
                        thisStack.setAmount(maxStackSize - remainder + amount);
                        amount = 0;
                    }
                }
            }

        }

        for (int i = 0; i < 36; i++) {
            ItemStack thisSlot = player.getInventory().getItem(i);
            if (thisSlot == null || thisSlot.getType() == Material.AIR) {
                if (amount == 0)
                    continue;
                if (amount >= maxStackSize) {
                    item.setAmount(maxStackSize);
                    player.getInventory().setItem(i, item);
                    amount -= maxStackSize;
                } else {
                    item.setAmount(amount);
                    player.getInventory().setItem(i, item);
                    amount = 0;
                }
            }
        }

        while (amount > 0) {
            if (amount >= maxStackSize) {
                item.setAmount(maxStackSize);
                amount -= maxStackSize;
            } else {
                item.setAmount(amount - maxStackSize);
                amount = 0;
            }
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }

    }
    
    /**
     * Returns true if the player is in the shop manager list or is the shop
     * owner
     * 
     * @param player
     * @param shop
     * @return
     */
    protected boolean isShopController(Shop shop) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (shop.getOwner().equalsIgnoreCase(player.getName()))
                return true;
            if (shop.getManagers() != null) {
                for (String manager : shop.getManagers()) {
                    if (player.getName().equalsIgnoreCase(manager)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }
    
    protected int countItemsInInventory(PlayerInventory inventory, ItemStack item) {
        int totalAmount = 0;
        boolean isDurable = LocalShops.getItemList().isDurable(item);

        for (Integer i : inventory.all(item.getType()).keySet()) {
            ItemStack thisStack = inventory.getItem(i);
            if (isDurable) {
                int damage = calcDurabilityPercentage(thisStack);
                if (damage > Config.ITEM_MAX_DAMAGE && Config.ITEM_MAX_DAMAGE != 0)
                    continue;
            } else {
                if (thisStack.getDurability() != item.getDurability())
                    continue;
            }
            totalAmount += thisStack.getAmount();
        }

        return totalAmount;
    }
    
    protected static int calcDurabilityPercentage(ItemStack item) {

        // calc durability prcnt
        short damage;
        if (item.getType() == Material.IRON_SWORD) {
            damage = (short) ((double) item.getDurability() / 250 * 100);
        } else {
            damage = (short) ((double) item.getDurability() / (double) item.getType().getMaxDurability() * 100);
        }

        return damage;
    }
    
    protected int removeItemsFromInventory(PlayerInventory inventory, ItemStack item, int amount) {

        boolean isDurable = LocalShops.getItemList().isDurable(item);

        // remove number of items from player adding stock
        for (int i : inventory.all(item.getType()).keySet()) {
            if (amount == 0)
                continue;
            ItemStack thisStack = inventory.getItem(i);
            if (isDurable) {
                int damage = calcDurabilityPercentage(thisStack);
                if (damage > Config.ITEM_MAX_DAMAGE && Config.ITEM_MAX_DAMAGE != 0)
                    continue;
            } else {
                if (thisStack.getDurability() != item.getDurability())
                    continue;
            }

            int foundAmount = thisStack.getAmount();
            if (amount >= foundAmount) {
                amount -= foundAmount;
                inventory.setItem(i, null);
            } else {
                thisStack.setAmount(foundAmount - amount);
                inventory.setItem(i, thisStack);
                amount = 0;
            }
        }

        return amount;

    }
    

    protected int countAvailableSpaceForItemInInventory(PlayerInventory inventory, ItemInfo item) {
        int count = 0;
        for (ItemStack thisSlot : inventory.getContents()) {
            if (thisSlot == null || thisSlot.getType() == Material.AIR) {
                count += 64;
                continue;
            }
            if (thisSlot.getTypeId() == item.typeId && thisSlot.getDurability() == item.subTypeId) {
                count += 64 - thisSlot.getAmount();
            }
        }

        return count;
    }
    
    protected boolean notifyPlayers(Shop shop, String[] messages) {
        Iterator<PlayerData> it = plugin.getPlayerData().values().iterator();
        while(it.hasNext()) {
            PlayerData p = it.next();
            if(p.shopList.contains(shop.getUuid())) {
                Player thisPlayer = plugin.getServer().getPlayer(p.playerName);
                for(String message : messages) {
                    thisPlayer.sendMessage(message);
                }
            }
        }
        return true;
    }
    
    protected double calculateDistance(ShopLocation from, ShopLocation to) {        
        double x1 = from.getX();
        double x2 = to.getX();

        double y1 = from.getY();
        double y2 = to.getY();

        double z1 = from.getZ();
        double z2 = to.getZ();

        double distance = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2) + Math.pow((z1 - z2), 2));
        
        return distance;
}
}
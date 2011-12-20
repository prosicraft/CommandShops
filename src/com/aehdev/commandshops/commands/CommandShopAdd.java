package com.aehdev.commandshops.commands;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.aehdev.commandshops.CommandShops;
import com.aehdev.commandshops.Config;
import com.aehdev.commandshops.ItemInfo;
import com.aehdev.commandshops.PlayerData;
import com.aehdev.commandshops.Search;
import com.aehdev.commandshops.Shop;

// TODO: Auto-generated Javadoc
/**
 * The Class CommandShopAdd.
 */
public class CommandShopAdd extends Command
{

	/**
	 * Instantiates a new command shop add.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopAdd(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Instantiates a new command shop add.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopAdd(CommandShops plugin, String commandLabel,
			CommandSender sender, String[] command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/* (non-Javadoc)
	 * @see com.aehdev.commandshops.commands.Command#process() */
	public boolean process()
	{
		Shop shop = null;

		// Get current shop
		if(sender instanceof Player)
		{
			// Get player & data
			Player player = (Player)sender;
			PlayerData pData = plugin.getPlayerData().get(player.getName());

			// Get Current Shop
			UUID shopUuid = pData.getCurrentShop();
			if(shopUuid != null)
			{
				shop = plugin.getShopData().getShop(shopUuid);
			}
			if(shop == null)
			{
				sender.sendMessage("You are not in a shop!");
				return false;
			}

			// Check Permissions
			if(!canUseCommand(CommandTypes.ADD))
			{
				sender.sendMessage(CommandShops.CHAT_PREFIX
						+ ChatColor.DARK_AQUA
						+ "You don't have permission to use this command");
				return false;
			}

			// Check if Player can Modify
			if(!isShopController(shop) && !canUseCommand(CommandTypes.ADMIN))
			{
				player.sendMessage(ChatColor.DARK_AQUA
						+ "You must be the shop owner or a manager to set this.");
				player.sendMessage(ChatColor.DARK_AQUA
						+ "The current shop owner is " + ChatColor.WHITE
						+ shop.getOwner());
				return true;
			}

			// add (player only command)
			Pattern pattern = Pattern.compile("(?i)add$");
			Matcher matcher = pattern.matcher(command);
			if(matcher.find())
			{
				ItemStack itemStack = player.getItemInHand();
				if(itemStack == null){ return false; }
				ItemInfo item = null;
				int amount = itemStack.getAmount();
				if(CommandShops.getItemList().isDurable(itemStack))
				{
					item = Search.itemById(itemStack.getTypeId());
					if(calcDurabilityPercentage(itemStack) > Config.MAX_DAMAGE
							&& Config.MAX_DAMAGE != 0)
					{
						sender.sendMessage(ChatColor.DARK_AQUA + "Your "
								+ ChatColor.WHITE + item.name
								+ ChatColor.DARK_AQUA
								+ " is too damaged to add to stock!");
						sender.sendMessage(ChatColor.DARK_AQUA
								+ "Items must be damanged less than "
								+ ChatColor.WHITE + Config.MAX_DAMAGE
								+ "%");
						return true;
					}
				}else{
					item = Search.itemById(itemStack.getTypeId(),
							itemStack.getDurability());
				}
				if(item == null)
				{
					sender.sendMessage("Could not find an item.");
					return false;
				}
				return shopAdd(shop, item, amount);
			}

			// add all (player only command)
			matcher.reset();
			pattern = Pattern.compile("(?i)add\\s+all$");
			matcher = pattern.matcher(command);
			if(matcher.find())
			{
				ItemStack itemStack = player.getItemInHand();
				if(itemStack == null){ return false; }
				ItemInfo item = null;
				if(CommandShops.getItemList().isDurable(itemStack))
				{
					item = Search.itemById(itemStack.getTypeId());
					if(calcDurabilityPercentage(itemStack) > Config.MAX_DAMAGE
							&& Config.MAX_DAMAGE != 0)
					{
						sender.sendMessage(ChatColor.DARK_AQUA + "Your "
								+ ChatColor.WHITE + item.name
								+ ChatColor.DARK_AQUA
								+ " is too damaged to add to stock!");
						sender.sendMessage(ChatColor.DARK_AQUA
								+ "Items must be damanged less than "
								+ ChatColor.WHITE + Config.MAX_DAMAGE
								+ "%");
						return true;
					}
				}else
				{
					item = Search.itemById(itemStack.getTypeId(),
							itemStack.getDurability());
				}
				if(item == null)
				{
					sender.sendMessage("Could not find an item.");
					return false;
				}
				int amount = countItemsInInventory(player.getInventory(),
						itemStack);
				return shopAdd(shop, item, amount);
			}

			// add int all
			matcher.reset();
			pattern = Pattern.compile("(?i)add\\s+(\\d+)\\s+all$");
			matcher = pattern.matcher(command);
			if(matcher.find())
			{
				int id = Integer.parseInt(matcher.group(1));
				ItemInfo item = Search.itemById(id);
				if(item == null)
				{
					sender.sendMessage("Could not find an item.");
					return false;
				}
				int count = countItemsInInventory(player.getInventory(),
						item.toStack());
				return shopAdd(shop, item, count);
			}

			// add int:int all
			matcher.reset();
			pattern = Pattern.compile("(?i)add\\s+(\\d+):(\\d+)\\s+all$");
			matcher = pattern.matcher(command);
			if(matcher.find())
			{
				int id = Integer.parseInt(matcher.group(1));
				short type = Short.parseShort(matcher.group(2));
				ItemInfo item = Search.itemById(id, type);
				if(item == null)
				{
					sender.sendMessage("Could not find an item.");
					return false;
				}
				int count = countItemsInInventory(player.getInventory(),
						item.toStack());
				return shopAdd(shop, item, count);
			}

			// shop add name, ... all
			matcher.reset();
			pattern = Pattern.compile("(?i)add\\s+(.*)\\s+all$");
			matcher = pattern.matcher(command);
			if(matcher.find())
			{
				String itemName = matcher.group(1);
				ItemInfo item = Search.itemByName(itemName);
				if(item == null)
				{
					sender.sendMessage("Could not find an item.");
					return false;
				}
				int count = countItemsInInventory(player.getInventory(),
						item.toStack());
				return shopAdd(shop, item, count);
			}

		}else
		{
			sender.sendMessage("Console is not implemented yet.");
			return false;
		}

		// Command matching

		// add int
		Pattern pattern = Pattern.compile("(?i)add\\s+(\\d+)$");
		Matcher matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			ItemInfo item = Search.itemById(id);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			return shopAdd(shop, item, 0);
		}

		// add int int
		matcher.reset();
		pattern = Pattern.compile("(?i)add\\s+(\\d+)\\s+(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			int count = Integer.parseInt(matcher.group(2));
			ItemInfo item = Search.itemById(id);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			return shopAdd(shop, item, count);
		}

		// add int:int
		matcher.reset();
		pattern = Pattern.compile("(?i)add\\s+(\\d+):(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo item = Search.itemById(id, type);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			return shopAdd(shop, item, 0);
		}

		// add int:int int
		matcher.reset();
		pattern = Pattern.compile("(?i)add\\s+(\\d+):(\\d+)\\s+(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			int id = Integer.parseInt(matcher.group(1));
			short type = Short.parseShort(matcher.group(2));
			ItemInfo item = Search.itemById(id, type);
			int count = Integer.parseInt(matcher.group(3));
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			return shopAdd(shop, item, count);
		}

		// shop add name, ... int
		matcher.reset();
		pattern = Pattern.compile("(?i)add\\s+(.*)\\s+(\\d+)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String itemName = matcher.group(1);
			ItemInfo item = Search.itemByName(itemName);
			int count = Integer.parseInt(matcher.group(2));
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			return shopAdd(shop, item, count);
		}

		// shop add name, ...
		matcher.reset();
		pattern = Pattern.compile("(?i)add\\s+(.*)$");
		matcher = pattern.matcher(command);
		if(matcher.find())
		{
			String itemName = matcher.group(1);
			ItemInfo item = Search.itemByName(itemName);
			if(item == null)
			{
				sender.sendMessage("Could not find an item.");
				return false;
			}
			return shopAdd(shop, item, 0);
		}

		// Show buy help
		sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
				+ " buy [itemname] [number] " + ChatColor.DARK_AQUA
				+ "- Buy this item.");
		return true;
	}

	/**
	 * Shop add.
	 * @param shop
	 * the shop
	 * @param item
	 * the item
	 * @param amount
	 * the amount
	 * @return true, if successful
	 */
	private boolean shopAdd(Shop shop, ItemInfo item, int amount)
	{
		Player player = null;

		// Assign in sender is a Player
		if(sender instanceof Player)
		{
			player = (Player)sender;
		}

		// Calculate number of items player has
		if(player != null)
		{
			int playerItemCount = countItemsInInventory(player.getInventory(),
					item.toStack());
			// Validate Count
			if(playerItemCount >= amount)
			{
				// Perform add
				log.info(String.format("[%s] Add %d of %s to %s",
						plugin.pdfFile.getName(), amount, item, shop));
			}else{
				// Nag player
				sender.sendMessage(ChatColor.DARK_AQUA + "You only have "
						+ ChatColor.WHITE + playerItemCount
						+ ChatColor.DARK_AQUA
						+ " in your inventory that can be added.");
				// amount = playerItemCount;
				return false;
			}

			// If ALL (amount == -1), set amount to the count the player has
			if(amount == -1)
			{
				amount = playerItemCount;
			}
		}

		// If shop contains item
		if(shop.containsItem(item))
		{
			// Check if stock is unlimited
			if(shop.isUnlimitedStock())
			{
				// nicely message user
				sender.sendMessage(String.format(
						"%s has unlimited stock and already carries %s!",
						shop.getName(), item.name));
				return true;
			}

			// Check if amount to be added is 0 (no point adding 0)
			if(amount == 0)
			{
				// nicely message user
				sender.sendMessage(String.format("%s already carries %s!",
						shop.getName(), item.name));
				return true;
			}
		}

		// Add item to shop if needed
		if(!shop.containsItem(item))
		{
			shop.addItem(item.typeId, item.subTypeId, 0, 0, 0, 10);
		}

		// Check stock settings, add stock if necessary
		if(shop.isUnlimitedStock())
		{
			sender.sendMessage(ChatColor.DARK_AQUA + "Succesfully added "
					+ ChatColor.WHITE + item.name + ChatColor.DARK_AQUA
					+ " to the shop.");
		}else{
			shop.addStock(item.name, amount);
			sender.sendMessage(ChatColor.DARK_AQUA + "Succesfully added "
					+ ChatColor.WHITE + item.name + ChatColor.DARK_AQUA
					+ " to the shop. Stock is now " + ChatColor.WHITE
					+ shop.getItem(item.name).getStock());
		}

		if(amount == 0)
		{
			sender.sendMessage(ChatColor.DARK_AQUA + item.name
					+ " is almost ready to be purchased or sold!");
			sender.sendMessage(ChatColor.DARK_AQUA + "Use " + ChatColor.WHITE
					+ "\"/shop set sell " + item.name + " price bundle\""
					+ ChatColor.DARK_AQUA + " to sell this item!");
			sender.sendMessage(ChatColor.DARK_AQUA + "Use " + ChatColor.WHITE
					+ "\"/shop set buy " + item.name + " price bundle\""
					+ ChatColor.DARK_AQUA + " to  buy this item!");
		}

		// log the transaction
		if(player != null)
		{
			int itemInv = shop.getItem(item.name).getStock();
			int startInv = itemInv - amount;
			if(startInv < 0)
			{
				startInv = 0;
			}
			plugin.getShopData().logItems(player.getName(), shop.getName(),
					"add-item", item.name, amount, startInv, itemInv);

			// take items from player only if shop doesn't have unlim stock
			if(!shop.isUnlimitedStock())
			{
				removeItemsFromInventory(player.getInventory(), item.toStack(),
						amount);
			}
		}
		plugin.getShopData().saveShop(shop);
		return true;
	}
}
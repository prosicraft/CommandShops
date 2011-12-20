package com.aehdev.commandshops.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.aehdev.commandshops.CommandShops;

// TODO: Auto-generated Javadoc
/**
 * The Class CommandShopHelp.
 */
public class CommandShopHelp extends Command
{

	/**
	 * Instantiates a new command shop help.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopHelp(CommandShops plugin, String commandLabel,
			CommandSender sender, String command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/**
	 * Instantiates a new command shop help.
	 * @param plugin
	 * the plugin
	 * @param commandLabel
	 * the command label
	 * @param sender
	 * the sender
	 * @param command
	 * the command
	 */
	public CommandShopHelp(CommandShops plugin, String commandLabel,
			CommandSender sender, String[] command)
	{
		super(plugin, commandLabel, sender, command);
	}

	/* (non-Javadoc)
	 * @see com.aehdev.commandshops.commands.Command#process() */
	public boolean process()
	{
		sender.sendMessage(CommandShops.CHAT_PREFIX + ChatColor.DARK_AQUA
				+ "Here are the available commands [required] <optional>");

		if(canUseCommand(CommandTypes.ADD))
		{
			sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " add"
					+ ChatColor.DARK_AQUA
					+ " - Add the item that you are holding to the shop.");
		}
		if(canUseCommand(CommandTypes.BROWSE))
		{
			sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
					+ " browse <buy|sell|itemname> " + ChatColor.DARK_AQUA
					+ "-List the shop's inventory");
		}
		if(canUseCommand(CommandTypes.BUY))
		{
			sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
					+ " buy [itemname] [number] " + ChatColor.DARK_AQUA
					+ "- Buy this item.");
		}
		if(canUseCommand(CommandTypes.CREATE))
		{
			sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
					+ " create [ShopName]" + ChatColor.DARK_AQUA
					+ " - Create a shop at your location.");
		}
		if(canUseCommand(CommandTypes.DESTROY))
		{
			sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
					+ " destroy" + ChatColor.DARK_AQUA
					+ " - Destroy the shop you're in.");
		}
		sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
				+ " find [itemname]" + ChatColor.DARK_AQUA
				+ " - Find closest shops by item name.");
		if(canUseCommand(CommandTypes.MOVE))
		{
			sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
					+ " move [ShopID]" + ChatColor.DARK_AQUA
					+ " - Move a shop to your location.");
		}
		sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
				+ " search [itemname]" + ChatColor.DARK_AQUA
				+ " - Search for an item by name.");
		if(canUseCommand(CommandTypes.SELECT))
		{
			sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
					+ " select" + ChatColor.DARK_AQUA
					+ " - Select two corners for custom shop size.");
		}
		if(canUseCommand(CommandTypes.SELL))
		{
			sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
					+ " sell <#|all>" + ChatColor.DARK_AQUA
					+ " - Sell the item in your hand.");
			sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
					+ " sell [itemname] [number]");
		}
		if(canUseCommand(CommandTypes.SET))
		{
			sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel + " set"
					+ ChatColor.DARK_AQUA + " - Display list of set commands");
		}
		if(canUseCommand(CommandTypes.REMOVE))
		{
			sender.sendMessage(ChatColor.WHITE + "   /" + commandLabel
					+ " remove [itemname]" + ChatColor.DARK_AQUA
					+ " - Stop selling item in shop.");
		}
		return true;
	}
}
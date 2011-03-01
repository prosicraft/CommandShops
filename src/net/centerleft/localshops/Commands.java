package net.centerleft.localshops;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.PrimitiveCuboid;

public class Commands {
	static boolean createShop( CommandSender sender, String[] args ) {
		//TODO Change this so that non players can create shops as long as they send x, y, z coords
		if(canUseCommand(sender, args) && args.length == 2 && (sender instanceof Player)) {
			//command format /shop create ShopName
			Player player = (Player)sender;
			Location location = player.getLocation();
			
			long x = (long)location.getX();
			long y = (long)location.getY();
			long z = (long)location.getZ();
			
			String shopName = args[1];
			
			Shop thisShop = new Shop();
			
			thisShop.setLocation(x, y, z);
			thisShop.setShopCreator(player.getName());
			thisShop.setShopOwner(player.getName());
			thisShop.setShopName(shopName);
			thisShop.setWorldName(player.getWorld().getName());
			
			//setup the cuboid for the tree
			long[] xyzA = new long[3];
			long[] xyzB = new long[3];
			
			xyzA[0] = x - (ShopData.shopSize / 2);
			xyzB[0] = x + (ShopData.shopSize / 2);
			xyzA[2] = z - (ShopData.shopSize / 2);
			xyzB[2] = z + (ShopData.shopSize / 2);
			
			xyzA[1] = y - 1;
			xyzB[1] = y + ShopData.shopHeight - 1;
			
			//need to check to see if the shop overlaps another shop
			if( shopPositionOk(  player, xyzA, xyzB )) {
				
				PrimitiveCuboid tempShopCuboid = new PrimitiveCuboid( xyzA, xyzB );
				tempShopCuboid.name = shopName;
				//insert the shop into the world
				LocalShops.cuboidTree.insert(tempShopCuboid);
				ShopData.shops.put(shopName, thisShop );
				
				//write the file
				if( ShopData.saveShop(thisShop) ) { 
					player.sendMessage( PlayerData.chatPrefix + ChatColor.WHITE + shopName + ChatColor.AQUA + " was created succesfully.");
					return true;
				} else {
					player.sendMessage( PlayerData.chatPrefix + ChatColor.AQUA + "There was an error, could not create shop.");
					return false;
				}
			}
		}
		return false;
	}
	
	static boolean canUseCommand( CommandSender sender, String[] args ) {
		//TODO add control tests
		return true;
	}

	public static void printHelp(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		sender.sendMessage( PlayerData.chatPrefix + ChatColor.AQUA + "Looks like you need some help?");
		
	}
	
	private static boolean shopPositionOk( Player player, long[] xyzA, long[] xyzB ) {
		BookmarkedResult res = new BookmarkedResult();
		
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzA[0], xyzA[1], xyzA[2] );
		if( shopOverlaps(player, res) ) return false;
		
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzA[0], xyzA[1], xyzB[2] );
		if( shopOverlaps(player, res) ) return false;
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzA[0], xyzB[1], xyzA[2] );
		if( shopOverlaps(player, res) ) return false;
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzA[0], xyzB[1], xyzB[2] );
		if( shopOverlaps(player, res) ) return false;
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzB[0], xyzA[1], xyzA[2] );
		if( shopOverlaps(player, res) ) return false;
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzB[0], xyzA[1], xyzB[2] );
		if( shopOverlaps(player, res) ) return false;
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzB[0], xyzB[1], xyzA[2] );
		if( shopOverlaps(player, res) ) return false;
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzB[0], xyzB[1], xyzB[2] );
		if( shopOverlaps(player, res) ) return false;
		return true;
	}
	
	private static boolean shopOverlaps( Player player, BookmarkedResult res ) {
		if( res.results.size() != 0 ) {
			for( PrimitiveCuboid shop : res.results) {
				if(shop.name != null) {
					player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "Could not create shop, it overlaps with " + ChatColor.WHITE 
							+ shop.name );
					return true;
				}
			}
		}
		return false;
	}

	public static void listShop(CommandSender sender, String[] args) {
		
		// TODO This needs a command for adding page #'s to the shop list
		// so that long pages will wrap to second page
		
		if(canUseCommand(sender, args) && (sender instanceof Player)) {
			Player player = (Player)sender;
			String playerName = player.getName();
			String inShopName;
			
			int pageNumber = 1;
			
			if(args.length == 2) {
				try {
					pageNumber = Integer.parseInt(args[1]);
				} catch (NumberFormatException ex) {
					
				}
			}
			
			if(args.length == 3 ) {
				try {
					pageNumber = Integer.parseInt(args[2]);
				} catch (NumberFormatException ex2) {
					
				}
			}
		
			//get the shop the player is currently in

			if( PlayerData.playerShopsList(playerName).size() == 1 ) {
				inShopName = PlayerData.playerShopList.get(playerName).get(0);
				Shop shop = ShopData.shops.get(inShopName);
				
				if( args.length > 1 ) {
					if( args[1].equalsIgnoreCase("buy") || args[1].equalsIgnoreCase("sell")) {
						printInventory( shop, player, args[1], pageNumber );
						
					} else {
						printInventory( shop, player, "list", pageNumber );
					}
				} else {
					printInventory( shop, player, "list", pageNumber );
				}
			} else {
				player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /shop list");
			}
		}
	}
	
	public static void printInventory( Shop shop, Player player, String buySellorList) {
		printInventory( shop, player, buySellorList, 1 );
	}
	
	public static void printInventory( Shop shop, Player player, String buySellorList, int pageNumber) {
		String inShopName = shop.getShopName();
		ArrayList<String> shopItems = shop.getItems();
		
		boolean buy = buySellorList.equalsIgnoreCase("buy");
		boolean sell = buySellorList.equalsIgnoreCase("sell");
		boolean list = buySellorList.equalsIgnoreCase("list");
		
		ArrayList<String> inventoryMessage = new ArrayList<String>();
		for(String item: shopItems ) {
			String subMessage = "   " + item;
			if(!list) {
				int price = 0;
				if(buy) {
				//get buy price
					price = shop.getItemBuyPrice(item);
				}
				if(sell) {
					price = shop.getItemSellPrice(item);
				}
	 			if(price == 0) continue;
				subMessage += ChatColor.AQUA + " [" + ChatColor.WHITE + price + " " + ShopData.currencyName 
					+ ChatColor.AQUA + "]";
				//get stack size
				int stack = shop.itemBuyAmount(item);
				if(buy) {
					stack = shop.itemBuyAmount(item);
				}
				if(sell) {
					stack = shop.itemSellAmount(item);
				}
				if( stack > 1 ) {
					subMessage += ChatColor.AQUA + " [" + ChatColor.WHITE + "Bundle: " + stack + ChatColor.AQUA + "]";
				}
			}
			//get stock
			int stock = shop.getItemStock(item);
			if(buy) {
				if(stock == 0) continue;
			}
			subMessage += ChatColor.AQUA + " [" + ChatColor.WHITE + "Stock: " + stock + ChatColor.AQUA + "]";
			inventoryMessage.add(subMessage);
		}
		
		String message = ChatColor.AQUA + "The shop " + ChatColor.WHITE + inShopName + ChatColor.AQUA;
		
		if( buy ) {
			message += " is selling:";
		} else if ( sell ) {
			message += " is buying:";
		} else {
			message += " trades in: ";
		}
		
		message += " (Page " + pageNumber + " of " 
			+ (int) Math.ceil((double) inventoryMessage.size() / (double) 7) + ")";
		
		player.sendMessage(message);
		
		int amount = (pageNumber > 0 ? (pageNumber - 1)*7 : 0);
		for (int i = amount; i < amount + 7; i++) {
            if (inventoryMessage.size() > i) {
            	player.sendMessage(inventoryMessage.get(i));
            }
        }
		
		if(!list) {
			String buySell = ( buy ? "buy" : "sell" );
			message = ChatColor.AQUA + "To " + buySell + " an item on the list type: " +
				ChatColor.WHITE + "/shop " + buySell + " ItemName [ammount]";
			player.sendMessage(message);
		} else {
			player.sendMessage(ChatColor.AQUA + "Type " + ChatColor.WHITE + "/shop list buy" 
					+ ChatColor.AQUA + " or " + ChatColor.WHITE + "/shop list sell");
			player.sendMessage(ChatColor.AQUA + "to see details about price and quantity.");
		}
	}

	public static boolean sellItemShop(CommandSender sender, String[] args) {
		if(!(sender instanceof Player) || !canUseCommand(sender, args)) return false;
		if(!ShopsPluginListener.useiConomy) return false;
		
		/* Available formats:
		 *  /shop sell
		 *  /shop sell #
		 *  /shop sell all
		 *  /shop sell item #
		 *  /shop sell item all
		 */
		
		Player player = (Player)sender;
		String playerName = player.getName();

		//get the shop the player is currently in
		if( PlayerData.playerShopsList(playerName).size() == 1 ) {
			String shopName = PlayerData.playerShopsList(playerName).get(0);
			Shop shop = ShopData.shops.get(shopName);
			
			ItemStack item = null;
			String itemName = null;
			int amount = 0;
			
			if(args.length == 1 || args.length == 2) {
			
				item = player.getInventory().getItemInHand();
				if( item == null || item.getType().getId() == Material.AIR.getId()) {
					return true;
				}
	

				if( item.getData() != null) {
					itemName = LocalShops.itemList.getItemName(item.getType().getId(), (int)item.getData().getData());
				} else {
					itemName = LocalShops.itemList.getItemName(item.getType().getId()).get(0);
				}
				
				amount = item.getAmount();
				if(args.length == 2) {
					int totalAmount = 0;
					for(Integer i : player.getInventory().all(item.getType()).keySet()) {
						totalAmount += player.getInventory().getItem(i).getAmount();
					}
					try {
						int numberToRemove = Integer.parseInt(args[1]);
						if( numberToRemove > totalAmount) {
							amount = totalAmount;
						} else {
							amount = numberToRemove;
						}
					} catch ( NumberFormatException ex1 ) {
						if(args[1].equalsIgnoreCase("all")) {
							amount = totalAmount;
						} else {
							player.sendMessage(ChatColor.AQUA + "Input problem. The format is " + ChatColor.WHITE + "/shop sell <# to sell>");
							return false;							
						}
					}
				}
				
			}
			
			if(args.length == 3) {
				item = LocalShops.itemList.getItem(args[1]);
				if(item == null) {
					player.sendMessage(ChatColor.AQUA + "Could not find an item of that type.");
					return false;
				}
				
System.out.println("Returned itemstack type " + item.getType().toString());
				int totalAmount = 0;
				for(Integer i : player.getInventory().all(item.getType()).keySet()) {
					totalAmount += player.getInventory().getItem(i).getAmount();
				}
				
				if(item.getData() != null) {
					itemName = LocalShops.itemList.getItemName(item.getType().getId(), (int)item.getData().getData());
				} else {
					itemName = LocalShops.itemList.getItemName(item.getType().getId()).get(0);
				}
				
				try {
					int numberToRemove = Integer.parseInt(args[2]);
					if( numberToRemove > totalAmount) {
						amount = totalAmount;
					} else {
						amount = numberToRemove;
					}
				} catch (NumberFormatException ex2 ) {
					if( args[2].equalsIgnoreCase("all")) {
						amount = totalAmount;
					} else {
						player.sendMessage(ChatColor.AQUA + "Input problem. The format is " + ChatColor.WHITE + "/shop sell <itemName> <# to sell>");
						return false;
					}
				}
				
			}
			
			
			//check if the shop is buying that item
			if(!shop.getItems().contains(itemName) || shop.getItemSellPrice(itemName) == 0) {
				player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shopName 
						+ ChatColor.AQUA + " is not buying " + ChatColor.WHITE + itemName 
						+ ChatColor.AQUA + " right now." );
				return false;
			}
			
			//calculate cost
			int bundles = amount / shop.itemSellAmount(itemName);
			int itemPrice = shop.getItemSellPrice(itemName);
			//recalculate # of items since may not fit cleanly into bundles
			amount = bundles * shop.itemSellAmount(itemName);
			int totalCost = bundles * itemPrice;
			
			//try to pay the player
			if(shop.isUnlimited()) {
				PlayerData.payPlayer(playerName, totalCost);
			} else {
				if(!PlayerData.payPlayer(shop.getShopOwner(), playerName, totalCost)) {
					//shop owner doesn't have enough money
					//get shop owner's balance and calculate how many it can buy
					long shopBalance = PlayerData.getBalance(shop.getShopOwner());
					int bundlesCanAford = (int)shopBalance / itemPrice;
					totalCost = bundlesCanAford * itemPrice;
					amount = bundlesCanAford * shop.itemSellAmount(itemName);
					if(!PlayerData.payPlayer(shop.getShopOwner(), playerName, totalCost)) {
						player.sendMessage(ChatColor.AQUA + "Unexpected money problem: could not complete sale.");
						return false;
					}
				}
			}
			
			player.sendMessage("Selling " + amount);
			//remove number of items from seller
			for(int i: player.getInventory().all(item.getType()).keySet()) {
				if( amount == 0 ) continue;
				ItemStack thisStack = player.getInventory().getItem(i);
				int foundAmount = thisStack.getAmount();
				if( amount >= foundAmount ) {
					amount -= foundAmount;
					player.getInventory().setItem(i, null);
				} else {
					int amountInStack = player.getInventory().getItem(i).getAmount();
					thisStack.setAmount(amountInStack - amount);
					player.getInventory().setItem(i, thisStack);
					amount = 0;
				}
				
			}
			
			shop.addStock(itemName, amount);

		} else {
			player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /shop " + args[0]);
		}
			
		return true;
	}
}

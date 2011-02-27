package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Shop {
	
	private String worldName;
	private String shopName;
	private Location shopLocation;
	private String shopOwner;
	private String shopCreator;
	private String[] shopManagers;
	private boolean unlimitedStock;
	
	private HashMap<String, Item> shopInventory;
	
	public Shop() {
		worldName = "";
		shopName = null;
		shopInventory = new HashMap<String, Item>();
		shopInventory.clear();
		shopLocation = new Location(0,0,0);
		shopOwner = "";
		shopCreator = "";
		shopManagers = null;
		unlimitedStock = false;
	}
	
	public void setWorldName( String name ) {
		worldName = name;
	}
	
	public String getWorldName() {
		return worldName;
	}
	
	public void setShopName( String name ) {
		shopName = name;
	}
	
	public String getShopName() {
		return shopName;
	}
	
	public void setShopOwner( String owner ) {
		shopOwner = owner;
	}

	private class Item {
		
		private String itemName;
		private int buyStackSize;
		private int buyStackPrice;
		private int sellStackSize;
		private int sellStackPrice;
		private int stock;
		
		public Item() {
			itemName = null;
			buyStackSize = 1;
			buyStackPrice = 0;
			sellStackSize = 1;
			sellStackPrice = 0;
			stock = 0;
		}
		
		public Item(String name ) {
			this.itemName = name;

			buyStackSize = 1;
			buyStackPrice = 0;
			sellStackSize = 1;
			sellStackPrice = 0;
			stock = 0;
		}

		public String itemName () {
			return this.itemName;
		}

		public void setSell(int sellPrice, int sellSize) {
			sellStackPrice = sellPrice;
			sellStackSize = sellSize;
			
		}

		public void setBuy(int buyPrice, int buySize) {
			buyStackPrice = buyPrice;
			buyStackSize = buySize;			
		}
	
	}
	
	private class Location {
		private long lx, ly, lz;
		
		public Location(long x, long y, long z) {
			lx = x;
			ly = y;
			lz = z;
		}
		
		public Location(long[] xyz) {
			if(xyz.length == 3) {
				lx = xyz[0];
				ly = xyz[1];
				lz = xyz[2];
			} else {
				lx = 0;
				ly = 0;
				lz = 0;
			}
		}
		
		public long[] getLocation() {
			long[] location = {lx, ly, lz};
			return location;
		}
		
		public boolean setLocation(long x, long y, long z) {
			lx = x;
			ly = y;
			lz = z;
			return true;
		}
		
		public boolean setLocation(long[] xyz) {
			if(xyz.length == 3) {
				lx = xyz[0];
				ly = xyz[1];
				lz = xyz[2];
			} else {
				lx = 0;
				ly = 0;
				lz = 0;
				return false;
			}
			return true;
		}
		
	}

	public void setShopCreator(String name) {
		shopCreator = name;
	}

	public void setShopManagers(String[] names) {
		shopManagers = names.clone();
	}

	public void setLocation(long[] position) {
		shopLocation.setLocation(position);
	}
	
	public void setLocation( long x, long y, long z) {
		shopLocation.setLocation(x, y, z);
	}

	public void setUnlimited(boolean b) {
		unlimitedStock = b;
	}

	public void addItem(int itemNumber, int itemData, int buyPrice,
			int buyStackSize, int sellPrice, int sellStackSize, int stock) {
		
		String itemName = LocalShops.itemList.getItemName(itemNumber, itemData);
		Item thisItem = new Item( itemName );
		
		thisItem.setBuy( buyPrice, buyStackSize );
		thisItem.setSell( sellPrice, sellStackSize );
		
		thisItem.stock = stock;
		
		if(shopInventory.containsKey(itemName)) {
			shopInventory.remove(itemName);
		}
		
		shopInventory.put(itemName, thisItem);
		
	}

	public String getShopOwner() {
		return this.shopOwner;
	}

	public String getShopCreator() {
		return this.shopCreator;
	}

	public String getShopPositionString() {
		String returnString = "";
		for( long coord : shopLocation.getLocation()) {
			returnString += coord + ",";
		}
		return returnString;
	}

	public String[] getShopManagers() {
		return shopManagers;
	}

	public String getValueofUnlimited() {
		if(this.unlimitedStock) {
			return "true";
		} 
		return "false";
	}

	public ArrayList<String> getItems() {
		ArrayList<String> allItemNames = new ArrayList<String>();

		Iterator itr = shopInventory.entrySet().iterator();
		while(itr.hasNext()) {
			Map.Entry item = (Map.Entry)itr.next();
			String name = ((Item)item.getValue()).itemName();
			allItemNames.add(name);
		}
		
		Collections.sort(allItemNames);
		
		return allItemNames;
	}

	public int getItemBuyPrice(String itemName ) {
		return shopInventory.get(itemName).buyStackPrice;
		
	}

	public int itemBuyAmount(String itemName) {
		return shopInventory.get(itemName).buyStackSize;
	}

	public int getItemSellPrice(String itemName) {
		return shopInventory.get(itemName).sellStackPrice;
	}
	
	public int itemSellAmount(String itemName) {
		return shopInventory.get(itemName).sellStackSize;
	}

	public int getItemStock(String itemName) {
		return shopInventory.get(itemName).stock;
	}

}
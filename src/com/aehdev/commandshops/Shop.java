package com.aehdev.commandshops;

import java.sql.ResultSet;
import java.util.Locale;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.PrimitiveCuboid;

// TODO: Auto-generated Javadoc
/**
 * The Class Shop.
 */
public class Shop
{
	/** The Constant log. */
	private static final Logger log = Logger.getLogger("Minecraft");

	/**
	 * Gets the ID of the shop a given player is standing in.
	 * @param player the player
	 * @return the ID of the shop containing the player, or -1 if the player is not in a shop.
	 */
	public static long getCurrentShop(Player player)
	{
		return getCurrentShop(	player.getLocation().getBlockX(),
								player.getLocation().getBlockY(),
								player.getLocation().getBlockZ());
	}
	
	/**
	 * Gets the ID of the shop a given point is in.
	 * 
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param z z-coordinate
	 * @return the ID of the shop containing the point, or -1 if the point is not in a shop.
	 */
	public static long getCurrentShop(int x, int y, int z)
	{
		String locQuery = String.format((Locale)null,"SELECT id FROM shops WHERE x<=%d AND x2>=%d AND y<=%d AND y2 >=%d AND z<=%d AND z2>=%d LIMIT 1",x,x,y,y,z,z);
		long id = -1;
		try{
			ResultSet resId = CommandShops.db.query(locQuery);
			if(!resId.next()) return -1;
			id = resId.getLong("id");
			resId.close();
		}catch(Exception e){
			log.warning(String.format((Locale)null,"[%s] - Couldn't detect shop: "+e, CommandShops.pdfFile.getName()));
		}
		return id;
	}
	
	/**
	 * Ensures that a shop's coordinates match its assigned region, if any.
	 * @param shop the ID of the shop to refresh
	 */
	public static void refreshRegionLocation(long shop)
	{
		if(CommandShops.worldguard == null) return;	//Can't do anything if worldguard is down.
		String locQuery = String.format((Locale)null,"SELECT region,world,x,y,z,x2,y2,z2 FROM shops WHERE id=%d LIMIT 1",shop);
		double[] xyzA = new double[3];
		double[] xyzB = new double[3];
		int x,y,z,x2,y2,z2;
		int oldx,oldy,oldz,oldx2,oldy2,oldz2;
		
		String region = "";
		String world = "";
		
		try{
			//get region and world for this shop
			ResultSet resId = CommandShops.db.query(locQuery);
			if(!resId.next()) throw new IllegalArgumentException();
			region = resId.getString("region");
			world = resId.getString("world");
			if(region == null || region == "NULL" || region == "") return; //shop isn't region based
			
			//get old coords for when we update the cuboid later
			oldx = resId.getInt("x");
			oldy = resId.getInt("y");
			oldz = resId.getInt("z");
			oldx2 = resId.getInt("x2");
			oldy2 = resId.getInt("y2");
			oldz2 = resId.getInt("z2");
			resId.close();
			
			//look up region coordinates in worldguard
			ProtectedRegion regionobj = CommandShops.worldguard.get(Bukkit.getWorld(world)).getRegion(region);
			BlockVector bva = regionobj.getMinimumPoint();
			BlockVector bvb = regionobj.getMaximumPoint();
			xyzA[0] = bva.getX();
			xyzA[1] = bva.getY();
			xyzA[2] = bva.getZ();
			xyzB[0] = bvb.getX();
			xyzB[1] = bvb.getY();
			xyzB[2] = bvb.getZ();
			
			//save up-to-date coordinates in shop
			x = Math.min((int)xyzA[0], (int)xyzB[0]);
			y = Math.min((int)xyzA[1], (int)xyzB[1]);
			z = Math.min((int)xyzA[2], (int)xyzB[2]);
			x2 = Math.max((int)xyzA[0], (int)xyzB[0]);
			y2 = Math.max((int)xyzA[1], (int)xyzB[1]);
			z2 = Math.max((int)xyzA[2], (int)xyzB[2]);
			String moveQuery = String.format((Locale)null,"UPDATE shops SET x=%d,y=%d,z=%d,x2=%d,y2=%d,z2=%d WHERE id=%d LIMIT 1"
																			,x,  y,   z,   x2,   y2,   z2, 			shop);
			CommandShops.db.query(moveQuery);
		}catch(IllegalArgumentException e){
			log.warning(String.format((Locale)null,"[%s] - Tried to refresh invalid shop ID: "+e, CommandShops.pdfFile.getName()));
			return;
		}catch(Exception e){
			log.warning(String.format((Locale)null,"[%s] - Database problem refreshing shop: "+e, CommandShops.pdfFile.getName()));
			return;
		}
		
		//update cuboid
		ShopLocation xyz = new ShopLocation((oldx+oldx2)/2,(oldy+oldy2)/2,(oldz+oldz2)/2);
		BookmarkedResult res = new BookmarkedResult();
		res = CommandShops.getCuboidTree().relatedSearch(res.bookmark, xyz.getX(), xyz.getY(), xyz.getZ());
		for(PrimitiveCuboid shopLocation: res.results)
		{
			if(shopLocation.id == -1) continue;
			if(!shopLocation.world.equalsIgnoreCase(world)) continue;
			CommandShops.getCuboidTree().delete(shopLocation);
		}
		
		PrimitiveCuboid newShop = new PrimitiveCuboid(x, y, z, x2, y2, z2);
		newShop.id = shop;
		newShop.world = world;
		CommandShops.getCuboidTree().insert(newShop);
	}
	
	/**
	 * Ensures that all shops' coordinates match their assigned regions, if any.
	 */
	public static void refreshRegionLocations()
	{
		if(CommandShops.worldguard == null) return;	//Can't do anything if worldguard is down.
		String regQuery = "SELECT id FROM shops WHERE region IS NOT NULL";
		try{
			ResultSet resReg = CommandShops.db.query(regQuery);
			while(resReg.next())
			{
				refreshRegionLocation(resReg.getInt("id"));
			}
			resReg.close();
		}catch(Exception e){
			log.warning(String.format((Locale)null,"[%s] - Database problem finding shops with regions for global refresh: "+e, CommandShops.pdfFile.getName()));
		}
	}
	
	/**
	 * Ensures that a shop's coordinates match its assigned region, if any.
	 * @param region the name of the region for which we will update a shop, if any.
	 * @param world the world of the region
	 */
	public static void refreshRegionLocation(String region, String world)
	{
		if(CommandShops.worldguard == null) return;	//Can't do anything if worldguard is down.
		String regQuery = "SELECT id FROM shops WHERE region='" + region + "' AND world='" + world + "' LIMIT 1";
		try{
			ResultSet resReg = CommandShops.db.query(regQuery);
			while(resReg.next())
			{
				refreshRegionLocation(resReg.getInt("id"));
			}
			resReg.close();
		}catch(Exception e){
			log.warning(String.format((Locale)null,"[%s] - Database problem finding shop with region to be refreshed: "+e, CommandShops.pdfFile.getName()));
		}
	}
}

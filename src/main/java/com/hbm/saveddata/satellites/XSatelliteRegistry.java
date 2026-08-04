package com.hbm.saveddata.satellites;

import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.items.ModItems;
import com.hbm.saveddata.SatelliteSavedData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XSatelliteRegistry {
	
	public static final List<Class<? extends SatelliteBase>> satellites = new ArrayList<>();
	public static final HashMap<Item, Class<? extends SatelliteBase>> itemToClass = new HashMap<>();
	private static final HashMap<Class<? extends SatelliteBase>, float[]> satelliteColors = new HashMap<>();
	
	public static void register() {

		// no enum items, sorry

		registerSatellite(SatelliteMapper.class, ModItems.sat_mapper, 0.538F, 1.0F, 0.523F);
		registerSatellite(SatelliteScanner.class, ModItems.sat_scanner, 0.544F, 0.680F, 1.0F);
		registerSatellite(SatelliteRadar.class, ModItems.sat_radar, 0.134F, 1.0F, 0.134F);
		registerSatellite(SatelliteDeathRay.class, ModItems.sat_laser, 0.221F, 0.663F, 1.0F);
		registerSatellite(SatelliteResonator.class, ModItems.sat_resonator, 1.0F, 0.646F, 0.181F);
		registerSatellite(SatelliteRelay.class, ModItems.sat_foeq, 1.0F, 0.15F, 0.15F);
		registerSatellite(SatelliteMiner.class, ModItems.sat_miner, 0.46F, 0.56F, 0.68F);
		registerSatellite(SatelliteLunarMiner.class, ModItems.sat_lunar_miner, 0.42F, 0.54F, 0.82F);
		registerSatellite(SatelliteDysonRelay.class, ModItems.sat_dyson_relay, 1.0F, 0.9F, 0.8F);
		registerSatellite(SatelliteHorizons.class, ModItems.sat_gerald, 0.0F, 0.0F, 0.0F);
		registerSatellite(SatelliteRailgun.class, ModItems.sat_war, 0.0F, 0.0F, 0.0F);
		registerSatellite(SatellitePrecisionLaser.class, ModItems.sat_precision_laser, 0.221F, 1.0F, 0.663F);

	}

	/**
	 * Register satellite.
	 * @param sat - Satellite class
	 * @param item - Satellite item (which will be placed in a rocket)
	 */
	public static void registerSatellite(Class<? extends SatelliteBase> sat, Item item, float r, float g, float b) {
		if(!itemToClass.containsKey(item) && !itemToClass.containsValue(sat)) {
			satellites.add(sat);
			itemToClass.put(item, sat);
			satelliteColors.put(sat, new float[] { r, g, b });
		}
	}
	
	public static void orbit(World world, ItemStack stack, int freq, double x, double y, double z) {
		if(world.isRemote) return;

		SatelliteBase sat = createFromItem(stack);
		
		if(sat != null) {
			SatelliteSavedData data = SatelliteSavedData.getData(world, (int)x, (int)z);
			data.sats.put(freq, sat);
			sat.onOrbit(world, x, y, z);
			data.markDirty();
		}
	}
	
	public static SatelliteBase createFromId(int i) {
		try {
			return satellites.get(i).newInstance();
		} catch(Exception e) { }
		return null;
	}
	
	public static SatelliteBase createFromItem(ItemStack stack) {
		try {
			return itemToClass.get(new ComparableStack(stack).makeSingular()).newInstance();
		} catch(Exception e) { }
		return null;
	}
	

	public static float[] getRegisteredColor(Item item) {
		Class<? extends SatelliteBase> satelliteClass = itemToClass.get(item);
		if(satelliteClass == null) {
			throw new IllegalStateException("No satellite class registered for item: " + item);
		}
		return getRegisteredColor(satelliteClass);
	}

	public static float[] getRegisteredColor(Class<? extends SatelliteBase> satelliteClass) {
		float[] color = satelliteColors.get(satelliteClass);
		if(color == null) {
			throw new IllegalStateException("No color registered for satellite class: " + satelliteClass);
		}
		return color;
	}

}

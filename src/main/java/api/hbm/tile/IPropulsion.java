package api.hbm.tile;

import com.hbm.dim.orbit.OrbitalStation;

import net.minecraft.tileentity.TileEntity;

// it go
public interface IPropulsion {

	/**
	 * Adds a propulsion system to a station
	 * Must be associated with a TileEntity
	 */

	// Register the propulsion source to a station, so the station can utilise it for travel
	public default void registerPropulsion() {
		OrbitalStation.addPropulsion(this);
	}

	// And unregister when the block is broken
	public default void unregisterPropulsion() {
		OrbitalStation.removePropulsion(this);
	}

	public TileEntity getTileEntity();
	
	// Is the engine sufficiently fueled to perform a burn changing the velocity of a given mass by deltaV
	public boolean canPerformBurn(int shipMass, double deltaV);
	
	// How much thrust does this engine produce? Higher = faster travel
	public float getThrust();

	// Turns the engine on if not already running
	// returns a number of ticks to wait before the engine can provide thrust
	public int startBurn();

	// Turns the engine off if it wasn't already running
	// returns a number of ticks to wait before another attempt to travel can be made
	public int endBurn();

}
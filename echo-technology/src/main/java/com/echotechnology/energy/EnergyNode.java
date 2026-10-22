package com.echotechnology.energy;

/**
 * Implemented by every block entity that participates in the Echo energy network.
 * Producers push energy into adjacent nodes whose storage can receive.
 */
public interface EnergyNode {
    EchoEnergyStorage getEnergyStorage();

    /** Conduits use diffusion between each other instead of plain pushing. */
    default boolean isConduit() {
        return false;
    }
}

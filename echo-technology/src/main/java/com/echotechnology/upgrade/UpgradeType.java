package com.echotechnology.upgrade;

public enum UpgradeType {
    /** Fabricator/charger use less energy; reactor extracts more energy per fuel item. */
    EFFICIENCY,
    /** Increases a machine's internal energy buffer. */
    CAPACITY,
    /** Faster processing / generation / charging at a small energy premium. */
    SPEED
}

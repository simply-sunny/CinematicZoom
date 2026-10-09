package net.fabricmc.loader.api;

import java.nio.file.Path;

// Run config reads/writes in the test directory, without starting Fabric or Minecraft.
public final class FabricLoader {
    public static FabricLoader getInstance() {
        return new FabricLoader();
    }

    public Path getConfigDir() {
        return Path.of(System.getProperty("cinematiczoom.testConfig"));
    }
}

package me.jellysquid.mods.sodium;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.jellysquid.mods.sodium.config.user.UserConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;

@Mod(SodiumClientMod.MOD_ID)
public class SodiumClientMod {
	private static UserConfig CONFIG;
	private static Logger LOGGER;

	public static final String MOD_ID = "sodium";

	public static final IEventBus FORGE_EVENT_BUS = MinecraftForge.EVENT_BUS;
	public static final IEventBus MOD_EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();

	private static String MOD_VERSION;

	public SodiumClientMod() {
		MOD_EVENT_BUS.addListener(this::onInitializeClient);

		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
				() -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

	}

	@SubscribeEvent
	public void onInitializeClient(FMLClientSetupEvent event) {
		ModContainer mod = ModLoadingContext.get().getActiveContainer();

		MOD_VERSION = mod.getModInfo().getVersion().toString();

		LOGGER = LogManager.getLogger("Sodium");
		CONFIG = loadConfig();
	}

	public static UserConfig options() {
		if (CONFIG == null) {
			throw new IllegalStateException("Config not yet available");
		}

		return CONFIG;
	}

	public static Logger logger() {
		if (LOGGER == null) {
			throw new IllegalStateException("Logger not yet available");
		}

		return LOGGER;
	}

	private static UserConfig loadConfig() {
		try {
			return UserConfig.load();
		} catch (Exception e) {
			LOGGER.error("Failed to load configuration file", e);
			LOGGER.error("Using default configuration file in read-only mode");

			var config = new UserConfig();
			config.setReadOnly();

			return config;
		}
	}

	public static void restoreDefaultOptions() {
		CONFIG = UserConfig.defaults();

		try {
			CONFIG.writeChanges();
		} catch (IOException e) {
			throw new RuntimeException("Failed to write config file", e);
		}
	}

	public static String getVersion() {
		if (MOD_VERSION == null) {
			throw new NullPointerException("Mod version hasn't been populated yet");
		}

		return MOD_VERSION;
	}

	public static boolean isDirectMemoryAccessEnabled() {
		return options().advanced.allowDirectMemoryAccess;
	}
}

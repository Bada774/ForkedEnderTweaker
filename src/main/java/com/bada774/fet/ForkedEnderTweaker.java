package com.bada774.fet;

import java.util.ArrayList;
import java.util.List;

import com.bada774.fet.utils.Logging;
import com.bada774.fet.utils.zrr.ZRRIntegration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MODID, name = Tags.MODNAME, version = Tags.VERSION, dependencies = "required:enderio@[5.3.67,);required-after:crafttweaker;after:zenrecipereloading")
public class ForkedEnderTweaker {

	private static final Logger LOGGER = LogManager.getLogger();

	public static final List<Runnable> LATE_QUEUE = new ArrayList<>();
	public static boolean LOAD_COMPLETE = false;



	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		if (Loader.isModLoaded("zenrecipereloading")) {
			try {
				ZRRIntegration.register();
			} catch (Exception ex) {
				LOGGER.error("Failed to register ZenRecipeReload module.", ex);
			}
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {

		LOAD_COMPLETE = true;

		for (Runnable r : LATE_QUEUE) {
			try {
				r.run();
			} catch (Exception ex) {
				Logging.logError("FET: Error executing queued action during PostInit. Check logs for further info.");
				LOGGER.error("Error executing queued action during PostInit:", ex);
			}
		}
		LATE_QUEUE.clear();
	}
}

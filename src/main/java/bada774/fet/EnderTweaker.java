package bada774.fet;

import java.util.ArrayList;
import java.util.List;

import bada774.fet.utils.Logging;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = EnderTweaker.MODID, name = EnderTweaker.MODNAME, version = EnderTweaker.VERSION, dependencies = "required:enderio@[5.3.67,);required-after:crafttweaker;after:zenrecipereloading")
public class EnderTweaker {

	public static final String MODID = "endertweaker";
	public static final String MODNAME = "ForkedEnderTweaker";
	public static final String VERSION = "2.0.2";

	public static final List<Runnable> LATE_QUEUE = new ArrayList<>();
	public static boolean LOAD_COMPLETE = false;

	@EventHandler
	public void preinit(FMLPreInitializationEvent e) {
		if (Loader.isModLoaded("zenrecipereloading")) {
			try {
				bada774.fet.utils.zrr.ZRRIntegration.register();
			} catch (Exception ex) {
				ex.printStackTrace();
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
				Logging.logError("EnderTweaker: Error executing queued action during PostInit");
				ex.printStackTrace();
			}
		}
		LATE_QUEUE.clear();
	}
}

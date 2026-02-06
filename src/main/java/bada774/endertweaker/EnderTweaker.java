package bada774.endertweaker;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = EnderTweaker.MODID, name = EnderTweaker.MODNAME, version = EnderTweaker.VERSION, dependencies = "required:enderio@[5.3.67,);required-after:crafttweaker;after:zenrecipereloading")
public class EnderTweaker {

	public static final String MODID = "endertweaker";
	public static final String MODNAME = "AlternativeEnderTweaker";
	public static final String VERSION = "1.2.5";
	public static final List<Runnable> ADDITIONS = new ArrayList<>();
	public static final List<Runnable> REMOVALS = new ArrayList<>();

	public static final List<Runnable> LATE_QUEUE = new ArrayList<>();
	public static boolean LOAD_COMPLETE = false;

	@EventHandler
	public void preinit(FMLPreInitializationEvent e) {
		if (Loader.isModLoaded("zenrecipereloading")) {
			try {
				bada774.endertweaker.utils.ZRR.ZRRIntegration.register();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		for (Runnable r : REMOVALS)
			r.run();
		REMOVALS.clear();

		LOAD_COMPLETE = true;

		for (Runnable r : LATE_QUEUE) {
			try {
				r.run();
			} catch (Exception ex) {
				System.out.println("EnderTweaker: Error executing queued action during PostInit");
				ex.printStackTrace();
			}
		}
		LATE_QUEUE.clear();

		// for (IAction action : LATE_ACTIONS) {
		// try {
		// crafttweaker.CraftTweakerAPI.apply(action);
		// } catch (Exception ex) {
		// System.out.println("EnderTweaker: Error executing action: " +
		// action.describe());
		// ex.printStackTrace();
		// }
		// }
		// LATE_ACTIONS.clear();
	}

	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent e) {
		for (Runnable r : ADDITIONS)
			r.run();
		ADDITIONS.clear();

	}
}

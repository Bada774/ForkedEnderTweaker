package bada774.endertweaker.utils.ZRR;

import bada774.endertweaker.utils.ZRR.callbacks.AlloySmelterCallbacks;
import bada774.endertweaker.utils.ZRR.callbacks.CombustionGenCallbacks;

import youyihj.zenrecipereloading.module.PlainModule;

public class EnderTweakerModule extends PlainModule {
        public EnderTweakerModule() {

                AlloySmelterCallbacks.register(this);
                CombustionGenCallbacks.register(this);
        }

}

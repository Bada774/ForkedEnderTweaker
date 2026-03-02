package bada774.endertweaker.utils.ZRR;

import bada774.endertweaker.utils.ZRR.callbacks.AlloySmelterCallbacks;
import bada774.endertweaker.utils.ZRR.callbacks.CombustionGenCallbacks;
import bada774.endertweaker.utils.ZRR.callbacks.EnchanterCallbacks;
import bada774.endertweaker.utils.ZRR.callbacks.SagMillCallbacks;
import bada774.endertweaker.utils.ZRR.callbacks.SliceNSpliceCallbacks;
import bada774.endertweaker.utils.ZRR.callbacks.SoulBinderCallbacks;
import bada774.endertweaker.utils.ZRR.callbacks.TankCallbacks;
import bada774.endertweaker.utils.ZRR.callbacks.VatCallbacks;

import youyihj.zenrecipereloading.module.PlainModule;

public class EnderTweakerModule extends PlainModule {
        public EnderTweakerModule() {

                AlloySmelterCallbacks.register(this);
                CombustionGenCallbacks.register(this);
                EnchanterCallbacks.register(this);
                SagMillCallbacks.register(this);
                SliceNSpliceCallbacks.register(this);
                SoulBinderCallbacks.register(this);
                TankCallbacks.register(this);
                VatCallbacks.register(this);
        }

}

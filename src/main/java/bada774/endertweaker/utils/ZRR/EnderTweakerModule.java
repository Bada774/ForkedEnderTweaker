package bada774.endertweaker.utils.zrr;

import bada774.endertweaker.utils.zrr.callbacks.AlloySmelterCallbacks;
import bada774.endertweaker.utils.zrr.callbacks.CombustionGenCallbacks;
import bada774.endertweaker.utils.zrr.callbacks.EnchanterCallbacks;
import bada774.endertweaker.utils.zrr.callbacks.SagMillCallbacks;
import bada774.endertweaker.utils.zrr.callbacks.SliceNSpliceCallbacks;
import bada774.endertweaker.utils.zrr.callbacks.SoulBinderCallbacks;
import bada774.endertweaker.utils.zrr.callbacks.TankCallbacks;
import bada774.endertweaker.utils.zrr.callbacks.VatCallbacks;
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

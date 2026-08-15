package dose.killeffect.client;

import net.fabricmc.api.ClientModInitializer;

public class KilleffectClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KillEffect.register();
    }
}

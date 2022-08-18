package isaac.mc.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MCClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("Initialize!");
    }
}

package isaac.mc.mixin;

import isaac.mc.acces.MyClassAccess;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KeyboardInput.class)
public class Save implements MyClassAccess {
    @Override
    public void access() {
        System.out.println("Accessed!");
    }
}

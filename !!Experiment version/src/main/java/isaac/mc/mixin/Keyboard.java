package isaac.mc.mixin;

import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class Keyboard {
    public void print() {
        System.out.println("In Another class!");

    }

    @Mixin(KeyboardInput.class)
    public static class PositionGet {
        //private boolean pressingForward;
        //public boolean pressingForward;

        @Inject(method = "tick", at = @At("TAIL"))
        public void forward(boolean bl, CallbackInfo ci) {
            //this.pressingForward = true;
            KeyboardInput ki = (KeyboardInput)(Object) this;
            System.out.println(ki.pressingForward);
            ki.pressingForward = true;
            //ki.movementSideways += 0.4f;
            //System.out.println(ki.pressingForward);

        }

        public void print(){
            System.out.println("In Another class!");



    }}
}

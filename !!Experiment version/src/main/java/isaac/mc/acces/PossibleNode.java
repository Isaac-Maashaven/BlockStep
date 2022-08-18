package isaac.mc.acces;

import net.minecraft.util.math.BlockPos;

public class PossibleNode {

    public BlockPos pos;
    public boolean jump1 = false;

    public double jumpForce = 0.0D;

    public PossibleNode(BlockPos posIn, boolean jumpIn, double jumpForceIn) {
        this.pos = posIn;
        this.jump1 = jumpIn;
        this.jumpForce = jumpForceIn;
    }
}

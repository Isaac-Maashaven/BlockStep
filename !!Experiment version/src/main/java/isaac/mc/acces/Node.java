package isaac.mc.acces;

import net.minecraft.util.math.BlockPos;

public class Node {
    public BlockPos pos;
    public double score = 0;
    public double distanceFromStart = 0;
    public Node prev;

    public boolean jump1 = false;
    public double jumpForce = 0.0D;
    public double GetScore(){

        return this.score;
    }

}

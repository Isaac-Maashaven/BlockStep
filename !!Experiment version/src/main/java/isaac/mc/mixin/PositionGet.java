package isaac.mc.mixin;


import isaac.mc.acces.Node;
import isaac.mc.acces.PossibleNode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.io.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Scanner; // Import the Scanner class to read text files

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.time.DateUtils.round;

@Mixin(PlayerEntity.class)
public abstract class PositionGet {

    @Shadow public abstract @Nullable ItemEntity dropItem(ItemStack stack, boolean retainOwnership);

    @Shadow public abstract @Nullable ItemEntity dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership);

    //@Shadow public abstract Optional<Vec3d> findRespawnPosition(ServerWorld world, BlockPos pos, boolean bl, boolean bl2);
    //MyClassAccess m = new Save();
    private int runNR = 1;
    private static boolean go = false;
    private static int jumps  = 0;
    private static boolean jumped = false;
    private static boolean landed = false;

    private static int a = 1;
    private static BlockPos finalGoal = null;
    private static List<Node> subGoalList= new LinkedList<Node>();//can be more optimal by making it list of blockpos
    private static BlockPos subGoal = null;

    private static boolean inAir = false;

    private static boolean NextTest = true;

    private static long StartTestTime = 0;
    private static BlockPos TestGoal = null;

    private static boolean StartTest = true;

    private static long StartCalculationTime = 0;

    private static long Timecalculating = 0;


    private boolean goSlow = false;
    private long goSlowBegin = 0;

    private double StuckX = 0;
    private double StuckZ = 0;
    private long TimeStuck = 0;

    private long stopjumptime = 0;

    private double RunStartX = 0;

    private double RunStartZ = 0;

    private boolean teleport = false;

    private static final DecimalFormat df = new DecimalFormat("0.00");

    public void DoOneRun() throws IOException {
        teleport = true;
        //Print Current position when a block is dropped
        PlayerEntity player = ( PlayerEntity) ( Object ) this;
        World worldIn = player.world;

        File myObj2 = new File("goto_data.txt");
        myObj2.createNewFile();
        String data = "10 10 10";
        try {
            File myObj = new File("goto_data.txt");
            Scanner myReader2 = new Scanner(myObj);
            int counter = 0;
            while (myReader2.hasNextLine()) {
                counter++;
                data = myReader2.nextLine();

                if(counter > runNR){
                    break;
                }

            }
            runNR++;
            System.out.println(data);
            myReader2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String coords[] = data.split(" ");
        //int nr = Integer.parseInt(coords[0]);
        int beginX = Integer.parseInt(coords[1]);
        int beginZ =Integer.parseInt(coords[2]);
        int endX = Integer.parseInt(coords[3]);
        int endZ = Integer.parseInt(coords[4]);





        player.teleport(beginX, 120, beginZ);
        //TimeUnit.SECONDS.sleep(15);


        Vec3d post = player.getPos();


        RunStartZ = post.getZ();
        RunStartX = post.getX();

        System.out.println("I am here:");
        System.out.println(post);




        StartTestTime = System.currentTimeMillis();
        System.out.println("We Start Now!!");
        System.out.println(StartTestTime);
        BlockPos goal = new BlockPos(endX, 200, endZ);
        TestGoal = goal;



        //Keyboard ki = new Keyboard();
        //ki.print();

        //((MyClassAccess)m).access();// Will print "Accessed!"

        //Make bot go!
        this.go = true;

        NextTest = false;
        StartTest = true;
    }




    //dropping item  is trigger for now
    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("TAIL"))
    public void dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) throws IOException{
        DoOneRun();
    }

    public Node getmin(List<Node> open){
        Node minByScore = new Node();
        if(!open.isEmpty()) {
            minByScore = open
                    .stream()
                    .min(Comparator.comparing(Node::GetScore))
                    .orElseThrow(NoSuchElementException::new);
        }
        else{
            minByScore = new Node();
            minByScore.score = Double.MAX_VALUE;
        }
        return minByScore;
    }

    public double dist(PossibleNode from,Node to) {
        double distance = Math.pow(from.pos.getX()-to.pos.getX(),2)+ Math.pow(from.pos.getZ()-to.pos.getZ(),2);
        distance = Math.sqrt(distance);
        if(from.jump1){
            distance = distance/1.3 * 0.39D/from.jumpForce;
        }
        return distance;
    }



    public List<PossibleNode> getList(Node minByScore){
        List<PossibleNode> direction= new ArrayList<>();

        //inner ring
        BlockPos forward = new BlockPos(minByScore.pos.getX() + 1,minByScore.pos.getY(),minByScore.pos.getZ());
        direction.add(new PossibleNode(forward, false,0.3D));
        BlockPos backward = new BlockPos(minByScore.pos.getX() - 1,minByScore.pos.getY(),minByScore.pos.getZ());
        direction.add(new PossibleNode(backward, false,0.3D));

        BlockPos left = new BlockPos(minByScore.pos.getX(),minByScore.pos.getY(),minByScore.pos.getZ() - 1);
        direction.add(new PossibleNode(left, false,0.3D));
        BlockPos right = new BlockPos(minByScore.pos.getX(),minByScore.pos.getY(),minByScore.pos.getZ() + 1);
        direction.add( new PossibleNode(right, false,0.3D));

        BlockPos f = new BlockPos(minByScore.pos.getX() + 1,minByScore.pos.getY(),minByScore.pos.getZ()-1);
        direction.add(new PossibleNode(f, false,0.3D));
        f = new BlockPos(minByScore.pos.getX() + 1,minByScore.pos.getY(),minByScore.pos.getZ()+1);
        direction.add(new PossibleNode(f, false,0.3D));

        f = new BlockPos(minByScore.pos.getX() - 1,minByScore.pos.getY(),minByScore.pos.getZ()-1);
        direction.add(new PossibleNode(f, false,0.3D));
        f = new BlockPos(minByScore.pos.getX() - 1,minByScore.pos.getY(),minByScore.pos.getZ()+1);
        direction.add(new PossibleNode(f, false,0.3D));

        //outer ring
        BlockPos forward2 = new BlockPos(minByScore.pos.getX() + 5,minByScore.pos.getY(),minByScore.pos.getZ());
        direction.add(new PossibleNode(forward2, true,0.39D));

        BlockPos forward3 = new BlockPos(minByScore.pos.getX() + 4,minByScore.pos.getY(),minByScore.pos.getZ() + 1);
        direction.add(new PossibleNode(forward3, true,0.35D));
        BlockPos forward4 = new BlockPos(minByScore.pos.getX() + 4,minByScore.pos.getY(),minByScore.pos.getZ() - 1);
        direction.add(new PossibleNode(forward4, true,0.35D));

        f = new BlockPos(minByScore.pos.getX() + 4,minByScore.pos.getY(),minByScore.pos.getZ() - 2);
        direction.add(new PossibleNode(f, true,0.35D));
        f = new BlockPos(minByScore.pos.getX() + 4,minByScore.pos.getY(),minByScore.pos.getZ() + 2);
        direction.add(new PossibleNode(f, true,0.35D));

        f = new BlockPos(minByScore.pos.getX() + 4,minByScore.pos.getY(),minByScore.pos.getZ() - 3);
        direction.add(new PossibleNode(f, true,0.39D));
        f = new BlockPos(minByScore.pos.getX() + 4,minByScore.pos.getY(),minByScore.pos.getZ() + 3);
        direction.add(new PossibleNode(f, true,0.39D));


        f = new BlockPos(minByScore.pos.getX() + 3,minByScore.pos.getY(),minByScore.pos.getZ() - 4);
        direction.add(new PossibleNode(f, true,0.39D));
        f = new BlockPos(minByScore.pos.getX() + 3,minByScore.pos.getY(),minByScore.pos.getZ() + 4);
        direction.add(new PossibleNode(f, true,0.39D));

        f = new BlockPos(minByScore.pos.getX() + 2,minByScore.pos.getY(),minByScore.pos.getZ() + 4);
        direction.add(new PossibleNode(f, true,0.35D));
        f = new BlockPos(minByScore.pos.getX() + 2,minByScore.pos.getY(),minByScore.pos.getZ() - 4);
        direction.add(new PossibleNode(f, true,0.35D));


        f = new BlockPos(minByScore.pos.getX() + 1,minByScore.pos.getY(),minByScore.pos.getZ() + 4);
        direction.add(new PossibleNode(f, true,0.35D));
        f = new BlockPos(minByScore.pos.getX() + 1,minByScore.pos.getY(),minByScore.pos.getZ() - 4);
        direction.add(new PossibleNode(f, true,0.35D));
        //outer ring middle
        f = new BlockPos(minByScore.pos.getX() + 0,minByScore.pos.getY(),minByScore.pos.getZ() + 5);
        direction.add(new PossibleNode(f, true,0.39D));
        f = new BlockPos(minByScore.pos.getX() + 0,minByScore.pos.getY(),minByScore.pos.getZ() - 5);
        direction.add(new PossibleNode(f, true,0.39D));


        //outer ring bottom
        f = new BlockPos(minByScore.pos.getX() - 5,minByScore.pos.getY(),minByScore.pos.getZ());
        direction.add(new PossibleNode(f, true,0.39D));

        f = new BlockPos(minByScore.pos.getX() - 4,minByScore.pos.getY(),minByScore.pos.getZ() + 1);
        direction.add(new PossibleNode(f, true,0.35D));
        f = new BlockPos(minByScore.pos.getX() - 4,minByScore.pos.getY(),minByScore.pos.getZ() - 1);
        direction.add(new PossibleNode(f, true,0.35D));

        f = new BlockPos(minByScore.pos.getX() - 4,minByScore.pos.getY(),minByScore.pos.getZ() - 2);
        direction.add(new PossibleNode(f, true,0.35D));
        f = new BlockPos(minByScore.pos.getX() - 4,minByScore.pos.getY(),minByScore.pos.getZ() + 2);
        direction.add(new PossibleNode(f, true,0.35D));

        f = new BlockPos(minByScore.pos.getX() - 4,minByScore.pos.getY(),minByScore.pos.getZ() - 3);
        direction.add(new PossibleNode(f, true,0.39D));
        f = new BlockPos(minByScore.pos.getX() - 4,minByScore.pos.getY(),minByScore.pos.getZ() + 3);
        direction.add(new PossibleNode(f, true,0.39D));


        f = new BlockPos(minByScore.pos.getX() - 3,minByScore.pos.getY(),minByScore.pos.getZ() - 4);
        direction.add(new PossibleNode(f, true,0.39D));
        f = new BlockPos(minByScore.pos.getX() - 3,minByScore.pos.getY(),minByScore.pos.getZ() + 4);
        direction.add(new PossibleNode(f, true,0.39D));

        f = new BlockPos(minByScore.pos.getX() - 2,minByScore.pos.getY(),minByScore.pos.getZ() + 4);
        direction.add(new PossibleNode(f, true,0.35D));
        f = new BlockPos(minByScore.pos.getX() - 2,minByScore.pos.getY(),minByScore.pos.getZ() - 4);
        direction.add(new PossibleNode(f, true,0.35D));

        f = new BlockPos(minByScore.pos.getX() - 1,minByScore.pos.getY(),minByScore.pos.getZ() + 4);
        direction.add(new PossibleNode(f, true,0.35D));
        f = new BlockPos(minByScore.pos.getX() - 1,minByScore.pos.getY(),minByScore.pos.getZ() - 4);
        direction.add(new PossibleNode(f, true,0.35D));


        //3rd ring
        f = new BlockPos(minByScore.pos.getX() + 4,minByScore.pos.getY(),minByScore.pos.getZ());
        direction.add(new PossibleNode(f, true,0.32D));

        f = new BlockPos(minByScore.pos.getX() + 3,minByScore.pos.getY(),minByScore.pos.getZ()+1);
        direction.add(new PossibleNode(f, true,0.25D));
        f = new BlockPos(minByScore.pos.getX() + 3,minByScore.pos.getY(),minByScore.pos.getZ()-1);
        direction.add(new PossibleNode(f, true,0.25D));

        f = new BlockPos(minByScore.pos.getX() + 3,minByScore.pos.getY(),minByScore.pos.getZ()+2);
        direction.add(new PossibleNode(f, true,0.25D));
        f = new BlockPos(minByScore.pos.getX() + 3,minByScore.pos.getY(),minByScore.pos.getZ()-2);
        direction.add(new PossibleNode(f, true,0.25D));

        //maybe higher value
        f = new BlockPos(minByScore.pos.getX() + 3,minByScore.pos.getY(),minByScore.pos.getZ()+3);
        direction.add(new PossibleNode(f, true,0.30D));
        f = new BlockPos(minByScore.pos.getX() + 3,minByScore.pos.getY(),minByScore.pos.getZ()-3);
        direction.add(new PossibleNode(f, true,0.30D));

        f = new BlockPos(minByScore.pos.getX() + 2,minByScore.pos.getY(),minByScore.pos.getZ()+3);
        direction.add(new PossibleNode(f, true,0.25D));
        f = new BlockPos(minByScore.pos.getX() + 2,minByScore.pos.getY(),minByScore.pos.getZ()-3);
        direction.add(new PossibleNode(f, true,0.25D));

        f = new BlockPos(minByScore.pos.getX() + 1,minByScore.pos.getY(),minByScore.pos.getZ()+3);
        direction.add(new PossibleNode(f, true,0.25D));
        f = new BlockPos(minByScore.pos.getX() + 1,minByScore.pos.getY(),minByScore.pos.getZ()-3);
        direction.add(new PossibleNode(f, true,0.25D));

        f = new BlockPos(minByScore.pos.getX(),minByScore.pos.getY(),minByScore.pos.getZ()+4);
        direction.add(new PossibleNode(f, true,0.32D));
        f = new BlockPos(minByScore.pos.getX() ,minByScore.pos.getY(),minByScore.pos.getZ()-4);
        direction.add(new PossibleNode(f, true,0.32D));


        //2nd ring
        f = new BlockPos(minByScore.pos.getX() + 3,minByScore.pos.getY(),minByScore.pos.getZ());
        direction.add(new PossibleNode(f, true,0.18D));

        f = new BlockPos(minByScore.pos.getX() + 2,minByScore.pos.getY(),minByScore.pos.getZ()+1);
        direction.add(new PossibleNode(f, true,0.15D));
        f = new BlockPos(minByScore.pos.getX() + 2,minByScore.pos.getY(),minByScore.pos.getZ()-1);
        direction.add(new PossibleNode(f, true,0.15D));

        f = new BlockPos(minByScore.pos.getX() + 2,minByScore.pos.getY(),minByScore.pos.getZ()+2);
        direction.add(new PossibleNode(f, true,0.18D));
        f = new BlockPos(minByScore.pos.getX() + 2,minByScore.pos.getY(),minByScore.pos.getZ()-2);
        direction.add(new PossibleNode(f, true,0.18D));

        f = new BlockPos(minByScore.pos.getX() + 1,minByScore.pos.getY(),minByScore.pos.getZ()+2);
        direction.add(new PossibleNode(f, true,0.15D));
        f = new BlockPos(minByScore.pos.getX() + 1,minByScore.pos.getY(),minByScore.pos.getZ()-2);
        direction.add(new PossibleNode(f, true,0.15D));

        f = new BlockPos(minByScore.pos.getX(),minByScore.pos.getY(),minByScore.pos.getZ()+3);
        direction.add(new PossibleNode(f, true,0.18D));
        f = new BlockPos(minByScore.pos.getX(),minByScore.pos.getY(),minByScore.pos.getZ()-3);
        direction.add(new PossibleNode(f, true,0.18D));


        //extra forward
        f = new BlockPos(minByScore.pos.getX()+2,minByScore.pos.getY(),minByScore.pos.getZ());
        direction.add(new PossibleNode(f, true,0.15D));
        f = new BlockPos(minByScore.pos.getX() ,minByScore.pos.getY(),minByScore.pos.getZ()+2);
        direction.add(new PossibleNode(f, true,0.15D));
        f = new BlockPos(minByScore.pos.getX(),minByScore.pos.getY(),minByScore.pos.getZ()-2);
        direction.add(new PossibleNode(f, true,0.15D));
        f = new BlockPos(minByScore.pos.getX()-2,minByScore.pos.getY(),minByScore.pos.getZ());
        direction.add(new PossibleNode(f, true,0.15D));


        ///////////////////////////////////
        ////Second layer
        /////////////////////////////////
        //outer ring
        f = new BlockPos(minByScore.pos.getX() + 1,minByScore.pos.getY()+1,minByScore.pos.getZ());
        direction.add(new PossibleNode(f, true,0.15D));
        f = new BlockPos(minByScore.pos.getX() - 1,minByScore.pos.getY()+1,minByScore.pos.getZ());
        direction.add(new PossibleNode(f, true,0.15D));
        f = new BlockPos(minByScore.pos.getX(),minByScore.pos.getY()+1,minByScore.pos.getZ()+1);
        direction.add(new PossibleNode(f, true,0.15D));
        f = new BlockPos(minByScore.pos.getX() ,minByScore.pos.getY()+1,minByScore.pos.getZ()-1);
        direction.add(new PossibleNode(f, true,0.15D));


        ///////////////////////////////////
        ////Layer -1
        /////////////////////////////////
        f = new BlockPos(minByScore.pos.getX() + 1,minByScore.pos.getY()-1,minByScore.pos.getZ());
        direction.add(new PossibleNode(f, false,0.1D));
        f = new BlockPos(minByScore.pos.getX() - 1,minByScore.pos.getY()-1,minByScore.pos.getZ());
        direction.add(new PossibleNode(f, false,0.1D));
        f = new BlockPos(minByScore.pos.getX(),minByScore.pos.getY()-1,minByScore.pos.getZ()+1);
        direction.add(new PossibleNode(f, false,0.1D));
        f = new BlockPos(minByScore.pos.getX(),minByScore.pos.getY()-1,minByScore.pos.getZ()-1);
        direction.add(new PossibleNode(f, false,0.1D));

        return direction;
    }



    public boolean CanIGetThere(BlockPos from, BlockPos to, boolean jump, World worldIn){
        int startiterX;
        if(from.getX() > to.getX()){
            startiterX = to.getX();
        }else{
            startiterX = from.getX();
        }
        int DistanceX = Math.abs(from.getX() - to.getX());

        int startiterZ;
        if(from.getZ() > to.getZ()){
            startiterZ = to.getZ();
        }else{
            startiterZ = from.getZ();
        }
        int DistanceZ = Math.abs(from.getZ() - to.getZ());


        if(from.getY() == to.getY()){
            //if(jump){

            //}else{
                for(int blX = startiterX + 1; blX < startiterX+DistanceX; blX++) {
                    for(int blZ = startiterZ + 1; blZ < startiterZ+DistanceZ; blZ++) {
                        //knee lvl
                        BlockPos posfeet2 = new BlockPos(blX, to.getY(), blZ);//cahnge to z
                        BlockState bs2 = worldIn.getBlockState(new BlockPos(blX, to.getY() + 1, blZ));//cahnge to z
                        if (bs2.isSolidBlock(worldIn, posfeet2)) {
                            return false;

                        }
                        //eye lvl
                        posfeet2 = new BlockPos(blX, to.getY() + 1, blZ);//cahnge to z
                        bs2 = worldIn.getBlockState(new BlockPos(blX, to.getY() + 2, blZ));//cahnge to z
                        if (bs2.isSolidBlock(worldIn, posfeet2)) {
                            return false;
                        }

                    }
                //}
            }
        }




        return true;
    }

    //Calculates path toward goal position
    public boolean gotoPos(BlockPos goal) throws IOException {
        //clean
        StartCalculationTime = System.currentTimeMillis();
        long start = System.currentTimeMillis();

        subGoalList = new LinkedList<Node>();
        finalGoal = null;
        subGoal = null;
        boolean CanIGetThere = false;

        //A* algo:
        //https://www.geeksforgeeks.org/a-search-algorithm/
        List<Node> open = new ArrayList<>();
        List<Node> closed = new ArrayList<>();

        PlayerEntity player = (PlayerEntity) (Object) this;
        World worldIn = player.world;

        //get player current block
        BlockPos pos = player.getBlockPos();

        //block on which the player is walking
        BlockPos posfeet = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());

        //block above block
        BlockPos posfeet2 = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        BlockPos posfeet3 = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
        //begin state
        Node n = new Node();
        n.pos = posfeet;
        open.add(n);
        boolean failed = false;
        boolean stop = false;
        while (!open.isEmpty() && !stop) {
            //get min nodeZ
            Node minByScore = getmin(open);

            BlockPos currentBlock = minByScore.pos;
            //pop min node
            open.remove(minByScore);


            List<PossibleNode> direction = new ArrayList<>();
            direction = getList(minByScore);

            for (int i = 0; i < direction.size(); i++) {
                BlockPos succesor = direction.get(i).pos;
                boolean jump = direction.get(i).jump1;
                long finish = System.currentTimeMillis();
                long timeElapsed = finish - start;
                if (succesor.getX() == goal.getX() && succesor.getZ() == goal.getZ()) {
                    stop = true;
                    timeElapsed = finish - start;
                    Timecalculating = timeElapsed;
                    System.out.println("time calculating:");
                    System.out.println(timeElapsed);
                } else if (timeElapsed > 60000) {
                    System.out.println("Time out!!!, calculation to long");
                    stop = true;
                    failed = true;
                } else {
                    BlockState bs = worldIn.getBlockState(succesor);
                    //block above
                    BlockState bs2 = worldIn.getBlockState(new BlockPos(succesor.getX(), succesor.getY() + 1, succesor.getZ()));
                    BlockState bs3 = worldIn.getBlockState(new BlockPos(succesor.getX(), succesor.getY() + 2, succesor.getZ()));
                    bs.isSolidBlock(worldIn, posfeet);


                    if (bs.isSolidBlock(worldIn, posfeet) && !bs2.isSolidBlock(worldIn, posfeet2) && !bs3.isSolidBlock(worldIn, posfeet3)) {
                        if (CanIGetThere(currentBlock, succesor, jump, worldIn)) {


                            //if(worldIn.getBlockState(posfeet).isSolidBlock(worldIn, succesor))
                            //Func
                            double g = minByScore.distanceFromStart + dist(direction.get(i), minByScore);//1 is distance between 2 blocks

                            double hsquared = Math.pow(succesor.getX() - goal.getX(), 2) + Math.pow(succesor.getY() - goal.getY(), 2) + Math.pow(succesor.getZ() - goal.getZ(), 2);

                            double h = Math.sqrt(hsquared);

                            double f = g + h;

                            //SAME POSITION copy from beneath remove closed by open
                            boolean skipt = false;
                            for (int k = 0; k < open.size(); k++) {
                                if (succesor.equals(open.get(k).pos)) {
                                    if (f >= open.get(k).score) {
                                        skipt = true;
                                    }
                                }
                            }
                            if (!skipt) {
                                boolean skip = false;
                                for (int j = 0; j < closed.size(); j++) {
                                    if (succesor.equals(closed.get(j).pos)) {
                                        if (f >= closed.get(j).score) {
                                            skip = true;
                                        }

                                    }
                                }
                                if (!skip) {
                                    Node m = new Node();
                                    m.pos = succesor;
                                    m.score = f;
                                    m.distanceFromStart = g;
                                    m.prev = minByScore;
                                    m.jump1 = direction.get(i).jump1;
                                    m.jumpForce = direction.get(i).jumpForce;
                                    open.add(m);
                                }

                            }
                        }
                    }
                }
            }
            closed.add(minByScore);
        }

        Node currentNode = closed.get(closed.size() - 1);
        while (currentNode != null) {
            subGoalList.add(currentNode);
            currentNode = currentNode.prev;
        }

        Collections.reverse(subGoalList);
        finalGoal = goal;
        //print path

        System.out.println("!!!!!!!!!!!!!GOALLIST!!!!!!!!!!!");
        for (int p = 0; p < subGoalList.size(); p++) {
            System.out.println(subGoalList.get(p).pos);
        }


        if (failed) {
            DoOneRun();
            return false;
        }
    return true;
    }

    public boolean closeEnough(BlockPos a, Vec3d b){

        double dist = Math.pow(a.getX()+0.5D- b.getX(),2)+ Math.pow(a.getZ()+0.5D- b.getZ(),2);

        if(dist < 0.05){
            return true;
        }
        return false;
    }
    public boolean closeEnoughtoGoal(BlockPos a, Vec3d b){
        double dist = Math.abs(a.getX()-b.getX())+Math.abs(a.getZ()-b.getZ());

        if(dist < 1.8){
            return true;
        }
        return false;
    }
    public boolean OnEdge(Vec3d pos){

        if(Math.abs(pos.getX()% 1.0) > 0.65){
            return true;
        }
        if(Math.abs(pos.getZ()% 1.0) > 0.65){
            return true;
        }
        if(Math.abs(pos.getX()% 1.0) < 0.35){
            return true;
        }
        if(Math.abs(pos.getZ()% 1.0) < 0.35){
            return true;
        }

        return false;
    }
    public void MoveToGoal() throws IOException {
        if(finalGoal != null) {
            if (!go) {
                return;
            }
            BlockPos previousGoal = null;
            double force = 0.3D;
            PlayerEntity player = (PlayerEntity) (Object) this;
            World worldIn = player.world;
            Vec3d pos = player.getPos();
            BlockPos currentPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());

            //if at end finish
            if (closeEnough(finalGoal, pos)) {
                go = false;
                DoOneRun();
                return;
            }

            //select current subgoal on first iteration
            if (subGoal == null) {

                jumped = false;
                if (subGoalList.isEmpty()) {
                    subGoal = finalGoal;
                } else {
                    subGoal = subGoalList.get(0).pos;
                }
                System.out.println("CurrentGoal:");
                System.out.println(subGoal);

            }


            //if at target switch target
            if (closeEnough(subGoal, pos)) {

                System.out.println("CurrentGoal:");
                System.out.println(subGoal);

                jumped = false;
                if (!subGoalList.isEmpty()) {
                    subGoalList.remove(0);
                }
                if (!subGoalList.isEmpty()) {
                    previousGoal = subGoal;
                    subGoal = subGoalList.get(0).pos;
                } else {
                    previousGoal = subGoal;
                    subGoal = finalGoal;
                }


            }
            if(!subGoalList.isEmpty()) {
                if (subGoalList.get(0).jumpForce == 0.1D) {
                    goSlow = true;
                    goSlowBegin = System.currentTimeMillis();
                }
            }

            if(System.currentTimeMillis() - goSlowBegin > 100 && System.currentTimeMillis() - goSlowBegin < 200){
                goSlow = true;
            }else{
                goSlow = false;
            }


            //move to subgoal

            Vec3d vel = player.getVelocity();

            if (!player.isOnGround() && inAir && !subGoalList.isEmpty()) {

                force = subGoalList.get(0).jumpForce;


            }else{
                force = 0.3D;
                inAir = false;
            }
            if(!subGoalList.isEmpty()){
                if(subGoalList.get(0).jumpForce == 0.05D){
                    force = 0.05D;
                }

            }
            if(goSlow){
                force = 0.01D;
            }



            double distX = Double.valueOf(subGoal.getX()+0.5D - pos.getX());
            double distZ = Double.valueOf(subGoal.getZ()+0.5D - pos.getZ());
            double Tan = Math.atan2(distX , distZ);


            MovementType movementType = MovementType.SELF;
            double vely = vel.y;
            if(pos.getY()-subGoal.getY()>0.0001){
                if(landed) {
                    jumped = true;
                    landed = false;
                }
                vely *= 0.5;
            }else{

                landed = true;
            }


            BlockState bs2 = worldIn.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ()));//cahnge to z
            Block b = bs2.getBlock();
            if(b.getDefaultState().getMaterial() == Material.WATER){
                vely = 0.15D;
                force = 0.15D;
            }


            if(Math.abs(StuckX-pos.getX())< 0.1 && Math.abs(StuckZ-pos.getZ())< 0.1){

            }else{
                TimeStuck = System.currentTimeMillis();
                StuckX = pos.getX();
                StuckZ = pos.getZ();
            }



            player.move(movementType, new Vec3d(vel.x + force*Math.sin(Tan), vely, vel.z+ force*Math.cos(Tan)));

            //Jump
            if(!subGoalList.isEmpty()) {
                if (subGoalList.get(0).jump1 && player.isOnGround() && OnEdge(pos) && !jumped) {
                    System.out.println("JUMP");
                    player.jump();
                    inAir = true;
                    jumps = 0;
                }
            }
            if(System.currentTimeMillis()-TimeStuck > 200&& player.isOnGround()){
                player.jump();
                System.out.println("STUCK JUMP");
                TimeStuck = System.currentTimeMillis();
                stopjumptime = System.currentTimeMillis();
                inAir = true;
            }







        }

    }


    public void writeResults(int nr, long time_start, long time_end, long Timecalculatingprint, double endx, double endz) throws IOException {
        File myObj = new File("results.txt");
        myObj.createNewFile();

        FileWriter fw = new FileWriter("results.txt", true);
        BufferedWriter myWriter = new BufferedWriter(fw);
        myWriter.write(String.valueOf(nr)+";"+ RunStartX+";"+ RunStartZ+";"+
                df.format(endx)+";"+ df.format(endz)+ String.valueOf(time_start)+ ";"+
                String.valueOf(time_end) + ";"+ String.valueOf(time_end-time_start-Timecalculatingprint)+";"+
                String.valueOf(Timecalculatingprint));
        myWriter.newLine();
        myWriter.close();
    }



    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) throws InterruptedException, IOException {
        PlayerEntity player = (PlayerEntity) (Object) this;
        Vec3d pos = player.getPos();
        Vec3d vel = player.getVelocity();
        if(teleport) {
            teleport = false;
            if (Math.abs(pos.getX() - RunStartX) > 3 || Math.abs(pos.getZ() - RunStartZ) > 3) {
                player.teleport(RunStartX, 120, RunStartZ);
                pos = player.getPos();
            }
        }
        /*Vec3d m = NextMove(pos);
        MovementType movementType = MovementType.SELF;
        player.move(movementType, m);
        */
        long current = System.currentTimeMillis();
        if( StartTestTime != 0) {
            if (current - StartTestTime > 8000.0) {
                if (StartTest) {
                    StartTest = false;
                    gotoPos(TestGoal);
                    if(Math.abs(pos.getX()-RunStartX)>3 || Math.abs(pos.getZ()-RunStartZ)>3){
                        player.teleport(RunStartX, 120, RunStartZ);
                        pos = player.getPos();
                    }

                }
                if (finalGoal != null) {
                    //todo to goal
                    if (closeEnough(finalGoal, pos)) {
                        long end = System.currentTimeMillis();
                        writeResults(runNR, StartCalculationTime,end,Timecalculating, pos.getX(), pos.getZ());
                        System.out.println("We are here!!!");
                        System.out.println(end);

                        StartTestTime = 0;
                        //TimeUnit.SECONDS.sleep(5);
                        NextTest = true;
                    }
                }
                MoveToGoal();
            }
        }
    }


    /*
    public Vec3d NextMove(Vec3d pos){
        PlayerEntity player = (PlayerEntity) (Object) this;
        Vec3d vel = player.getVelocity();
        double force = 0.25;
        if(this.go) {

            this.a += 1;
            if (this.a % 400 < 100) {
                return new Vec3d(vel.x+force, vel.y, vel.z);
            } else if (this.a % 400 < 200) {
                return new Vec3d(vel.x, vel.y, vel.z+force);
            } else if (this.a % 400 < 300) {
                return new Vec3d(vel.x-force, vel.y, vel.z);
            } else if (this.a%400 ==301){
                player.jump();
                Vec3d vel2 = player.getVelocity();
                return new Vec3d(vel2.x, vel2.y, vel2.z);
            }
            else{
                return new Vec3d(vel.x, vel.y, vel.z-force);
            }
        }else{
            return new Vec3d(vel.x, vel.y, vel.z);
        }
    }*/
}
//USEFULL Later:
//getBlockBreakingSpeed(BlockState block)
//p.getworld()
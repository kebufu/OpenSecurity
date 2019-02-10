package pcl.opensecurity.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import pcl.opensecurity.common.tileentity.TileEntityRolldoor;
import pcl.opensecurity.common.tileentity.TileEntityRolldoorController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RolldoorHelper {
    public static HashMap<BlockPos, TileEntityRolldoor> getDoors(TileEntityRolldoorController controller) {
        HashMap<BlockPos, TileEntityRolldoor> doors = new HashMap<>();

        TileEntityRolldoor firstElement = getAdjacentRolldoor(controller);

        if(firstElement == null)
            return doors;

        doors.put(firstElement.getPos(), firstElement);

        ArrayList<EnumFacing> faces = getAdjacentRolldoors(firstElement.getWorld(), firstElement.getPos());

        if(faces.size() == 0) // no doors adjacent to the first rolldoor block
            return doors;

        Vec3i searchVector = faces.get(0).getDirectionVec(); // do further searchs only on the same axis

        BlockPos searchPos = firstElement.getPos();
        for(int i=0; doors.size() < TileEntityRolldoor.MAX_LENGTH; i++){
            searchPos = searchPos.add(searchVector);
            TileEntity tile = firstElement.getWorld().getTileEntity(searchPos);
            if(tile instanceof TileEntityRolldoor)
                doors.put(searchPos, (TileEntityRolldoor) tile);
            else
                break;
        }

        searchPos = firstElement.getPos();
        for(int i=0; doors.size() < TileEntityRolldoor.MAX_LENGTH; i--){
            searchPos = searchPos.subtract(searchVector);
            TileEntity tile = firstElement.getWorld().getTileEntity(searchPos);
            if(tile instanceof TileEntityRolldoor)
                doors.put(searchPos, (TileEntityRolldoor) tile);
            else
                break;
        }


        return doors;
    }

    public static ArrayList<EnumFacing> getAdjacentRolldoors(World world, BlockPos pos) {
        ArrayList<EnumFacing> doors = new ArrayList<>();

        for (Map.Entry<EnumFacing, TileEntity> entry : getAdjacentTileEntities(world, pos).entrySet())
            if (entry.getValue() instanceof TileEntityRolldoor)
                doors.add(entry.getKey());

        return doors;
    }

    public static TileEntityRolldoor getAdjacentRolldoor(TileEntity controller){
        for (Map.Entry<EnumFacing, TileEntity> entry : getAdjacentTileEntities(controller.getWorld(), controller.getPos()).entrySet()){
            if(entry.getValue() instanceof TileEntityRolldoor)
                return (TileEntityRolldoor) entry.getValue();
        }

        return null;
    }

    public static HashMap<EnumFacing, TileEntity> getAdjacentTileEntities(World world, BlockPos pos) {
        HashMap<EnumFacing, TileEntity> tiles = new HashMap<>();

        for (EnumFacing direction : EnumFacing.VALUES) {
            TileEntity tile = world.getTileEntity(pos.add(direction.getDirectionVec()));
            if (tile != null)
                tiles.put(direction, tile);
        }

        return tiles;
    }

    public static TileEntityRolldoorController getAdjacentController(World world, BlockPos pos){
        for (Map.Entry<EnumFacing, TileEntity> entry : getAdjacentTileEntities(world, pos).entrySet()){
            if(entry.getValue() instanceof TileEntityRolldoorController)
                return (TileEntityRolldoorController) entry.getValue();
        }

        return null;
    }

}



package github.kasuminova.mmce.common.event.machine;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.world.IBlockPos;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.SmartInterfaceData;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

@ZenRegister
@ZenClass("mods.modularmachinery.SmartInterfaceUpdateEvent")
public class SmartInterfaceUpdateEvent extends MachineEvent {
    private final BlockPos           interfacePos;
    private final SmartInterfaceData newData;

    public SmartInterfaceUpdateEvent(TileMultiblockMachineController controller, BlockPos interfacePos, SmartInterfaceData newData) {
        super(controller);
        this.interfacePos = interfacePos;
        this.newData = newData;
    }

    @ZenGetter("interfacePos")
    @Optional.Method(modid = "crafttweaker")
    public IBlockPos getInterfacePos() {
        return CraftTweakerMC.getIBlockPos(interfacePos);
    }

    public BlockPos getInterfaceBlockPos() {
        return interfacePos;
    }

    @ZenGetter("newData")
    public SmartInterfaceData getNewData() {
        return newData;
    }
}

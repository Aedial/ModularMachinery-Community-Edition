package github.kasuminova.mmce.common.helper;


import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.block.IBlockState;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;

import java.util.function.Predicate;

@ZenRegister
@ZenClass("mods.modularmachinery.IBlockStatePredicate")
@FunctionalInterface
public interface IBlockStatePredicate extends Predicate<IBlockState> {

    @Override
    @Optional.Method(modid = "crafttweaker")
    boolean test(IBlockState t);

}

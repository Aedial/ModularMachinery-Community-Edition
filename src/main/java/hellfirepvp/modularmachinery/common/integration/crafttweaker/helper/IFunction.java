package hellfirepvp.modularmachinery.common.integration.crafttweaker.helper;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

import java.util.function.Function;

@ZenRegister
@FunctionalInterface
@ZenClass("mods.modularmachinery.IFunction")
public interface IFunction<T, R> extends Function<T, R> {

    @Override
    R apply(T param);
}

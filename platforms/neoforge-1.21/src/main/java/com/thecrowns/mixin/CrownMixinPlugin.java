package com.thecrowns.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Records successful class transformations so optional injections cannot fail silently. */
public final class CrownMixinPlugin implements IMixinConfigPlugin {
    private static final Set<String> APPLIED = ConcurrentHashMap.newKeySet();

    public static boolean wasApplied(String simpleName) {
        return APPLIED.contains(simpleName);
    }

    public static Set<String> appliedMixins() {
        return Set.copyOf(APPLIED);
    }

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return true; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        int separator = mixinClassName.lastIndexOf('.');
        APPLIED.add(separator >= 0 ? mixinClassName.substring(separator + 1) : mixinClassName);
    }
}

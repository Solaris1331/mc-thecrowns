package com.thecrowns.network.legacy;

import java.util.function.Supplier;

/** Target descriptors retained only so existing Crown logic can keep its explicit send intent. */
public final class PacketDistributor {
    public static final TargetFactory PLAYER = new TargetFactory();
    public static final TargetFactory DIMENSION = new TargetFactory();

    private PacketDistributor() {}

    public static final class TargetFactory {
        public Target with(Supplier<?> supplier) {
            return new Target(supplier.get());
        }
    }

    public record Target(Object value) {}
}

package dose.killeffect.client.effects;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class ButterflyEffect {
    private static final DustParticleEffect PINK = new DustParticleEffect(0xFF8CCB, 0.80f);
    private static final DustParticleEffect SKY = new DustParticleEffect(0x8DDCFF, 0.80f);
    private static final DustParticleEffect LILAC = new DustParticleEffect(0xC69CFF, 0.80f);

    private ButterflyEffect() {}

    public static void spawn(ClientWorld world, Vec3d pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d center = new Vec3d(pos.x, pos.y + 1.15, pos.z);

        Vec3d camPos;
        try {
            camPos = mc != null && mc.gameRenderer != null && mc.gameRenderer.getCamera() != null
                    ? mc.gameRenderer.getCamera().getPos()
                    : center.add(0.0, 0.0, 1.0);
        } catch (Throwable t) {
            camPos = center.add(0.0, 0.0, 1.0);
        }

        Vec3d normal = camPos.subtract(center);
        if (normal.lengthSquared() < 1.0E-6) normal = new Vec3d(0.0, 0.0, 1.0);
        normal = normal.normalize();

        Vec3d right = new Vec3d(0.0, 1.0, 0.0).crossProduct(normal);
        if (right.lengthSquared() < 1.0E-6) right = new Vec3d(1.0, 0.0, 0.0);
        right = right.normalize();

        Vec3d up = normal.crossProduct(right);
        if (up.lengthSquared() < 1.0E-6) up = new Vec3d(0.0, 1.0, 0.0);
        up = up.normalize();

        for (int b = 0; b < 6; b++) {
            double orbit = Math.PI * 2.0 * b / 6.0 + 0.24;
            double lateral = Math.cos(orbit) * (0.62 + 0.16 * (b % 2));
            double vertical = 0.18 + b * 0.26 + 0.10 * Math.sin(orbit * 2.0);
            Vec3d c = center.add(right.multiply(lateral)).add(up.multiply(vertical));
            ParticleEffect wing = b % 3 == 0 ? PINK : (b % 3 == 1 ? SKY : LILAC);

            world.addParticle(ParticleTypes.END_ROD, c.x, c.y, c.z, 0.0, 0.018, 0.0);

            for (int side = -1; side <= 1; side += 2) {
                for (int i = 0; i < 10; i++) {
                    double t = i / 9.0;
                    double wingX = side * (0.10 + 0.24 * Math.sin(t * Math.PI));
                    double wingY = 0.19 * Math.cos(t * Math.PI) + 0.06 * Math.sin(t * Math.PI * 2.0);
                    Vec3d p = c.add(right.multiply(wingX)).add(up.multiply(wingY));
                    world.addParticle(wing, p.x, p.y, p.z,
                            side * 0.008, 0.025 + Math.random() * 0.018, 0.0);
                }
            }
        }

        for (int i = 0; i < 22; i++) {
            double a = Math.random() * Math.PI * 2.0;
            double r = 0.25 + Math.random() * 1.15;
            ParticleEffect p = i % 2 == 0 ? PINK : LILAC;
            world.addParticle(p,
                    center.x + Math.cos(a) * r,
                    center.y + Math.random() * 1.35,
                    center.z + Math.sin(a) * r,
                    -Math.sin(a) * 0.015,
                    0.02 + Math.random() * 0.035,
                    Math.cos(a) * 0.015);
        }

        play(world, center.x, center.y, center.z, "block.cherry_leaves.step", 0.85f, 1.45f);
        play(world, center.x, center.y, center.z, "block.amethyst_block.chime", 0.92f, 1.72f);
    }

    private static void play(ClientWorld world, double x, double y, double z, String id, float volume, float pitch) {
        SoundEvent sound = sound(id);
        if (sound != null) world.playSound(x, y, z, sound, SoundCategory.PLAYERS, volume, pitch, false);
    }

    private static SoundEvent sound(String id) {
        try {
            Identifier ident = Identifier.of("minecraft", id);
            if (Registries.SOUND_EVENT.containsId(ident)) {
                SoundEvent sound = Registries.SOUND_EVENT.get(ident);
                if (sound != null) return sound;
            }
        } catch (Throwable ignored) {}
        Object fallback = SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
        if (fallback instanceof SoundEvent sound) return sound;
        try {
            if (fallback instanceof net.minecraft.registry.entry.RegistryEntry<?> entry) {
                Object value = entry.value();
                if (value instanceof SoundEvent sound) return sound;
            }
        } catch (Throwable ignored) {}
        return null;
    }
}

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

public final class MoonlightEffect {
    private static final DustParticleEffect WHITE = new DustParticleEffect(0xFFFBE8, 0.95f);
    private static final DustParticleEffect BLUE = new DustParticleEffect(0xB9D9FF, 0.90f);

    private MoonlightEffect() {}

    public static void spawn(ClientWorld world, Vec3d pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d center = new Vec3d(pos.x, pos.y + 1.38, pos.z);

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

        Vec3d moonCenter = center.add(right.multiply(-0.18)).add(up.multiply(0.20));
        for (int i = 0; i <= 48; i++) {
            double a = -Math.PI * 0.66 + Math.PI * 1.32 * (i / 48.0);
            Vec3d p = moonCenter
                    .add(right.multiply(Math.cos(a) * 0.82))
                    .add(up.multiply(Math.sin(a) * 0.82));
            ParticleEffect dust = i % 3 == 0 ? BLUE : WHITE;
            world.addParticle(dust, p.x, p.y, p.z, 0.0, 0.012, 0.0);
        }

        Vec3d innerCenter = moonCenter.add(right.multiply(0.27));
        for (int i = 0; i <= 36; i++) {
            double a = -Math.PI * 0.62 + Math.PI * 1.24 * (i / 36.0);
            Vec3d p = innerCenter
                    .add(right.multiply(Math.cos(a) * 0.52))
                    .add(up.multiply(Math.sin(a) * 0.52));
            world.addParticle(BLUE, p.x, p.y, p.z, 0.0, 0.010, 0.0);
        }

        for (int i = 0; i < 28; i++) {
            double rx = (Math.random() - 0.5) * 2.0;
            double ry = 0.35 + Math.random() * 1.55;
            double rz = (Math.random() - 0.5) * 0.45;
            Vec3d p = center
                    .add(right.multiply(rx))
                    .add(up.multiply(ry))
                    .add(normal.multiply(rz));
            ParticleEffect dust = i % 4 == 0 ? WHITE : BLUE;
            world.addParticle(dust, p.x, p.y, p.z, 0.0, -0.018 - Math.random() * 0.025, 0.0);
            if (i % 7 == 0) {
                world.addParticle(ParticleTypes.END_ROD, p.x, p.y, p.z, 0.0, -0.012, 0.0);
            }
        }

        play(world, center.x, center.y, center.z, "block.amethyst_block.chime", 1.02f, 1.08f);
        play(world, center.x, center.y, center.z, "block.note_block.bell", 0.62f, 1.62f);
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

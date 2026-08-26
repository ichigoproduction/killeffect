package dose.killeffect.client.effects;

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

public final class LotusEffect {
    private static final DustParticleEffect PINK = new DustParticleEffect(0xFF9FCB, 0.95f);
    private static final DustParticleEffect WHITE = new DustParticleEffect(0xFFF3FA, 0.90f);
    private static final DustParticleEffect GOLD = new DustParticleEffect(0xFFE6A3, 0.80f);

    private LotusEffect() {}

    public static void spawn(ClientWorld world, Vec3d pos) {
        double x = pos.x;
        double y = pos.y + 0.08;
        double z = pos.z;

        for (int layer = 0; layer < 2; layer++) {
            int count = layer == 0 ? 112 : 88;
            double phase = layer == 0 ? 0.0 : Math.PI / 8.0;
            double scale = layer == 0 ? 1.00 : 0.68;

            for (int i = 0; i < count; i++) {
                double a = Math.PI * 2.0 * i / count + phase;
                double petal = Math.abs(Math.sin(4.0 * a));
                double r = scale * (0.18 + 0.86 * petal);
                double px = x + Math.cos(a) * r;
                double pz = z + Math.sin(a) * r;
                double py = y + 0.05 + 0.24 * petal + (layer == 1 ? 0.08 : 0.0);
                ParticleEffect dust = layer == 1 ? WHITE : (i % 5 == 0 ? GOLD : PINK);

                world.addParticle(dust, px, py, pz,
                        Math.cos(a) * 0.006,
                        0.014 + petal * 0.012,
                        Math.sin(a) * 0.006);
            }
        }

        for (int i = 0; i < 30; i++) {
            double a = Math.random() * Math.PI * 2.0;
            double r = Math.random() * 0.34;
            double py = y + 0.18 + Math.random() * 1.25;
            ParticleEffect dust = i % 3 == 0 ? GOLD : WHITE;

            world.addParticle(dust,
                    x + Math.cos(a) * r,
                    py,
                    z + Math.sin(a) * r,
                    (Math.random() - 0.5) * 0.012,
                    0.025 + Math.random() * 0.035,
                    (Math.random() - 0.5) * 0.012);

            if (i % 6 == 0) {
                world.addParticle(ParticleTypes.END_ROD, x, py, z, 0.0, 0.025, 0.0);
            }
        }

        play(world, x, y + 0.5, z, "block.cherry_leaves.break", 0.95f, 0.92f);
        play(world, x, y + 0.5, z, "block.amethyst_cluster.break", 1.08f, 1.32f);
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

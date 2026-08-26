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

public final class AuroraEffect {
    private static final DustParticleEffect CYAN = new DustParticleEffect(0x66FFF0, 0.95f);
    private static final DustParticleEffect BLUE = new DustParticleEffect(0x72A7FF, 0.95f);
    private static final DustParticleEffect VIOLET = new DustParticleEffect(0xC47CFF, 0.95f);

    private AuroraEffect() {}

    public static void spawn(ClientWorld world, Vec3d pos) {
        double x = pos.x;
        double z = pos.z;
        double baseY = pos.y + 0.12;

        for (int ribbon = 0; ribbon < 3; ribbon++) {
            ParticleEffect dust = ribbon == 0 ? CYAN : (ribbon == 1 ? BLUE : VIOLET);
            double phase = ribbon * (Math.PI * 2.0 / 3.0);

            for (int i = 0; i < 44; i++) {
                double t = i / 43.0;
                double angle = phase + t * Math.PI * 4.4;
                double radius = 0.48 + 0.18 * Math.sin(t * Math.PI * 2.0 + phase);
                double px = x + Math.cos(angle) * radius;
                double py = baseY + t * 2.35 + 0.08 * Math.sin(angle * 1.7);
                double pz = z + Math.sin(angle) * radius;
                double tangent = 0.018 + 0.012 * (1.0 - t);

                world.addParticle(dust, px, py, pz,
                        -Math.sin(angle) * tangent,
                        0.025 + t * 0.018,
                        Math.cos(angle) * tangent);

                if ((i + ribbon * 3) % 11 == 0) {
                    world.addParticle(ParticleTypes.END_ROD, px, py, pz, 0.0, 0.025, 0.0);
                }
            }
        }

        for (int i = 0; i < 26; i++) {
            double a = Math.PI * 2.0 * i / 26.0;
            double r = 0.74 + 0.08 * Math.sin(a * 3.0);
            ParticleEffect dust = i % 3 == 0 ? CYAN : (i % 3 == 1 ? BLUE : VIOLET);
            world.addParticle(dust, x + Math.cos(a) * r, pos.y + 0.09, z + Math.sin(a) * r,
                    0.0, 0.012, 0.0);
        }

        play(world, x, baseY + 1.0, z, "block.amethyst_block.chime", 1.15f, 1.22f);
        play(world, x, baseY + 1.0, z, "block.beacon.power_select", 0.75f, 1.42f);
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

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

public final class StarlightEffect {
    private static final DustParticleEffect WHITE = new DustParticleEffect(0xFFF9DE, 0.85f);
    private static final DustParticleEffect BLUE = new DustParticleEffect(0xA8D8FF, 0.85f);
    private static final DustParticleEffect GOLD = new DustParticleEffect(0xFFE7A0, 0.80f);

    private StarlightEffect() {}

    public static void spawn(ClientWorld world, Vec3d pos) {
        double x = pos.x;
        double y = pos.y + 1.05;
        double z = pos.z;

        for (int i = 0; i < 30; i++) {
            double a = Math.random() * Math.PI * 2.0;
            double speed = 0.05 + Math.random() * 0.16;
            double vy = (Math.random() - 0.35) * 0.24;
            ParticleEffect dust = i % 3 == 0 ? GOLD : (i % 2 == 0 ? BLUE : WHITE);
            world.addParticle(dust, x, y, z, Math.cos(a) * speed, vy, Math.sin(a) * speed);
            if (i % 6 == 0) {
                world.addParticle(ParticleTypes.END_ROD, x, y, z,
                        Math.cos(a) * speed * 0.45, vy * 0.45, Math.sin(a) * speed * 0.45);
            }
        }

        for (int ring = 0; ring < 2; ring++) {
            int count = ring == 0 ? 9 : 13;
            double radius = ring == 0 ? 0.78 : 1.22;
            for (int i = 0; i < count; i++) {
                double a = Math.PI * 2.0 * i / count + ring * 0.27;
                double px = x + Math.cos(a) * radius;
                double py = y + Math.sin(a * 2.0 + ring) * 0.42 + (ring == 0 ? 0.10 : 0.42);
                double pz = z + Math.sin(a) * radius;
                ParticleEffect dust = i % 4 == 0 ? GOLD : (i % 2 == 0 ? BLUE : WHITE);

                world.addParticle(ParticleTypes.END_ROD, px, py, pz, 0.0, 0.018, 0.0);
                for (int ray = 0; ray < 4; ray++) {
                    double ra = ray * Math.PI / 2.0;
                    world.addParticle(dust,
                            px + Math.cos(ra) * 0.10,
                            py + Math.sin(ra) * 0.10,
                            pz,
                            0.0, 0.012, 0.0);
                }
            }
        }

        play(world, x, y, z, "block.amethyst_cluster.hit", 1.05f, 1.65f);
        play(world, x, y, z, "entity.experience_orb.pickup", 0.72f, 1.85f);
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

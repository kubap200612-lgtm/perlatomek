package pl.pearltrail;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.*;

/**
 * Smuga z cząsteczek teleportacji endermana (PORTAL) za rzuconą perłą kresu.
 * Smuga ciągnie się od miejsca rzutu do miejsca uderzenia i znika chwilę po lądowaniu.
 * Mod działa tylko po stronie klienta, więc działa też na serwerach bez moda.
 */
public class PearlTrailClient implements ClientModInitializer {

    /** Odstęp między punktami smugi (w blokach). Mniej = gęstsza smuga. */
    private static final double SPACING = 0.25;
    /** Szansa na cząsteczkę w każdym punkcie smugi w każdym ticku. */
    private static final float DENSITY = 0.35f;
    /** Ile ticków smuga zostaje po uderzeniu perły (20 ticków = 1 sekunda). */
    private static final int FADE_TICKS = 30;
    /** Maksymalna liczba punktów jednej smugi (ochrona przed lagami). */
    private static final int MAX_POINTS = 1500;

    private static final Map<Integer, Trail> TRAILS = new HashMap<>();

    private static class Trail {
        final List<Vec3d> points = new ArrayList<>();
        Vec3d last;
        int deadTicks = -1; // -1 = perła wciąż leci
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_WORLD_TICK.register(PearlTrailClient::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> TRAILS.clear());
    }

    private static void tick(ClientWorld world) {
        Set<Integer> alive = new HashSet<>();

        // 1. Zapisz trasę każdej lecącej perły
        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof EnderPearlEntity pearl)) continue;

            int id = pearl.getId();
            alive.add(id);
            Trail trail = TRAILS.computeIfAbsent(id, k -> new Trail());
            Vec3d pos = pearl.getPos();

            if (trail.last == null) {
                trail.points.add(pos);
            } else {
                double dist = trail.last.distanceTo(pos);
                if (dist > 0.01) {
                    int steps = Math.max(1, (int) Math.ceil(dist / SPACING));
                    for (int i = 1; i <= steps; i++) {
                        trail.points.add(trail.last.lerp(pos, (double) i / steps));
                    }
                }
            }
            trail.last = pos;

            while (trail.points.size() > MAX_POINTS) {
                trail.points.remove(0);
            }
        }

        Random random = world.getRandom();

        // 2. Rysuj smugi i usuwaj stare
        Iterator<Map.Entry<Integer, Trail>> it = TRAILS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Trail> entry = it.next();
            Trail trail = entry.getValue();

            if (!alive.contains(entry.getKey())) {
                if (trail.deadTicks == -1) {
                    trail.deadTicks = 0;
                    // Wybuch cząsteczek w miejscu uderzenia, jak przy teleporcie endermana
                    if (trail.last != null) {
                        for (int i = 0; i < 40; i++) {
                            spawnPortal(world, random, trail.last, 0.6);
                        }
                    }
                } else {
                    trail.deadTicks++;
                }
                if (trail.deadTicks > FADE_TICKS) {
                    it.remove();
                    continue;
                }
            }

            // Po uderzeniu smuga stopniowo rzednie
            float chance = DENSITY;
            if (trail.deadTicks >= 0) {
                chance *= 1.0f - (float) trail.deadTicks / FADE_TICKS;
            }

            for (Vec3d p : trail.points) {
                if (random.nextFloat() < chance) {
                    spawnPortal(world, random, p, 0.1);
                }
            }
        }
    }

    /** Cząsteczka dokładnie taka, jakiej używa enderman przy teleportacji. */
    private static void spawnPortal(ClientWorld world, Random random, Vec3d p, double spread) {
        world.addParticle(
                ParticleTypes.PORTAL,
                p.x + (random.nextDouble() - 0.5) * spread,
                p.y + (random.nextDouble() - 0.5) * spread,
                p.z + (random.nextDouble() - 0.5) * spread,
                (random.nextDouble() - 0.5) * 2.0,
                -random.nextDouble(),
                (random.nextDouble() - 0.5) * 2.0
        );
    }
}

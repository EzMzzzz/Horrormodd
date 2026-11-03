package com.horrormod;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Random ambient scares that happen to players in the dark (caves, night). */
public final class ScareManager {
    public enum Type { FOOTSTEPS, WHISPER, DOOR, DARKNESS, STALKER }

    private record Task(int at, Runnable run) {}

    private static final Map<UUID, Long> NEXT = new HashMap<>();
    private static final List<Task> TASKS = new ArrayList<>();

    private ScareManager() {}

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int now = server.getTicks();
            Iterator<Task> it = TASKS.iterator();
            List<Runnable> due = new ArrayList<>();
            while (it.hasNext()) {
                Task t = it.next();
                if (now >= t.at) { due.add(t.run); it.remove(); }
            }
            due.forEach(Runnable::run);
        });

        ServerTickEvents.END_WORLD_TICK.register(ScareManager::tickWorld);
    }

    private static void tickWorld(ServerWorld world) {
        long now = world.getTime();
        if (now % 20 != 0) return;
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (p.isSpectator()) continue;
            long next = NEXT.computeIfAbsent(p.getUuid(), k -> now + delay(world.random));
            if (now < next) continue;
            boolean dark = world.getLightLevel(p.getBlockPos()) <= 7 || world.isNight();
            if (!dark) { NEXT.put(p.getUuid(), now + 600); continue; }
            trigger(world, p, pick(world.random));
            NEXT.put(p.getUuid(), now + delay(world.random));
        }
    }

    private static long delay(Random r) { return 20L * 30 + r.nextInt(20 * 90); } // 30-120 s

    private static Type pick(Random r) {
        int n = r.nextInt(100);
        if (n < 30) return Type.FOOTSTEPS;
        if (n < 55) return Type.WHISPER;
        if (n < 70) return Type.DOOR;
        if (n < 85) return Type.DARKNESS;
        return Type.STALKER;
    }

    private static void sound(ServerWorld w, Vec3d pos, SoundEvent s, float vol, float pitch) {
        w.playSound(null, pos.x, pos.y, pos.z, s, SoundCategory.AMBIENT, vol, pitch);
    }

    private static Vec3d behind(ServerPlayerEntity p, double dist) {
        Vec3d back = Vec3d.fromPolar(0, p.getYaw() + 180);
        return p.getPos().add(back.multiply(dist));
    }

    private static void later(ServerWorld w, int ticks, Runnable r) {
        TASKS.add(new Task(w.getServer().getTicks() + ticks, r));
    }

    public static void trigger(ServerWorld w, ServerPlayerEntity p, Type type) {
        switch (type) {
            case FOOTSTEPS -> {
                SoundEvent step = w.getBlockState(p.getBlockPos().down()).getSoundGroup().getStepSound();
                for (int i = 0; i < 8; i++) {
                    final double dist = 9 - i;
                    later(w, i * 9, () -> { if (!p.isRemoved()) sound(w, behind(p, dist), step, 1.0f, 0.9f); });
                }
            }
            case WHISPER -> {
                sound(w, behind(p, 4), SoundEvents.ENTITY_WARDEN_NEARBY_CLOSER, 1.0f, 0.8f);
                later(w, 40, () -> { if (!p.isRemoved()) sound(w, behind(p, 2), SoundEvents.ENTITY_PHANTOM_AMBIENT, 0.8f, 0.5f); });
            }
            case DOOR -> {
                sound(w, behind(p, 10), SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, 1.2f, 0.7f);
                later(w, 30, () -> { if (!p.isRemoved()) sound(w, behind(p, 10), SoundEvents.BLOCK_IRON_DOOR_OPEN, 1.0f, 0.6f); });
            }
            case DARKNESS -> {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 160, 0, false, false));
                for (int i = 0; i < 3; i++) {
                    later(w, i * 14, () -> { if (!p.isRemoved()) sound(w, p.getPos(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, 1.5f, 1.0f); });
                }
            }
            case STALKER -> spawnStalker(w, p);
        }
    }

    private static void spawnStalker(ServerWorld w, ServerPlayerEntity p) {
        Box area = p.getBoundingBox().expand(64);
        if (w.getEntitiesByType(HorrorMod.STALKER, area, e -> true).size() >= 2) return;
        for (int attempt = 0; attempt < 30; attempt++) {
            double angle = w.random.nextDouble() * Math.PI * 2;
            double dist = 16 + w.random.nextInt(10);
            int x = (int) Math.floor(p.getX() + Math.cos(angle) * dist);
            int z = (int) Math.floor(p.getZ() + Math.sin(angle) * dist);
            int y0 = p.getBlockPos().getY() + w.random.nextInt(7) - 3;
            BlockPos pos = new BlockPos(x, y0, z);
            if (!w.getBlockState(pos).isAir() || !w.getBlockState(pos.up()).isAir()) continue;
            if (!w.getBlockState(pos.down()).isSolidBlock(w, pos.down())) continue;
            if (w.getLightLevel(pos) > 7) continue;
            StalkerEntity s = HorrorMod.STALKER.create(w);
            if (s == null) return;
            s.refreshPositionAndAngles(x + 0.5, y0, z + 0.5, w.random.nextFloat() * 360f, 0);
            w.spawnEntity(s);
            return;
        }
    }

    public static void clear(UUID id) { NEXT.remove(id); }
}

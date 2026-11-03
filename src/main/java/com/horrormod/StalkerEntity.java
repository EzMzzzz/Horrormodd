package com.horrormod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Tall, silent stalker. It freezes completely while any player is looking at it
 * and moves fast the moment they look away. Its hit blinds you with Darkness.
 */
public class StalkerEntity extends HostileEntity {
    private boolean frozen = false;

    public StalkerEntity(EntityType<? extends StalkerEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 10;
    }

    public static DefaultAttributeContainer.Builder createStalkerAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.34)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 32.0f));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    private boolean isWatched() {
        for (PlayerEntity p : this.getWorld().getPlayers()) {
            if (p.isSpectator() || p.squaredDistanceTo(this) > 48 * 48) continue;
            Vec3d look = p.getRotationVec(1.0f).normalize();
            Vec3d toMe = this.getEyePos().subtract(p.getEyePos()).normalize();
            if (look.dotProduct(toMe) > 0.6 && p.canSee(this)) return true;
        }
        return false;
    }

    @Override
    public void tick() {
        if (!this.getWorld().isClient) {
            frozen = isWatched();
            if (frozen) {
                this.getNavigation().stop();
                Vec3d v = this.getVelocity();
                this.setVelocity(0, Math.min(0, v.y), 0);
            }
        }
        super.tick();
    }

    @Override
    public void travel(Vec3d movementInput) {
        super.travel(frozen ? Vec3d.ZERO : movementInput);
    }

    @Override
    public boolean tryAttack(Entity target) {
        if (frozen) return false;
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 120, 0));
        }
        return hit;
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_ENDERMAN_AMBIENT; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_ENDERMAN_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_ENDERMAN_DEATH; }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) { return true; }
}

package dev.zenhao.melon.mixins

import net.minecraft.util.math.Vec3d

interface IExplosion {
    operator fun set(pos: Vec3d?, power: Float, createFire: Boolean)
}

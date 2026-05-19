package com.vrm.avatarrenderer.rendering

import com.google.android.filament.gltfio.FilamentAsset
import timber.log.Timber
import kotlin.math.sin
import kotlin.math.PI

class AnimationController(private val asset: FilamentAsset) {

    private data class AnimationState(
        val name: String,
        var time: Float = 0f,
        var duration: Float = 1f,
        var isPlaying: Boolean = false,
        var isLooping: Boolean = true
    )

    private var currentAnimation: AnimationState? = null
    private var blinkTimer = 0f
    private val blinkInterval = 4f // Blink every 4 seconds
    private val blinkDuration = 0.15f // Blink lasts 150ms
    
    // Animation definitions - map animation names to durations
    private val animationDefs = mapOf(
        "Walk" to 1.5f,
        "Run" to 1.0f,
        "Idle" to 2.0f,
        "Jump" to 0.8f,
        "Blink" to 0.15f,
        "Dance" to 3.0f
    )

    init {
        // Load glTF animations if available
        try {
            val animationCount = asset.animationCount
            Timber.d("Avatar has $animationCount animations")
            
            for (i in 0 until animationCount) {
                val name = asset.getAnimationName(i)
                Timber.d("Animation $i: $name")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading animations from asset")
        }
    }

    fun playAnimation(name: String) {
        try {
            val duration = animationDefs[name] ?: 1f
            currentAnimation = AnimationState(
                name = name,
                duration = duration,
                isPlaying = true,
                isLooping = name != "Blink"
            )
            Timber.d("Started animation: $name (duration: ${duration}s)")
        } catch (e: Exception) {
            Timber.e(e, "Error playing animation: $name")
        }
    }

    fun pauseAnimation() {
        currentAnimation?.isPlaying = false
        Timber.d("Animation paused")
    }

    fun resumeAnimation() {
        currentAnimation?.isPlaying = true
    }

    fun stopAnimation() {
        currentAnimation = null
    }

    fun update(deltaTime: Float) {
        try {
            // Update current animation
            currentAnimation?.let { anim ->
                if (anim.isPlaying) {
                    anim.time += deltaTime
                    
                    // Apply animation based on type
                    when (anim.name) {
                        "Walk" -> updateWalkAnimation(anim)
                        "Run" -> updateRunAnimation(anim)
                        "Idle" -> updateIdleAnimation(anim)
                        "Jump" -> updateJumpAnimation(anim)
                        "Dance" -> updateDanceAnimation(anim)
                        "Blink" -> updateBlinkAnimation(anim)
                    }

                    // Handle animation looping
                    if (anim.time >= anim.duration) {
                        if (anim.isLooping) {
                            anim.time = 0f
                        } else {
                            currentAnimation = null
                        }
                    }
                }
            }

            // Update automatic blinking when not in blink animation
            if (currentAnimation?.name != "Blink") {
                updateAutomaticBlink(deltaTime)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating animation")
        }
    }

    private fun updateWalkAnimation(anim: AnimationState) {
        // Normalize time to 0-1
        val t = (anim.time % anim.duration) / anim.duration
        
        // Calculate position offset (forward movement)
        val positionOffset = 3f * t // Move 3 units forward over animation duration
        val verticalBob = sin(t * PI * 2).toFloat() * 0.1f // Vertical bobbing
        
        // Update skeleton if available
        asset.root?.let { root ->
            try {
                // Animation would be applied to bones here
                // For now, this is a placeholder for bone-based animation
                Timber.v("Walk animation - time: $t")
            } catch (e: Exception) {
                Timber.e(e, "Error updating walk animation")
            }
        }
    }

    private fun updateRunAnimation(anim: AnimationState) {
        val t = (anim.time % anim.duration) / anim.duration
        val positionOffset = 5f * t // Run faster than walk
        val verticalBob = sin(t * PI * 2).toFloat() * 0.15f
        
        Timber.v("Run animation - time: $t")
    }

    private fun updateIdleAnimation(anim: AnimationState) {
        val t = (anim.time % anim.duration) / anim.duration
        // Subtle swaying
        val sway = sin(t * PI).toFloat() * 0.05f
        
        Timber.v("Idle animation - time: $t")
    }

    private fun updateJumpAnimation(anim: AnimationState) {
        val t = (anim.time % anim.duration) / anim.duration
        
        // Jump arc - peaks at 0.5
        val jumpHeight = if (t < 0.5f) {
            sin(t * PI).toFloat() * 2f
        } else {
            sin(t * PI).toFloat() * 2f
        }
        
        Timber.v("Jump animation - time: $t, height: $jumpHeight")
    }

    private fun updateDanceAnimation(anim: AnimationState) {
        val t = (anim.time % anim.duration) / anim.duration
        
        // Hip sway
        val hipSway = sin(t * PI * 4).toFloat() * 0.3f
        val shoulderRotation = sin(t * PI * 2).toFloat() * 0.2f
        
        Timber.v("Dance animation - time: $t")
    }

    private fun updateBlinkAnimation(anim: AnimationState) {
        val t = (anim.time % anim.duration) / anim.duration
        
        // Simple blink curve - open, close, open
        val blinkAmount = when {
            t < 0.3f -> t / 0.3f // Opening
            t < 0.7f -> 1f - ((t - 0.3f) / 0.4f) // Closing
            else -> ((t - 0.7f) / 0.3f) // Opening again
        }
        
        // blinkAmount would be used to control eye mesh morph targets
        Timber.v("Blink animation - time: $t")
    }

    private fun updateAutomaticBlink(deltaTime: Float) {
        blinkTimer += deltaTime
        
        if (blinkTimer >= blinkInterval) {
            playAnimation("Blink")
            blinkTimer = 0f
        }
    }

    val animationCount: Int
        get() = try {
            asset.animationCount
        } catch (e: Exception) {
            0
        }

    fun getAnimationName(index: Int): String = try {
        asset.getAnimationName(index)
    } catch (e: Exception) {
        "Unknown"
    }
}

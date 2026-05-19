package com.vrm.avatarrenderer.rendering

import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.Material
import com.google.android.filament.MeshAssimp
import com.google.android.filament.Scene
import com.google.android.filament.TransformManager
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import timber.log.Timber
import java.nio.ByteBuffer

class AvatarRenderer(
    private val engine: Engine,
    private val scene: Scene
) {

    private var currentAsset: FilamentAsset? = null
    private var assetLoader: AssetLoader? = null
    private var resourceLoader: ResourceLoader? = null
    private var currentAnimation: AnimationController? = null
    
    private val entityManager = EntityManager.get()
    private val transformManager = engine.transformManager
    
    var isAvatarLoaded = false
        private set

    init {
        // Initialize glTF/GLB loaders
        try {
            assetLoader = AssetLoader(engine, null, entityManager)
            resourceLoader = ResourceLoader(engine)
            Timber.d("AssetLoader initialized successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize AssetLoader")
        }
    }

    fun loadAvatarFromBytes(data: ByteArray) {
        try {
            Timber.d("Loading avatar from ${data.size} bytes")
            
            // Clean up previous asset
            currentAsset?.let { unloadAvatar() }

            val buffer = ByteBuffer.allocateDirect(data.size).apply {
                put(data)
                rewind()
            }

            // Load asset
            assetLoader?.let { loader ->
                currentAsset = loader.createAsset(buffer).also { asset ->
                    if (asset != null) {
                        // Load resources (textures, materials, etc.)
                        resourceLoader?.asyncBeginLoad(asset)
                        resourceLoader?.loadResources(asset)
                        
                        // Add to scene
                        scene.addEntities(asset.entities)
                        
                        // Configure root transform
                        asset.root.let { root ->
                            val tm = transformManager.getInstance(root)
                            transformManager.setTransform(tm, 
                                floatArrayOf(
                                    1f, 0f, 0f, 0f,
                                    0f, 1f, 0f, 0f,
                                    0f, 0f, 1f, 0f,
                                    0f, 0f, 0f, 1f
                                )
                            )
                        }
                        
                        // Initialize animation
                        currentAnimation = AnimationController(asset)
                        
                        isAvatarLoaded = true
                        Timber.d("Avatar loaded successfully. Entities: ${asset.entities.size}")
                        
                        // Auto-start idle or default animation
                        playAnimation("Idle")
                    } else {
                        Timber.e("Failed to create asset from buffer")
                        isAvatarLoaded = false
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading avatar")
            isAvatarLoaded = false
        }
    }

    fun unloadAvatar() {
        try {
            currentAsset?.let { asset ->
                scene.removeEntities(asset.entities)
                currentAsset = null
            }
            currentAnimation = null
            isAvatarLoaded = false
            Timber.d("Avatar unloaded")
        } catch (e: Exception) {
            Timber.e(e, "Error unloading avatar")
        }
    }

    fun playAnimation(name: String) {
        try {
            currentAnimation?.playAnimation(name)
            Timber.d("Playing animation: $name")
        } catch (e: Exception) {
            Timber.e(e, "Error playing animation: $name")
        }
    }

    fun pauseAnimation() {
        try {
            currentAnimation?.pauseAnimation()
            Timber.d("Animation paused")
        } catch (e: Exception) {
            Timber.e(e, "Error pausing animation")
        }
    }

    fun update(deltaTime: Float) {
        try {
            currentAnimation?.update(deltaTime)
        } catch (e: Exception) {
            Timber.e(e, "Error updating animation")
        }
    }

    fun cleanup() {
        try {
            unloadAvatar()
            assetLoader = null
            resourceLoader = null
            Timber.d("AvatarRenderer cleaned up")
        } catch (e: Exception) {
            Timber.e(e, "Error cleaning up renderer")
        }
    }
}

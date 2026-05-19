package com.vrm.avatarrenderer.rendering

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.google.android.filament.Engine
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.View
import com.google.android.filament.android.FilamentAndroid
import timber.log.Timber
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class FilamentRenderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GLSurfaceView(context, attrs, defStyleAttr) {

    interface RenderListener {
        fun onSurfaceCreated()
        fun onSurfaceDestroyed()
        fun onFrameRender(deltaTime: Float)
    }

    lateinit var engine: Engine
    lateinit var renderer: Renderer
    lateinit var scene: Scene
    lateinit var view: View

    private var renderListener: RenderListener? = null
    private var lastFrameTime = 0L
    private val isFilamentSupported: Boolean

    init {
        isFilamentSupported = FilamentAndroid.loadFilament()
        
        if (!isFilamentSupported) {
            Timber.e("Filament is not supported on this device")
        }

        // Configure surface
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setEGLContextClientVersion(3) // OpenGL ES 3.0

        // Set renderer
        setRenderer(FilamentRenderer())
        renderMode = RENDERMODE_CONTINUOUSLY
        
        preserveEGLContextOnPause = true
    }

    fun setRenderListener(listener: RenderListener) {
        this.renderListener = listener
    }

    fun destroy() {
        try {
            queueEvent {
                if (::renderer.isInitialized) {
                    renderer.destroy()
                }
                if (::view.isInitialized) {
                    view.destroy()
                }
                if (::scene.isInitialized) {
                    scene.destroy()
                }
                if (::engine.isInitialized) {
                    engine.destroy()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error destroying Filament resources")
        }
    }

    private inner class FilamentRenderer : Renderer {
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            Timber.d("Filament surface created")
            
            // Initialize Filament engine
            engine = Engine.create()
            renderer = engine.createRenderer()
            scene = engine.createScene()
            view = engine.createView()

            // Configure view
            view.scene = scene
            view.camera = engine.createCamera().apply {
                setPosition(0.0, 1.7, 4.0)
                lookAt(0.0, 1.0, 0.0)
                setProjectionFov(45.0, 1.0, 0.1, 100.0)
            }

            renderListener?.onSurfaceCreated()
            lastFrameTime = System.nanoTime()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            Timber.d("Surface changed: ${width}x${height}")
            view.viewport = com.google.android.filament.Viewport(0, 0, width, height)
        }

        override fun onDrawFrame(gl: GL10?) {
            val currentTime = System.nanoTime()
            val deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0f
            lastFrameTime = currentTime

            // Notify listener of frame update
            renderListener?.onFrameRender(deltaTime)

            // Render frame
            if (renderer.beginFrame(view)) {
                renderer.render(view)
                renderer.endFrame()
            }
        }
    }
}

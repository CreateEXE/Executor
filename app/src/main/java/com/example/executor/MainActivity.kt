package com.vrm.avatarrenderer

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import timber.log.Timber
import com.vrm.avatarrenderer.rendering.AvatarRenderer
import com.vrm.avatarrenderer.rendering.FilamentRenderView
import android.view.ViewGroup
import android.widget.FrameLayout

class MainActivity : AppCompatActivity() {

    private lateinit var renderView: FilamentRenderView
    private var avatarRenderer: AvatarRenderer? = null
    private lateinit var loadAvatarButton: Button
    private lateinit var playAnimationButton: Button
    private lateinit var pauseAnimationButton: Button
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            loadAvatarFromUri(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Create root container
        val rootContainer = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(rootContainer)

        // Initialize Filament render view
        renderView = FilamentRenderView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        rootContainer.addView(renderView, 0)

        // Create control panel
        val controlPanel = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                200,
                android.view.Gravity.BOTTOM
            )
            setBackgroundColor(android.graphics.Color.argb(220, 0, 0, 0))
        }

        // Load Avatar Button
        loadAvatarButton = Button(this).apply {
            text = "Load Avatar (GLB)"
            layoutParams = FrameLayout.LayoutParams(
                250,
                50,
                android.view.Gravity.CENTER_HORIZONTAL | android.view.Gravity.TOP
            ).also {
                it.topMargin = 20
            }
            setOnClickListener { openFilePicker() }
        }
        controlPanel.addView(loadAvatarButton)

        // Play Animation Button
        playAnimationButton = Button(this).apply {
            text = "Play Walk"
            layoutParams = FrameLayout.LayoutParams(
                120,
                50,
                android.view.Gravity.START | android.view.Gravity.BOTTOM
            ).also {
                it.bottomMargin = 20
                it.leftMargin = 20
            }
            setOnClickListener {
                avatarRenderer?.playAnimation("Walk")
            }
            isEnabled = false
        }
        controlPanel.addView(playAnimationButton)

        // Pause Animation Button
        pauseAnimationButton = Button(this).apply {
            text = "Pause"
            layoutParams = FrameLayout.LayoutParams(
                120,
                50,
                android.view.Gravity.END | android.view.Gravity.BOTTOM
            ).also {
                it.bottomMargin = 20
                it.rightMargin = 20
            }
            setOnClickListener {
                avatarRenderer?.pauseAnimation()
            }
            isEnabled = false
        }
        controlPanel.addView(pauseAnimationButton)

        rootContainer.addView(controlPanel)

        // Initialize avatar renderer
        renderView.setRenderListener(object : FilamentRenderView.RenderListener {
            override fun onSurfaceCreated() {
                Timber.d("Surface created, initializing renderer")
                avatarRenderer = AvatarRenderer(renderView.engine, renderView.scene)
                updateControlsState()
            }

            override fun onSurfaceDestroyed() {
                Timber.d("Surface destroyed")
                avatarRenderer?.cleanup()
                avatarRenderer = null
            }

            override fun onFrameRender(deltaTime: Float) {
                avatarRenderer?.update(deltaTime)
            }
        })
    }

    private fun openFilePicker() {
        filePickerLauncher.launch("application/octet-stream")
    }

    private fun loadAvatarFromUri(uri: Uri) {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@MainActivity, "Loading avatar...", Toast.LENGTH_SHORT).show()
                Timber.d("Loading avatar from URI: $uri")
                
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    avatarRenderer?.loadAvatarFromBytes(bytes)
                    updateControlsState()
                    Toast.makeText(
                        this@MainActivity,
                        "Avatar loaded successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Auto-start blinking animation
                    avatarRenderer?.playAnimation("Blink")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load avatar")
                Toast.makeText(
                    this@MainActivity,
                    "Failed to load avatar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateControlsState() {
        val isAvatarLoaded = avatarRenderer?.isAvatarLoaded == true
        playAnimationButton.isEnabled = isAvatarLoaded
        pauseAnimationButton.isEnabled = isAvatarLoaded
    }

    override fun onDestroy() {
        super.onDestroy()
        renderView.destroy()
    }

    override fun onPause() {
        super.onPause()
        renderView.onPause()
    }

    override fun onResume() {
        super.onResume()
        renderView.onResume()
    }
}

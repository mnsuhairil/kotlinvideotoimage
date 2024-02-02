package dev.su.kotlinvideotoimage

import FrameExtractor
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val SELECT_VIDEO_REQUEST_CODE = 1
    private var selectedVideoUri: Uri? = null
    private var progressBar: ProgressBar? = null
    private var className: String? = null
    private val CAPTURE_VIDEO_REQUEST_CODE = 2
    private lateinit var textureView: TextureView
    private val squareSize = 128

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.selectVideoButton).setOnClickListener {
            // Prompt the user for the class name
            showClassNameDialog()
        }

        textureView = findViewById(R.id.textureView)

        findViewById<Button>(R.id.captureVideoButton).setOnClickListener {
            captureVideo()
        }


        progressBar = findViewById(R.id.progressBar)
        progressBar?.max = 100
        findViewById<Button>(R.id.startProcessButton).setOnClickListener {
            if (selectedVideoUri != null && className != null) {
                // Start the frame extraction process
                extractFrames(selectedVideoUri!!, className!!)
            }

        }
    }
    override fun onResume() {
        super.onResume()
        // Ensure the TextureView is available for drawing when the activity is resumed
        if (textureView.isAvailable) {
            drawSquare()
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    private fun drawSquare() {
        val canvas = textureView.lockCanvas()
        val paint = Paint()

        // Clear the canvas
        canvas?.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)

        // Draw the square
        paint.color = Color.RED
        canvas?.drawRect((textureView.width - squareSize).toFloat(), 0f, textureView.width.toFloat(), squareSize.toFloat(), paint)

        // Unlock the canvas to apply the drawing
        textureView.unlockCanvasAndPost(canvas!!)
    }

    // SurfaceTextureListener for TextureView
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {


        override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
            drawSquare()

        }

        override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

        }

        override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
            return true

        }

        override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
        }
    }

    private fun selectVideo() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, SELECT_VIDEO_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SELECT_VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {
            val videoUri: Uri? = data?.data
            videoUri?.let {
                selectedVideoUri = it
            }
            findViewById<Button>(R.id.startProcessButton).visibility = View.VISIBLE
        }
    }

    private fun showClassNameDialog() {
        val editText = EditText(this)

        AlertDialog.Builder(this)
            .setTitle("Enter Class Name")
            .setView(editText)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                // Retrieve the class name entered by the user
                className = editText.text.toString()

                // Start the frame extraction process with the provided class name
                selectVideo()
            })
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun captureVideo() {
        val captureVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        captureVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1) // 0 means lowest quality
        captureVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 128 * 128) // Set the size limit
        captureVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5) // Set the duration limit (5 seconds)
        startActivityForResult(captureVideoIntent, CAPTURE_VIDEO_REQUEST_CODE)
    }

    private fun extractFrames(videoUri: Uri, className: String) {
        val videoPath = getVideoPath(videoUri)
        progressBar?.visibility = View.VISIBLE

        val frameExtractorTrain = FrameExtractor(this, videoPath, className, this,false)
        frameExtractorTrain.extractFrames()
        progressBar?.visibility = View.VISIBLE

        val frameExtractorValid = FrameExtractor(this, videoPath, className, this,true)
        frameExtractorValid.extractFrames()

    }

    fun onProgressUpdate(progress: Int) {
        // Update your progress bar here using 'progress'
        progressBar?.progress = progress
    }

    fun onExtractionComplete() {
        // Extraction complete, you can hide or handle the progress bar
        progressBar?.visibility = View.GONE
        progressBar?.progress = 0
        // Perform any other necessary actions
    }
    private fun getVideoPath(uri: Uri): String {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        cursor?.moveToFirst()
        val path = cursor?.getString(columnIndex ?: 0) ?: ""
        cursor?.close()
        return path
    }
}

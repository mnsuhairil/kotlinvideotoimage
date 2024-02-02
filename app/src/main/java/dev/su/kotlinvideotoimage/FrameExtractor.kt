import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import dev.su.kotlinvideotoimage.MainActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

interface FrameExtractionCallback {
    fun onProgressUpdate(progress: Int)
    fun onExtractionComplete()
}

class FrameExtractor(
    private val context: Context,
    private val videoPath: String,
    private val className: String,
    private val callback: MainActivity,
    private  val isvalid: Boolean
) {
    private val TAG = "FrameExtractor"
    private val retriever = MediaMetadataRetriever()

    fun extractFrames() {
        retriever.setDataSource(videoPath)

        val framesFolder =
            File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "data")
        framesFolder.mkdirs()

        val duration =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                ?: 0

        Log.d(TAG, "Video duration: $duration milliseconds")

        val frameRate = 30 // Change this to the actual frame rate of your video

        ExtractFramesTask(framesFolder, duration, frameRate, callback, isvalid).execute()
//        ExtractFramesTask(framesFolder, duration, frameRate, callback, isvalid).execute()
    }

    private fun saveFrame(
        framesFolder: File,
        time: Long,
        frameBitmap: Bitmap?,
        frameNumber: Int,
        isValidation: Boolean
    ) {
        try {
            frameBitmap?.let {
                // Create class-specific folders "train" and "valid" inside the framesFolder
                val classFolder = File(framesFolder, if (isValidation) "valid" else "train")
                val specificClassFolder = File(classFolder, className)
                specificClassFolder.mkdirs()

                // Save the frame in the selected folder
                val file = File(specificClassFolder, "frame_${time}.jpg")
                val fos = FileOutputStream(file)
                it.compress(Bitmap.CompressFormat.JPEG, 30, fos)
                fos.close()

                Log.d(TAG, "Image saved at: ${file.absolutePath}")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "Error saving frame: ${e.message}")
        }
    }

    private inner class ExtractFramesTask(
        private val framesFolder: File,
        private val duration: Long,
        private val frameRate: Int,
        private val callback: MainActivity,
        private val isValidation: Boolean
    ) : AsyncTask<Void, Int, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            try {
                val targetFrameCount = if (isValidation) 100 else 400
                val timeStep = duration / targetFrameCount.toFloat()

                for (frameNumber in 0 until targetFrameCount) {
                    val time = (frameNumber * timeStep).toLong()

                    val frameBitmap = retriever.getFrameAtTime(
                        time * 1000,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                    saveFrame(framesFolder, time, frameBitmap, frameNumber, isValidation)

                    val progress = ((frameNumber.toFloat() / targetFrameCount) * 100).toInt()
                    publishProgress(progress)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during frame extraction: ${e.message}")
            }
            return null
        }

        override fun onProgressUpdate(vararg values: Int?) {
            values[0]?.let {
                callback?.onProgressUpdate(it)
            }
        }

        override fun onPostExecute(result: Void?) {
            callback?.onExtractionComplete()
        }
    }
}

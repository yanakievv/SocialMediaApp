@file:Suppress("DEPRECATION")

package com.example.socialmediaappv2.upload

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.hardware.camera2.params.MeteringRectangle
import android.media.Image
import android.media.ImageReader
import android.media.MediaScannerConnection
import android.os.*
import android.provider.MediaStore.Images.Media
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.view.TextureView.SurfaceTextureListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils
import com.example.socialmediaappv2.R
import com.example.socialmediaappv2.UserInfoPresenter
import com.example.socialmediaappv2.contract.Contract
import com.example.socialmediaappv2.data.SharedPreference
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_camera2.*
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt
import kotlin.math.sqrt


private const val REQUEST_CAMERA_PERMISSION = 200
private const val GET_LAT_LONG = "GETLATLONG"
internal lateinit var presenter: Contract.UserInfoPresenter
private lateinit var fusedLocationClient: FusedLocationProviderClient


class Camera2Activity : AppCompatActivity(), Contract.MainView {

    private val tag = "AndroidCameraApi"
    private val orientations = SparseIntArray()

    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSessions: CameraCaptureSession? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null

    private var imageDimension: Size? = null
    private var imageReader: ImageReader? = null
    private val file: File? = null

    private var lat: Double = 0.0
    private var long: Double = 0.0

    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null

    private lateinit var sharedPref: SharedPreference

    private var camera = 0 //0 is for back 1 is for front
    private var flash: Boolean = false

    var fingeSpacing = 0F
    var zoomLevel = 1F

    private class ImageSaver(val mImage: Image, val mFile: File, val angle: Float): Runnable {
        override fun run() {
            val buffer: ByteBuffer = mImage.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            var output: FileOutputStream? = null
            try {
                output = FileOutputStream(mFile)
                output.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                mImage.close()
                if (null != output) {
                    try {
                        output.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                rotateImage(mFile, angle)
            }
        }
        private fun rotateImage(tempPhoto: File, angle: Float) {
            val photoPath: String = tempPhoto.path.toString()
            var bmp = BitmapFactory.decodeFile(photoPath)

            val matrix = Matrix()
            matrix.postRotate(angle)
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)

            val fOut: FileOutputStream
            try {
                fOut = FileOutputStream(tempPhoto)
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut)
                fOut.flush()
                fOut.close()
            } catch (e1: FileNotFoundException) {
                e1.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }





    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_camera2)

        orientations.append(Surface.ROTATION_0, 90)
        orientations.append(Surface.ROTATION_90, 0)
        orientations.append(Surface.ROTATION_180, 270)
        orientations.append(Surface.ROTATION_270, 180)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPref = SharedPreference(this)
        setPresenter(UserInfoPresenter(this))
        runBlocking { presenter.init(
            sharedPref.getString("publisherId")!!,
            "",
            this@Camera2Activity
        )
        }
        textureView.surfaceTextureListener = textureListener
        disableButtons()
        takePictureButton.setOnClickListener { takePicture() }
        backButton.setOnClickListener {
            closeCamera()
            finish()
        }
        swapButton.setOnClickListener {
            closeCamera()
            openCamera(if (camera == 0) 1 else 0)
            camera = if (camera == 0) 1 else 0
            sharedPref.save("orientation", camera)
            updateFlashButton(camera == 0)
        }
        flashButton.setOnClickListener {
            flash = !flash
            updateFlashButton(true)
            sharedPref.save("flash", flash)
        }
        textureView.setOnTouchListener(object : View.OnTouchListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                try {
                    val activity: Activity? = this@Camera2Activity
                    val manager = activity!!.getSystemService(CAMERA_SERVICE) as CameraManager
                    val characteristics = manager.getCameraCharacteristics(cameraId!!)
                    val maxZoom =
                        characteristics[CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM]!! //for LGE LM-G710 its 8.0
                    val action: Int = event.action
                    val currentFingerSpacing: Float
                    if (event.pointerCount > 1) {
                        currentFingerSpacing = getFingerSpacing(event)
                        if (fingeSpacing != 0F) {
                            if (currentFingerSpacing > fingeSpacing && maxZoom > zoomLevel) {
                                zoomLevel += .05F
                            } else if (currentFingerSpacing < fingeSpacing && zoomLevel > 1) {
                                zoomLevel -= .05F
                                if (zoomLevel < 1) zoomLevel = 1F
                            }
                            val matrix = Matrix()
                            matrix.setScale(
                                zoomLevel,
                                zoomLevel,
                                textureView.width / 2F,
                                textureView.height / 2F
                            )
                            textureView.setTransform(matrix)
                        }
                        fingeSpacing = currentFingerSpacing
                    } else {
                        if (action == MotionEvent.ACTION_UP) {
                            // end of tap
                        }
                    }
                } catch (e: CameraAccessException) {
                    throw RuntimeException("can not access camera.", e)
                }
                return true
            }
        })
    }

    private fun calculateFocusRect(x: Float, y: Float): MeteringRectangle? {
        val areaSize = 50
        val left: Int = clamp(x.toInt() - areaSize / 2, 0, textureView.width - areaSize)
        val top: Int = clamp(y.toInt() - areaSize / 2, 0, textureView.height - areaSize)
        val rectF = RectF(
            left.toFloat(),
            top.toFloat(),
            (left + areaSize).toFloat(),
            (top + areaSize).toFloat()
        )
        val focusRect = Rect(
            rectF.left.roundToInt(),
            rectF.top.roundToInt(), rectF.right.roundToInt(),
            rectF.bottom.roundToInt()
        )
        return MeteringRectangle(focusRect, 1)
    }

    private fun clamp(x: Int, min: Int, max: Int): Int {
        if (x > max) {
            return max
        }
        return if (x < min) {
            min
        } else x
    }

    private var textureListener: SurfaceTextureListener = object : SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera(camera)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.e(tag, "onOpened")
            cameraDevice = camera
            createCameraPreview()
            enableButtons()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice?.close()
            cameraDevice = null
        }
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        mBackgroundThread?.quitSafely()
        try {
            mBackgroundThread?.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun takePicture() {
        disableButtons()
        if (!getLatLong())
        {
            return
        }
        if (null == cameraDevice) {
            Log.e(tag, "cameraDevice null")
            return
        }
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val characteristics = manager.getCameraCharacteristics(cameraDevice!!.id)
            val jpegSizes: Array<Size>?
            jpegSizes =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                    .getOutputSizes(ImageFormat.JPEG)
            var width = 640
            var height = 480
            if (jpegSizes != null && jpegSizes.isNotEmpty()) {
                width = jpegSizes[0].width
                height = jpegSizes[0].height
            }
            val reader: ImageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 5)
            val outputSurfaces: MutableList<Surface> = ArrayList(2)
            outputSurfaces.add(reader.surface)
            outputSurfaces.add(Surface(textureView.surfaceTexture))
            val captureBuilder: CaptureRequest.Builder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE) // *
            captureBuilder.addTarget(reader.surface)
            Zoom(characteristics).setZoom(captureBuilder, zoomLevel)
            if (flash && camera == 0) captureBuilder.set(
                CaptureRequest.FLASH_MODE,
                CaptureRequest.FLASH_MODE_SINGLE
            )
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            // Orientation
            @Suppress("DEPRECATION") val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                applicationContext.display?.rotation
            } else {
                windowManager.defaultDisplay.rotation
            }
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, orientations.get(rotation!!))
            val readerListener: ImageReader.OnImageAvailableListener =
                ImageReader.OnImageAvailableListener {
                    SharedPreference.imagesTaken++
                    val angle =
                        if (camera == 1 && getRotation(this@Camera2Activity) == 0F) {
                            270F + getRotation(this@Camera2Activity)!!
                        }
                        else {
                            90F + getRotation(this@Camera2Activity)!!
                        }
                    val pathD = getExternalFilesDir(null)
                        .toString() + "/" + Environment.DIRECTORY_DCIM + "/"
                    val mediaStorageDir = File(pathD, "MyAlbum")
                    if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {
                            Log.d("MyCameraApp", "failed to create directory")
                        }
                    }
                    val timeStamp: String =
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val mFile = File(mediaStorageDir, "ImageName_$timeStamp.jpeg")

                    val values = ContentValues()
                    values.put(Media.TITLE, "ImageName")
                    values.put(Media.CONTENT_TYPE, "image/jpeg")
                    values.put("_data", mFile.absolutePath)
                    MediaScannerConnection.scanFile(
                        applicationContext, arrayOf(file.toString()),
                        arrayOf(mFile.name), null
                    )

                    val cr: ContentResolver = applicationContext.contentResolver
                    cr.insert(Media.EXTERNAL_CONTENT_URI, values)
                    mBackgroundHandler!!.post(ImageSaver(it.acquireNextImage(), mFile, angle))
                    presenter.addPost(
                        mFile.absolutePath,
                        if (camera == 0) 1 + rotation else -1 - rotation,
                        doubleArrayOf(
                            lat,
                            long
                        ),
                        this
                    )
                    sharedPref.save("lat", lat.toString())
                    sharedPref.save("long", long.toString())
                }
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)
            val captureListener: CaptureCallback = object : CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    createCameraPreview()
                }
            }
            cameraDevice!!.createCaptureSession(
                outputSurfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        try {
                            session.capture(
                                captureBuilder.build(),
                                captureListener,
                                mBackgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {}
                },
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        enableButtons()
    }

    private fun createCameraPreview() {
        try {
            val texture: SurfaceTexture = textureView.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder!!.addTarget(surface)
            cameraDevice?.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        if (null == cameraDevice) {
                            return
                        }
                        cameraCaptureSessions = cameraCaptureSession
                        updatePreview()
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        Toast.makeText(
                            this@Camera2Activity,
                            "Configuration change",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun openCamera(id: Int) {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Log.e(tag, "is camera open")
        try {
            cameraId = manager.cameraIdList[id]
            val characteristics = manager.getCameraCharacteristics(cameraId as String)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@Camera2Activity,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CAMERA_PERMISSION
                )
                return
            }
            manager.openCamera(cameraId!!, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        Log.e(tag, "openCamera X")
    }

    private fun updatePreview() {
        if (null == cameraDevice) {
            Log.e(tag, "updatePreview error, return")
        }
        captureRequestBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            cameraCaptureSessions?.setRepeatingRequest(
                captureRequestBuilder!!.build(),
                null,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun closeCamera() {
        if (null != cameraDevice) {
            cameraDevice!!.close()
            cameraDevice = null
        }
        if (null != imageReader) {
            imageReader!!.close()
            imageReader = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(
                    this@Camera2Activity,
                    "Cannot use app without granting proper camera permissions!",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(tag, "onResume")
        camera = sharedPref.getInt("orientation")
        flash = sharedPref.getBoolean("flash")
        updateFlashButton(camera == 0)
        startBackgroundThread()
        if (textureView.isAvailable) {
            openCamera(camera)
        } else {
            textureView.surfaceTextureListener = textureListener
            textureView.rotation = getRotation(this@Camera2Activity)!!
            //Toast.makeText(this@Camera2Activity, getRotation(this@Camera2Activity).toString(), Toast.LENGTH_SHORT).show()
        }
    }



    override fun onPause() {
        Log.e(tag, "onPause")
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    override fun setPresenter(_presenter: Contract.UserInfoPresenter) {
        presenter = _presenter
    }

    private fun disableButtons() {
        takePictureButton.isEnabled = false
        backButton.isEnabled = false
    }

    private fun enableButtons() {
        takePictureButton.isEnabled = true
        backButton.isEnabled = true
    }

    private fun getLatLong(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(GET_LAT_LONG, "Permissions not granted.")
            return false
        }
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                lat = it.latitude
                long = it.longitude
                Log.e(
                    GET_LAT_LONG,
                    "Successful fetch. Coordinates are: ${it.latitude} ${it.longitude}."
                )
            }
        }
        return true
    }

    //Determine the space between the first two fingers
    private fun getFingerSpacing(event: MotionEvent): Float {
        val x: Float = event.getX(0) - event.getX(1)
        val y: Float = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y.toDouble()).toFloat()
    }

    private fun getRotation(context: Context): Float? {
        val rotation: Int =
            (context.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.orientation
        return when (rotation) {
            Surface.ROTATION_0 -> 0F
            Surface.ROTATION_90 -> 270F
            Surface.ROTATION_180 -> 180F
            else -> 90F
        }
    }
    private fun updateFlashButton(visible: Boolean) { //hide the flash button when using front facing camera
        if (visible) {
            flashButton.visibility = View.VISIBLE
            if (flash) {
                flashButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_baseline_flash_on_48
                    )
                )
            } else {
                flashButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_baseline_flash_off_48
                    )
                )
            }
        }
        else flashButton.visibility = View.INVISIBLE
    }
}
class Zoom(characteristics: CameraCharacteristics) {
    private val mCropRegion: Rect = Rect()
    var maxZoom: Float

    private val mSensorSize: Rect? = characteristics[CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE]
    var hasSupport: Boolean
    fun setZoom(builder: CaptureRequest.Builder, zoom: Float) {
        if (!hasSupport) {
            return
        }
        val newZoom: Float = MathUtils.clamp(
            zoom, DEFAULT_ZOOM_FACTOR,
            maxZoom
        )
        val centerX: Int = mSensorSize!!.width() / 2
        val centerY: Int = mSensorSize.height() / 2
        val deltaX = (0.5f * mSensorSize.width() / newZoom).toInt()
        val deltaY = (0.5f * mSensorSize.height() / newZoom).toInt()
        mCropRegion.set(
            centerX - deltaX,
            centerY - deltaY,
            centerX + deltaX,
            centerY + deltaY
        )
        builder.set(CaptureRequest.SCALER_CROP_REGION, mCropRegion)
    }

    companion object {
        private const val DEFAULT_ZOOM_FACTOR = 1.0f
    }

    init {
        if (mSensorSize == null) {
            maxZoom = DEFAULT_ZOOM_FACTOR
            hasSupport = false
        }
        val value = characteristics[CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM]
        maxZoom = if (value == null || value < DEFAULT_ZOOM_FACTOR) DEFAULT_ZOOM_FACTOR else value
        hasSupport = maxZoom.compareTo(DEFAULT_ZOOM_FACTOR) > 0
    }
}


@file:Suppress("DEPRECATION")

package com.example.socialmediaappv2.upload

import android.Manifest
import android.R.attr
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.Image
import android.media.ImageReader
import android.media.MediaScannerConnection
import android.os.*
import android.provider.MediaStore.Images.Media
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.socialmediaappv2.R
import com.example.socialmediaappv2.UserInfoPresenter
import com.example.socialmediaappv2.contract.Contract
import com.example.socialmediaappv2.data.App.currentProfile
import kotlinx.android.synthetic.main.activity_camera2.*
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


private const val REQUEST_CAMERA_PERMISSION = 200
internal lateinit var presenter: Contract.UserInfoPresenter


class Camera2Activity : AppCompatActivity(), Contract.MainView {

    private var paths: MutableList<String> = mutableListOf()

    private val tag = "AndroidCameraApi"
    private val orientations = SparseIntArray()

    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSessions: CameraCaptureSession? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null

    private var imageDimension: Size? = null
    private var imageReader: ImageReader? = null
    private val file: File? = null


    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null

    private class ImageSaver(val mImage: Image, val mFile: File): Runnable {
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
            }
        }

    }



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera2)

        orientations.append(Surface.ROTATION_0, 0)
        orientations.append(Surface.ROTATION_90, 90)
        orientations.append(Surface.ROTATION_180, 180)
        orientations.append(Surface.ROTATION_270, 270)

        setPresenter(UserInfoPresenter(this))
        runBlocking { presenter.init(
            currentProfile.currentUser.publisherId,
            "",
            this@Camera2Activity
        )}
        textureView.surfaceTextureListener = textureListener

        Toast.makeText(this, currentProfile.currentUser.displayName, Toast.LENGTH_SHORT).show()

        takePictureButton.setOnClickListener { takePicture() }
        backButton.setOnClickListener { closeCamera() }

    }





    private var textureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            // Transform image size according to surface width and height
        }

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

    @SuppressLint("InlinedApi")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun takePicture() {
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
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
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
                    currentProfile.imagesTaken++
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
                    values.put(Media.DATE_TAKEN, System.currentTimeMillis())
                    values.put(Media.ORIENTATION, orientations.get(attr.rotation))
                    values.put(Media.CONTENT_TYPE, "image/jpeg")
                    values.put("_data", mFile.absolutePath)
                    MediaScannerConnection.scanFile(
                        applicationContext, arrayOf(file.toString()),
                        arrayOf(mFile.name), null
                    )

                    val cr: ContentResolver = applicationContext.contentResolver
                    cr.insert(Media.EXTERNAL_CONTENT_URI, values)
                    presenter.addPost(mFile.absolutePath)
                    mBackgroundHandler!!.post(ImageSaver(it.acquireNextImage(), mFile))
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
    }

    private fun createCameraPreview() {
        try {
            /*
    E/AndroidRuntime: FATAL EXCEPTION: Camera Background
    Process: com.example.socialmediaappv2, PID: 20041
    java.lang.IllegalStateException: Session has been closed; further changes are illegal.
        at android.hardware.camera2.impl.CameraCaptureSessionImpl.checkNotClosed(CameraCaptureSessionImpl.java:835)
        at android.hardware.camera2.impl.CameraCaptureSessionImpl.capture(CameraCaptureSessionImpl.java:164)
        at com.example.socialmediaappv2.upload.Camera2Activity$takePicture$1.onConfigured(Camera2Activity.kt:253)
        at android.hardware.camera2.impl.CallbackProxies$SessionStateCallbackProxy.lambda$onConfigured$0$CallbackProxies$SessionStateCallbackProxy(CallbackProxies.java:53)
        at android.hardware.camera2.impl.-$$Lambda$CallbackProxies$SessionStateCallbackProxy$soW0qC12Osypoky6AfL3P2-TeDw.run(Unknown Source:4)
        at android.os.Handler.handleCallback(Handler.java:883)
        at android.os.Handler.dispatchMessage(Handler.java:100)
        at android.os.Looper.loop(Looper.java:214)
        at android.os.HandlerThread.run(HandlerThread.java:67)
        */
            setPresenter(UserInfoPresenter(this))
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

    private fun openCamera() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Log.e(tag, "is camera open")
        try {
            cameraId = manager.cameraIdList[0]
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
        finish()
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
        startBackgroundThread()
        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = textureListener
        }
    }

    override fun onPause() {
        Log.e(tag, "onPause")
        //closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    override fun setPresenter(_presenter: Contract.UserInfoPresenter) {
        presenter = _presenter
    }

    private fun disableButtons() {

    }
}
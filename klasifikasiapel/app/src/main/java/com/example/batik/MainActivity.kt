package com.example.batik

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.vincent.filepicker.Constant
import com.vincent.filepicker.activity.ImagePickActivity
import com.vincent.filepicker.filter.entity.ImageFile
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.Throws

lateinit var captureButton : Button
val REQUEST_IMAGE_CAPTURE = 1
private val PERMISSION_REQUEST_CODE: Int = 101
private var mCurrentPhotoPath: String? = null
class MainActivity : AppCompatActivity() {

    lateinit var image: MultipartBody.Part

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        supportActionBar?.title = "Upload"
        captureButton = findViewById(R.id.btn_camera)
        captureButton.setOnClickListener {
            if (checkPersmission()) takePicture() else requestPermission()
        }

        btn_exit.setOnClickListener{
            finish()
            System.exit(0)
        }

        Upload()
        btn_upload_hilang()

    }

    // memberikan onClick listener pada btnPickUpload
    fun Upload() {
        btnUpload.setOnClickListener {

            // check permission untuk android M dan ke atas.
            // saat permission disetujui oleh user maka jalan script untuk intent ke pick image.
            if (EasyPermissions.hasPermissions(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                val i = Intent(this, ImagePickActivity::class.java)
                i.putExtra(Constant.MAX_NUMBER, 1)
                startActivityForResult(i, Constant.REQUEST_CODE_PICK_IMAGE)
            } else {
                // tampilkan permission request saat belum mendapat permission dari user
                EasyPermissions.requestPermissions(
                    this,
                    "This application need your permission to access photo gallery.",
                    991,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }

        }
    }

    fun btn_upload_hilang() {
        // membrikan onclick listener pada btnUpload
        btnUpload.setOnClickListener {

            img_camera.visibility = View.GONE
            btn_camera.visibility = View.GONE
            btnUpload.visibility = View.GONE

        }
    }

    // override method onOptionItemSelected untuk handle click lisnter dari back button yang ada
    // pada action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item?.itemId) {

            android.R.id.home -> {
                this.finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // override method onActivityResult untuk handling data dari pickImageActivity.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constant.REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {

            // memunculkan btnUpload
            btnUpload.visibility = View.VISIBLE

            // membuat variable yang menampung path dari picked image.
            val pickedImg = data.getParcelableArrayListExtra<ImageFile>(Constant.RESULT_PICK_IMAGE)
                ?.get(0)?.path

            // membuat request body yang berisi file dari picked image.
            val requestBody =
                RequestBody.create(MediaType.parse("multipart"), File(mCurrentPhotoPath))

            // mengoper value dari requestbody sekaligus membuat form data untuk upload. dan juga mengambil nama dari picked image
            image = MultipartBody.Part.createFormData("image", File(pickedImg)?.name, requestBody)

            // mempilkan image yang akan diupload dengan glide ke imgUpload.
            Glide.with(this).load(pickedImg).into(img_camera)

            btnUpload.setOnClickListener {
                val intent = Intent(this, resultactivity::class.java)
                intent.putExtra("IMG", pickedImg.toString())
                startActivity(intent)
            }

        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

            // memunculkan btnUpload
            btnUpload.visibility = View.VISIBLE


            // membuat request body yang berisi file dari picked image.
            val requestBody = RequestBody.create(MediaType.parse("multipart"), File(mCurrentPhotoPath))


            // mengoper value dari requestbody sekaligus membuat form data untuk upload. dan juga mengambil nama dari picked image
            image = MultipartBody.Part.createFormData("image",
                File(mCurrentPhotoPath)?.name,requestBody)

            // mempilkan image yang akan diupload dengan glide ke imgUpload.
            Glide.with(this).load(mCurrentPhotoPath).into(img_camera)

            btnUpload.setOnClickListener {
                val intent = Intent(this, resultactivity::class.java)
                intent.putExtra("IMG", mCurrentPhotoPath.toString())
                startActivity(intent)
            }

        }

    }

    private fun checkPersmission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun takePicture() {

        val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File = createFile()

        val uri: Uri = FileProvider.getUriForFile(
            this,
            "com.example.batik.fileprovider",
            file
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)

    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ), PERMISSION_REQUEST_CODE)
    }

    @Throws(IOException::class)
    private fun createFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {

                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    takePicture()
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
            }
        }
    }
}
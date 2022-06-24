package com.example.batik

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.batik.api.ApiConfig
import com.example.batik.api.response.Default
import kotlinx.android.synthetic.main.activity_resultactivity.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File

class resultactivity : AppCompatActivity() {
    lateinit var image: MultipartBody.Part
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultactivity)
        val actionbar = supportActionBar
        actionbar!!.title = "Hasil"
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)

        // mempilkan image yang akan diupload dengan glide ke imgUpload.
        Glide.with(this).load(intent.getStringExtra("IMG")).into(imgResult)

        // mempilkan image yang akan diupload dengan glide ke imgUpload.
        Glide.with(this).load(intent.getStringExtra("IMG")).into(imgResult)

        val requestBody = RequestBody.create(MediaType.parse("multipart"), File(intent.getStringExtra("IMG")))

        // mengoper value dari requestbody sekaligus membuat form data untuk upload. dan juga mengambil nama dari picked image
        image = MultipartBody.Part.createFormData("image", File(intent.getStringExtra("IMG"))?.name,requestBody)

        Result()

//        back1.setOnClickListener {
//            intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }
//        back2.setOnClickListener {
//            intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun Result(){

        // meampilkan progress dialog
        val loading = ProgressDialog(this)
        loading.setMessage("Please wait...")
        loading.show()

        // init retrofit
        val call = ApiConfig().instance().upload(image)

        // membaut request ke api
        call.enqueue(object : retrofit2.Callback<Default>{

            // handling request saat fail
            override fun onFailure(call: Call<Default>?, t: Throwable?) {
                loading.dismiss()
                Toast.makeText(applicationContext,"Connection error", Toast.LENGTH_SHORT).show()
                Log.d("ONFAILURE",t.toString())
            }

            // handling request saat response.
            override fun onResponse(call: Call<Default>?, response: Response<Default>?) {

                // dismiss progress dialog
                loading.dismiss()

                // menampilkan pesan yang diambil dari response.
                //Toast.makeText(applicationContext,response?.body()?.nm_brg,Toast.LENGTH_SHORT).show()


                namaApel.setText(response?.body()?.nama)
                deskripsi.setText(response?.body()?.deskripsi)
                ciri.setText(response?.body()?.ciri)
                accuracy.setText(response?.body()?.accuracy)
                if (response?.body()?.nama.isNullOrBlank()){
                    namaApel.setText("\"Tidak terdefinisi karena akurasi rendah\"")
                    deskripsi.setText("tidak ada")
                    ciri.setText("tidak ada")
                    accuracy.setText("Tidak ada")
                }
            }


        })


    }
}
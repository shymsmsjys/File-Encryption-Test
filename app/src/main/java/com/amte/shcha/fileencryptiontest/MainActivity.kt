package com.amte.shcha.fileencryptiontest

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.*
import java.security.InvalidKeyException
import java.security.Key
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {
    private val saveFile: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val innerIntent = Intent(Intent.ACTION_GET_CONTENT)
            innerIntent.type = "video/*"
            innerIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            val wrapperIntent = Intent.createChooser(innerIntent, null)
            startActivityForResult(wrapperIntent, REQUEST_CODE_ATTACH_VIDEO)
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, sPermissions, REQUEST_CODE_PERMISSION)
        }
        //        saveFile = createFile();
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionsResult, grantResults = " + grantResults[0])
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            Log.d(TAG, "bail due to resultCode=$resultCode")
            return
        }
        Log.i(TAG, "file uri = " + data!!.data)
        var savedFileUri = encryptFile(data.data!!)
        Log.i( TAG, "savedFileUri path = ${savedFileUri.path}")
    }

    private fun encryptFile(uri: Uri): Uri {
        var saveFile = File(FILE_DIR, "encrypted2.mpg")
        var input: InputStream? = null
        var fout: FileOutputStream? = null
        try {
            input = contentResolver.openInputStream(uri)
            val secureKey: Key = SecretKeySpec(AES_KEY.toByteArray(charset("UTF-8")),
                    "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, secureKey)

            if (input is FileInputStream) {

                Log.d(TAG, "file = ${saveFile.path}")
                val parentFile = saveFile.parentFile
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    Log.e(TAG, "file out: mkdirs for " + parentFile.path + " failed!")
                }
                fout = FileOutputStream(saveFile)
                val buffer = ByteArray(BUFFER_SIZE)
                var size = 0
                while (input.read(buffer).also { size = it } != -1) {
                    fout.write(cipher.doFinal(buffer), 0, size)     // encryption
//                    fout.write(buffer, 0, size)   // just copy
                }

//                Key secureKey = new SecretKeySpec(AES_KEY.getBytes("UTF-8"), "AES");
//                Cipher cipher = Cipher.getInstance("AES");
//                cipher.init(Cipher.ENCRYPT_MODE, secureKey);
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } finally {
            try {
                input?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Exception caught while closing input: ", e)
            }
            try {
                fout?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Exception caught while closing output: ", e)
            }
        }
        return Uri.fromFile(saveFile)
    }

    private fun insertMediaDb(uri: Uri) {
        // to do.. not working

        val values = ContentValues()
//        values.apply {
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis())
        values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Video.Media.RELATIVE_PATH, uri.path)
        values.put(MediaStore.Video.Media.DISPLAY_NAME, "aaaaaaa")
//        }
        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        Log.d(TAG, " collection = $collection")
        val mediaUri = applicationContext.contentResolver.insert(collection, values)
        Log.d(TAG, "mediaUri = $mediaUri")
    }

    private fun createFile(): File {
        val file = File(FILE_DIR)
        if (!file.exists()) file.mkdir()
        return File(FILE_DIR, "encrypted.mpg")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERMISSION = 100
        private const val REQUEST_CODE_ATTACH_VIDEO = 102
        private const val AES_KEY = "dpfxldldkfdpvmdk"
        var sPermissions = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private val FILE_DIR = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).absolutePath
        private const val BUFFER_SIZE = 8 * 1024
    }
}
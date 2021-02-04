package com.amte.shcha.fileencryptiontest;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Environment;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.text.SimpleDateFormat;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_PERMISSION     = 100;
    private static final int REQUEST_CODE_ATTACH_VIDEO     = 102;
    private static final String AES_KEY = "dpfxldldkfdpvmdk";
    public static String [] sPermissions = new String [] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final String FILE_DIR = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM).getAbsolutePath();
    private static final int BUFFER_SIZE = 8 * 1024;
    private File saveFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                innerIntent.setType("video/*");
                innerIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                Intent wrapperIntent = Intent.createChooser(innerIntent, null);
                startActivityForResult(wrapperIntent, REQUEST_CODE_ATTACH_VIDEO);
            }
        });

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, sPermissions, REQUEST_CODE_PERMISSION);
        }
//        saveFile = createFile();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i (TAG, "onRequestPermissionsResult, grantResults = " + grantResults[0]);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Log.d(TAG, "bail due to resultCode=" + resultCode);
            return;
        }
        Log.i (TAG, "file uri = " + data.getData());

        ContentResolver cr = getContentResolver();
        InputStream input = null;
        FileOutputStream fout = null;

        try {
            input = cr.openInputStream(data.getData());

            Key secureKey = new SecretKeySpec(AES_KEY.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secureKey);

            if (input instanceof FileInputStream) {
                File saveFile = new File(FILE_DIR,"encrypted.mpg");
                Log.d(TAG, "file = " + saveFile.getPath());
                File parentFile = saveFile.getParentFile();
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    Log.e(TAG, "file out: mkdirs for " + parentFile.getPath() + " failed!");
                }

                FileInputStream fin = (FileInputStream) input;
                fout = new FileOutputStream(saveFile);
                byte[] buffer = new byte[BUFFER_SIZE];
                int size = 0;
                while ( (size = fin.read(buffer)) != -1 ) {
                    cipher.doFinal(buffer);
                    fout.write(cipher.doFinal(buffer), 0, size);
                }

//                Key secureKey = new SecretKeySpec(AES_KEY.getBytes("UTF-8"), "AES");
//                Cipher cipher = Cipher.getInstance("AES");
//                cipher.init(Cipher.ENCRYPT_MODE, secureKey);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception caught while closing input: ", e);
                }
            }

            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception caught while closing output: ", e);
                }
            }
        }

    }

    private File createFile() {
        File file = new File(FILE_DIR);
        if(!file.exists()) file.mkdir();

        return new File(FILE_DIR, "encrypted.mpg");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
package cc.zombiebrainzjuice.pyandroidaccessory;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

    TextView mText;
    UsbManager mUsbManager;
    UsbAccessory mAccessory;
    ParcelFileDescriptor mFileDescriptor;
    FileInputStream mInputStream;
    BufferedInputStream buf = null;
    String TAG = "DebugPy";

    int mMaxIteration = 200;
    int i = 1;

    Runnable mUpdateUI = new Runnable() {
        @Override
        public void run() {
            String message = String.valueOf(i) + '/';
            message += String.valueOf(mMaxIteration) + ':';
            message += System.getProperty("line.separator");
            mText.append(message);
        }
    };

    Runnable mListenerTask = new Runnable() {
        @Override
        public void run() {

            if (i <= mMaxIteration) {
                Log.d(TAG, String.valueOf(i));
                byte[] buffer = new byte[4];
                int ret;

                try {
                    ret = mInputStream.read(buffer);
                    Log.d(TAG, "Return: " + ret);
                    Log.d(TAG, buffer.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Error during read ");
                }

                mText.post(mUpdateUI);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                     e.printStackTrace();
                }
                i++;

                new Thread(this).start();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mText = (TextView) findViewById(R.id.display_area);
        mText.setMovementMethod(new ScrollingMovementMethod());

        Intent intent = getIntent();
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mAccessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);

        if (mAccessory == null) {
            mText.append("Not started by the Accessory directly" + System.getProperty("line.separator"));
            return;
        }

        Log.v(TAG, mAccessory.toString());
        mFileDescriptor = mUsbManager.openAccessory(mAccessory);
        if (mFileDescriptor != null) {
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
        }
        Log.v(TAG, mFileDescriptor.toString());
        //new Thread(mListenerTask).start();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}

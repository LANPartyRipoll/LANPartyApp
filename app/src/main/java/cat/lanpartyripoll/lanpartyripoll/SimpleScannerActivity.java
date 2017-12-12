package cat.lanpartyripoll.lanpartyripoll;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by abde on 6/11/17.
 */

public class SimpleScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private static final String TAG = "TAG";
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        if (rawResult.getBarcodeFormat() == BarcodeFormat.QR_CODE) {
            Intent intent = getIntent();
            intent.putExtra(String.valueOf(Constants.QR_CAMERA_RESULT), rawResult.getText());
            this.setResult(RESULT_OK, intent);
            mScannerView.resumeCameraPreview(this);
            finish();
        }
        else
            Toast.makeText(this, "No se ha detectado un código QR", Toast.LENGTH_LONG).show();
    }
}


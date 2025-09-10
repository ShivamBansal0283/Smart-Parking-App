package com.example.parking_system;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanQRCodeActivity extends AppCompatActivity {
    private String selectedVehicleRegNo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);

        selectedVehicleRegNo = getIntent().getStringExtra("vehicleNumber");

        // Initialize the QR code scanner
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan the QR code to get parking details");
        integrator.setCameraId(0);  // Use front camera
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan(); // Start scanning
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the QR scan result
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_LONG).show();
            } else {
                // Successfully scanned, pass data back to the ParkingFragment
                String scannedResult = result.getContents();  // The scanned parking ID or data
                Intent resultIntent = new Intent();
                resultIntent.putExtra("SCAN_RESULT", scannedResult);
                resultIntent.putExtra("vehicleNumber", selectedVehicleRegNo);

                setResult(RESULT_OK, resultIntent);  // Send back to ParkingFragment
                finish();  // Close the activity
            }
        }
    }
}
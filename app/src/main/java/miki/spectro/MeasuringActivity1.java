package miki.spectro;


import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import ir.androidexception.filepicker.dialog.DirectoryPickerDialog;
import miki.spectro.interfaces.BleCallback;
import miki.spectro.utils.SimpleConnection;


public class MeasuringActivity1 extends AppCompatActivity {
    private String request, bleName, data = "", currentData = "", data_for_file = "", dateText = "", timeText = "";
    private SimpleConnection simpleConnection;
    public ArrayList<TextView> textView = new ArrayList<>();
    public String[] names = {"R: ", "S: ", "T: ", "U: ", "V: ", "W: ", "Violet: ", "Blue: ", "Green: ", "Yellow: ", "Orange: ", "Red: ", "Temp: "};
    public ArrayList<String> readyData = new ArrayList<>();
    public MyLocation myLocation;
    public MyLocation.LocationResult locationResult;
    public DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()), timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private double longitude = 0, latitude = 0;
    public Date currentDate;
    public Button saveButton;





    private BleCallback bleCallbacks(){
        return new BleCallback(){

            @Override
            public void onBleConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onBleConnectionStateChange(gatt, status, newState);
            }

            @Override
            public void onBleServiceDiscovered(BluetoothGatt gatt, int status) {
                super.onBleServiceDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i("TAG", "MeasuringActivity1: Sending...");
                    simpleConnection.ble.write(request.getBytes());
                }
            }

            @Override
            public void onBleCharacteristicChange(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onBleCharacteristicChange(gatt, characteristic);
                currentData = characteristic.getStringValue(0);
                data += currentData;
                if(data.endsWith(";")){
                    currentDate = new Date();
                    dateText = dateFormat.format(currentDate);
                    timeText = timeFormat.format(currentDate);
                    data_for_file += dateText + " " + timeText + ": ";
                    data = data.substring(0, data.length()-1);
                    readyData.addAll(Arrays.asList(data.split("/")));
                    if(readyData.size() > 10){
                        for (int i = 0; i < textView.size(); i++) {
                            textView.get(i).setText(String.format("%s%s", names[i], readyData.get(i)));
                            data_for_file += names[i] + readyData.get(i) + "; ";
                            //Log.i("TAG", "MeasuringActivity1: " + i + " " + names[i] + readyData.get(i));
                        }
                        myLocation.getLocation(MeasuringActivity1.this, locationResult);
                        data_for_file += "Latitude: " + latitude + "; Longitude: " + longitude + ";";
                        Log.i("TAG", "MeasuringActivity1: " + data_for_file);
                        data_for_file = "";
                        data = "";
                        readyData = new ArrayList<>();
                    }
                }
            }

            @Override
            public void onBleRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onBleRead(gatt, characteristic, status);
            }

            @Override
            public void onBleWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onBleWrite(gatt, characteristic, status);
            }
        };
    }


    //@RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measuring_layout1);
        textView.add(findViewById(R.id.textViewR));
        textView.add(findViewById(R.id.textViewS));
        textView.add(findViewById(R.id.textViewT));
        textView.add(findViewById(R.id.textViewU));
        textView.add(findViewById(R.id.textViewV));
        textView.add(findViewById(R.id.textViewW));

        textView.add(findViewById(R.id.textViewViolet));
        textView.add(findViewById(R.id.textViewBlue));
        textView.add(findViewById(R.id.textViewGreen));
        textView.add(findViewById(R.id.textViewYellow));
        textView.add(findViewById(R.id.textViewOrange));
        textView.add(findViewById(R.id.textViewRed));

        textView.add(findViewById(R.id.textViewTemp));
        saveButton = findViewById(R.id.save_button);

        request = getIntent().getStringExtra("request");
        bleName = getIntent().getStringExtra("bleName");
        simpleConnection = new SimpleConnection(MeasuringActivity1.this, bleName, bleCallbacks());

        locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                //Log.i("TAG", "MeasuringActivity1: Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());
            }
        };
        myLocation = new MyLocation();

        saveButton.setOnClickListener(v -> {
            if(permissionGranted()) {
                DirectoryPickerDialog directoryPickerDialog = new DirectoryPickerDialog(this,
                        () -> Toast.makeText(MeasuringActivity1.this, "Canceled!!", Toast.LENGTH_SHORT).show(),
                        files -> Toast.makeText(MeasuringActivity1.this, files[0].getPath(), Toast.LENGTH_SHORT).show()
                );
                directoryPickerDialog.show();
            }
            else{
                requestPermission();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        simpleConnection.ble.disconnect();
    }

    private boolean permissionGranted(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }
}

package miki.spectro;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Instrumentation;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import ir.androidexception.filepicker.dialog.DirectoryPickerDialog;
import miki.spectro.interfaces.BleCallback;
import miki.spectro.utils.Permissions;
import miki.spectro.utils.SimpleConnection;


public class MeasuringActivity1 extends AppCompatActivity {
    private String request, bleName, data = "", currentData = "", dateText = "", timeText = "", path, csvDate = "", csvTime = "";
    private SimpleConnection simpleConnection;
    private ArrayList<TextView> textView = new ArrayList<>();
    private String[] names = {"R: ", "S: ", "T: ", "U: ", "V: ", "W: ", "Violet: ", "Blue: ", "Green: ", "Yellow: ", "Orange: ", "Red: ", "Temp: "}, data_for_file;
    private ArrayList<String> readyData = new ArrayList<>();
    private MyLocation myLocation;
    private MyLocation.LocationResult locationResult;
    private DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()),
            timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()),
            csvDateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault()),
            csvTimeFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
    private double longitude = 0, latitude = 0, accuracy = 0;
    private Date currentDate;
    private Button saveButton, reloadButton;
    private ArrayList<String[]> csvData = new ArrayList<>();
    private static final int WRITE_DOCUMENT_REQUEST = 1;
    private boolean flag = true;

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
                data_for_file = new String[18];
                currentData = characteristic.getStringValue(0);
                data += currentData;
                if(data.endsWith(";") && flag){
                    data = data.substring(0, data.length()-1);
                    readyData.addAll(Arrays.asList(data.split("/")));
                    if(readyData.size() > 10){
                        myLocation.getLocation(MeasuringActivity1.this, locationResult);
                        currentDate = new Date();
                        dateText = dateFormat.format(currentDate);
                        timeText = timeFormat.format(currentDate);
                        csvDate = csvDateFormat.format(currentDate);
                        csvTime = csvTimeFormat.format(currentDate);
                        data_for_file[0] = dateText;
                        data_for_file[1] = timeText;

                        for (int i = 0; i < textView.size(); i++) {
                            textView.get(i).setText(String.format("%s%s", names[i], readyData.get(i)));
                            data_for_file[i + 2] = readyData.get(i);
                        }
                        data_for_file[15] = String.valueOf(latitude);
                        data_for_file[16] = String.valueOf(longitude);
                        data_for_file[17] = String.valueOf(accuracy);
                        if(!data_for_file[16].equals("0.0") && !data_for_file[4].equals("")){
                            csvData.add(data_for_file);
                            Log.i("TAG", "MeasuringActivity1: " + Arrays.toString(data_for_file));
                        }
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

    @SuppressLint("MissingPermission")
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
        reloadButton = findViewById(R.id.reload_button);

        request = getIntent().getStringExtra("request");
        bleName = getIntent().getStringExtra("bleName");
        csvData.add(new String[]{"Дата", "Время", "R", "S", "T", "U", "V", "W", "Violet", "Blue", "Green", "Yellow", "Orange", "Red", "Температура", "Широта", "Долгота", "Точность местоположения(м)"});
        simpleConnection = new SimpleConnection(MeasuringActivity1.this, bleName, bleCallbacks());
        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Log.i("TAG", "IMHERE");
                        if (result.getData() != null) {
                            Log.i("TAG", result.getData().getData().toString());
                            grantUriPermission(getPackageName(), result.getData().getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            getContentResolver().takePersistableUriPermission(result.getData().getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                            writeFile(result.getData().getData().toString());
                        }
                    }else if(result.getResultCode() == Activity.RESULT_CANCELED){
                        flag = true;
                        simpleConnection = new SimpleConnection(MeasuringActivity1.this, bleName, bleCallbacks());
                    }
                }
        );

        locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                try {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    accuracy = location.getAccuracy();
                }catch (Exception e){
                    Log.e("ERROR", e.toString());
                }
            }
        };
        myLocation = new MyLocation();

        saveButton.setOnClickListener(v -> {
            flag = false;
            simpleConnection.ble.disconnect();
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_TITLE, "Spectro_" + csvDate + "_" + csvTime + ".csv");
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory());
            someActivityResultLauncher.launch(intent);
        });

        reloadButton.setOnClickListener(v -> {
            simpleConnection.ble.disconnect();
            flag = true;
            simpleConnection = new SimpleConnection(MeasuringActivity1.this, bleName, bleCallbacks());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //simpleConnection.ble.disconnect();
    }

    public void writeFile(String pickedFile){
        try{
            OutputStream outputStream = getContentResolver().openOutputStream(Uri.parse(pickedFile));
            CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream));
            writer.writeAll(csvData);
            writer.close();
            finish();
        }catch (Exception e) {
            Log.e("ERROR", e.toString());
        }
    }

}

package miki.spectro;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import ir.androidexception.filepicker.dialog.DirectoryPickerDialog;
import miki.spectro.interfaces.BleCallback;
import miki.spectro.utils.Constants;
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
    private Button saveButton;
    private ArrayList<String[]> csvData = new ArrayList<>();

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
                if(data.endsWith(";")){
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
                        if(!data_for_file[16].equals("0.0")){
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
        csvData.add(new String[]{"Дата", "Время", "R", "S", "T", "U", "V", "W", "Violet", "Blue", "Green", "Yellow", "Orange", "Red", "Температура", "Широта", "Долгота", "Точность местоположения(м)"});
        simpleConnection = new SimpleConnection(MeasuringActivity1.this, bleName, bleCallbacks());

        locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                accuracy = location.getAccuracy();
            }
        };
        myLocation = new MyLocation();

        saveButton.setOnClickListener(v -> {

            DirectoryPickerDialog directoryPickerDialog = new DirectoryPickerDialog(this,
                    () -> {

                    },
                    files -> {

                        path = files[0].getPath();
                        String csv = (path + "/Spectro_" + csvDate + "_" + csvTime + ".csv");
                        CSVWriter writer = null;
                        try {
                            writer = new CSVWriter(new FileWriter(csv));
                            writer.writeAll(csvData);
                            writer.close();
                        } catch (Exception e) {
                            Log.e("ERROR", e.toString());
                        }
                        //simpleConnection.ble.write(Constants.RESTART.getBytes());
                        Intent intent = new Intent(MeasuringActivity1.this, SetupMeasurementActivity.class);
                        simpleConnection.ble.disconnect();
                        startActivity(intent);
                    }
            );
            directoryPickerDialog.show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        simpleConnection.ble.disconnect();
    }
}

package miki.spectro;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import miki.spectro.interfaces.BleCallback;
import miki.spectro.utils.SimpleConnection;

public class MeasuringActivity extends AppCompatActivity {
    private String request, bleName, data = "", currentData = "", dateText = "", timeText = "", csvDate = "", csvTime = "";
    private SimpleConnection simpleConnection;
    private final ArrayList<TextView> textView = new ArrayList<>();
    private final String[] names = {"R: ", "S: ", "T: ", "U: ", "V: ", "W: ", "Violet: ", "Blue: ", "Green: ", "Yellow: ", "Orange: ", "Red: ", "Temp: "};
    private String[] data_for_file;
    private ArrayList<String> readyData = new ArrayList<>();
    private MyLocation myLocation;
    private MyLocation.LocationResult locationResult;
    private final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private final DateFormat csvDateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
    private final DateFormat csvTimeFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
    private double longitude = 0, latitude = 0, accuracy = 0;
    private Date currentDate;
    private final ArrayList<String[]> csvData = new ArrayList<>();
    private boolean flag = false;
    private int currentDataId = 1, numberOfMeasure = 1, dataCount = 25;

    private BleCallback bleCallbacks() {
        return new BleCallback() {

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

            @SuppressLint("DefaultLocale")
            @Override
            public void onBleCharacteristicChange(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onBleCharacteristicChange(gatt, characteristic);
                data_for_file = new String[19];
                currentData = characteristic.getStringValue(0);
                data += currentData;
                if (data.endsWith(";") && flag) {
                    data = data.substring(0, data.length() - 1);
                    readyData.addAll(Arrays.asList(data.split("/")));
                    if (readyData.size() > 10) {
                        if (currentDataId == dataCount+1) {
                            flag = false;
                            currentDataId = 1;
                            numberOfMeasure++;
                            runOnUiThread(() -> Toast.makeText(MeasuringActivity.this, "Измерение завершено!", Toast.LENGTH_LONG).show());
                            Arrays.fill(data_for_file, " ");
                            csvData.add(data_for_file);
                        } else {
                            myLocation.getLocation(MeasuringActivity.this, locationResult);
                            currentDate = new Date();
                            dateText = dateFormat.format(currentDate);
                            timeText = timeFormat.format(currentDate);
                            csvDate = csvDateFormat.format(currentDate);
                            csvTime = csvTimeFormat.format(currentDate);
                            textView.get(13).setText(String.format("Номер эксперимента: %d", numberOfMeasure));
                            textView.get(14).setText(String.format("Номер замера: %d", currentDataId));
                            data_for_file[0] = String.valueOf(numberOfMeasure);
                            data_for_file[1] = dateText;
                            data_for_file[2] = timeText;
                            for (int i = 0; i < textView.size()-2; i++) {
                                textView.get(i).setText(String.format("%s%s", names[i], readyData.get(i)));
                                data_for_file[i + 3] = readyData.get(i);
                            }
                            data_for_file[16] = String.valueOf(latitude);
                            data_for_file[17] = String.valueOf(longitude);
                            data_for_file[18] = String.valueOf(accuracy);
                            if (!(data_for_file[17].equals("0.0") || data_for_file[5].equals(""))) {
                                csvData.add(data_for_file);
                                Log.i("TAG", currentDataId + " MeasuringActivity1: " + Arrays.toString(data_for_file));
                                currentDataId++;
                            }
                            data = "";
                            readyData = new ArrayList<>();
                        }
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
        setContentView(R.layout.measuring_layout);
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
        textView.add(findViewById(R.id.textMeasureNumber));
        textView.add(findViewById(R.id.textMeasureCount));
        Button saveButton = findViewById(R.id.save_button);
        Button reloadButton = findViewById(R.id.reload_button);
        Button startButton = findViewById(R.id.start_measure_button);

        request = getIntent().getStringExtra("request");
        bleName = getIntent().getStringExtra("bleName");
        csvData.add(new String[]{"id", "Дата", "Время", "R", "S", "T", "U", "V", "W", "Violet", "Blue", "Green", "Yellow", "Orange", "Red", "Температура", "Широта", "Долгота", "Точность местоположения(м)"});
        simpleConnection = new SimpleConnection(MeasuringActivity.this, bleName, bleCallbacks());

        locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                try {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    accuracy = location.getAccuracy();
                } catch (Exception e) {
                    Log.e("ERROR", e.toString());
                }
            }
        };
        myLocation = new MyLocation();

        startButton.setOnClickListener(v -> {
            flag = true;
        });

        saveButton.setOnClickListener(v -> {
            if (currentDataId != 0) {
                Toast.makeText(MeasuringActivity.this, "Измерение не завершено!", Toast.LENGTH_LONG).show();
            } else {
                flag = false;
                simpleConnection.ble.disconnect();
                writeFile();
            }
        });

        reloadButton.setOnClickListener(v -> {
            flag = false;
            simpleConnection.ble.disconnect();
            simpleConnection = new SimpleConnection(MeasuringActivity.this, bleName, bleCallbacks());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void writeFile() {
        try {
            File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Spectro_" + csvDate + "_" + csvTime + ".csv");
            grantUriPermission(getPackageName(), Uri.fromFile(outputFile), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            OutputStream outputStream = getContentResolver().openOutputStream(Uri.fromFile(outputFile));
            CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream));
            writer.writeAll(csvData);
            writer.close();
            finish();
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
        }
    }
}

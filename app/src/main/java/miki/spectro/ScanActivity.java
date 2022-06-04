package miki.spectro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import miki.spectro.adapters.BasicList;
import miki.spectro.models.BluetoothLE;
import miki.spectro.utils.BluetoothLEHelper;
import miki.spectro.utils.Constants;
import miki.spectro.utils.Permissions;


public class ScanActivity extends AppCompatActivity {
    private BluetoothLEHelper ble;
    private AlertDialog dAlert;
    private ListView listBle;
    private Button btnScan;

    private AlertDialog setDialogInfo(String title, String message, boolean btnVisible){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_standart, null);
        TextView btnNeutral = view.findViewById(R.id.btnNeutral);
        TextView txtTitle   = view.findViewById(R.id.txtTitle);
        TextView txtMessage = view.findViewById(R.id.txtMessage);
        txtTitle.setText(title);
        txtMessage.setText(message);
        if(btnVisible){
            btnNeutral.setVisibility(View.VISIBLE);
        }else{
            btnNeutral.setVisibility(View.GONE);
        }
        btnNeutral.setOnClickListener(view1 -> dAlert.dismiss());
        builder.setView(view);
        return builder.create();
    }

    @SuppressLint("MissingPermission")
    private void setList(){
        if(ble.getListDevices().size() > 0){
            ArrayList<BluetoothLE> aBleAvailable = new ArrayList<>(ble.getListDevices());
            BasicList mAdapter = new BasicList(this, R.layout.simple_row_list, aBleAvailable) {
                @Override
                public void onItem(Object item, View view, int position) {
                    TextView txtName = view.findViewById(R.id.txtText);
                    String aux = ((BluetoothLE) item).getName() + "    " + ((BluetoothLE) item).getMacAddress();
                    txtName.setText(aux);
                }
            };
            listBle.setAdapter(mAdapter);
            listBle.setOnItemClickListener((parent, view, position, id) -> {
                BluetoothLE  itemValue = (BluetoothLE) listBle.getItemAtPosition(position);
                Intent intent = new Intent(ScanActivity.this, MeasuringMenuActivity.class);
                intent.putExtra("bleName", itemValue.getDevice().getName());
                startActivity(intent);
            });
        }else{
            dAlert = setDialogInfo(getResources().getString(R.string.ups), getResources().getString(R.string.not_find), true);
            dAlert.show();
        }
    }

    private void scanCollars(){
        if(!ble.isScanning()) {
            dAlert = setDialogInfo(getResources().getString(R.string.scan_in_progress), getResources().getString(R.string.loading), false);
            dAlert.show();
            Handler mHandler = new Handler();
            ble.scanLeDevice(true);
            mHandler.postDelayed(() -> {
                dAlert.dismiss();
                setList();
            },ble.getScanPeriod());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
        if(Permissions.permissionGranted(ScanActivity.this)) {
            Log.i("TAG", "GRANTED");
            ble = new BluetoothLEHelper(this);
            listBle  = findViewById(R.id.listBle);
            btnScan  = findViewById(R.id.scanBle);
            btnScan.setOnClickListener(v -> {
                if(Permissions.isReadyForScan(ScanActivity.this)){
                    scanCollars();
                }else{
                    Toast.makeText(ScanActivity.this, getResources().getString(R.string.acception), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            Permissions.requestPermission(ScanActivity.this);
        }
    }
}

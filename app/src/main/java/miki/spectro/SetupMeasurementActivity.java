package miki.spectro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import miki.spectro.utils.Constants;

public class SetupMeasurementActivity extends AppCompatActivity {
    private Spinner measurement_mode_spinner, gain_spinner;
    private EditText time_text;
    private Button send_config_button;
    private String[] gain = {"default", "1x", "3.7x", "16x", "64x"};
    private String[] mode;
    private String request;
    private int selected_mode, selected_gain, selected_time;
    private String bleName;

    public void createDropDownList(Spinner spinner, String[] data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public int select_position(int position){
        if(position == 0){
            return  3;
        }else {
            return position - 1;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_layout);
        mode = new String[]{"default", getString(R.string.mode1), getString(R.string.mode2), getString(R.string.mode3), getString(R.string.mode4)};
        bleName = getIntent().getStringExtra("bleName");
        measurement_mode_spinner = findViewById(R.id.measurement_mode);
        gain_spinner = findViewById(R.id.gain);
        time_text = findViewById(R.id.time);
        send_config_button = findViewById(R.id.send_config_button);
        createDropDownList(gain_spinner, gain);
        createDropDownList(measurement_mode_spinner, mode);
        time_text.setText(Constants.TIME);

        measurement_mode_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_mode = select_position(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        gain_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_gain = select_position(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        send_config_button.setOnClickListener(view -> {
            try {
                selected_time = Integer.parseInt(time_text.getText().toString());
                if(!((selected_time < 256) && (selected_time > -1))){
                    Toast.makeText(SetupMeasurementActivity.this, getResources().getString(R.string.set_number), Toast.LENGTH_LONG).show();
                }else{
                    request ="{mode:" + selected_mode + ",gain:" + selected_gain + ",time:" + selected_time + "}/";
                    Intent intent = new Intent(SetupMeasurementActivity.this, MeasuringActivity.class);
                    intent.putExtra("request", request);
                    intent.putExtra("bleName", bleName);
                    startActivity(intent);
                }
            }catch (Exception e){
                Toast.makeText(SetupMeasurementActivity.this, getResources().getString(R.string.set_digits), Toast.LENGTH_LONG).show();
            }
        });
    }
}
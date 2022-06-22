package miki.spectro.utils;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
import miki.spectro.R;

public class NavigationLayout extends RelativeLayout
{
    public Spinner measurement_mode_spinner, gain_spinner;
    public EditText time_text;
    public Button send_config_button;
    public String[] gain = {"default", "1x", "3.7x", "16x", "64x"};
    public String[] mode;
    public String request = "{mode:" + 3 + ",gain:" + 3 + ",time:" + 50 + "}/";
    public int selected_mode, selected_gain, selected_time;
    public Context context;

    public void createDropDownList(Spinner spinner, String[] data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, data);
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

    public String getRequest() {
        return request;
    }

    public NavigationLayout(Context context, RelativeLayout parent)
    {
        super(context);
        initView(context,parent);
    }

    public void initView(final Context cont, RelativeLayout parent)
    {
        context = cont;
        View view= LayoutInflater.from(context).inflate(R.layout.view_drawer_layout,parent,true);
        measurement_mode_spinner = view.findViewById(R.id.measurement_mode);
        gain_spinner = view.findViewById(R.id.gain);
        time_text = view.findViewById(R.id.time);
        send_config_button = view.findViewById(R.id.send_config_button);
        mode = new String[]{"default", getResources().getString(R.string.mode1), getResources().getString(R.string.mode2), getResources().getString(R.string.mode3), getResources().getString(R.string.mode4)};
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

        send_config_button.setOnClickListener(v -> {
            try {
                selected_time = Integer.parseInt(time_text.getText().toString());
                if(!((selected_time < 256) && (selected_time > -1))){
                    Toast.makeText(context, getResources().getString(R.string.set_number), Toast.LENGTH_LONG).show();
                }else{
                    request = "{mode:" + selected_mode + ",gain:" + selected_gain + ",time:" + selected_time + "}/";
                }
            }catch (Exception e){
                Toast.makeText(context, getResources().getString(R.string.set_digits), Toast.LENGTH_LONG).show();
            }
        });


    }
}

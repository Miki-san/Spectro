package miki.spectro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MeasuringMenuActivity extends AppCompatActivity {
    private Button simple_measurement_button, autoset_button;
    private String bleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measuring_menu_layout);
        bleName = getIntent().getStringExtra("bleName");
        simple_measurement_button = findViewById(R.id.simple_measuring_button);
        autoset_button = findViewById(R.id.autoset_button);

        simple_measurement_button.setOnClickListener(view -> {
            Intent intent = new Intent(MeasuringMenuActivity.this, SetupMeasurementActivity.class);
            intent.putExtra("bleName", bleName);
            startActivity(intent);
        });

        autoset_button.setOnClickListener(view -> {
            startActivity(new Intent(MeasuringMenuActivity.this, AutosetActivity.class));
        });
    }
}

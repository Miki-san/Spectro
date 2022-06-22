package miki.spectro.old;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.jjoe64.graphview.GraphView;

import miki.spectro.R;

public class MeasuringActivity_old extends AppCompatActivity {
    private GraphView graphView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measuring_layout_old);
        graphView = findViewById(R.id.graph);
        graphView.getViewport().setScalable(true);
        graphView.getViewport().setScrollable(true);

    }
}

package miki.spectro;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;

public class MeasuringActivity extends AppCompatActivity {
    public GraphView graphView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measuring_layout);
        graphView = findViewById(R.id.graph);
        graphView.getViewport().setScalable(true);
        graphView.getViewport().setScrollable(true);

    }
}

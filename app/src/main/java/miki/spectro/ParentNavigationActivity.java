package miki.spectro;

import android.widget.RelativeLayout;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;

import miki.spectro.utils.NavigationLayout;

public class ParentNavigationActivity extends AppCompatActivity {
    NavigationLayout navigationLayout;
    RelativeLayout left_drawer;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        setupMenu();
    }

    public void setupMenu()
    {
        left_drawer= findViewById(R.id.left_drawer);
        navigationLayout = new NavigationLayout(getApplicationContext(),left_drawer);
        left_drawer.addView(navigationLayout);
    }
}
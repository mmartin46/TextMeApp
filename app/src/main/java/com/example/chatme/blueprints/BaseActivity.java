package com.example.chatme.blueprints;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.chatme.clickables.Input;

import java.util.HashMap;
import java.util.Map;

/* Creates a class with an initializing method
 for a Map of inputViews and a Map of inputButtons
  Make sure you use initializeAssets() for your onCreate() method.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public Map<Input.WhichText, TextView> inputViews;
    public Map<Input.WhichButton, AppCompatButton> inputButtons;

    public void initializeAssets() {
        initializeInputViews();
        initializeInputButtons();
    }

    public abstract void initializeInputViews();

    public abstract void initializeInputButtons();

    // Checks if a button has been clicked.
    public abstract void buttonClickedListener();
}

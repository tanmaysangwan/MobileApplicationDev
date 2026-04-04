package com.example.currencycoverter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final double INR_PER_USD = 92.0;
    private static final double INR_PER_EUR = 106.0;
    private static final double INR_PER_JPY = 0.58;

    private final ActivityResultLauncher<Intent> openSettings = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> recreate());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.applySavedNightMode(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.topToolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                openSettings.launch(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        View mainContent = findViewById(R.id.main);
        mainContent.setAlpha(0f);
        mainContent.setTranslationY(24f);
        mainContent.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(380)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        String[] currencies = {"INR", "USD", "JPY", "EUR"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinnerFrom = findViewById(R.id.spinnerFromCurrency);
        Spinner spinnerTo = findViewById(R.id.spinnerToCurrency);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        EditText editTextAmount = findViewById(R.id.editTextAmount);
        TextView textViewResult = findViewById(R.id.textViewResult);
        Button buttonConvert = findViewById(R.id.buttonConvert);

        buttonConvert.setOnClickListener(v -> {
            String amountText = editTextAmount.getText().toString().trim();
            if (amountText.isEmpty()) {
                textViewResult.setText("Enter an amount.");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException e) {
                textViewResult.setText("Enter a valid number.");
                return;
            }

            String from = (String) spinnerFrom.getSelectedItem();
            String to = (String) spinnerTo.getSelectedItem();

            double result;
            if (from.equals(to)) {
                result = amount;
            } else {
                double amountInInr = amount * inrPerOneUnit(from);
                result = amountInInr / inrPerOneUnit(to);
            }

            String summary = formatAmount(amount) + " " + from + " = "
                    + formatAmount(result) + " " + to;
            String rate = buildRateLine(from, to);
            String updated = "Last updated: March 2026";

            textViewResult.setText(summary + "\n" + rate + "\n" + updated);
        });
    }

    /** How many INR one unit of {@code currency} is worth (INR is base). */
    private static double inrPerOneUnit(String currency) {
        switch (currency) {
            case "INR":
                return 1.0;
            case "USD":
                return INR_PER_USD;
            case "EUR":
                return INR_PER_EUR;
            case "JPY":
                return INR_PER_JPY;
            default:
                return 1.0;
        }
    }

    private static String formatAmount(double value) {
        if (value == (long) value) {
            return String.format(Locale.US, "%d", (long) value);
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private static String buildRateLine(String from, String to) {
        if ("INR".equals(from) && "INR".equals(to)) {
            return "Rate: 1 INR = 1 INR";
        }
        String code = !"INR".equals(to) ? to : from;
        double inr = inrPerOneUnit(code);
        String rateStr = (inr == (long) inr)
                ? String.format(Locale.US, "%d", (long) inr)
                : String.format(Locale.US, "%.2f", inr);
        return "Rate: 1 " + code + " = " + rateStr + " INR";
    }
}

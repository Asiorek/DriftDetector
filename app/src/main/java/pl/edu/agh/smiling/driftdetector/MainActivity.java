package pl.edu.agh.smiling.driftdetector;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import pl.edu.agh.smiling.driftdetector.rest.ServiceGenerator;
import pl.edu.agh.smiling.driftdetector.rest.SmilingClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getSimpleName();

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text_from_server);

        Button buttonStart = (Button) findViewById(R.id.button_start);
        Button buttonInput = (Button) findViewById(R.id.button_input);
        Button buttonRead = (Button) findViewById(R.id.button_read);

        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                read("gender_phone.csv");
            }
        });

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                DriftDetector driftDetector =

                for(int i = 0; i < 10; i++){

                    startStreaming(String.valueOf(i));
                }
            }
        });

        buttonInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                input();
            }
        });
    }

    private void read(String filename) {
        SmilingClient client = ServiceGenerator.getClient();
//
        Call<String> call = client.read(filename);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    textView.setText(response.body());
                } else {
                    textView.setText("NO TEXT FROM SERVER");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                if (t.toString().contains("ConnectException")) {
                    Snackbar.make(findViewById(R.id.activity_main), "FAILURE", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startStreaming(String line) {
        SmilingClient client = ServiceGenerator.getClient();
//
        Call<String> call = client.startStreaming(line);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    textView.setText(response.body());
                } else {
                    textView.setText("NO TEXT FROM SERVER");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                if (t.toString().contains("ConnectException")) {
                    Snackbar.make(findViewById(R.id.activity_main), "FAILURE", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void input() {
        SmilingClient client = ServiceGenerator.getClient();
//
        Call<String> call = client.input("result.csv");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    textView.setText(response.body());
                } else {
                    textView.setText("NO TEXT FROM SERVER");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                if (t.toString().contains("ConnectException")) {
                    Snackbar.make(findViewById(R.id.activity_main), "FAILURE", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startStream() {
        SmilingClient client = ServiceGenerator.getClient();
//
        Call<String> call = client.startStream();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    textView.setText(response.body());
                } else {
                    textView.setText("NO TEXT FROM SERVER");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                if (t.toString().contains("ConnectException")) {
                    Snackbar.make(findViewById(R.id.activity_main), "FAILURE", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}

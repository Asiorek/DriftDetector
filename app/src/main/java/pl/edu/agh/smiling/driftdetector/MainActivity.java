package pl.edu.agh.smiling.driftdetector;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.kaazing.net.sse.SseEventReader;
import org.kaazing.net.sse.SseEventSource;
import org.kaazing.net.sse.SseEventSourceFactory;
import org.kaazing.net.sse.SseEventType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import pl.edu.agh.smiling.driftdetector.rest.ServiceGenerator;
import pl.edu.agh.smiling.driftdetector.rest.SmilingClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import smile.stream.DriftDetector;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.text_from_server)
    TextView textView;
    @BindView(R.id.textViewNumberOfDrifts)
    TextView textViewNumberOfDrifts;

    @BindView(R.id.spinner)
    Spinner spinner;

    @BindView(R.id.button_stop)
    Button buttonStop;
    @BindView(R.id.button_start)
    Button buttonStart;

    @BindView(R.id.button_dataset)
    Button buttonDataset;

    //TODO: set localhost
    private final static String host = "http://192.168.43.203:5000/";
    private final static String startStream = "subscribe";
    private SseEventSource sseEventSource;

    private AsyncTask asyncTask;

    private DriftDetector driftDetector;

    int iterator = 0;

    boolean driftDetected = false;
    private int numberOfDrifts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        spinner.setOnItemSelectedListener(this);

        List<String> datasets = new ArrayList<>();
        datasets.add("1CDT");
        datasets.add("5CVT");
        datasets.add("SEA");
        datasets.add("Airlines");
        datasets.add("Electricity");
        datasets.add("TalkingMobile");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, datasets);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);

        buttonDataset.setOnClickListener(view -> {
            SmilingClient client = ServiceGenerator.getClient();

            String filename = spinner.getSelectedItem().toString().toLowerCase() + ".csv";
            Call<ResponseBody> call = client.chooseDataset(filename);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "onResponse: SUCCESS: " + response.body());
                        Toast.makeText(getApplication(), response.message() , Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "onResponse: FAILURE");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t);
                }
            });
        });

        buttonStart.setOnClickListener(view -> {
            String uri = host + startStream;
            Log.d(TAG, "onCreate: URI: " + uri);
            buttonDataset.setVisibility(View.GONE);
            asyncTask = new HttpRequest().execute(uri);
        });

        buttonStop.setOnClickListener(view -> {
            buttonDataset.setVisibility(View.VISIBLE);
            asyncTask.cancel(true);
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();

        Toast.makeText(parent.getContext(), "Selected dataset: " + item, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public class HttpRequest extends AsyncTask<String, String, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                final SseEventSourceFactory sseEventSourceFactory = SseEventSourceFactory.createEventSourceFactory();
                sseEventSource = sseEventSourceFactory.createEventSource(new URI(params[0]));
                sseEventSource.connect();

                final SseEventReader sseEventReader = sseEventSource.getEventReader();

                SseEventType type = sseEventReader.next();

                while (type != SseEventType.EOS) {

                    Log.d(TAG, "onClick: new event");
                    if (type != null && type.equals(SseEventType.DATA)) {
                        CharSequence data = sseEventReader.getData();

                        Log.d(TAG, "onClick: " + data.toString());

                        if (iterator == 0) {
                            Log.d(TAG, "doInBackground: Drift Detector started!");
                            //Initialize Drift Detector for the first element
                            driftDetector = DriftDetector.getInstance(200, "knn", data.toString());
                        } else {
                            Log.d(TAG, "doInBackground: Drift Detector Update!");
                            if (driftDetected) {
                                //check if drift is detected and update a TextView with number od drifts
                                ++numberOfDrifts;
                                Toast.makeText(MainActivity.this, "DRIFT DETECTED on " + iterator, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "doInBackground: DRIFT DETECTED on " + iterator);
                                driftDetected = false;
                            } else {
                                //update drift detector
                                driftDetected = driftDetector.update(data.toString(), 0, iterator);
                            }
                        }
                        publishProgress(new String[]{data.toString(), String.valueOf(driftDetector.getNumberOfDrifts())});
                        iterator++;
                    } else {
                        Log.d(TAG, "onClick: type null or not data: " + type);
                        publishProgress("empty", String.valueOf(numberOfDrifts));
                    }
                    type = sseEventReader.next();
//                    Thread.sleep(20);
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... data) {
            textView.setText(data[0]);
            textViewNumberOfDrifts.setText(data[1]);
            super.onProgressUpdate(data);
        }

        @Override
        protected void onCancelled() {
            try {
                sseEventSource.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.onCancelled();
        }
    }
}

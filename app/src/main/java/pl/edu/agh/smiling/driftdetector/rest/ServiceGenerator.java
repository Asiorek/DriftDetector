package pl.edu.agh.smiling.driftdetector.rest;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by joanna on 1/5/17.
 */

public class ServiceGenerator {

    public static final String API_BASE_URL = "http://192.168.43.203:5000/";

    private static final String TAG = ServiceGenerator.class.getSimpleName();

    private SmilingClient ekatalogClient;
    private Retrofit retrofit;

    private ServiceGenerator() {

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                //TODO: 2016-12-14 Consider setting timeouts below to prevent SocketTimeoutException in the future.
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .addNetworkInterceptor(logging)
                .build();

        retrofit = builder.client(client).build();
        ekatalogClient = retrofit.create(SmilingClient.class);
    }

    public static Retrofit getRetrofit() {
        return ServiceGenerator.getInstance().retrofit;
    }

    private static ServiceGenerator getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static SmilingClient getClient() {
        return ServiceGenerator.getInstance().ekatalogClient;
    }

    private static class SingletonHolder {
        private static final ServiceGenerator INSTANCE = new ServiceGenerator();
    }
}

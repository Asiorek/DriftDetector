package pl.edu.agh.smiling.driftdetector.rest;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by joanna on 12/3/16.
 *
 */
public interface SmilingClient {

    @GET("/")
    Call<String> hello();

    @GET("/stream")
    Call<String> start();

    @GET("/streamStart")
    Call<String> startStream();

    @GET("/streaming")
    Call<String> startStreaming(@Query("line") String line);

    @GET("/read")
    Call<String> read(@Query("filename") String filename);

    @GET("/input")
    Call<String> input(@Query("filename") String filename);

    @GET("/json")
    Call<String> json();
}

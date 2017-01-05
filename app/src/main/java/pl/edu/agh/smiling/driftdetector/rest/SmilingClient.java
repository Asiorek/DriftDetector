package pl.edu.agh.smiling.driftdetector.rest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by joanna on 1/5/17.
 */
public interface SmilingClient {

    @GET("dataset")
    Call<ResponseBody> chooseDataset(@Query("filename") String filename);
}

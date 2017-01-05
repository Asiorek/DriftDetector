package pl.edu.agh.smiling.driftdetector.rest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author joanna
 * @since 12/1/16
 * An interface, which enable to choose,
 * which dataset the user want to be streamed from server
 */
public interface SmilingClient {

    @GET("dataset")
    Call<ResponseBody> chooseDataset(@Query("filename") String filename);
}

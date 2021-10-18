package model;

import com.google.gson.annotations.SerializedName;

public class ApiGroup {
    @SerializedName("name")
    public String name;
    @SerializedName("base_url")
    public String baseUrl;
    @SerializedName("api")
    public ApiHolder[] holders;
}

package model;

import com.google.gson.annotations.SerializedName;

public class ApiHolder {
    @SerializedName("id")
    public int id;
    @SerializedName("name")
    public String name;
    @SerializedName("route")
    public String route;
    @SerializedName("path")
    public String path;
    @SerializedName("method")
    public String method;
    @SerializedName("queries")
    public String[] queries;
    @SerializedName("fields")
    public String[] fields;
    @SerializedName("headers")
    public String[] headers;
    @SerializedName("type")
    public String type;
    @SerializedName("base_url")
    public String baseUrl;

    public boolean isPost() {
        return method != null && method.equals("post");
    }

    public boolean isPut() {
        return method != null && method.equals("put");
    }
}

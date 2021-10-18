package task;

public interface Callback<E> {
    void onResponse(E data, String message) throws Exception;
}

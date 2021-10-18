package task;

public interface Action<E> {
    E onRunning();
    void onResponse(E data);
}

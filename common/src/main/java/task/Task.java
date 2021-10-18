package task;

import android.os.AsyncTask;

@SuppressWarnings("unchecked")
public class Task extends AsyncTask<String, Void, Object> {
    private Action action;

    public static void submit(Action action) {
        new Task(action).run();
    }

    private Task(Action action) {
        this.action = action;
    }

    private void run() {
        execute();
    }

    public void onPrepared() {}

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onPrepared();
    }

    @Override
    protected Object doInBackground(String... params) {
        return action.onRunning();
    }

    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        action.onResponse(result);
    }
}

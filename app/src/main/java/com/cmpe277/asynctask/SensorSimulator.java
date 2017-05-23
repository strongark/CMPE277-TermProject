package com.cmpe277.asynctask;

import android.os.AsyncTask;

import java.util.Random;

/**
 * Created by tranpham on 3/18/17.
 */

public class SensorSimulator extends AsyncTask<Integer,Integer,Integer> {

    public interface AsyncResponse {
        void progressUpdate(Integer...values);
        void postExecute();
        void cancel();
    }

    AsyncResponse delegateRes=null;

    SensorSimulator(AsyncResponse response){
        delegateRes=response;
    }
    @Override
    protected Integer doInBackground(Integer... params) {

        Random rdTemp = new Random(Utils.RANDOM_SEED);
        Random rdHumidity = new Random(Utils.RANDOM_SEED+7);
        Random rdLight = new Random(Utils.RANDOM_SEED+13);
        for (Integer loop_count:params){
            for (int i=0;i<loop_count&&!isCancelled();i++) {
                Utils.sleepForInSecs(3);
                ///randomize sensor value
                Integer humidity=rdTemp.nextInt(100);
                Integer temperature=rdHumidity.nextInt(70);
                Integer light=rdLight.nextInt(1000);
                publishProgress(i+1,temperature,humidity,light);
            }
        }
        return 1;
    }

    @Override
    protected void onPreExecute() {
//        _sensorDriver.toggleButton(false,true);
//        _logView.setText("INITIALIZING....");
//        _logView.append("-------------------------------\n");

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        delegateRes.progressUpdate(values[0],values[1],values[2],values[3]);
    }

    @Override
    protected void onPostExecute(Integer integer) {
        //toggleButton(true,false);
//        _sensorDriver.toggleButton(true,false);
//        _logView.append("-------------------------------\n");
//        _logView.append("COMPLETE");
        delegateRes.postExecute();
    }

    @Override
    protected void onCancelled() {
        delegateRes.cancel();
//        _logView.append("-------------------------------\n");
//        _logView.append("CANCEL");
    }
}

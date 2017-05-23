package com.cmpe277.asynctask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class SensorDriverActivity extends Activity {

    SensorSimulator sensor=null;
    static final String TAG="MySensorDriverActiviy";
    Integer loop_count=20;
    Integer outputCount=0;
    private SensorManager mSensorManager;
    private Sensor mLightSensor;
    private Sensor mTempSensor;
    private Sensor mHumiditySensor;
    float max_lux=7F;

    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Integer sensorType=event.sensor.getType();
            float sensorValue=event.values[0];
            switch (sensorType){
                case Sensor.TYPE_LIGHT:
                    Log.d(TAG, "Ambient light: "+sensorValue);
                    updateStatus(outputCount++,0,0,(int)sensorValue);
                    break;
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    Log.d(TAG, "Ambient temperature: "+sensorValue);
                    updateStatus(outputCount++,(int)sensorValue,0,0);
                    break;
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    Log.d(TAG, "Humidity: "+sensorValue);
                    updateStatus(outputCount++,0,(int)sensorValue,0);
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.mnu_setting:
                //open setting activity
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_driver);
        toggleButton(true,false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Log.d(TAG, "onCreate: initialize sensor");
        //initiate sensor
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mTempSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mHumiditySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        if(mLightSensor==null)
            Toast.makeText(this,"Light sensor not found",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAllSensors();
    }

    public void onListenSensor(View view){
        Log.d(TAG, "onListenSensor: start");
        //read max lux value from preference


        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        max_lux =  Float.parseFloat(shared.getString("max_lux", "7F"));

        if(mLightSensor!=null)
        {
            mSensorManager.registerListener(sensorEventListener,mLightSensor
                    ,SensorManager.SENSOR_DELAY_NORMAL);
            toggleButton(false,true);
        }

        if(mTempSensor!=null){
            mSensorManager.registerListener(sensorEventListener,mTempSensor
                    , SensorManager.SENSOR_DELAY_NORMAL);
        }

        if(mHumiditySensor!=null){
            mSensorManager.registerListener(sensorEventListener,mHumiditySensor
                    , SensorManager.SENSOR_DELAY_NORMAL);
        }


    }

    public void onStopListenSensor(View view){
        stopAllSensors();
    }

    private void stopAllSensors()
    {
        Log.d(TAG, "onStopListenSensor: stop");
        if(mLightSensor!=null)
        {
            mSensorManager.unregisterListener(sensorEventListener,mLightSensor);
        }

        if(mTempSensor!=null){
            mSensorManager.unregisterListener(sensorEventListener,mTempSensor);
        }

        if(mHumiditySensor!=null){
            mSensorManager.unregisterListener(sensorEventListener,mHumiditySensor);
        }
        toggleButton(true,false);
    }
    public void toggleButton(boolean listenEnabled, boolean stopListen) {
        Button btnListen = (Button) findViewById(R.id.btn_listen);
        btnListen.setEnabled(listenEnabled);

        Button btnStopListen = (Button) findViewById(R.id.btn_stopListen);
        btnStopListen.setEnabled(stopListen);
    }


    public void updateStatus(final Integer count, final Integer temperature
                            ,final Integer humidity, final Integer light)
    {
        final TextView _humidView=(TextView)findViewById(R.id.txt_humidity);
        final TextView _tempView=(TextView)findViewById(R.id.txt_temperature);
        final TextView _activityView=(TextView)findViewById(R.id.txt_activities);
        final TextView _loopView=(TextView)findViewById(R.id.txt_sensors);
        final TextView _logView= (TextView)findViewById(R.id.txt_log);

        Handler handler =new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(humidity>0)
                    _humidView.setText(humidity.toString());
                if(temperature>0)
                    _tempView.setText(temperature.toString());
                if(light>0)
                    _activityView.setText(light.toString());
                Integer remain_loop=count;
                _loopView.setText(remain_loop.toString());
                _logView.append("Output "+count+"\n");
                _logView.append("Temperature: "+temperature+" F"+"\n");
                _logView.append("Humidity: "+humidity+"%"+"\n");
                _logView.append("Light: "+light+"\n");
                _logView.append("-------------------------------\n");
                _logView.setMovementMethod(new ScrollingMovementMethod());
                /*invoke the API call to control the light*/

                Integer lightIntensity = (int)((max_lux-light)*255/max_lux);
                Log.d(TAG, "Adjust light to: "+lightIntensity);
                adjustLight(lightIntensity);
            }
        });
    }

    public void adjustLight(Integer intensity)
    {
        if(intensity>255){
            intensity=255;
            Log.d(TAG, "adjust intensity");
        }

        if(intensity<0)
        {
            intensity=0;
            Log.d(TAG, "adjust intensity");
        }

        Log.d(TAG, "light intensity: "+intensity);

        HttpClient client = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("http://192.168.4.1/led/"+intensity);
        HttpResponse response= null;

        try {
            response = client.execute(new HttpHost("192.168.4.1",80),httpget);
        } catch (IOException e) {
            e.printStackTrace();
            stopAllSensors();
            Toast.makeText(this,"Server not found",Toast.LENGTH_SHORT).show();

        }
        //Log.d(TAG, "adjustLight: "+response.getStatusLine());
    }

    public void onLightOn(View vew){
        adjustLight(255);
    }

    public void onLightOff(View vew){
        adjustLight(0);
    }

}

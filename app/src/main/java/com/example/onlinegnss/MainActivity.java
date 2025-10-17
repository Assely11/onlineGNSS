package com.example.onlinegnss;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.GnssMeasurementsEvent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

//import org.postgresql.*;

public class MainActivity extends AppCompatActivity {
    private String strConnect;
    private String strDisconnect;
    private String strStart;
    private String strStop;

    private TextView gnssTextView;
    private TextView recordButton;
    private TextView connectButton;
    private TextView logView;

    private Boolean isConnect=false;
    private Boolean isRecord=false;

    private SqlExecute sqlExecute=new SqlExecute();

    private String[] permissions=new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    private GnssMeasurementsEvent gnssMeasurementsEvent=null;

    private Handler handler=new Handler();
    private LocationManager locationManager;

    private Drawable enableButton;
    private Drawable unableButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Initialize();
        InitializeButton();
        askPermissions();
    }

    private void Initialize(){
        strConnect=(String) this.getString(R.string.connect);
        strDisconnect=(String) this.getString(R.string.disconnect);
        strStart=(String) this.getString(R.string.start);
        strStop=(String) this.getString(R.string.stop);

        gnssTextView=(TextView) findViewById(R.id.gnssMeasurements);
        recordButton=(TextView) findViewById(R.id.record);
        connectButton=(TextView) findViewById(R.id.connect);

        logView=(TextView)findViewById(R.id.logView);
        logView.setText("[INFO] Application Start");

        connectButton.setEnabled(true);
        recordButton.setEnabled(false);

        enableButton=getDrawable(R.drawable.button);
        unableButton=getDrawable(R.drawable.button2);
//        unableButton=getDrawable(R.drawable.buttonenable);

        connectButton.setBackground(enableButton);
        recordButton.setBackground(unableButton);


        locationManager=(LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this,"Request Localization Permission",Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.registerGnssMeasurementsCallback(gnssmeasurementCallBack);
        handler.post(reloadUI);
    }

    private void InitializeButton(){
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectSwitch();
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordSwitch();
            }
        });
    }

    private void askPermissions(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //Toast.makeText(this,"Request Localization Permission",Toast.LENGTH_LONG).show();
            // return;
            ActivityCompat.requestPermissions(this,permissions,1);
        }
    }
    private static Boolean isCreatTable=false;
    private void connectSwitch(){
        if(!isConnect){

            Thread thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    sqlExecute.connectSql();
                    isCreatTable =sqlExecute.createTable();
                }
            });
            try {
                thread.start();
            }catch (Exception e){
                Log.e("Error",e.toString());
            }
            long startTime=System.currentTimeMillis();
            long nowTime=System.currentTimeMillis();
            while (Math.abs(nowTime-startTime)<500){nowTime=System.currentTimeMillis();}
            // int count=0;
            // while (!sqlExecute.isConnected){count++;if(count>1000000000){break;}}
            // while (thread.isAlive()){break;}
            if(sqlExecute.isConnected){
                connectButton.setText(strDisconnect);
                Toast.makeText(this,"Connect Success",Toast.LENGTH_LONG).show();
                isConnect=true;
                //gnssTextView.setText(sqlExecute.results);
                logView.setText("[INFO] DataBase Connected");
                if(isCreatTable){
                    logView.setText("[INFO] Table Created "+Build.ID);
                    recordButton.setEnabled(true);
                    recordButton.setBackground(enableButton);
                }else {
                    logView.setText("[ERROR] "+sqlExecute.errorLog);
                    //logView.setText("[INFO] Table Connected"+Build.MODEL);
                    recordButton.setEnabled(false);
                    recordButton.setBackground(unableButton);
                    isRecord=false;
                }
            }else {
                recordButton.setEnabled(false);
                recordButton.setBackground(enableButton);
                logView.setText("[ERROR] "+sqlExecute.errorLog);
                isRecord=false;
            }
        }else{
            Thread thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    sqlExecute.disConnectSql();
                }
            });
            try{
                thread.start();
            }catch (Exception e){}
            long startTime=System.currentTimeMillis();
            long nowTime=System.currentTimeMillis();
            while (Math.abs(nowTime-startTime)<100){nowTime=System.currentTimeMillis();}
            if(!sqlExecute.isConnected){
                connectButton.setText(strConnect);
                isConnect=false;
                Toast.makeText(this,"DisConnect Success",Toast.LENGTH_LONG).show();
                logView.setText("[INFO] DataBase DisConnect");
                recordButton.setEnabled(false);
                recordButton.setBackground(unableButton);
                isRecord=false;
            }else{
                logView.setText("[ERROR] "+sqlExecute.errorLog);
            }
        }


    }

    private void recordSwitch(){
        if(!isRecord){
            recordButton.setText(strStop);
            isRecord=true;
            connectButton.setEnabled(false);
            connectButton.setBackground(unableButton);
        }else {
            recordButton.setText(strStart);
            isRecord=false;
            connectButton.setEnabled(true);
            connectButton.setBackground(enableButton);
            logView.setText("[INFO] Stop Record");
        }
    }

    private GnssMeasurementsEvent.Callback gnssmeasurementCallBack=new GnssMeasurementsEvent.Callback() {
        @Override
        public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
            super.onGnssMeasurementsReceived(eventArgs);
            gnssMeasurementsEvent=eventArgs;
            // insert data into database
            if(isRecord){
                Thread thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sqlExecute.insertData(eventArgs);
                    }
                });
                thread.start();
            }
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return super.equals(obj);
        }

        @NonNull
        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString();
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }
    };

    private Runnable reloadUI=new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this,1000);
            if(gnssMeasurementsEvent!=null){
                gnssTextView.setText(gnssMeasurementsEvent.toString());
            }

            if(isRecord){
                if(sqlExecute.errorLog.length()==0){
                    logView.setText("[INFO] Recording");
                }else {
                    logView.setText("[ERROR] "+sqlExecute.errorLog);
                }
            }
        }
    };
}
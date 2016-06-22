package org.androidtown.lbs.location;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 Program to get location data and send it to server
 Author: Kim Young Song. Kim Ma Ro . Lee Won Sang
 E-mail Address: infall346@gmail.com / maro2345@gamil.com / WonSang12@gmail.com
 Android Term Project
 Last Changed: June 22, 2016
 */


public class MainActivity extends AppCompatActivity {

    RadioGroup departure;
    RadioGroup sit;
    Button start;
    Button stop;

    static int [] enter = new int[15];
    int mistake = 20;  //오차 거리
    Location bus_stop [] = new Location[15];
    Bus myBus = new Bus();
    float []distance = new float[15];

    SharedPreferences sh_Pref;
    SharedPreferences.Editor toEdit;
    String driver_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        departure = (RadioGroup) findViewById(R.id.departure);
        sit = (RadioGroup) findViewById(R.id.sit);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);


        sh_Pref = getSharedPreferences("Driver_ID", MODE_PRIVATE);
        toEdit = sh_Pref.edit();
        driver_id = sh_Pref.getString("Username", "0");

        if (driver_id.equals("0")) {
            toEdit.putString("Username", "2");    //Give ID to bus driver
            toEdit.commit();
            driver_id = sh_Pref.getString("Username", "0");
        }

        register();  //register bus stop


        checkDangerousPermissions();
    }

    private void checkDangerousPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * 위치 정보 확인을 위해 정의한 메소드
     */
    private void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);   //정확도(ACCURACY_COARSE :네트웍, ACCURACY_FINE :GPS)
        String provider = manager.getBestProvider(criteria, true);

        // 위치 정보를 받을 리스너 생성
        GPSListener gpsListener = new GPSListener();
        long minTime = 2000;
        float minDistance = 0;

        try {
            Location lastLocation = manager.getLastKnownLocation(provider);
            if (lastLocation != null) {
                Double latitude = lastLocation.getLatitude();
                Double longitude = lastLocation.getLongitude();

                //    Toast.makeText(getApplicationContext(), "Last Known Location : " + "Latitude : " + latitude + "\nLongitude:" + longitude, Toast.LENGTH_LONG).show();
            }
        } catch(SecurityException ex) {
            ex.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), "위치 확인이 시작되었습니다. 로그를 확인하세요.", Toast.LENGTH_SHORT).show();

    }

    //Register latitude and longitude of bus stop
    public void register()
    {
        for(int i =1; i<=10; i++) {
            bus_stop[i] = new Location("" + i);
            enter[i] = 0;
        }

        bus_stop[1].setLatitude(37.450722);bus_stop[1].setLongitude(127.127486); //새롬

        bus_stop[2].setLatitude(37.450895);bus_stop[2].setLongitude(127.130214);  //가천

        bus_stop[3].setLatitude(37.452220);bus_stop[3].setLongitude(127.131732);  //아름

        bus_stop[4].setLatitude(37.452617);bus_stop[4].setLongitude(127.132457);   //중도

        bus_stop[5].setLatitude(37.452920);bus_stop[5].setLongitude(127.133250);  //중도 - 세종  체크

        bus_stop[6].setLatitude(37.453301);bus_stop[6].setLongitude(127.133947);  //세종

        bus_stop[7].setLatitude(37.454383);bus_stop[7].setLongitude(127.134822);  //긱사

        bus_stop[8].setLatitude(37.452522);bus_stop[8].setLongitude(127.130151);   //아름관 - 창조

        bus_stop[9].setLatitude(37.451941);bus_stop[9].setLongitude(127.128809);   //창조

        bus_stop[10].setLatitude(37.451654);bus_stop[10].setLongitude(127.127514);   //국제

    }

    /**
     * 리스너 클래스 정의
     */
    private class GPSListener implements LocationListener {
        /**
         * 위치 정보가 확인될 때 자동 호출되는 메소드
         */
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            for(int i =1; i<= 10; i++)
                distance[i] = bus_stop[i].distanceTo(location);

            for(int i =1; i<= 10; i++) {
                if (distance[i] <= mistake && enter[i] == 0) {
                    enter[i] = 1;

                    Toast.makeText(getApplicationContext(), "신호 옴 " + i, Toast.LENGTH_SHORT).show();

                    myBus.setLastStation(myBus.getCurrentStation());
                    myBus.setCurrentStation(i);

                    Date d = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");

                    int sitId = sit.getCheckedRadioButtonId();
                    int destId = departure.getCheckedRadioButtonId();

                    if (sitId == 2131492950)  //잔여:1
                        sitId = 1;
                    else                    //만차: 0
                        sitId = 0;

                    if (destId == 2131492946)  //아름관:1
                        destId = 1;
                    else                      //기숙사: 0
                        destId = 0;

                    insertToDatabase(sdf.format(d), String.valueOf(myBus.getLastStaion()), String.valueOf(myBus.getCurrentStation()), driver_id, String.valueOf(sitId), String.valueOf(destId));

                    for(int j=1 ; j<=10 ; j++)
                        if(j!=i)
                            enter[j] = 0;

                }
            }

        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    public void selectSet(View v) {
        int departId = departure.getCheckedRadioButtonId();
        int sitId = sit.getCheckedRadioButtonId();

        if (R.id.none_depart != departId && R.id.none_sit != sitId) {
            if (!stop.hasFocus()) {
                start.setFocusableInTouchMode(true);
                start.requestFocus();
                start.setEnabled(true);
            }
        }
    }

    public void stop(View v) {
        stop.setFocusable(false);
        stop.setEnabled(false);

        departure.check(R.id.none_depart);
        sit.check(R.id.none_sit);

        for (int i = 0; i < departure.getChildCount(); i++) {
            departure.getChildAt(i).setEnabled(true);
            sit.getChildAt(i).setEnabled(true);
        }
    }

    public void start(View v) {
        start.setFocusable(false);
        start.setEnabled(false);

        for (int i = 0; i < departure.getChildCount(); i++) {
            departure.getChildAt(i).setEnabled(false);
        }


        register();
        startLocationService();

        stop.setFocusableInTouchMode(true);
        stop.requestFocus();
        stop.setEnabled(true);
    }


    //Send data to Server
    private void insertToDatabase(String date, String last, String current, String bus_id, String seat, String dest) {

        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this, "Please Wait", null, true, true);

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    String date = (String) params[0];
                    String last = (String) params[1];
                    String current = (String) params[2];
                    String bus_id = (String) params[3];
                    String seat = (String) params[4];
                    String dest = (String) params[5];

                    String link = "http://gachonladybug.esy.es/default.php";
                    String data = URLEncoder.encode("date", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8");
                    data += "&" + URLEncoder.encode("last", "UTF-8") + "=" + URLEncoder.encode(last, "UTF-8");
                    data += "&" + URLEncoder.encode("current", "UTF-8") + "=" + URLEncoder.encode(current, "UTF-8");
                    data += "&" + URLEncoder.encode("bus_id", "UTF-8") + "=" + URLEncoder.encode(bus_id, "UTF-8");
                    data += "&" + URLEncoder.encode("seat", "UTF-8") + "=" + URLEncoder.encode(seat, "UTF-8");
                    data += "&" + URLEncoder.encode("dest", "UTF-8") + "=" + URLEncoder.encode(dest, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    return sb.toString();

                } catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                }

            }
        }

        InsertData task = new InsertData();
        task.execute(date, last, current, bus_id, seat, dest);
    }

}

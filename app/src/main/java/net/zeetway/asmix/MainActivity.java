package net.zeetway.asmix;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_REQUEST_CODE = 102;
    private static MediaPlayer mediaPlayer;
    private static String audioFilePath;
    private static Button startButton;

    private static String downloadHash;

    SQLiteDatabase mydatabase;
    int maxVolume = 100;

    Context context;
    private BroadcastReceiver mDLCompleteReceiver;

    private void downloadFile(String strUrl, String fileName) {
        try {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);

            ByteArrayOutputStream outstream = new ByteArrayOutputStream();

            byte[] byteArray = outstream.toByteArray();

            fos.write(byteArray);
            fos.close();
        } catch(Exception e) {

        }

    }

    private String wget(String action, String data) {
        String result;
        result = "";
        try {
            URL url = new URL("http://192.168.122.91:3333/" + action);

            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            String urlParameters = data;
            connection.setRequestMethod("POST");
            connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
            connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
            connection.setDoOutput(true);
            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
            dStream.writeBytes(urlParameters);
            dStream.flush();
            dStream.close();
            int responseCode = connection.getResponseCode();

            //System.out.println("\nSending 'POST' request to URL : " + url);
            //System.out.println("Post parameters : " + urlParameters);
            //System.out.println("Response Code : " + responseCode);

            final StringBuilder output = new StringBuilder("Request URL " + url);
            output.append(System.getProperty("line.separator") + "Request Parameters " + urlParameters);
            //output.append(System.getProperty("line.separator")  + "Response Code " + responseCode);
            //output.append(System.getProperty("line.separator")  + "Type " + "POST");
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            StringBuilder responseOutput = new StringBuilder();
            //System.out.println("output===============" + br);
            while((line = br.readLine()) != null ) {
                responseOutput.append(line);
            }
            br.close();

            output.append(System.getProperty("line.separator") + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());

            System.out.println(output);

            result = responseOutput.toString();

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String string = bundle.getString("myKey");

            TextView myTextView =
                    (TextView)findViewById(R.id.textView);
            myTextView.setText(string);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                STORAGE_REQUEST_CODE);

        //startButton = (Button) findViewById(R.id.button);

        mydatabase = openOrCreateDatabase("asmix.db",MODE_PRIVATE, null);

        //mydatabase.execSQL("DROP TABLE IF EXISTS task;");
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS task (Time VARCHAR, TaskId VARCHAR, Hash VARCHAR, Days VARCHAR, BeginDate VARCHAR, EndDate VARCHAR, BeginTime VARCHAR, EndTime VARCHAR);");
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS hashname (Hash VARCHAR, Filename VARCHAR);");
        System.out.println(mydatabase.getPath());




        //startButton.setEnabled(false);



        new Thread(new Runnable() {
            public void run() {
                Cursor resultSet = mydatabase.rawQuery("Select * from Task", null);
                System.out.println(resultSet.getCount());

                String id = "3";
                int i = 0;
                while (i<1)
                {
                    long endTime = System.currentTimeMillis() +
                            2*1000;
                    String testTime = Long.toString(System.currentTimeMillis()/1000);
                    while (System.currentTimeMillis() < endTime) {
                        synchronized (this) {
                            try {
                                wait(endTime -
                                        System.currentTimeMillis());
                            } catch (Exception e) {
                            }
                        }
                    }
                    final TextView tv =
                            (TextView)findViewById(R.id.textView);
                    String input = tv.getText().toString();

                    if (input.length() > 0 ) {
                        String action3, data3, response3;
                        action3 = "?task=setStatus";
                        String full_filename = input.substring(input.lastIndexOf("/") + 1);
                        String completed_task_id = full_filename.substring(0, full_filename.indexOf("_"));
                        data3 = "data=<?xml version=\"1.0\" encoding=\"UTF-8\"?><TASKS><ID>" + id + "</ID><TASK><TASK_ID>" + completed_task_id + "</TASK_ID><RUNTIME>__:__:__</RUNTIME><STATUS>COMPLETED</STATUS></TASK></TASKS>";

                        response3 = wget(action3, data3);
                        Message msg = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("myKey", "");
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        //tv.setText("");
                    }
                    String action, data, response, response_tasks, action1, data1, action2, data2, response2, task_id;

                    action = "?task=getStatus";
                    data = "data=<?xml version=\"1.0\" encoding=\"UTF-8\"?><STATUS><ID>" + id + "</ID><DONE /></STATUS>";

                    response = wget(action, data);

                    if (response.indexOf("<STATUS><TASKS/></STATUS>") != -1) {
                        action1 = "?task=getJob";
                        data1 = "data=<?xml version=\"1.0\" encoding=\"UTF-8\"?><STATUS><ID>" + id + "</ID></STATUS>";
                        response_tasks = wget(action1, data1);

                        if (response_tasks.indexOf("<TYPE>PLAYLIST.MUSIC</TYPE>") != -1) {
                            task_id = response_tasks.substring(response_tasks.indexOf("<TASK_ID>") + 9, response_tasks.indexOf("</TASK_ID>"));

                            action2 = "?task=setStatus";
                            data2 = "data=<?xml version=\"1.0\" encoding=\"UTF-8\"?><TASKS><ID>" + id + "</ID><TASK><TASK_ID>" + task_id + "</TASK_ID><RUNTIME>__:__:__</RUNTIME><STATUS>RECEIVED</STATUS></TASK></TASKS>";
                            response2 = wget(action2, data2);

                            final String beginDate = response_tasks.substring(response_tasks.indexOf("<BEGINDATE>") + 11, response_tasks.indexOf("</BEGINDATE>"));
                            final String endDate = response_tasks.substring(response_tasks.indexOf("<ENDDATE>") + 9, response_tasks.indexOf("</ENDDATE>"));

                            final String beginTimeTag = response_tasks.substring(response_tasks.indexOf("<BEGINTIME>") + 11, response_tasks.indexOf("</BEGINTIME>"));
                            final String endTimeTag = response_tasks.substring(response_tasks.indexOf("<ENDTIME>") + 9, response_tasks.indexOf("</ENDTIME>"));

                            final String hash = response_tasks.substring(response_tasks.indexOf("<HASH>") + 6, response_tasks.indexOf("</HASH>"));
                            final String days = response_tasks.substring(response_tasks.indexOf("<DAYS>") + 6, response_tasks.indexOf("</DAYS>"));

                            final String period = response_tasks.substring(response_tasks.indexOf("<PERIOD>") + 8, response_tasks.indexOf("</PERIOD>"));

                            long longperiod = 900;

                            switch (period) {
                                case "12": longperiod = 5*60;
                                    break;
                                case "10": longperiod =6*60;
                                    break;
                                case "8": longperiod =7*60+30;
                                    break;
                                case "6": longperiod =10*60;
                                    break;
                                case "5": longperiod =12*60;
                                    break;
                                case "4": longperiod =15*60;
                                    break;
                                case "3": longperiod =20*60;
                                    break;
                                case "2": longperiod =30*60;
                                    break;
                                case "1": longperiod =60*60;
                                    break;
                                case "0.8": longperiod =60*60 + 15*60;
                                    break;
                                case "0.6": longperiod =60*60 + 40*60;
                                    break;
                                case "0.5": longperiod =120*60;
                                    break;
                                case "0.4": longperiod =120*60 + 30*60;
                                    break;
                                case "0.3": longperiod =180*60 + 20*60;
                                    break;
                                case "0.2": longperiod =5*60*60;
                                    break;
                                case "0.1": longperiod =18*60*60;
                                    break;
                            }

                            final String runtime = "00:00:00";

                            String startTime = "2016-09-09 " + beginTimeTag;
                            String stopTime = "2016-09-09 " + endTimeTag;
                            Date startDate, stopDate;
                            startDate = new Date();
                            stopDate = new Date();

                            try {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

                                startDate = format.parse(startTime);
                                stopDate = format.parse(stopTime);

                            } catch (java.text.ParseException e) {
                                e.printStackTrace();
                            }

                            //Time time1 = new Time();
                            //System.out.println((startDate.getTime()));
                            //System.out.println((stopDate.getTime()));
                            int count = 0;
                            for (long time = startDate.getTime(); time < stopDate.getTime(); time+=longperiod*1000) {
                                //count ++;
                                //System.out.println(count);
                                //System.out.println((new Time(time).toString()));
                                mydatabase.execSQL("INSERT INTO Task VALUES('" + new Time(time).toString() + "', '" + task_id + "', '" + hash + "', '" + days + "', '" + beginDate + "', '" + endDate + "', '" + beginTimeTag + "', '" + endTimeTag + "');");
                            }

                            //Time VARCHAR, TaskId VARCHAR, Hash VARCHAR, Days VARCHAR, BeginDate VARCHAR, EndDate VARCHAR, BeginTime VARCHAR, EndTime VARCHAR
                            //mydatabase.execSQL("INSERT INTO Task VALUES('" + runtime + "', '" + task_id + "', '" + hash + "', '" + days + "', '" + beginDate + "', '" + endDate + "', '" + beginTimeTag + "', '" + endTimeTag + "');");

                            String action4, data4, response4;
                            action4 = "?task=setStatus";
                            data4 = "data=<?xml version=\"1.0\" encoding=\"UTF-8\"?><TASKS><ID>" + id + "</ID><TASK><TASK_ID>" + task_id + "</TASK_ID><RUNTIME>__:__:__</RUNTIME><STATUS>PROGRESS</STATUS></TASK></TASKS>";
                            response4 = wget(action4, data4);
                        }

                        if (response_tasks.indexOf("<TYPE>DOWNLOADS</TYPE>") != -1 ) {
                            task_id = "0";
                            task_id = response_tasks.substring(response_tasks.indexOf("<TASK_ID>") + 9, response_tasks.indexOf("</TASK_ID>"));
                            // Message msg = handler.obtainMessage();
                            //Bundle bundle = new Bundle();
                            //bundle.putString("myKey", task_id);
                            //msg.setData(bundle);
                            //handler.sendMessage(msg);


                            action2 = "?task=setStatus";
                            data2 = "data=<?xml version=\"1.0\" encoding=\"UTF-8\"?><TASKS><ID>" + id + "</ID><TASK><TASK_ID>" + task_id + "</TASK_ID><RUNTIME>__:__:__</RUNTIME><STATUS>PROGRESS</STATUS></TASK></TASKS>";
                            response2 = wget(action2, data2);

                            //Log.i(TAG, "URL----: " + response_tasks.substring(response_tasks.indexOf("<URL>") + 5, response_tasks.indexOf("</URL>") - 5 ));
                            //downloadFile(response_tasks.substring(response_tasks.indexOf("<URL>") + 5, response_tasks.indexOf("</URL>") - 5 ), "test.png" );




                            final String url = response_tasks.substring(response_tasks.indexOf("<URL>") + 5, response_tasks.indexOf("</URL>"));
                            final String name = response_tasks.substring(response_tasks.indexOf("<NAME>") + 6, response_tasks.indexOf("</NAME>"));
                            final String hash = response_tasks.substring(response_tasks.indexOf("<HASH>") + 6, response_tasks.indexOf("</HASH>"));
                            downloadHash = hash;

                            final TextView tvStatus = (TextView)findViewById(R.id.textView);

                            DownloadManager.Request request;
                            request = new DownloadManager.Request(Uri.parse(url));
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                            request.setTitle("DM Example");
                            request.setDescription("Downloading file");

                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

                            String[] allowedTypes = {"png", "jpg", "jpeg", "gif", "webp", "mp3", "ico"};
                            String suffix = "mp3";

                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS +
                                    File.separator + "image_test", task_id + "_" + name);





                            //final StringBuilder output_filename = new  StringBuilder("");
                            //String fname = Environment.DIRECTORY_DOWNLOADS + File.separator + "image_test" + File.separator + task_id + "_" + name;


                            //System.out.println(fname);






                            request.allowScanningByMediaScanner();
                            final DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

                            final long DL_ID = dm.enqueue(request);

                            mDLCompleteReceiver = new BroadcastReceiver() {

                                @Override
                                public void onReceive(Context context, Intent intent) {
                                /* our download */
                                    if (DL_ID == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)) {

                                        //                    tvStatus.clearAnimation();
                                    /* get the path of the downloaded file */
                                        DownloadManager.Query query = new DownloadManager.Query();
                                        query.setFilterById(DL_ID);
                                        Cursor cursor = dm.query(query);
                                        if (!cursor.moveToFirst()) {
                                            //                        tvStatus.setText("Download error: cursor is empty");
                                            return;
                                        }

                                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                                                != DownloadManager.STATUS_SUCCESSFUL) {
                                            //                        tvStatus.setText("Download failed: no success status");
                                            return;
                                        }

                                        String path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                                        //final String response2;
                                        //response2 = wget("action2", "data2");
                                        //Bundle bundle1 = new Bundle();
                                        //String val = bundle1.getString("myKey");
                                        //System.out.println("output-------------------------------------------" + val);
                                        //String data2 = "data=<?xml version=\"1.0\" encoding=\"UTF-8\"?><TASKS><ID>3</ID><TASK><TASK_ID>1048</TASK_ID><RUNTIME>__:__:__</RUNTIME><STATUS>COMPLETED</STATUS></TASK></TASKS>";
                                        //String response2 = wget("?task=setStatus", "data=<?xml version=\"1.0\" encoding=\"UTF-8\"?><TASKS><ID>3</ID><TASK><TASK_ID>1048</TASK_ID><RUNTIME>__:__:__</RUNTIME><STATUS>COMPLETED</STATUS></TASK></TASKS>");
                                        //Message msg = handler.obtainMessage();
                                        //Bundle bundle = new Bundle();
                                        //bundle.putString("myKey", "111111"+ val+"2222");
                                        //msg.setData(bundle);
                                        //handler.sendMessage(msg);

                                        tvStatus.setText(path);
                                        mydatabase.execSQL("INSERT INTO Hashname VALUES('" + downloadHash + "', '" + path +"');");

                                        //downloadHash;

                                    }
                                }
                            };

                            registerReceiver(mDLCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


                            data2 = "data=<?xml version=\"1.0\" encoding=\"UTF-8\"?><TASKS><ID>" + id + "</ID><TASK><TASK_ID>" + task_id + "</TASK_ID><RUNTIME>__:__:__</RUNTIME><STATUS>COMPLETED</STATUS></TASK></TASKS>";
                            //response2 = wget(action2, data2);
                            //Message msg1 = handler.obtainMessage();
                            //Bundle bundle1 = new Bundle();
                            //String val = bundle.getString("myKey");


                        }
                    }



                    //get_file("filename.mp3", "http://www.zeetway.net/03.mp3");
                    //
                    //KEY_NAME
                    // KEY_URL

                    //Message msg = handler.obtainMessage();
                    //Bundle bundle = new Bundle();
                    //bundle.putString("myKey", "111111");
                    //msg.setData(bundle);
                    //handler.sendMessage(msg);
                }

            }
        }).start();



        new Thread(new Runnable() {
            public void run() {

                int duration;
                mediaPlayer = new MediaPlayer();

                int i = 0;
                while (i<1)
                {
                    duration = 1000;

                    String fname = "";

                    mediaPlayer.release();
                    mediaPlayer = null;

                    mediaPlayer = new MediaPlayer();

                    int currentVolume = 10;
                    float log1=(float)(Math.log(maxVolume-currentVolume)/Math.log(maxVolume));
                    mediaPlayer.setVolume(1-log1, 1-log1);

                    String runtime = new Time(System.currentTimeMillis()).toString();
                    System.out.println(runtime + "  player time");
                    Cursor resultSetHash = mydatabase.rawQuery("Select * from Task Where Time='" + runtime + "';", null);
                    System.out.println(resultSetHash.getCount());

                    if (resultSetHash.getCount() > 0) {
                        resultSetHash.moveToFirst();
                        final String hash = resultSetHash.getString(2);

                        System.out.println(hash);

                        Cursor resultSet = mydatabase.rawQuery("Select * from Hashname Where Hash = '" + hash + "';", null);
                        System.out.println(resultSet.getCount() + ": filenames");
                        if (resultSet.getCount() > 0) {
                            resultSet.moveToFirst();

                            while (resultSet.isAfterLast() == false) {
                                final String filename = resultSet.getString(1);
                                System.out.println(filename);

                                System.out.println(resultSet.getCount());
                                try {
                                    //mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/image_test/1048_provintsiya_30_2016.04.01mono.mp3");
                                    mediaPlayer.setDataSource(filename);
                                    mediaPlayer.prepare();

                                } catch (IOException e) {
                                }
                                mediaPlayer.start();


                                fname = Integer.toString(mediaPlayer.getDuration());
                                System.out.println(fname);

                                duration = mediaPlayer.getDuration();

                                synchronized (this) {
                                    try {
                                        wait(duration);
                                    } catch (Exception e) {
                                    }
                                }
                                System.out.println("Player ------------------------------------");

                                resultSet.moveToNext();
                            }
                        }
                    }




                    long endTime = System.currentTimeMillis() + 1000;// + duration;

                    String testTime = Long.toString(System.currentTimeMillis()/1000);
                    while (System.currentTimeMillis() < endTime) {
                        synchronized (this) {
                            try {
                                wait(endTime -
                                        System.currentTimeMillis());
                            } catch (Exception e) {
                            }
                        }
                    }


                    //Message msg = handler.obtainMessage();
                    //Bundle bundle = new Bundle();
                    //bundle.putString("myKey", "33333");
                    //msg.setData(bundle);
                    //handler.sendMessage(msg);
                }
            }
        }).start();






    }

    protected void requestPermission(String permissionType, int requestCode) {
        int permission = ContextCompat.checkSelfPermission(this,
                permissionType);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{permissionType}, requestCode
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case STORAGE_REQUEST_CODE: {

                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this,
                            "External Storage permission required",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}

package org.tensorflow.lite.examples.detection;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.examples.detection.env.Logger;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Nampmenu extends AppCompatActivity {

    private Spinner animalspinner;
    private String[] animal_list={"Cat","Dog","Bird","Turtle"};
    GPSTracker gpsTracker;
    TextView locationt,nampanimal,fedback1,username_inp;
    ImageView iview;
    ProgressBar progressBar;
    Button uploadButton;
    String lati;
    String longi;
    EditText username;
    RequestQueue queue;
    JsonObjectRequest jsonObjReq;
    String mark_data_url = "https://jsrealtime-database.firebaseio.com/mark_data/animals/";
    FirebaseStorage storage ;
    String download_url=new String();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.namp_menu);
        Intent intent = getIntent();
        String message = intent.getStringExtra("animal");
        FirebaseApp.initializeApp(getApplicationContext());
        queue = Volley.newRequestQueue(this);

        storage=FirebaseStorage.getInstance("gs://jsrealtime-database.appspot.com");


        animalspinner = (Spinner) findViewById(R.id.spinner_animal);
        ArrayAdapter<String> dataAdapterForIller = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, animal_list);

        animalspinner.setAdapter(dataAdapterForIller);
        int selectindex=0;
            if(message.equals("cat"))
                selectindex=0;
        if(message.equals("dog"))
            selectindex=1;
        if(message.equals("bird"))
            selectindex=2;
        locationt= findViewById(R.id.location_info_text);
        nampanimal=findViewById(R.id.detection_text);
        nampanimal.setText("a "+message+" is detected");


        locationt= findViewById(R.id.location_info_text);
        gpsTracker = new GPSTracker(getApplicationContext());
        setLocationAddress();


        //views
        username=(EditText) findViewById(R.id.username_inp) ;
        animalspinner.setSelection(selectindex);
        iview=findViewById(R.id.imageView);
        iview.setImageBitmap(DetectorActivity.b);
        uploadButton=findViewById(R.id.submit_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDrawable drawable = (BitmapDrawable) iview.getDrawable();
                Bitmap bmap = drawable.getBitmap();
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                bmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                byte[] data=baos.toByteArray();
                //BURASI DEİŞCEK NODE NAME OLCAK
                SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd ");
                Random generator=new Random();
                int uplaodid=generator.nextInt(9999999);
                String path="animal_imgs/"+""+username.getText()+""+nampanimal.getText()+mdformat.get2DigitYearStart().toString()+"uid"+uplaodid;
                StorageReference fire_imgref=storage.getReference(path);
                uploadButton.setEnabled(false);
                //send animal image to the storage
                UploadTask uploadTask=fire_imgref.putBytes(data);
                ProgressDialog progressDialog = new ProgressDialog(Nampmenu.this);
                progressDialog.setMessage("Devam eden işleminiz bulunmaktadır. Lütfen bekleyiniz..");
                progressDialog.show();

                uploadTask.addOnSuccessListener(Nampmenu.this,new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //progressBar.setVisibility(View.GONE);
                        uploadButton.setEnabled(true);
                        progressDialog.dismiss();
                        fedback1=findViewById(R.id.fedback1);
                        fedback1.setTextColor(Color.GREEN);
                        fedback1.setText("THANKS FOR YOUR SUPPORT.");
                        fedback1.setTextSize(20);
                        iview.setVisibility(View.INVISIBLE);
                        //final Uri[] download_uri = new Uri[1];
                        fire_imgref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                Uri u= uri;
                                download_url=u.toString();
                                //sen animal data
                                JSONObject js = new JSONObject();
                                String spinner_value=animalspinner.getSelectedItem().toString();
                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd ");
                                String strDate = "" + mdformat.format(calendar.getTime());
                                String surl=mark_data_url+""+username.getText()+""+spinner_value+""+mdformat.get2DigitYearStart().toString()+".json";
                                try {
                                    JSONObject jsonobject_one = new JSONObject();

                                    js.put("description", ""+spinner_value);
                                    js.put("lat", ""+lati);
                                    js.put("lng", ""+longi);
                                    js.put("time", ""+strDate);
                                    js.put("user", ""+username.getText());
                                    js.put("img_url", ""+download_url);
                                    js.put("status", "0");
                                    js.put("health", "normal");


            /*jsonobject_one.put("devicetype", "I");

            JSONObject jsonobject_TWO = new JSONObject();
            jsonobject_TWO.put("value", "event");
            JSONObject jsonobject = new JSONObject();

            jsonobject.put("requestinfo", jsonobject_TWO);
            jsonobject.put("request", jsonobject_one);

*/


                                }catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                jsonObjReq = new JsonObjectRequest(
                                        Request.Method.PUT, surl, js,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                //create_toastmessage();
                                                Toast toast=Toast.makeText(getApplicationContext(),"Information was sent",Toast.LENGTH_SHORT);
                                                toast.setMargin(50,50);
                                                toast.show();


                                            }
                                        }, new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                    }
                                }) {

                                    /**
                                     * Passing some request headers
                                     */
                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        HashMap<String, String> headers = new HashMap<String, String>();
                                        headers.put("Content-Type", "application/json; charset=utf-8");
                                        return headers;
                                    }





// Add the request to the RequestQueue.

                                };
                                queue.add(jsonObjReq);



                            }
                        });




                    }
                });
            }
        });


    }

    private void setLocationAddress() {
        if (gpsTracker.getLocation() != null) {
            if (gpsTracker.getLatitude() != 0 && gpsTracker.getLongitude() != 0) {
                locationt.setText("Your location:"+gpsTracker.getLatitude()+","+gpsTracker.getLongitude());
                lati=""+gpsTracker.getLatitude();
                longi=""+gpsTracker.getLongitude();

                // Do whatever you want
            } else {
                buildAlertMessageNoGps();
            }
        } else {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("location not determined");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "try again",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        setLocationAddress();
                    }
                });

        builder1.setNegativeButton(
                android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }

    /*public void create_toastmessage(){

        LayoutInflater inflater = getLayoutInflater();

        View toastLayout = inflater.inflate(R.layout.toast_lay_1,
                (ViewGroup) findViewById(R.id.toast_root_view));

        TextView header = (TextView) toastLayout.findViewById(R.id.toast_header);
        header.setTextColor(Color.BLUE);
        header.setText("Message for you:");

        TextView body = (TextView) toastLayout.findViewById(R.id.toast_body);
        body.setText("Animal inforamtion sended"+"\n Thank you for your support.");

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        toast.show();
    }*/
}

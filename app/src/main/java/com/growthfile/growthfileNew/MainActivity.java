package com.growthfile.growthfileNew;

import android.Manifest;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;

import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;

import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.*;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.provider.Settings.Secure;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getSimpleName();
    private WebView mWebView;
    SwipeRefreshLayout swipeToRefresh;

    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;
    private Context mContext;
    private static final int CAMERA_ONLY_REQUEST = 111;
    private static final int PHOTO_GALLERY_REQUEST = 112;
    private static final int PHOTO_CAMERA_REQUEST = 113;
    int LOCATION_PERMISSION_CODE = 115;
    private String pictureImagePath = "";
    public boolean allowLoadUrl = false;
    public AlertDialog appAlert;
    public boolean hasPageFinished = false;
    public static final String BROADCAST_ACTION = "com.growthfile.growthfileNew";
    private BroadcastReceiver broadcastReceiver;

    private void setWebViewClient() {
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!hasPageFinished) {
                    Log.d("onPageFinished", "true");
                    hasPageFinished = true;
                    mWebView.evaluateJavascript("native.setName('Android')", null);
                    FirebaseInstanceId.getInstance().getInstanceId()
                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (!task.isSuccessful()) {
                                        Log.w("MainActivity", "getInstanceId failed", task.getException());
                                        return;
                                    }

                                    String token = task.getResult().getToken();
                                    Log.e("FCMToken", token);
                                    mWebView.evaluateJavascript("native.setFCMToken('" + token + "')", null);
                                }
                            });


                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("geo:")) {
                    Intent mapIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                        MainActivity.this.startActivity(mapIntent);
                    }
                    return true;
                }

                view.loadUrl(url);
                return true;
            }

            @RequiresApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                shouldOverrideUrlLoading(view,url);
                return true;
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case CAMERA_ONLY_REQUEST:
                if (resultCode == RESULT_OK) {
                    File imgFile = new File(pictureImagePath);
                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        mWebView.loadUrl("javascript:setFilePath('" + encodeImage(myBitmap) + "')");

                    }
                }
                break;

            case PHOTO_GALLERY_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri uri = intent.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        if (bitmap != null) {
                            mWebView.loadUrl("javascript:readUploadedFile('" + encodeImage(bitmap) + "')");
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                break;
            case PHOTO_CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {
                    File imgFile = new File(pictureImagePath);
                    if (imgFile.exists()) {
                        Toast.makeText(MainActivity.this,"picture path is not null",Toast.LENGTH_LONG).show();
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                        mWebView.loadUrl("javascript:readUploadedFile('" + encodeImage(myBitmap) + "')");
                    }
                }
                break;
        }
    }

    public String encodeImage(Bitmap bm) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        Log.d("encoded",encoded);
        return encoded;
    }


    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();

        StrictMode.setVmPolicy(builder.build());
        setContentView(R.layout.activity_main);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        new CertPin().execute();

        registerMyReceiver();
        mContext = getApplicationContext();

        if(!checkDeviceOsCompatibility()) {
            try {
                showOsUncompatibleDialog();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

        PackageManager pm = getApplicationContext().getPackageManager();
        boolean isWebViewInstalled = isAndroidSystemWebViewInstalled("com.google.android.webview", pm);

        if(!isWebViewInstalled) {
            try {
                createAlertBoxJson();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

        if(!allowLoadUrl) {
            return;

        }

        LoadApp();
        swipeToRefresh = findViewById(R.id.swipeToRefresh);
        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeToRefresh.setRefreshing(true);
                mWebView.evaluateJavascript("javascript:requestCreator('Null')", null);
            }
        });

    }

    private boolean checkDeviceOsCompatibility() {
        if (VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return false;
        }
        return true;
    }

    private  void showOsUncompatibleDialog() throws JSONException{


        JSONObject alert = new JSONObject();
        alert.put("title", "App Incompatible");
        alert.put("message", "This App is incompatible with your Android Device. Please upgrade your android version to use Growthfile");
        alert.put("cancelable", false);
        JSONObject button = new JSONObject();
        button.put("text", "");
        button.put("show", false);


        JSONObject clickAction = new JSONObject();
        JSONObject redirection = new JSONObject();

        redirection.put("text", "");
        redirection.put("value", false);

        clickAction.put("redirection", redirection);

        button.put("clickAction", clickAction);
        alert.put("button", button);

        alertBox(MainActivity.this, alert.toString(4));

    }

    private void registerMyReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("broadcastReceiver", "taken");
                String fcmBody;
                try {
                    fcmBody = intent.getStringExtra("fcmNotificationData");
                    mWebView.evaluateJavascript("runRead(" + fcmBody + ")", null);
                } catch (Exception e) {
                    mWebView.evaluateJavascript("runRead()", null);
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == CAMERA_ONLY_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                try {
                    startActivityForResult(photoCameraIntent(), CAMERA_ONLY_REQUEST);
                } catch (IOException e) {
                    e.printStackTrace();
                    createIntentForCameraOnly();
                }
            }
        } else if (requestCode == PHOTO_CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&  grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                try {
                    startActivityForResult(photoCameraIntent(), PHOTO_CAMERA_REQUEST);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == PHOTO_GALLERY_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(photoGalleryIntent(), PHOTO_GALLERY_REQUEST);
            }
        }
        else if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    allowLoadUrl = true;
                    LoadApp();
            }
            else {

            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!checkLocationPermission()) {
            String[] PERMISSIONS = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };
            ActivityCompat.requestPermissions(this,PERMISSIONS,LOCATION_PERMISSION_CODE);
        }
        else {
            allowLoadUrl = true;
            try {
                String script = "try { runRead() }catch(e){}";
                mWebView.evaluateJavascript(script, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            swipeToRefresh.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {

                    if (mWebView.getScrollY() == 0) {
                        swipeToRefresh.setEnabled(true);

                    } else {
                        swipeToRefresh.setEnabled(false);
                    }
                }
            });
        }
    }

    @Override
    protected  void onResume(){
        super.onResume();

        if(!checkLocationPermission()) {
            String[] PERMISSIONS = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };
            ActivityCompat.requestPermissions(this,PERMISSIONS,LOCATION_PERMISSION_CODE);
        }
        else {
            allowLoadUrl = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        swipeToRefresh.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // make sure to unregister your receiver after finishing of this activity
        unregisterReceiver(broadcastReceiver);
    }

    public void createProfileIntent() {
        final String[] PERMISSIONS_PHOTO_CAMERA = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        final String[] PERMISSIONS_PHOTO_GALLERY = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        final Context context = MainActivity.this;
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Image Location");
        builder.setCancelable(true);
        builder.setItems(new CharSequence[]
                        {"Camera", "Gallery"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:

                                if (!hasPermissions(MainActivity.this, PERMISSIONS_PHOTO_CAMERA)) {
                                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_PHOTO_CAMERA, PHOTO_CAMERA_REQUEST);
                                } else {
                                    try {
                                        startActivityForResult(photoCameraIntent(), PHOTO_CAMERA_REQUEST);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                break;
                            case 1:
                                if (!hasPermissions(MainActivity.this, PERMISSIONS_PHOTO_GALLERY)) {
                                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_PHOTO_GALLERY, PHOTO_GALLERY_REQUEST);
                                } else {
                                    startActivityForResult(photoGalleryIntent(), PHOTO_GALLERY_REQUEST);
                                }
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    public Intent photoCameraIntent() throws  IOException{
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";

        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File file = new File(pictureImagePath);
        Uri outputFileUri = Uri.fromFile(file);
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        return takePicture;

    }

    public Intent photoGalleryIntent() {
        Intent choosePicture = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        choosePicture.setType("image/*");
        return choosePicture;
    }

    public void LoadApp() {

        this.mWebView = findViewById(R.id.activity_main_webview);

        WebSettings webSettings = this.mWebView.getSettings();
        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "AndroidInterface");


        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webSettings.setDomStorageEnabled(true);

        webSettings.setAllowFileAccess(true);
        webSettings.setGeolocationEnabled(true);

        webSettings.setDatabaseEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        webSettings.setAppCacheEnabled(true);
        webSettings.setGeolocationDatabasePath(getApplicationContext().getFilesDir().getPath());
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setScrollbarFadingEnabled(true);

        if (!isNetworkAvailable()) { // loading offline
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        mWebView.loadUrl("https://growthfile-testing.firebaseapp.com");
        mWebView.requestFocus(View.FOCUS_DOWN);
        setWebViewClient();

    }


    public void createAlertBoxJson() throws JSONException {
        String messageString = "This app is incompatible with your Android device. To make your device compatible with this app, Click okay to install/update your System webview from Play store";
        String title = "App Incompatibility Issue";

        JSONObject json = new JSONObject();
        json.put("title", title);
        json.put("message", messageString);
        json.put("cancelable", false);

        JSONObject button = new JSONObject();
        button.put("text", "Okay");
        button.put("show", true);


        JSONObject clickAction = new JSONObject();
        JSONObject redirection = new JSONObject();


        redirection.put("text", "com.google.android.webview");
        redirection.put("value", true);


        clickAction.put("redirection", redirection);


        button.put("clickAction", clickAction);

        json.put("button", button);
        String jsonString = json.toString(4);

        alertBox(MainActivity.this, jsonString);
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    public static Certificate getCertificateForRawResource(int resourceId, Context context) {
        CertificateFactory cf = null;
        Certificate ca = null;
        Resources resources = context.getResources();
        InputStream caInput = resources.openRawResource(resourceId);

        try {
            cf = CertificateFactory.getInstance("X.509");
            ca = cf.generateCertificate(caInput);
        } catch (CertificateException e) {
        } finally {
            try {
                caInput.close();
            } catch (IOException e) {
                Log.e(TAG, "exception", e);
            }
        }

        return ca;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean isAndroidSystemWebViewInstalled(String pckgname, PackageManager packageManager) {

        if (isDeviceBelowNougat()) {
            try {

                if (packageManager.getApplicationInfo(pckgname, 0).enabled) {
                    //minor hack
                    int stableVersion = 70;

                    PackageInfo pi = packageManager.getPackageInfo(pckgname, 0);
                    String shortenVersionName = pi.versionName.length() < 2 ? pi.versionName : pi.versionName.substring(0, 2);
                    int parsedShortenVersion = Integer.parseInt(shortenVersionName);
                    return parsedShortenVersion >= stableVersion;

                }
                return false;

            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }

        } else {
            return true;
        }


    }

    private boolean isDeviceBelowNougat() {
        return VERSION.SDK_INT < Build.VERSION_CODES.N && VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }


    public void alertBox(@NonNull Context context, @NonNull String dialogData) throws JSONException {

        final JSONObject data = new JSONObject(dialogData);
        String title = data.getString("title");
        String message = data.getString("message");

        boolean cancelable = data.getBoolean("cancelable");

        boolean showButton = data.getJSONObject("button").getBoolean("show");

        Log.d(TAG, "alertBox: started");


        final AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(cancelable);

        if (showButton) {
            final boolean allowRedirection = data.getJSONObject("button").getJSONObject("clickAction").getJSONObject("redirection").getBoolean("value");
            final String redirectionText = data.getJSONObject("button").getJSONObject("clickAction").getJSONObject("redirection").getString("text");
            String buttonText = data.getJSONObject("button").getString("text");

            Log.d(TAG, "alertBox: cancelable false");

            builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    if (allowRedirection) {

                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + redirectionText)));
                        } catch (android.content.ActivityNotFoundException noPs) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + redirectionText)));
                        }
                    }
                }
            });
        }

        builder.setIcon(android.R.drawable.ic_dialog_alert);
        appAlert = builder.create();
        appAlert.show();
    }

    public boolean gpsEnabled() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);

        assert service != null;
        Log.d(TAG, "gpsEnabled: " + service.isProviderEnabled(LocationManager.GPS_PROVIDER));
        return service.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }



    public void createIntentForCameraOnly(){
        Intent CAMERA_ONLY_INTENT = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (CAMERA_ONLY_INTENT.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(CAMERA_ONLY_INTENT, CAMERA_ONLY_REQUEST);
        }
    }

    public boolean checkLocationPermission(){

        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };

        return hasPermissions(MainActivity.this, PERMISSIONS);
    }

    public class viewLoadJavaInterface {
        Context mContext;

        viewLoadJavaInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void showDialog(String title,String body){
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(title);
            builder.setMessage(body);
            builder.setCancelable(true);
            AlertDialog dialog = builder.create();
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }

        @JavascriptInterface
        public String getCellularData(){
            CellularInformation mCellularInformation = new CellularInformation(MainActivity.this);
            return mCellularInformation.fullCellularInformation();
        }

        @JavascriptInterface
        public boolean isConnectionActive() {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }

        @JavascriptInterface
        public void startConversation(final String view) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (view.equals("conversation") || view.equals("selector")) {
                        Log.d(TAG, "run: yes");
                        swipeToRefresh.setEnabled(false);
                    }
                }
            });
        }

        @JavascriptInterface
        public  void  openImagePicker(){
            createProfileIntent();
        }

        @JavascriptInterface

        public void startCamera() {
            if(VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                String[] PERMISSIONS_CAMERA = {
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };

                if (!hasPermissions(MainActivity.this, PERMISSIONS_CAMERA)) {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_CAMERA, CAMERA_ONLY_REQUEST);
                }
                else {
                    try {
                        startActivityForResult(photoCameraIntent(),CAMERA_ONLY_REQUEST);
                    } catch (IOException e) {
                        e.printStackTrace();
                        createIntentForCameraOnly();
                    }
                }
            }
            else {
                try {
                    startActivityForResult(photoCameraIntent(),CAMERA_ONLY_REQUEST);
                } catch (IOException e) {
                    createIntentForCameraOnly();
                }
            }
        }

        @JavascriptInterface
        public void startKeyboard() {
            InputMethodManager inputMethodManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(findViewById(R.id.activity_main_webview),
                    InputMethodManager.SHOW_FORCED);
        }

        @JavascriptInterface
        public String getDeviceId() throws JSONException {
            JSONObject device = new JSONObject();

            String androidId = Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceBrand = Build.MANUFACTURER;
            String deviceModel = Build.MODEL;
            String osVersion = VERSION.RELEASE;
            String deviceBaseOs = "android";

            device.put("id", androidId);
            device.put("deviceBrand", deviceBrand);
            device.put("deviceModel", deviceModel);
            device.put("osVersion", osVersion);
            device.put("baseOs", deviceBaseOs);
            device.put("appVersion", 5);
            String deviceInfo = device.toString(4);
            return deviceInfo;
        }

        @JavascriptInterface
        public boolean isLocationPermissionGranted() {
            return checkLocationPermission();
        }

        @JavascriptInterface
        public boolean isGpsEnabled() {
            return gpsEnabled();
        }


        @JavascriptInterface
        public void stopRefreshing(final boolean stopRefreshing) {
            Log.d(TAG, "stopRefreshing: " + stopRefreshing);
            Log.d(TAG, "stopRefreshing: " + MainActivity.this.swipeToRefresh.canChildScrollUp());
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    swipeToRefresh.setRefreshing(!stopRefreshing);
                }
            });
        }
    }


    @Override
    public void onBackPressed() {

        if (mWebView.canGoBack()) {
            mWebView.goBack(); // emulates back history
        } else {
            super.onBackPressed();
        }
    }

}
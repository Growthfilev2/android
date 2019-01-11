//package com.growthfile.growthfileNew;
//
//import android.Manifest;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.ApplicationInfo;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.content.res.Configuration;
//import android.content.res.Resources;
//import android.database.Cursor;
//import android.graphics.Bitmap;
//import android.location.Location;
//import android.location.LocationManager;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.net.Uri;
//import android.net.wifi.ScanResult;
//import android.net.wifi.WifiInfo;
//import android.net.wifi.WifiManager;
//import android.os.Build;
//import android.os.Build.VERSION;
//import android.os.Bundle;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.provider.Settings.Secure;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.telephony.TelephonyManager;
//import android.telephony.gsm.GsmCellLocation;
//import android.text.TextUtils;
//import android.util.Base64;
//import android.util.Log;
//import android.view.View;
//import android.view.ViewTreeObserver;
//import android.view.WindowManager;
//import android.view.inputmethod.InputMethodManager;
//import android.webkit.GeolocationPermissions;
//import android.webkit.JavascriptInterface;
//import android.webkit.ValueCallback;
//import android.webkit.WebChromeClient;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.UserProfileChangeRequest;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.security.cert.Certificate;
//import java.security.cert.CertificateException;
//import java.security.cert.CertificateFactory;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//
//
//public class copy extends AppCompatActivity {
//    private static final int FCR = 1;
//    private static final String TAG = copy.class.getSimpleName();
//    private static Location location;
//    private String mCM;
//
//    private ValueCallback<Uri> mUM;
//    private ValueCallback<Uri[]> mUMA;
//    private Bitmap imageBitmap;
//    private static final int profile_photo_code = 999;
//    private final static int FILECHOOSER_RESULTCODE = 1;
//    private WebView mWebView;
//    SwipeRefreshLayout swipeToRefresh;
//    private static final String loadTypeInit = "init";
//    private static final String loadTypeUpdate = "update";
//    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;
//    private Context mContext;
//    private static final int CAMERA_ONLY_REQUEST = 1888;
//    private static final int CAMERA_ONLY_PERMISSION_CODE = 100;
//    private boolean background_app = false;
//    public AlertDialog appAlert;
//
//    private FirebaseAuth mAuth;
//
//
//    public class NewWebChromeClient extends WebChromeClient {
//
//        @Override
//        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
//            Log.i(TAG, "onGeolocationPermissionsShowPrompt()");
//
//            final boolean remember = false;
//            callback.invoke(origin, true, remember);
//        }
//
//        @Override
//        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//            Intent[] intentArray;
//            File photoFile = null;
//            if (copy.this.mUMA != null) {
//                copy.this.mUMA.onReceiveValue(null);
//            }
//            copy.this.mUMA = filePathCallback;
//            Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
//            if (takePictureIntent.resolveActivity(copy.this.getPackageManager()) != null) {
//                try {
//                    photoFile = copy.this.createImageFile();
//                    takePictureIntent.putExtra("PhotoPath", copy.this.mCM);
//                } catch (IOException ex) {
//                }
//                if (photoFile != null) {
//                    copy mainActivity = copy.this;
//                    StringBuilder stringBuilder = new StringBuilder();
//                    stringBuilder.append("file:");
//                    stringBuilder.append(photoFile.getAbsolutePath());
//                    mainActivity.mCM = stringBuilder.toString();
//                    takePictureIntent.putExtra("output", Uri.fromFile(photoFile));
//                } else {
//                    takePictureIntent = null;
//                }
//            }
//
//
//            Intent contentSelectionIntent = new Intent(Intent.ACTION_PICK,
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            if (takePictureIntent != null) {
//                Intent[] intentArr = new Intent[copy.FCR];
//                intentArr[0] = takePictureIntent;
//                intentArray = intentArr;
//            } else {
//                intentArray = new Intent[0];
//            }
//            Intent chooserIntent = new Intent("android.intent.action.CHOOSER");
//            chooserIntent.putExtra("android.intent.extra.INTENT", contentSelectionIntent);
//            chooserIntent.putExtra("android.intent.extra.TITLE", "Image Chooser");
//            chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", intentArray);
//
//            copy.this.startActivityForResult(chooserIntent, copy.FCR);
//            return true;
//        }
//
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            if (url.contains("geo:")) {
//                Intent mapIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
//                mapIntent.setPackage("com.google.android.apps.maps");
//                if (mapIntent.resolveActivity(copy.this.getPackageManager()) != null) {
//                    copy.this.startActivity(mapIntent);
//                }
//                return true;
//            }
//
//            view.loadUrl(url);
//            return true;
//        }
//    }
//
//    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        super.onActivityResult(requestCode, resultCode, intent);
//
//        switch (requestCode) {
//            case CAMERA_ONLY_REQUEST:
//                if (resultCode == RESULT_OK) {
//                    Bundle extras = intent.getExtras();
//                    imageBitmap = (Bitmap) extras.get("data");
//                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                    byte[] byteArray = byteArrayOutputStream.toByteArray();
//                    String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
//                    mWebView.loadUrl("javascript:setFilePath('" + encoded + "')");
//
//                }
//                break;
//
//
//            case FCR:
//                if (VERSION.SDK_INT >= 21) {
//                    Uri[] results = null;
//                    if (resultCode == -1 && requestCode == FCR) {
//                        if (this.mUMA != null) {
//                            Uri[] uriArr;
//                            if (intent != null) {
//                                String dataString = intent.getDataString();
//                                if (dataString != null) {
//                                    uriArr = new Uri[FCR];
//                                    uriArr[0] = Uri.parse(dataString);
//                                    results = uriArr;
//                                }
//                            } else if (this.mCM != null) {
//                                uriArr = new Uri[FCR];
//                                uriArr[0] = Uri.parse(this.mCM);
//                                results = uriArr;
//                            }
//                        } else {
//                            return;
//                        }
//                    }
//                    this.mUMA.onReceiveValue(results);
//                    this.mUMA = null;
//                } else if (requestCode == FCR && this.mUM != null) {
//                    Uri result;
//                    if (intent != null) {
//                        if (resultCode == -1) {
//                            result = intent.getData();
//                            this.mUM.onReceiveValue(result);
//                            this.mUM = null;
//                        }
//                    }
//                    result = null;
//                    this.mUM.onReceiveValue(result);
//                    this.mUM = null;
//                }
//        }
//
//
//    }
//
//    //     Create an image file
//    private File createImageFile() throws IOException {
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "img_" + timeStamp + "_";
//        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        return File.createTempFile(imageFileName, ".jpg", storageDir);
//    }
//
//
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//    }
//
//    protected void onCreate(Bundle savedInstanceState) {
//
//        mAuth = FirebaseAuth.getInstance();
//
//
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
//                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
//
//        new CertPin().execute();
//
//
//        mContext = getApplicationContext();
//        try {
//            LoadApp(loadTypeInit);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        swipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
//        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                try {
//                    LoadApp(loadTypeUpdate);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//    }
//
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        //check for permission
//        int PERMISSION_ALL = 1;
//        String[] PERMISSIONS = {
//                Manifest.permission.CAMERA,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//        };
//
//        if (!hasPermissions(this, PERMISSIONS)) {
//            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
//        }
//
//
//
//        swipeToRefresh.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
//            @Override
//            public void onScrollChanged() {
//                if (mWebView.getScrollY() == 0) {
//                    swipeToRefresh.setEnabled(true);
//                } else {
//                    swipeToRefresh.setEnabled(false);
//                }
//            }
//        });
//    }
//
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        swipeToRefresh.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
//    }
//
//
//    public void LoadApp(String type) throws JSONException {
//
//        this.mWebView = (WebView) findViewById(R.id.activity_main_webview);
//
//        WebSettings webSettings = this.mWebView.getSettings();
//        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "Fetchview");
//        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "FetchCameraForAttachment");
//        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "openAndroidKeyboard");
//        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "androidLocation");
//        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "AndroidId");
//        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this), "gps");
//        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this),"Towers");
//        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this),"Android");
//        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this),"AndroidRefreshing");
//        mWebView.addJavascriptInterface(new viewLoadJavaInterface(this),"Internet");
//
//
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setLoadWithOverviewMode(true);
//        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
//        webSettings.setDomStorageEnabled(true);
//
//        webSettings.setAllowFileAccess(true);
//        webSettings.setGeolocationEnabled(true);
//
//        webSettings.setDatabaseEnabled(true);
//        webSettings.setUseWideViewPort(true);
//        webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
//        webSettings.setAppCacheEnabled(true);
//        webSettings.setGeolocationDatabasePath(getApplicationContext().getFilesDir().getPath());
//        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
//        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
//        mWebView.setScrollbarFadingEnabled(true);
//
//        if (VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        }
//
//        if (type.equals("init")) {
//
//            if (!isNetworkAvailable()) { // loading offline
//                webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
//            }
//
//            if(VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
//                JSONObject alert = new JSONObject();
//                alert.put("title","App Incompatible");
//                alert.put("message","This App is incompatible with your Android Device. Please upgrade your android version to use Growthfile");
//                alert.put("cancelable",false);
//                JSONObject button = new JSONObject();
//                button.put("text","");
//                button.put("show",false);
//
//
//                JSONObject clickAction = new JSONObject();
//                JSONObject redirection = new JSONObject();
//
//                redirection.put("text","");
//                redirection.put("value",false);
//
//                clickAction.put("redirection",redirection);
//
//                button.put("clickAction",clickAction);
//                alert.put("button",button);
//
//                alertBox(copy.this,alert.toString(4));
//                return;
//            }
//
//
//            PackageManager pm = getApplicationContext().getPackageManager();
//            boolean isWebViewInstalled = isAndroidSystemWebViewInstalled("com.google.android.webview", pm);
//
//            if(isWebViewInstalled) {
//                Log.d("webview", "LoadApp: Android system webview is installed");
//                mWebView.loadUrl("https://growthfile-207204.firebaseapp.com");
//                mWebView.requestFocus(View.FOCUS_DOWN);
//            }
//            else {
//                createAlertBoxJson();
//            }
//        }
//
//        if (type.equals("update")) {
//
//            swipeToRefresh.setRefreshing(true);
//            mWebView.evaluateJavascript("javascript:requestCreator('Null','true')",null);
//        }
//
//     mWebView.setWebViewClient(new WebViewClient() {
//
//      @Override
//      public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        view.loadUrl(url);
//        return true;
//      }
//
//      @Override
//      public void onPageFinished(WebView view, String url) {
//          Log.d(TAG, "onPageFinished: page has finished loading");
//          mWebView.evaluateJavascript("native.setName('Android')",null);
//      }
//
//      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//
//          AlertDialog alertDialog = new AlertDialog.Builder(copy.this).create();
//          alertDialog.setTitle("Message");
//          alertDialog.setMessage("Please Make sure that you have a working internet connection.");
//          alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Retry",
//                  new DialogInterface.OnClickListener() {
//                      public void onClick(DialogInterface dialog, int which) {
//                          mWebView.reload();
//                          dialog.dismiss();
//                      }
//                  });
//          alertDialog.show();
//      }
//    });
//
//    this.mWebView.setWebChromeClient(new NewWebChromeClient());
//
//
//  }
//
//    public  void createAlertBoxJson() throws  JSONException{
//      String messageString = "This app is incompatible with your Android device. To make your device compatible with this app, Click okay to install/update your System webview from Play store";
//      String title = "App Incompatibility Issue";
//
//
//      JSONObject json = new JSONObject();
//      json.put("title",title);
//      json.put("message",messageString);
//      json.put("cancelable",false);
//
//      JSONObject button = new JSONObject();
//      button.put("text","Okay");
//      button.put("show",true);
//
//
//      JSONObject clickAction = new JSONObject();
//      JSONObject redirection = new JSONObject();
//
//
//      redirection.put("text","com.google.android.webview");
//      redirection.put("value",true);
//
//
//      clickAction.put("redirection",redirection);
//
//
//      button.put("clickAction",clickAction);
//
//      json.put("button",button);
//      String jsonString = json.toString(4);
//
//      alertBox(copy.this, jsonString);
//  }
//
//    // firebase work
//    public void checkCurrentUser() {
//        // [START check_current_user]
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            // User is signed in
//        } else {
//            firebaseUI.
//            // No user is signed in
//        }
//        // [END check_current_user]
//    }
//    public void getUserProfile() {
//        // [START get_user_profile]
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            // Name, email address, and profile photo Url
//            String name = user.getDisplayName();
//            String email = user.getEmail();
//            Uri photoUrl = user.getPhotoUrl();
//
//            // Check if user's email is verified
////            boolean emailVerified = user.isEmailVerified();
//
//            // The user's ID, unique to the Firebase project. Do NOT use this value to
//            // authenticate with your backend server, if you have one. Use
//            // FirebaseUser.getIdToken() instead.
////            String uid = user.getUid();
//        }
//        // [END get_user_profile]
//    }
//    public void updateProfile() {
//        // [START update_profile]
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                .setDisplayName("Jane Q. User")
//                .setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
//                .build();
//
//        user.updateProfile(profileUpdates)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Log.d(TAG, "User profile updated.");
//                        }
//                    }
//                });
//        // [END update_profile]
//    }
//
//    public void updateEmail() {
//        // [START update_email]
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//        user.updateEmail("user@example.com")
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Log.d(TAG, "User email address updated.");
//                        }
//                    }
//                });
//        // [END update_email]
//    }
//
//
//    public static boolean areThereMockPermissionApps(Context context) {
//        int count = 0;
//
//        PackageManager pm = context.getPackageManager();
//        List<ApplicationInfo> packages =
//                pm.getInstalledApplications(PackageManager.GET_META_DATA);
//
//        for (ApplicationInfo applicationInfo : packages) {
//            try {
//                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName,
//                        PackageManager.GET_PERMISSIONS);
//                // Get Permissions
//                String[] requestedPermissions = packageInfo.requestedPermissions;
//
//                if (requestedPermissions != null) {
//                    for (int i = 0; i < requestedPermissions.length; i++) {
//                        if (requestedPermissions[i]
//                                .equals("android.permission.ACCESS_MOCK_LOCATION")
//                                && !applicationInfo.packageName.equals(context.getPackageName())) {
//                            count++;
//                        }
//                    }
//                }
//            } catch (PackageManager.NameNotFoundException e) {
//                Log.e("Got exception " , e.getMessage());
//            }
//        }
//
//        if (count > 0)
//            return true;
//        return false;
//    }
//
//    public static boolean hasPermissions(Context context, String...permissions) {
//        if (context != null && permissions != null) {
//            for (String permission: permissions) {
//                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//    public static Certificate getCertificateForRawResource(int resourceId, Context context) {
//        CertificateFactory cf = null;
//        Certificate ca = null;
//        Resources resources = context.getResources();
//        InputStream caInput = resources.openRawResource(resourceId);
//
//        try {
//            cf = CertificateFactory.getInstance("X.509");
//            ca = cf.generateCertificate(caInput);
//        } catch (CertificateException e) {
//        } finally {
//            try {
//                caInput.close();
//            } catch (IOException e) {
//                Log.e(TAG, "exception", e);
//            }
//        }
//
//        return ca;
//    }
//
//    public Uri getImageUri(Context inContext, Bitmap inImage) {
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
//        return Uri.parse(path);
//    }
//
//    public String getRealPathFromURI(Uri uri) {
//        String path = "";
//        if (getContentResolver() != null) {
//            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
//            if (cursor != null) {
//                cursor.moveToFirst();
//                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
//                path = cursor.getString(idx);
//                cursor.close();
//            }
//        }
//        return path;
//    }
//
//    private String networkType() {
//        TelephonyManager teleMan = (TelephonyManager)
//                getSystemService(Context.TELEPHONY_SERVICE);
//        int networkType = teleMan.getNetworkType();
//        switch (networkType) {
//
//            case TelephonyManager.NETWORK_TYPE_1xRTT: return "CDMA";
//            case TelephonyManager.NETWORK_TYPE_CDMA: return "CDMA";
//            case TelephonyManager.NETWORK_TYPE_EDGE: return "GSM";
//            case TelephonyManager.NETWORK_TYPE_EHRPD: return "CDMA";
//            case TelephonyManager.NETWORK_TYPE_EVDO_0: return "CDMA";
//            case TelephonyManager.NETWORK_TYPE_EVDO_A: return "CDMA";
//            case TelephonyManager.NETWORK_TYPE_EVDO_B: return "CDMA";
//            case TelephonyManager.NETWORK_TYPE_GPRS: return "GSM";
//            case TelephonyManager.NETWORK_TYPE_HSDPA: return "WCDMA";
//            case TelephonyManager.NETWORK_TYPE_HSPA: return "WCDMA";
//            case TelephonyManager.NETWORK_TYPE_HSPAP: return "WCDMA";
//            case TelephonyManager.NETWORK_TYPE_HSUPA: return "WCDMA";
//            case TelephonyManager.NETWORK_TYPE_LTE: return "LTE";
//            case TelephonyManager.NETWORK_TYPE_UMTS: return "WCDMA";
//            case TelephonyManager.NETWORK_TYPE_UNKNOWN: return "unknown";
//        }
//        throw new RuntimeException("New type of network");
//    }
//
//    private boolean isNetworkAvailable() {
//    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
//    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//  }
//
//
//
//    private  boolean isAndroidSystemWebViewInstalled(String pckgname,PackageManager packageManager){
//
//
//        if(isDeviceBelowNougat()) {
//            try {
//
//                if(packageManager.getApplicationInfo(pckgname,0).enabled) {
//                    //minor hack
//                    int stableVersion = 70;
//
//                    PackageInfo pi = packageManager.getPackageInfo(pckgname, 0);
//                    String shortenVersionName = pi.versionName.length() < 2 ? pi.versionName : pi.versionName.substring(0, 2);
//                    int parsedShortenVersion = Integer.parseInt(shortenVersionName);
//                    if(parsedShortenVersion < stableVersion) {
//
//                        return false;
//                    }
//
//                    return true;
//
//                }
//                return false;
//
//            }catch(PackageManager.NameNotFoundException e){
//                return false;
//            }
//
//        }
//        else {
//            return true;
//        }
//
//
//  };
//
//     private boolean isDeviceBelowNougat() {
//
//
//
//      if(VERSION.SDK_INT < Build.VERSION_CODES.N && VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//            return true;
//      }
//        return false;
//    }
//
//    public void alertBox(@NonNull Context context, @NonNull String dialogData) throws JSONException {
//
//
//
//        final JSONObject data = new JSONObject(dialogData);
//        String title = data.getString("title");
//        String message = data.getString("message");
//
//        boolean cancelable = data.getBoolean("cancelable");
//
//        boolean showButton = data.getJSONObject("button").getBoolean("show");
//
//        Log.d(TAG, "alertBox: started");
//
//
//        final AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
//
//        builder.setTitle(title);
//        builder.setMessage(message);
//         builder.setCancelable(cancelable);
//        if(showButton) {
//            final boolean allowRedirection = data.getJSONObject("button").getJSONObject("clickAction").getJSONObject("redirection").getBoolean("value");
//            final String redirectionText = data.getJSONObject("button").getJSONObject("clickAction").getJSONObject("redirection").getString("text");
//            String buttonText = data.getJSONObject("button").getString("text");
//
//            Log.d(TAG, "alertBox: cancelable false");
//
//            builder.setPositiveButton(buttonText, new DialogInterface   .OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//
//                        if(allowRedirection) {
//
//                            try {
//                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + redirectionText)));
//                            } catch (android.content.ActivityNotFoundException noPs) {
//                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + redirectionText)));
//                            }
//                        }
//                }
//            });
//        }
//
//        builder.setIcon(android.R.drawable.ic_dialog_alert);
//        appAlert = builder.create();
//        appAlert.show();
//    }
//
//    private boolean gpsEnabled(){
//        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
//
//        assert service != null;
//        Log.d(TAG, "gpsEnabled: "+service.isProviderEnabled(LocationManager.GPS_PROVIDER));
//        return service.isProviderEnabled(LocationManager.GPS_PROVIDER);
//    }
//
//
//    public class viewLoadJavaInterface {
//    Context mContext;
//    viewLoadJavaInterface(Context c) {
//      mContext = c;
//    }
//
//    @JavascriptInterface
//    public boolean isConnectionActive () {
//        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(copy.this.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//        return networkInfo != null && networkInfo.isConnected();
//    }
//
//    @JavascriptInterface
//    public void notification(String dialogData){
//
//        try {
//            alertBox(copy.this,dialogData);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @JavascriptInterface
//    public void startConversation(final String view) {
//        runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
//                if (view.equals("conversation") || view.equals("selector")) {
//                    Log.d(TAG, "run: yes");
//                    swipeToRefresh.setEnabled(false);
//                }
//            }
//        });
//    }
//
//    @JavascriptInterface
//    public void startCamera() {
//      Intent CAMERA_ONLY_INTENT = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//      if (CAMERA_ONLY_INTENT.resolveActivity(getPackageManager()) != null) {
//        startActivityForResult(CAMERA_ONLY_INTENT, CAMERA_ONLY_REQUEST);
//      }
//    }
//    @JavascriptInterface
//    public void startKeyboard() {
//      InputMethodManager inputMethodManager = (InputMethodManager)
//      getSystemService(Context.INPUT_METHOD_SERVICE);
//      inputMethodManager.showSoftInput(findViewById(R.id.activity_main_webview),
//        InputMethodManager.SHOW_FORCED);
//    }
//
//    @JavascriptInterface
//    public String getDeviceId() throws  JSONException {
//        JSONObject device = new JSONObject();
//
//        String androidId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
//        String deviceBrand  = Build.MANUFACTURER;
//        String deviceModel = Build.MODEL;
//        String osVersion = VERSION.RELEASE;
//        String deviceBaseOs = "android";
//
//        device.put("id",androidId);
//        device.put("deviceBrand",deviceBrand);
//        device.put("deviceModel",deviceModel);
//        device.put("osVersion",osVersion);
//        device.put("baseOs",deviceBaseOs);
//        device.put("appVersion",4);
//        String deviceInfo =  device.toString(4);
//        return deviceInfo;
//    };
//
//    @JavascriptInterface
//      public String getCellularData() throws JSONException {
//        String apiRequest;
//        JSONObject json = new JSONObject();
//        try {
//
//            TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//            assert tel != null;
//            String networkOperator = tel.getNetworkOperator();
//
//            if (!TextUtils.isEmpty(networkOperator)) {
//                int mcc = Integer.parseInt(networkOperator.substring(0, 3));
//                int mnc = Integer.parseInt(networkOperator.substring(3));
//
//                if (ActivityCompat.checkSelfPermission(copy.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    String[] CoarsePerm = {
//                            Manifest.permission.ACCESS_COARSE_LOCATION
//                    };
//
//                    ActivityCompat.requestPermissions(copy.this, CoarsePerm, 1);
//
//                    String NotAllowed = json.toString(4);
//                    return NotAllowed;
//                }
//
//                GsmCellLocation cellLocation = (GsmCellLocation) tel.getCellLocation();
//
//                final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//                assert wifiManager != null;
//
//                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//
//
//                int cellid = cellLocation.getCid();
//                int celllac = cellLocation.getLac();
//
//
//                List<ScanResult> apList = wifiManager.getScanResults();
//
//
//                json.put("homeMobileCountryCode", mcc);
//                json.put("homeMobileNetworkCode", mnc);
//                if (!networkType().equals("unknown")) {
//                    json.put("radioType", networkType());
//                }
//                json.put("considerIp", "true");
//
//
//                if (!apList.isEmpty()) {
//                    JSONArray wifi = new JSONArray();
//
//                    for (int i = 0; i < apList.size(); i++) {
//                        JSONObject aps = new JSONObject();
//
//                        aps.put("macAddress", apList.get(i).BSSID);
//                        aps.put("signalStrength", Integer.valueOf(apList.get(i).level));
//                        wifi.put(aps);
//                    }
//                    json.put("wifiAccessPoints", wifi);
//
//                }
//
//                json.put("carrier", tel.getNetworkOperatorName());
//                if(cellid >= 0) {
//                JSONArray towers = new JSONArray();
//
//                JSONObject cells = new JSONObject();
//                cells.put("cellId", cellid);
//                cells.put("locationAreaCode", celllac);
//                cells.put("mobileCountryCode", mcc);
//                cells.put("mobileNetworkCode", mnc);
//                towers.put(cells);
//                json.put("cellTowers", towers);
//                }
//            }
//        } catch (Exception e){
//           e.printStackTrace();
//        }
//        apiRequest = json.toString(4);
//        return  apiRequest;
//    }
//
//
//    @JavascriptInterface
//      public String isEnabled() throws  JSONException{
//        int PERMISSION_ALL = 1;
//        String[] PERMISSIONS = {
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//
//        };
//        JSONObject services = new JSONObject();
//
//
//         if (!gpsEnabled()){
//            services.put("active",false);
//            services.put("name","gps");
//            return  services.toString(4);
//        }
//         if(!hasPermissions(copy.this, PERMISSIONS)){
//
//            services.put("active",false);
//            services.put("name","permission");
//            return  services.toString(4);
//        }
//        services.put("active",true);
//        services.put("name","both");
//        return services.toString(4);
//
//    }
//
//    @JavascriptInterface
//      public void stopRefreshing(final boolean stopRefreshing){
//        Log.d(TAG, "stopRefreshing: "+stopRefreshing);
//        Log.d(TAG, "stopRefreshing: "+ copy.this.swipeToRefresh.canChildScrollUp());
//        runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
//               swipeToRefresh.setRefreshing(!stopRefreshing);
//            }
//        });
//    }
//  }
//
//  @Override
//  public void onBackPressed() {
//
//    if (mWebView.canGoBack()) {
//      mWebView.goBack(); // emulates back history
//    } else {
//      super.onBackPressed();
//    }
//  };
//}
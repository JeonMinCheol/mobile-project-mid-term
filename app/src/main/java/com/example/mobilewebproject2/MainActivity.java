package com.example.mobilewebproject2;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.internal.utils.ImageUtil;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private final int CAMERA_REQUEST_CODE = 100;
    private final int GPS_REQUEST_CODE = 101;
    private final String epsg="epsg:4326";
    private final String GEO_API_KEY = "434197BE-5034-31E3-B9BA-D4C187C1B393";
    private String searchPoint;
    private String currentLocation;
    private double latitude;
    private double longitude;
    private ToggleButton button;
    private PreviewView previewView;
    private TextView textView;
    private ProcessCameraProvider processCameraProvider;
    private ImageCapture imageCapture;
    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.toggleButton);
        previewView = findViewById(R.id.previewView);
        textView = findViewById(R.id.textView);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://172.21.94.197:5000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        post = retrofit.create(Post.class);

        try {
            processCameraProvider = ProcessCameraProvider.getInstance(this).get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        int CAMERA_PERMISSION = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        int ACCESS_FINE_LOCATION_PERMISSION = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int ACCESS_COARSE_LOCATION_PERMISSION = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        // CAMERA 권한 확인
        if (CAMERA_PERMISSION != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this,"권한 승인이 필요합니다",Toast.LENGTH_LONG).show();

            // 사용자가 권한 할당을 수락
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.CAMERA) == false){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            }
        }

        // GPS 권한 확인
        if (ACCESS_FINE_LOCATION_PERMISSION != PackageManager.PERMISSION_GRANTED && ACCESS_COARSE_LOCATION_PERMISSION != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this,"권한 승인이 필요합니다",Toast.LENGTH_LONG).show();

            // 사용자가 권한 할당을 수락
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == false){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, GPS_REQUEST_CODE);
            }
        }

        final LocationListener gpsLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if(button.isChecked())
                {
                    latitude = location.getLatitude();   // 위도
                    longitude = location.getLongitude(); // 경도
                    searchPoint = longitude+","+latitude;
                    getAddress();
                }
            }
        };

        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000,
                1,
                gpsLocationListener);

        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Timer timer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        takePicture();

                        // 사진 + 위치 전송 메서드 추가
                        if(button.isChecked()) {
                            getAddress();
                            createPost();
                        }
                    }
                };

                if(button.isChecked()){
                    bindPreview();
                    bindImageCapture();

                    if(location != null){
                        latitude = location.getLatitude();   // 위도
                        longitude = location.getLongitude(); // 경도
                        searchPoint = longitude+","+latitude;

                        if(textView.getText() != null)
                            textView.setText(currentLocation);
                    }

                    timer.schedule(timerTask,1000,3000);
                }
                else{
                    timerTask.cancel();
                    timer.cancel();
                    textView.setText("중지 중..");

                    onPause();
                }
            }
        });
    }

    public Boolean getAddress() {
        try {
            String url = "https://api.vworld.kr/req/address?service=address&request=getAddress&format=json&type=PARCEL&key="
                    +GEO_API_KEY + "&crs=" + epsg + "&point=" + searchPoint;

            OkHttpClient client = new OkHttpClient();

            Request.Builder builder = new Request.Builder().url(url).get();
            Request req = builder.build();

            okhttp3.Response response = client.newCall(req).execute();
            if(response.isSuccessful()) {
                String userString = response.body().string();
                JSONObject jsonObject = new JSONObject(userString);

                currentLocation = jsonObject
                        .getJSONObject("response")
                        .getJSONArray("result")
                        .getJSONObject(0)
                        .get("text")
                        .toString();
            }
            else
                System.out.println("Error Occurred!");

            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public Image takePicture() {
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            @ExperimentalGetImage
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                Image mediaImage = image.getImage();

                if(mediaImage != null) {
                    ByteBuffer buffer = mediaImage.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

                    String filePath = getExternalFilesDir(null).getAbsolutePath() + File.separator + "my_image.jpg";
                    File file = new File(filePath);

                    try {
                        FileOutputStream fos = new FileOutputStream(file);

                        // Bitmap을 JPEG 형식으로 파일에 저장
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                        fos.flush();
                        fos.close();

                        // 파일 저장 성공
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 파일 저장 실패
                    }
                    image.close();
                }

                super.onCaptureSuccess(image);
            }
        });
        return null;
    }

    @Override
    protected void onPause() {
        processCameraProvider.unbindAll();
        super.onPause();
    }

    private void bindPreview() {
        // 후면 카메라 사용
        CameraSelector cameraSelector = new CameraSelector
                .Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview
                .Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        processCameraProvider.bindToLifecycle(this, cameraSelector,preview);
    }

    private void bindImageCapture() {
        // 후면 카메라 사용
        CameraSelector cameraSelector = new CameraSelector
                .Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        imageCapture = new ImageCapture
                .Builder()
                .build();

        processCameraProvider.bindToLifecycle(this, cameraSelector, imageCapture);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                // 요청이 취소되었다면 grantResults가 비어있음
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "승인이 허가되어 있습니다.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "아직 승인받지 않았습니다.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void createPost() {
        RequestBody title = RequestBody.create(MediaType.parse("text/plain"), "번째 위반 차량 발견");
        RequestBody text = RequestBody.create(MediaType.parse("text/plain"), currentLocation);

        String filePath = getExternalFilesDir(null).getAbsolutePath() + File.separator + "my_image.jpg";

        File imageFile = new File(filePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
        MultipartBody.Part image = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

        Call call = post.sendData(title, text, image);

        call.enqueue(new Callback<ReceiveDTO>() {
            @Override
            public void onResponse(Call<ReceiveDTO> call, Response<ReceiveDTO> response) {
                if(!response.isSuccessful()) {
                    Log.d("response error", String.valueOf(response.code()));
                }
                else {
                    ReceiveDTO receiveDTO = response.body();

                    Log.d("response body", receiveDTO.result);
                }
            }

            @Override
            public void onFailure(Call<ReceiveDTO> call, Throwable t) {
                Log.d("onFailure", String.valueOf(t.getMessage()));
            }
        });
    }
}
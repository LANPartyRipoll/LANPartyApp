package cat.lanpartyripoll.lanpartyripoll;

import android.Manifest;
import android.accounts.NetworkErrorException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnCamera;
    private TextView tvInscripcio;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCamera = (Button) findViewById(R.id.btnCamera);
        tvInscripcio = (TextView) findViewById(R.id.tvInscripcio);
        btnCamera.setOnClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, Constants.PERMISSION_REQUEST_INTERNET);
                    else
                        OpenCamera();
                else
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                break;
            case Constants.PERMISSION_REQUEST_INTERNET:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    OpenCamera();
                else
                    Toast.makeText(this, "Please grant internet permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    private void OpenCamera() {
        Intent intent;
        intent = new Intent(this, SimpleScannerActivity.class);
        startActivityForResult(intent, Constants.QR_CAMERA_RESULT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case Constants.QR_CAMERA_RESULT:
                if (resultCode == RESULT_OK) {
                    Bundle res = data.getExtras();
                    String result = res.getString(String.valueOf(Constants.QR_CAMERA_RESULT));
                    new GetParticipant().execute(result);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnCamera:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.PERMISSION_REQUEST_CAMERA);

                }
                else
                    OpenCamera();
                break;

        }
    }

    class GetParticipant extends AsyncTask<String, Void, String>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            tvInscripcio.setText("");
        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder reposta = new StringBuilder("");
            try{
                String qrCode=params[0];
                String json=GetData(qrCode);
                if (json == null)
                    throw new NetworkErrorException("El servei no es troba disponible o el dispoitiu no té accés a internet.");
                JSONObject jsonObj=new JSONObject(json);
                reposta.append("Participant: ").append(jsonObj.getString("nom")).append(" ").append(jsonObj.getString("cognom"));
                reposta.append(System.getProperty("line.separator"));
                reposta.append("Categoria: ").append(jsonObj.getString("categoria"));
                reposta.append(System.getProperty("line.separator"));
                reposta.append("Equip: ").append(jsonObj.getString("equip"));
                reposta.append(System.getProperty("line.separator"));
                reposta.append("Nick: ").append(jsonObj.getString("nick"));
                reposta.append(System.getProperty("line.separator"));
                reposta.append("Major: ").append(jsonObj.getString("major"));
                reposta.append(System.getProperty("line.separator"));
                reposta.append("Pagat: ").append(jsonObj.getString("pagat"));
            } catch (Exception ex) {
                reposta.append("ERROR: ").append(ex.getMessage());
            }
            return reposta.toString();
        }

        @Override
        protected void onPostExecute(String inscripcio) {
            super.onPostExecute(inscripcio);
            progressBar.setVisibility(View.INVISIBLE);
            tvInscripcio.setText(inscripcio.toString());
        }

        private String GetData(String qrCode) {
            String url = "http://url/inscripcions/" + qrCode;
            HttpURLConnection connexio;
            InputStream inputStream;
            try{
                connexio=(HttpURLConnection)(new URL(url).openConnection());
                connexio.setRequestMethod("GET");
                connexio.connect();

                StringBuilder buffer=new StringBuilder();
                inputStream=connexio.getInputStream();

                BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));

                String linia;
                while ((linia=br.readLine())!=null)
                {
                    buffer.append(linia);
                }

                br.close();
                inputStream.close();
                connexio.disconnect();

                return buffer.toString();


            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Connection failed", e.getMessage());
            }
            return null;

        }
    }
}

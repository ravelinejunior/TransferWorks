package br.com.transferwork.activities.corridas;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Objects;

import br.com.transferwork.R;
import br.com.transferwork.activities.usuario.RequisicoesMotoristaActivity;
import br.com.transferwork.config.ConfiguracaoFirebase;
import br.com.transferwork.helper.UsuarioFirebase;

public class CorridasActivity extends AppCompatActivity implements OnMapReadyCallback {
    //maps
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localMotorista;
    private GoogleMap gMap;

    //firebase
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference referenceFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corridas);

        user = UsuarioFirebase.getUsuarioAtual();
        referenceFirebase = ConfiguracaoFirebase.getDatabaseReference();
        carregarElementos();

    }

    public void aceitarCorrida(View view) {

    }

    public void carregarElementos(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.corrida);
        toolbar.setLogo(android.R.drawable.ic_menu_mapmode);
        toolbar.setPadding(20,0,10,0);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setLeft(10);

        auth = ConfiguracaoFirebase.getFirebaseAuth();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        recuperarLocalizacaoUsuario();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void recuperarLocalizacaoUsuario() {


        //configurar o location manager para recuperar os serviços de gps
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //recuperar lat e long
                double latitudeUsuario = location.getLatitude();
                double longitudeUsuario = location.getLongitude();
                localMotorista = new LatLng(latitudeUsuario, longitudeUsuario);

                gMap.clear();
                //criar um marcador
                gMap.addMarker(new MarkerOptions()
                        .position(localMotorista)
                        .title("Meu local")
                        .snippet("Local que " + Objects.requireNonNull(auth.getCurrentUser()).getDisplayName() + " está.")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
                );

                //criar um zoom inicial
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localMotorista, 18));

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //Solicitar atualizações de localização do usuario
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 3, locationListener);

        }

    }

    @Override
    public boolean onNavigateUp() {
        deslogarBackPressed();
        return super.onNavigateUp();
    }

    @Override
    public void onBackPressed() {
        deslogarBackPressed();
        //super.onBackPressed();
    }

    private Context getContext(){
        return CorridasActivity.this;
    }

    private void deslogarBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.deslogar_usuario);
        builder.setMessage(R.string.sair_app_message);
        builder.setIcon(android.R.drawable.ic_menu_info_details);
        builder.setPositiveButton(R.string.confirmar_message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                auth = ConfiguracaoFirebase.getFirebaseAuth();
                auth.signOut();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancelar_message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_passageiro, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_sair:
                deslogarBackPressed();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}

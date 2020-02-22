package br.com.transferwork.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import br.com.transferwork.R;
import br.com.transferwork.activities.usuario.CadastroActivity;
import br.com.transferwork.activities.usuario.LoginActivity;
import br.com.transferwork.helper.Permissoes;
import br.com.transferwork.helper.UsuarioFirebase;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //removendo a ActionBar
        getSupportActionBar().hide();

        //validando as permissoes
        Permissoes.validarPermissoes(permissoes,this,1);
        //chamar metodo onRequestPermissionsResult



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissoesResultado:grantResults){
            if (permissoesResultado == PackageManager.PERMISSION_DENIED){
                //caso a permissão tenha sido negada
                alertaValidacaoPermissoes();
            }
        }
    }

    public void alertaValidacaoPermissoes(){
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);
        aBuilder.setTitle("Permissões");
        aBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        aBuilder.setCancelable(false);
        aBuilder.setMessage("Voce deve aceitar as permissões");
        aBuilder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog a = aBuilder.create();
        a.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        UsuarioFirebase.redirecionaTipoUsuario(MainActivity.this);
    }

    public void abrirCadastroAct(View v){
        Intent i = new Intent(this, CadastroActivity.class);
        startActivity(i);
    }

    public void abrirLoginAct(View v){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
}

package br.com.transferwork.activities.usuario;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import br.com.transferwork.R;
import br.com.transferwork.config.ConfiguracaoFirebase;
import br.com.transferwork.helper.UsuarioFirebase;

public class RequisicoesMotoristaActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requisicoes_motorista);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        auth = ConfiguracaoFirebase.getFirebaseAuth();
        auth.signOut();
    }
}

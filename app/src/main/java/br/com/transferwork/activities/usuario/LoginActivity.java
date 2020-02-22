package br.com.transferwork.activities.usuario;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import br.com.transferwork.R;
import br.com.transferwork.config.ConfiguracaoFirebase;
import br.com.transferwork.helper.UsuarioFirebase;
import br.com.transferwork.model.Usuario;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText inputEditEmailLogin;
    private TextInputEditText inputEditSenhaLogin;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        carregarElementos();


    }

    public void carregarElementos(){
        inputEditEmailLogin = findViewById(R.id.inputText_email_logar);
        inputEditSenhaLogin = findViewById(R.id.inputEditText_senha_logar);
    }

    public void logarBotao(View v){

        //validar o login do usuario
        String textoEmailDigitado = inputEditEmailLogin.getText().toString();
        String textoSenhaDigitado = inputEditSenhaLogin.getText().toString();

        try{
            if (!textoEmailDigitado.isEmpty()){
                if (!textoSenhaDigitado.isEmpty()){

                    //criar objeto usuario
                    Usuario usuario = new Usuario();
                    usuario.setEmailUsuario(textoEmailDigitado);
                    usuario.setSenhaUsuario(textoSenhaDigitado);
                    logarUsuario(usuario,v);

                }else{
                    Snackbar.make(v,"Digite uma senha!",Snackbar.LENGTH_SHORT).show();
                }
            }else{
                Snackbar.make(v,"Digite um email!",Snackbar.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void logarUsuario(final Usuario user, final View view){
        //verificar se existe um usuario
        auth = ConfiguracaoFirebase.getFirebaseAuth();
        auth.signInWithEmailAndPassword(user.getEmailUsuario(),user.getSenhaUsuario())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //metodo para redirecionar tipo usuario para cada activity
                            UsuarioFirebase.redirecionaTipoUsuario(LoginActivity.this);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(view,"Erro ao logar: "+e.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });
    }




}

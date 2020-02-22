package br.com.transferwork.activities.usuario;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import br.com.transferwork.R;
import br.com.transferwork.config.ConfiguracaoFirebase;
import br.com.transferwork.helper.UsuarioFirebase;
import br.com.transferwork.model.Usuario;

public class CadastroActivity extends AppCompatActivity {
    private TextInputEditText nomeCadastroTexto;
    private TextInputEditText emailCadastroTexto;
    private TextInputEditText senhaCadastroTexto;
    private Button botaoCadadstrar;
    private Switch botaoSwitchCadastrar;

    //firebase
    private DatabaseReference referenciaDatabase;
    private FirebaseAuth autenticacaoFirebase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        carregarElementos();

    }

    public void validarCamposCadastro(View view){
        //recuperar os textos
        String nomeValidado = nomeCadastroTexto.getText().toString();
        String emailValidado = emailCadastroTexto.getText().toString();
        String senhaValidado = senhaCadastroTexto.getText().toString();

        if (!nomeValidado.isEmpty()){ // nome
            if (!emailValidado.isEmpty()){ // email
                if (!senhaValidado.isEmpty()){ // senha

                    //validando os dados do usuario
                    Usuario usuario = new Usuario();
                    usuario.setNomeUsuario(nomeValidado);
                    usuario.setEmailUsuario(emailValidado);
                    usuario.setSenhaUsuario(senhaValidado);
                    usuario.setTipoUsuario(verificarTipoUsuario());

                    cadastrarUsuario(usuario);


                }else{
                    Toast.makeText(this, "Preencha o campo senha", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Preencha o campo email", Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(this, "Preencha o campo nome", Toast.LENGTH_SHORT).show();
        }

    }

    public String verificarTipoUsuario(){

        return botaoSwitchCadastrar.isChecked()? "M" : "P";//se for verdadeiro retorna o primeiro caso nao, retorna o segundo
    }

    public void cadastrarUsuario(final Usuario usuario){
        autenticacaoFirebase = ConfiguracaoFirebase.getFirebaseAuth();
        autenticacaoFirebase.createUserWithEmailAndPassword(
                usuario.getEmailUsuario(),usuario.getSenhaUsuario()
        ).addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
               try{
                   if (task.isSuccessful()){
                       //recuperar a id do usuario
                       String idUsuarioAutenticado = task.getResult().getUser().getUid();
                       usuario.setId(idUsuarioAutenticado);

                       //metodo criado na classe usuario
                       usuario.salvarUsuario();

                       //atualiza nome no UserProfile do firebase
                       UsuarioFirebase.atualizarNomeUsuario(usuario.getNomeUsuario());

                    /*
                    Caso ususario seja passageiro vai para o maps
                    se nao, vai para tela de requisições
                   */

                       if (verificarTipoUsuario() == "P"){
                           startActivity(new Intent(CadastroActivity.this,PassageiroActivity.class));
                           Toast.makeText(CadastroActivity.this, "Passageiro cadastrado com sucesso.", Toast.LENGTH_SHORT).show();
                       }else{
                           startActivity(new Intent(CadastroActivity.this,RequisicoesMotoristaActivity.class));
                           Toast.makeText(CadastroActivity.this, "Motorista cadastrado com sucesso.", Toast.LENGTH_SHORT).show();
                       }

                   }
               }catch (Exception e){
                   e.printStackTrace();
               }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CadastroActivity.this, "Erro ao cadastrar usuario. "+e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }


    public void carregarElementos(){
        nomeCadastroTexto = findViewById(R.id.inputText_nome_cadastrar_id);
        emailCadastroTexto = findViewById(R.id.inputText_email_cadastrar_id);
        senhaCadastroTexto = findViewById(R.id.inputText_senha_cadastrar_id);
        botaoCadadstrar = findViewById(R.id.botao_cadastrar_id);
        botaoSwitchCadastrar = findViewById(R.id.Switch_btn_cadastrar_id);


    }
}

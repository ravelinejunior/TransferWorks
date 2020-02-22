package br.com.transferwork.model;

import android.view.View;


import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;

import br.com.transferwork.config.ConfiguracaoFirebase;

public class Requisicao {

    private String id;
    private String status;
    private Usuario passageiro;
    private Usuario motorista;
    private DestinoDigitado destinoDigitado;

    public Requisicao() {

    }

    public static final String STATUS_AGUARDANDO = "aguardando"; // quando usuario chama o carro
    public static final String STATUS_A_CAMINHO = "acaminho"; // motorista está a caminho apos aceitar
    public static final String STATUS_VIAGEM = "viagem"; // usuario está na viagem
    public static final String STATUS_FINALIZADO = "finalizado"; // usuario finalizou

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Usuario getPassageiro() {
        return passageiro;
    }

    public void setPassageiro(Usuario passageiro) {
        this.passageiro = passageiro;
    }

    public Usuario getMotorista() {
        return motorista;
    }

    public void setMotorista(Usuario motorista) {
        this.motorista = motorista;
    }

    public DestinoDigitado getDestinoDigitado() {
        return destinoDigitado;
    }

    public void setDestinoDigitado(DestinoDigitado destinoDigitado) {
        this.destinoDigitado = destinoDigitado;
    }

    public void salvar(View view){
        DatabaseReference databaseReference = ConfiguracaoFirebase.getDatabaseReference();
        DatabaseReference requisicoesDatabase = databaseReference.child("requisicoes");

        //recuperar id da requisição
        String idRequisicao = requisicoesDatabase.push().getKey();
        setId(idRequisicao);

        requisicoesDatabase.child(getId()).setValue(this);
        Snackbar.make(view,"Salvo com sucesso!",Snackbar.LENGTH_SHORT).show();

    }
}

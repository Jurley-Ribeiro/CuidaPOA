package com.example.cuidapoa.model;

import java.util.List;

public class UBS {
    private String id;
    private String nome;
    private String endereco;
    private String bairro;
    private String telefone;
    private String horarioFuncionamento;
    private String email;
    private List<String> servicos;
    private double latitude;
    private double longitude;
    private boolean abertaAgora;

    // Construtor vazio (necessário para Firebase)
    public UBS() {
    }

    // Construtor básico
    public UBS(String nome, String endereco, String bairro, String telefone, String horarioFuncionamento) {
        this.nome = nome;
        this.endereco = endereco;
        this.bairro = bairro;
        this.telefone = telefone;
        this.horarioFuncionamento = horarioFuncionamento;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getHorarioFuncionamento() {
        return horarioFuncionamento;
    }

    public void setHorarioFuncionamento(String horarioFuncionamento) {
        this.horarioFuncionamento = horarioFuncionamento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getServicos() {
        return servicos;
    }

    public void setServicos(List<String> servicos) {
        this.servicos = servicos;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isAbertaAgora() {
        return abertaAgora;
    }

    public void setAbertaAgora(boolean abertaAgora) {
        this.abertaAgora = abertaAgora;
    }

    // Método auxiliar para endereço completo
    public String getEnderecoCompleto() {
        return endereco + " - " + bairro;
    }
}
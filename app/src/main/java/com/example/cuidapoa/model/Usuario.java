package com.example.cuidapoa.model;

public class Usuario {
    private String id;
    private String nome;
    private String email;
    private String tipo; // "admin" ou "cidadao"

    // Construtor vazio (necessário para Firebase)
    public Usuario() {
    }

    // Construtor completo
    public Usuario(String nome, String email, String tipo) {
        this.nome = nome;
        this.email = email;
        this.tipo = tipo;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    // Método auxiliar
    public boolean isAdmin() {
        return "admin".equals(tipo);
    }
}
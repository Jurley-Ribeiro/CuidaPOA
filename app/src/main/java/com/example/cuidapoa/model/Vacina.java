package com.example.cuidapoa.model;

public class Vacina {
    private String id;
    private String nome;
    private String descricao;
    private String faixaEtaria;
    private int numeroDoses;
    private boolean disponivel;
    private String observacoes;

    // Construtor vazio (necessário para Firebase)
    public Vacina() {
    }

    // Construtor completo
    public Vacina(String nome, String descricao, String faixaEtaria,
                  int numeroDoses, boolean disponivel) {
        this.nome = nome;
        this.descricao = descricao;
        this.faixaEtaria = faixaEtaria;
        this.numeroDoses = numeroDoses;
        this.disponivel = disponivel;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getFaixaEtaria() {
        return faixaEtaria;
    }

    public void setFaixaEtaria(String faixaEtaria) {
        this.faixaEtaria = faixaEtaria;
    }

    public int getNumeroDoses() {
        return numeroDoses;
    }

    public void setNumeroDoses(int numeroDoses) {
        this.numeroDoses = numeroDoses;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
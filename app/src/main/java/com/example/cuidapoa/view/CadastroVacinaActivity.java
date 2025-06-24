package com.example.cuidapoa.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.cuidapoa.R;
import com.example.cuidapoa.model.Vacina;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CadastroVacinaActivity extends AppCompatActivity {

    // Views
    private Toolbar toolbar;
    private TextInputLayout tilNome, tilDescricao, tilFaixaEtaria, tilDoses, tilObservacoes;
    private TextInputEditText etNome, etDescricao, etFaixaEtaria, etObservacoes;
    private AutoCompleteTextView etDoses;
    private MaterialSwitch switchDisponivel;
    private Button btnSalvar;
    private ProgressBar progressBar;

    // Dados
    private Vacina vacinaEditando = null;
    private boolean modoEdicao = false;

    // Lista temporária (simular banco de dados)
    public static List<Vacina> vacinasCadastradas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_vacina);

        inicializarViews();
        configurarToolbar();
        configurarDropdownDoses();
        configurarListeners();

        // Verificar se é edição
        verificarModoEdicao();
    }

    private void inicializarViews() {
        toolbar = findViewById(R.id.toolbar_cadastro);

        // TextInputLayouts
        tilNome = findViewById(R.id.til_nome_vacina);
        tilDescricao = findViewById(R.id.til_descricao_vacina);
        tilFaixaEtaria = findViewById(R.id.til_faixa_etaria);
        tilDoses = findViewById(R.id.til_doses);
        tilObservacoes = findViewById(R.id.til_observacoes);

        // EditTexts
        etNome = findViewById(R.id.et_nome_vacina);
        etDescricao = findViewById(R.id.et_descricao_vacina);
        etFaixaEtaria = findViewById(R.id.et_faixa_etaria);
        etDoses = findViewById(R.id.et_doses);
        etObservacoes = findViewById(R.id.et_observacoes);

        // Outros
        switchDisponivel = findViewById(R.id.switch_disponivel);
        btnSalvar = findViewById(R.id.btn_salvar_vacina);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cadastrar Vacina");
        }
    }

    private void configurarDropdownDoses() {
        // Configurar dropdown para número de doses
        String[] doses = {"1", "2", "3", "4", "5", "Dose única", "Anual"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                doses
        );
        etDoses.setAdapter(adapter);
    }

    private void configurarListeners() {
        // Limpar erros ao digitar
        etNome.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tilNome.setError(null);
        });

        etDescricao.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tilDescricao.setError(null);
        });

        etFaixaEtaria.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tilFaixaEtaria.setError(null);
        });

        etDoses.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tilDoses.setError(null);
        });

        // Botão salvar
        btnSalvar.setOnClickListener(v -> validarESalvar());
    }

    private void verificarModoEdicao() {
        // Verificar se recebeu uma vacina para editar
        String vacinaId = getIntent().getStringExtra("vacina_id");
        if (vacinaId != null) {
            modoEdicao = true;
            carregarVacinaParaEdicao(vacinaId);
        }
    }

    private void carregarVacinaParaEdicao(String vacinaId) {
        // Simular busca da vacina
        for (Vacina v : vacinasCadastradas) {
            if (v.getId().equals(vacinaId)) {
                vacinaEditando = v;
                break;
            }
        }

        if (vacinaEditando != null) {
            preencherCampos();
            getSupportActionBar().setTitle("Editar Vacina");
            btnSalvar.setText("Atualizar");
        }
    }

    private void preencherCampos() {
        etNome.setText(vacinaEditando.getNome());
        etDescricao.setText(vacinaEditando.getDescricao());
        etFaixaEtaria.setText(vacinaEditando.getFaixaEtaria());
        etDoses.setText(String.valueOf(vacinaEditando.getNumeroDoses()));
        etObservacoes.setText(vacinaEditando.getObservacoes());
        switchDisponivel.setChecked(vacinaEditando.isDisponivel());
    }

    private void validarESalvar() {
        // Obter valores
        String nome = etNome.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();
        String faixaEtaria = etFaixaEtaria.getText().toString().trim();
        String dosesStr = etDoses.getText().toString().trim();
        String observacoes = etObservacoes.getText().toString().trim();
        boolean disponivel = switchDisponivel.isChecked();

        // Resetar erros
        tilNome.setError(null);
        tilDescricao.setError(null);
        tilFaixaEtaria.setError(null);
        tilDoses.setError(null);

        // Validar
        boolean valido = true;

        if (TextUtils.isEmpty(nome)) {
            tilNome.setError("Nome da vacina é obrigatório");
            valido = false;
        } else if (nome.length() < 3) {
            tilNome.setError("Nome deve ter pelo menos 3 caracteres");
            valido = false;
        }

        if (TextUtils.isEmpty(descricao)) {
            tilDescricao.setError("Descrição é obrigatória");
            valido = false;
        } else if (descricao.length() < 10) {
            tilDescricao.setError("Descrição deve ter pelo menos 10 caracteres");
            valido = false;
        }

        if (TextUtils.isEmpty(faixaEtaria)) {
            tilFaixaEtaria.setError("Faixa etária é obrigatória");
            valido = false;
        }

        if (TextUtils.isEmpty(dosesStr)) {
            tilDoses.setError("Número de doses é obrigatório");
            valido = false;
        }

        // Converter doses
        int doses = 1;
        try {
            if (dosesStr.equals("Dose única")) {
                doses = 1;
            } else if (dosesStr.equals("Anual")) {
                doses = -1; // Indicador especial para dose anual
            } else {
                doses = Integer.parseInt(dosesStr);
            }
        } catch (NumberFormatException e) {
            tilDoses.setError("Número de doses inválido");
            valido = false;
        }

        if (valido) {
            salvarVacina(nome, descricao, faixaEtaria, doses, observacoes, disponivel);
        }
    }

    private void salvarVacina(String nome, String descricao, String faixaEtaria,
                              int doses, String observacoes, boolean disponivel) {
        mostrarCarregando(true);

        // Simular delay de rede
        btnSalvar.postDelayed(() -> {
            if (modoEdicao && vacinaEditando != null) {
                // Atualizar vacina existente
                vacinaEditando.setNome(nome);
                vacinaEditando.setDescricao(descricao);
                vacinaEditando.setFaixaEtaria(faixaEtaria);
                vacinaEditando.setNumeroDoses(doses);
                vacinaEditando.setObservacoes(observacoes);
                vacinaEditando.setDisponivel(disponivel);

                mostrarMensagemSucesso("Vacina atualizada com sucesso!");
            } else {
                // Criar nova vacina
                Vacina novaVacina = new Vacina(nome, descricao, faixaEtaria, doses, disponivel);
                novaVacina.setId(UUID.randomUUID().toString());
                novaVacina.setObservacoes(observacoes);

                // Adicionar à lista temporária
                vacinasCadastradas.add(novaVacina);

                mostrarMensagemSucesso("Vacina cadastrada com sucesso!");
            }

            mostrarCarregando(false);

            // Voltar após 1 segundo
            btnSalvar.postDelayed(() -> {
                setResult(RESULT_OK);
                finish();
            }, 1000);

        }, 1500);
    }

    private void mostrarCarregando(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        btnSalvar.setEnabled(!mostrar);

        // Desabilitar campos durante carregamento
        etNome.setEnabled(!mostrar);
        etDescricao.setEnabled(!mostrar);
        etFaixaEtaria.setEnabled(!mostrar);
        etDoses.setEnabled(!mostrar);
        etObservacoes.setEnabled(!mostrar);
        switchDisponivel.setEnabled(!mostrar);
    }

    private void mostrarMensagemSucesso(String mensagem) {
        Snackbar.make(btnSalvar, mensagem, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.success))
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Verificar se há alterações não salvas
        if (houveAlteracoes()) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Descartar alterações?")
                    .setMessage("Existem alterações não salvas. Deseja realmente sair?")
                    .setPositiveButton("Sair", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private boolean houveAlteracoes() {
        // Verificar se algum campo foi preenchido
        return !TextUtils.isEmpty(etNome.getText()) ||
                !TextUtils.isEmpty(etDescricao.getText()) ||
                !TextUtils.isEmpty(etFaixaEtaria.getText()) ||
                !TextUtils.isEmpty(etDoses.getText()) ||
                !TextUtils.isEmpty(etObservacoes.getText());
    }
}

package com.example.cuidapoa.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
import com.example.cuidapoa.model.UBS;
import com.example.cuidapoa.repository.UBSRepository;
import com.example.cuidapoa.util.PhoneMaskTextWatcher;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

public class CadastroUBSActivity extends AppCompatActivity {

    // Views
    private Toolbar toolbar;
    private TextInputLayout tilNome, tilEndereco, tilBairro, tilTelefone, tilHorario, tilEmail;
    private TextInputEditText etNome, etEndereco, etTelefone, etHorario, etEmail;
    private AutoCompleteTextView etBairro;
    private ChipGroup chipGroupServicos;
    private MaterialSwitch switchAberta;
    private Button btnSalvar;
    private ProgressBar progressBar;

    // Dados
    private UBS ubsEditando = null;
    private boolean modoEdicao = false;
    private List<String> servicosSelecionados = new ArrayList<>();

    // Firebase
    private UBSRepository ubsRepository;

    // Lista de serviços disponíveis
    private static final String[] SERVICOS_DISPONIVEIS = {
            "Clínica Geral", "Pediatria", "Ginecologia", "Odontologia",
            "Vacinação", "Farmácia", "Psicologia", "Nutrição",
            "Fisioterapia", "Enfermagem", "Coleta de Exames"
    };

    // Lista de bairros de Porto Alegre
    private static final String[] BAIRROS_POA = {
            "Centro Histórico", "Floresta", "Vila Nova", "Restinga",
            "Lomba do Pinheiro", "Partenon", "Belém Novo", "Rubem Berta",
            "Cidade Baixa", "Menino Deus", "Santana", "Azenha",
            "Jardim Botânico", "Petrópolis", "Tristeza", "Vila Ipiranga"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_ubs);

        // Inicializar Repository
        ubsRepository = UBSRepository.getInstance();

        inicializarViews();
        configurarToolbar();
        configurarDropdownBairro();
        configurarChipsServicos();
        configurarListeners();

        // Verificar se é edição
        verificarModoEdicao();
    }

    private void inicializarViews() {
        toolbar = findViewById(R.id.toolbar_cadastro);

        // TextInputLayouts
        tilNome = findViewById(R.id.til_nome_ubs);
        tilEndereco = findViewById(R.id.til_endereco_ubs);
        tilBairro = findViewById(R.id.til_bairro_ubs);
        tilTelefone = findViewById(R.id.til_telefone_ubs);
        tilHorario = findViewById(R.id.til_horario_ubs);
        tilEmail = findViewById(R.id.til_email_ubs);

        // EditTexts
        etNome = findViewById(R.id.et_nome_ubs);
        etEndereco = findViewById(R.id.et_endereco_ubs);
        etBairro = findViewById(R.id.et_bairro_ubs);
        etTelefone = findViewById(R.id.et_telefone_ubs);
        etHorario = findViewById(R.id.et_horario_ubs);
        etEmail = findViewById(R.id.et_email_ubs);

        // Outros
        chipGroupServicos = findViewById(R.id.chip_group_servicos);
        switchAberta = findViewById(R.id.switch_aberta);
        btnSalvar = findViewById(R.id.btn_salvar_ubs);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cadastrar UBS");
        }
    }

    private void configurarDropdownBairro() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                BAIRROS_POA
        );
        etBairro.setAdapter(adapter);
    }

    private void configurarChipsServicos() {
        for (String servico : SERVICOS_DISPONIVEIS) {
            Chip chip = new Chip(this);
            chip.setText(servico);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.background_light);
            chip.setChipStrokeColorResource(R.color.primary_teal);
            chip.setChipStrokeWidth(2);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    servicosSelecionados.add(servico);
                    chip.setChipBackgroundColorResource(R.color.primary_teal_light);
                } else {
                    servicosSelecionados.remove(servico);
                    chip.setChipBackgroundColorResource(R.color.background_light);
                }
            });

            chipGroupServicos.addView(chip);
        }
    }

    private void configurarListeners() {
        // Adicionar máscara ao campo de telefone
        etTelefone.addTextChangedListener(new PhoneMaskTextWatcher(etTelefone));

        // Limpar erros ao digitar
        etNome.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tilNome.setError(null);
        });

        etEndereco.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tilEndereco.setError(null);
        });

        etBairro.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tilBairro.setError(null);
        });

        etTelefone.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tilTelefone.setError(null);
        });

        etHorario.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tilHorario.setError(null);
        });

        // Botão salvar
        btnSalvar.setOnClickListener(v -> validarESalvar());
    }

    private void verificarModoEdicao() {
        String ubsId = getIntent().getStringExtra("ubs_id");
        if (ubsId != null) {
            modoEdicao = true;
            carregarUBSParaEdicao(ubsId);
        }
    }

    private void carregarUBSParaEdicao(String ubsId) {
        mostrarCarregando(true);

        // Buscar UBS no Firebase
        ubsRepository.obterUBSUmaVez(new UBSRepository.OnUBSLoadedListener() {
            @Override
            public void onUBSLoaded(List<UBS> ubsList) {
                mostrarCarregando(false);

                // Procurar a UBS pelo ID
                for (UBS u : ubsList) {
                    if (u.getId().equals(ubsId)) {
                        ubsEditando = u;
                        break;
                    }
                }

                if (ubsEditando != null) {
                    preencherCampos();
                    getSupportActionBar().setTitle("Editar UBS");
                    btnSalvar.setText("Atualizar");
                } else {
                    Snackbar.make(btnSalvar, "UBS não encontrada", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getColor(R.color.error))
                            .show();
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                mostrarCarregando(false);
                Snackbar.make(btnSalvar, "Erro ao carregar UBS: " + error, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getColor(R.color.error))
                        .show();
                finish();
            }
        });
    }

    private void preencherCampos() {
        etNome.setText(ubsEditando.getNome());
        etEndereco.setText(ubsEditando.getEndereco());
        etBairro.setText(ubsEditando.getBairro());
        etTelefone.setText(ubsEditando.getTelefone());
        etHorario.setText(ubsEditando.getHorarioFuncionamento());
        etEmail.setText(ubsEditando.getEmail());
        switchAberta.setChecked(ubsEditando.isAbertaAgora());

        // Marcar chips dos serviços
        if (ubsEditando.getServicos() != null) {
            servicosSelecionados.clear();
            servicosSelecionados.addAll(ubsEditando.getServicos());

            for (int i = 0; i < chipGroupServicos.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupServicos.getChildAt(i);
                if (servicosSelecionados.contains(chip.getText().toString())) {
                    chip.setChecked(true);
                }
            }
        }
    }

    private void validarESalvar() {
        // Obter valores
        String nome = etNome.getText().toString().trim();
        String endereco = etEndereco.getText().toString().trim();
        String bairro = etBairro.getText().toString().trim();
        String telefone = etTelefone.getText().toString().trim();
        String horario = etHorario.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        boolean abertaAgora = switchAberta.isChecked();

        // Resetar erros
        tilNome.setError(null);
        tilEndereco.setError(null);
        tilBairro.setError(null);
        tilTelefone.setError(null);
        tilHorario.setError(null);
        tilEmail.setError(null);

        // Validar
        boolean valido = true;

        if (TextUtils.isEmpty(nome)) {
            tilNome.setError("Nome da UBS é obrigatório");
            valido = false;
        } else if (nome.length() < 5) {
            tilNome.setError("Nome deve ter pelo menos 5 caracteres");
            valido = false;
        }

        if (TextUtils.isEmpty(endereco)) {
            tilEndereco.setError("Endereço é obrigatório");
            valido = false;
        } else if (endereco.length() < 10) {
            tilEndereco.setError("Endereço deve ter pelo menos 10 caracteres");
            valido = false;
        }

        if (TextUtils.isEmpty(bairro)) {
            tilBairro.setError("Bairro é obrigatório");
            valido = false;
        }

        if (TextUtils.isEmpty(telefone)) {
            tilTelefone.setError("Telefone é obrigatório");
            valido = false;
        } else if (!telefone.matches("\\(\\d{2}\\) \\d{4,5}-\\d{4}")) {
            tilTelefone.setError("Formato inválido. Use: (XX) XXXXX-XXXX");
            valido = false;
        }

        if (TextUtils.isEmpty(horario)) {
            tilHorario.setError("Horário de funcionamento é obrigatório");
            valido = false;
        }

        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email inválido");
            valido = false;
        }

        if (servicosSelecionados.isEmpty()) {
            Snackbar.make(btnSalvar, "Selecione pelo menos um serviço", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColor(R.color.error))
                    .show();
            valido = false;
        }

        if (valido) {
            // Verificar se já existe uma UBS com o mesmo nome (apenas para cadastro novo)
            if (!modoEdicao) {
                verificarEExisteSalvar(nome, endereco, bairro, telefone, horario, email, abertaAgora);
            } else {
                salvarUBS(nome, endereco, bairro, telefone, horario, email, abertaAgora);
            }
        }
    }

    private void verificarEExisteSalvar(String nome, String endereco, String bairro,
                                        String telefone, String horario, String email, boolean abertaAgora) {
        mostrarCarregando(true);

        ubsRepository.verificarUBSExiste(nome, new UBSRepository.OnUBSExistsListener() {
            @Override
            public void onResult(boolean exists) {
                if (exists) {
                    mostrarCarregando(false);
                    tilNome.setError("Já existe uma UBS com este nome");
                } else {
                    salvarUBS(nome, endereco, bairro, telefone, horario, email, abertaAgora);
                }
            }
        });
    }

    private void salvarUBS(String nome, String endereco, String bairro,
                           String telefone, String horario, String email, boolean abertaAgora) {
        if (!modoEdicao) {
            // Criar nova UBS
            UBS novaUBS = new UBS(nome, endereco, bairro, telefone, horario);
            novaUBS.setEmail(email);
            novaUBS.setAbertaAgora(abertaAgora);
            novaUBS.setServicos(new ArrayList<>(servicosSelecionados));

            // Adicionar ao Firebase
            ubsRepository.adicionarUBS(novaUBS, new UBSRepository.OnUBSOperationListener() {
                @Override
                public void onSuccess() {
                    mostrarCarregando(false);
                    mostrarMensagemSucesso("UBS cadastrada com sucesso!");

                    // Voltar após 1 segundo
                    btnSalvar.postDelayed(() -> {
                        setResult(RESULT_OK);
                        finish();
                    }, 1000);
                }

                @Override
                public void onError(String error) {
                    mostrarCarregando(false);
                    Snackbar.make(btnSalvar, "Erro ao cadastrar: " + error, Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getColor(R.color.error))
                            .show();
                }
            });
        } else {
            // Atualizar UBS existente
            ubsEditando.setNome(nome);
            ubsEditando.setEndereco(endereco);
            ubsEditando.setBairro(bairro);
            ubsEditando.setTelefone(telefone);
            ubsEditando.setHorarioFuncionamento(horario);
            ubsEditando.setEmail(email);
            ubsEditando.setAbertaAgora(abertaAgora);
            ubsEditando.setServicos(new ArrayList<>(servicosSelecionados));

            // Atualizar no Firebase
            ubsRepository.atualizarUBS(ubsEditando, new UBSRepository.OnUBSOperationListener() {
                @Override
                public void onSuccess() {
                    mostrarCarregando(false);
                    mostrarMensagemSucesso("UBS atualizada com sucesso!");

                    // Voltar após 1 segundo
                    btnSalvar.postDelayed(() -> {
                        setResult(RESULT_OK);
                        finish();
                    }, 1000);
                }

                @Override
                public void onError(String error) {
                    mostrarCarregando(false);
                    Snackbar.make(btnSalvar, "Erro ao atualizar: " + error, Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getColor(R.color.error))
                            .show();
                }
            });
        }
    }

    private void mostrarCarregando(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        btnSalvar.setEnabled(!mostrar);

        // Desabilitar campos durante carregamento
        etNome.setEnabled(!mostrar);
        etEndereco.setEnabled(!mostrar);
        etBairro.setEnabled(!mostrar);
        etTelefone.setEnabled(!mostrar);
        etHorario.setEnabled(!mostrar);
        etEmail.setEnabled(!mostrar);
        switchAberta.setEnabled(!mostrar);

        for (int i = 0; i < chipGroupServicos.getChildCount(); i++) {
            chipGroupServicos.getChildAt(i).setEnabled(!mostrar);
        }
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
        // Se estiver carregando, não permitir voltar
        if (progressBar.getVisibility() == View.VISIBLE) {
            return;
        }

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
        if (modoEdicao && ubsEditando != null) {
            // Comparar com dados originais
            String nomeAtual = etNome.getText().toString().trim();
            String enderecoAtual = etEndereco.getText().toString().trim();
            String bairroAtual = etBairro.getText().toString().trim();
            String telefoneAtual = etTelefone.getText().toString().trim();
            String horarioAtual = etHorario.getText().toString().trim();
            String emailAtual = etEmail.getText().toString().trim();
            boolean abertaAtual = switchAberta.isChecked();

            // Verificar serviços
            boolean servicosIguais = servicosSelecionados.size() == ubsEditando.getServicos().size() &&
                    servicosSelecionados.containsAll(ubsEditando.getServicos());

            return !nomeAtual.equals(ubsEditando.getNome()) ||
                    !enderecoAtual.equals(ubsEditando.getEndereco()) ||
                    !bairroAtual.equals(ubsEditando.getBairro()) ||
                    !telefoneAtual.equals(ubsEditando.getTelefone()) ||
                    !horarioAtual.equals(ubsEditando.getHorarioFuncionamento()) ||
                    !emailAtual.equals(ubsEditando.getEmail() != null ? ubsEditando.getEmail() : "") ||
                    abertaAtual != ubsEditando.isAbertaAgora() ||
                    !servicosIguais;
        } else {
            // Para novo cadastro, verificar se algum campo foi preenchido
            return !TextUtils.isEmpty(etNome.getText()) ||
                    !TextUtils.isEmpty(etEndereco.getText()) ||
                    !TextUtils.isEmpty(etBairro.getText()) ||
                    !TextUtils.isEmpty(etTelefone.getText()) ||
                    !TextUtils.isEmpty(etHorario.getText()) ||
                    !TextUtils.isEmpty(etEmail.getText()) ||
                    !servicosSelecionados.isEmpty();
        }
    }
}
package com.example.cuidapoa.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.cuidapoa.R;
import com.example.cuidapoa.repository.AuthRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilSenha;
    private TextInputEditText etEmail, etSenha;
    private Button btnLogin;
    private TextView tvEsqueceuSenha;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    // Firebase
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar AuthRepository
        authRepository = AuthRepository.getInstance();

        // Configurar views
        initViews();

        // Configurar Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.login_titulo);
        }

        // Configurar listeners
        btnLogin.setOnClickListener(v -> validarELogar());
        tvEsqueceuSenha.setOnClickListener(v -> mostrarDialogRecuperarSenha());

        // Configurar limpeza de erros ao digitar
        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && tilEmail.getError() != null) {
                tilEmail.setError(null);
            }
        });

        etSenha.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && tilSenha.getError() != null) {
                tilSenha.setError(null);
            }
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_login);
        tilEmail = findViewById(R.id.til_email);
        tilSenha = findViewById(R.id.til_senha);
        etEmail = findViewById(R.id.et_email);
        etSenha = findViewById(R.id.et_senha);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        tvEsqueceuSenha = findViewById(R.id.tv_esqueceu_senha);

        // Remover pré-preenchimento para produção
        // Para testes, você pode descomentar:
        // etEmail.setText("admin@cuidapoa.com");
        // etSenha.setText("Admin@123");
    }

    private void validarELogar() {
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        // Resetar erros
        tilEmail.setError(null);
        tilSenha.setError(null);

        // Validar campos
        boolean valido = true;

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.campo_obrigatorio));
            valido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email inválido");
            valido = false;
        }

        if (TextUtils.isEmpty(senha)) {
            tilSenha.setError(getString(R.string.campo_obrigatorio));
            valido = false;
        } else if (senha.length() < 6) {
            tilSenha.setError("A senha deve ter pelo menos 6 caracteres");
            valido = false;
        }

        if (valido) {
            realizarLogin(email, senha);
        }
    }

    private void realizarLogin(String email, String senha) {
        // Mostrar progresso
        mostrarCarregando(true);

        // Fazer login com Firebase
        authRepository.login(email, senha, new AuthRepository.OnAuthListener() {
            @Override
            public void onSuccess() {
                // Login bem-sucedido
                mostrarCarregando(false);

                // Verificar se é admin
                boolean isAdmin = authRepository.isAdmin();
                String mensagem = isAdmin ? "Bem-vindo, Gestor!" : "Bem-vindo!";

                Snackbar.make(btnLogin, mensagem, Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getColor(R.color.success))
                        .show();

                // Voltar para MainActivity
                Intent intent = new Intent();
                intent.putExtra("usuario_logado", true);
                intent.putExtra("is_admin", isAdmin);
                setResult(RESULT_OK, intent);

                // Pequeno delay para mostrar mensagem
                btnLogin.postDelayed(() -> finish(), 1000);
            }

            @Override
            public void onError(String error) {
                mostrarCarregando(false);

                // Tratar erros específicos
                String mensagemErro;
                if (error.contains("no user record")) {
                    mensagemErro = "Email não cadastrado";
                } else if (error.contains("password is invalid")) {
                    mensagemErro = "Senha incorreta";
                } else if (error.contains("network")) {
                    mensagemErro = "Erro de conexão. Verifique sua internet";
                } else {
                    mensagemErro = "Email ou senha incorretos";
                }

                Snackbar.make(btnLogin, mensagemErro, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getColor(R.color.error))
                        .show();
            }
        });
    }

    private void mostrarDialogRecuperarSenha() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_recuperar_senha, null);
        TextInputEditText etEmailRecuperar = dialogView.findViewById(R.id.et_email_recuperar);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Recuperar Senha")
                .setMessage("Digite seu email para receber as instruções de recuperação")
                .setView(dialogView)
                .setPositiveButton("Enviar", (dialog, which) -> {
                    String email = etEmailRecuperar.getText().toString().trim();
                    if (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        enviarEmailRecuperacao(email);
                    } else {
                        Snackbar.make(btnLogin, "Digite um email válido", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void enviarEmailRecuperacao(String email) {
        mostrarCarregando(true);

        authRepository.resetarSenha(email, new AuthRepository.OnAuthListener() {
            @Override
            public void onSuccess() {
                mostrarCarregando(false);
                new MaterialAlertDialogBuilder(LoginActivity.this)
                        .setTitle("Email Enviado")
                        .setMessage("Verifique sua caixa de entrada e siga as instruções para redefinir sua senha.")
                        .setPositiveButton("OK", null)
                        .show();
            }

            @Override
            public void onError(String error) {
                mostrarCarregando(false);
                String mensagem = error.contains("no user") ?
                        "Email não cadastrado" :
                        "Erro ao enviar email. Tente novamente.";

                Snackbar.make(btnLogin, mensagem, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getColor(R.color.error))
                        .show();
            }
        });
    }

    private void mostrarCarregando(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!mostrar);
        etEmail.setEnabled(!mostrar);
        etSenha.setEnabled(!mostrar);
        tvEsqueceuSenha.setEnabled(!mostrar);
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
        super.onBackPressed();
    }
}
package com.example.cuidapoa.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.cuidapoa.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    // Login fixo para teste acadêmico
    private static final String EMAIL_ADMIN = "admin@cuidapoa.com";
    private static final String SENHA_ADMIN = "123456";

    private TextInputLayout tilEmail, tilSenha;
    private TextInputEditText etEmail, etSenha;
    private Button btnLogin;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configurar views
        initViews();

        // Configurar Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.login_titulo);
        }

        // Configurar listener do botão
        btnLogin.setOnClickListener(v -> validarELogar());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_login);
        tilEmail = findViewById(R.id.til_email);
        tilSenha = findViewById(R.id.til_senha);
        etEmail = findViewById(R.id.et_email);
        etSenha = findViewById(R.id.et_senha);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);

        // Pré-preencher para facilitar teste
        etEmail.setText(EMAIL_ADMIN);
        etSenha.setText(SENHA_ADMIN);
    }

    private void validarELogar() {
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        // Resetar erros
        tilEmail.setError(null);
        tilSenha.setError(null);

        // Validar campos vazios
        boolean valido = true;

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.campo_obrigatorio));
            valido = false;
        }

        if (TextUtils.isEmpty(senha)) {
            tilSenha.setError(getString(R.string.campo_obrigatorio));
            valido = false;
        }

        if (valido) {
            realizarLogin(email, senha);
        }
    }

    private void realizarLogin(String email, String senha) {
        // Mostrar progresso
        mostrarCarregando(true);

        // Simular delay de rede
        btnLogin.postDelayed(() -> {
            mostrarCarregando(false);

            // Verificar credenciais
            if (EMAIL_ADMIN.equals(email) && SENHA_ADMIN.equals(senha)) {
                // Login bem-sucedido
                Snackbar.make(btnLogin, getString(R.string.login_sucesso),
                        Snackbar.LENGTH_SHORT).show();

                // Voltar para MainActivity
                Intent intent = new Intent();
                intent.putExtra("usuario_logado", true);
                setResult(RESULT_OK, intent);

                // Pequeno delay para mostrar mensagem
                btnLogin.postDelayed(this::finish, 1000);

            } else {
                // Falha no login
                Snackbar.make(btnLogin, "Email ou senha incorretos",
                        Snackbar.LENGTH_LONG).show();
            }
        }, 1500); // 1.5 segundos de delay
    }

    private void mostrarCarregando(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!mostrar);
        etEmail.setEnabled(!mostrar);
        etSenha.setEnabled(!mostrar);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

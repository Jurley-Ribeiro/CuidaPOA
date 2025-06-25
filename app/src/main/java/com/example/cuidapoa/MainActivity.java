package com.example.cuidapoa;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.cuidapoa.repository.AuthRepository;
import com.example.cuidapoa.model.Usuario;
import com.example.cuidapoa.view.LoginActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    // Firebase
    private AuthRepository authRepository;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Instalar a SplashScreen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Firebase - habilitar persistência offline
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            // Já foi habilitado
        }

        // Inicializar AuthRepository
        authRepository = AuthRepository.getInstance();

        // Configurar Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configurar Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Configurar Navigation Component
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        // Definir os destinos de nível superior
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setOpenableLayout(drawerLayout)
                .build();

        // Conectar NavigationUI com a ActionBar
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Configurar listener do Navigation Drawer
        navigationView.setNavigationItemSelectedListener(this);

        // Verificar estado de login
        verificarEstadoLogin();
    }

    private void verificarEstadoLogin() {
        if (authRepository.isLogado()) {
            // Usuário está logado
            Usuario usuario = authRepository.getUsuarioAtual();
            if (usuario != null) {
                isAdmin = usuario.isAdmin();
                atualizarUIUsuarioLogado(usuario);
            } else {
                // Carregar dados do usuário
                authRepository.carregarDadosUsuario(new AuthRepository.OnAuthListener() {
                    @Override
                    public void onSuccess() {
                        Usuario user = authRepository.getUsuarioAtual();
                        if (user != null) {
                            isAdmin = user.isAdmin();
                            atualizarUIUsuarioLogado(user);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        // Falha ao carregar dados
                    }
                });
            }
        } else {
            // Usuário não está logado
            atualizarUIUsuarioDeslogado();
        }
    }

    private void atualizarUIUsuarioLogado(Usuario usuario) {
        // Atualizar header do navigation drawer
        View headerView = navigationView.getHeaderView(0);
        TextView tvNome = headerView.findViewById(R.id.tv_header_nome);
        TextView tvEmail = headerView.findViewById(R.id.tv_header_email);

        if (tvNome != null) {
            tvNome.setText(usuario.getNome());
        }
        if (tvEmail != null) {
            tvEmail.setText(usuario.getEmail());
        }

        // Atualizar menu
        Menu menu = navigationView.getMenu();
        MenuItem loginItem = menu.findItem(R.id.nav_login);
        if (loginItem != null) {
            loginItem.setTitle("Sair");
            loginItem.setIcon(R.drawable.ic_logout_24); // Você precisará criar este ícone
        }

        // Passar estado de admin para fragments
        getIntent().putExtra("is_admin", isAdmin);

        // Mostrar mensagem de boas-vindas
        String tipoUsuario = isAdmin ? "Gestor" : "Cidadão";
        Snackbar.make(drawerLayout, "Bem-vindo, " + tipoUsuario + "!", Snackbar.LENGTH_SHORT).show();
    }

    private void atualizarUIUsuarioDeslogado() {
        // Atualizar header do navigation drawer
        View headerView = navigationView.getHeaderView(0);
        TextView tvNome = headerView.findViewById(R.id.tv_header_nome);
        TextView tvEmail = headerView.findViewById(R.id.tv_header_email);

        if (tvNome != null) {
            tvNome.setText(R.string.app_name);
        }
        if (tvEmail != null) {
            tvEmail.setText("Acesso cidadão");
        }

        // Atualizar menu
        Menu menu = navigationView.getMenu();
        MenuItem loginItem = menu.findItem(R.id.nav_login);
        if (loginItem != null) {
            loginItem.setTitle("Login Gestor");
            loginItem.setIcon(R.drawable.ic_login_24); // Use o ícone existente
        }

        // Remover estado de admin
        isAdmin = false;
        getIntent().putExtra("is_admin", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sobre) {
            mostrarDialogSobre();
            return true;
        }
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            navController.navigate(R.id.nav_home);
        } else if (id == R.id.nav_login) {
            if (authRepository.isLogado()) {
                // Se está logado, fazer logout
                mostrarDialogLogout();
            } else {
                // Se não está logado, abrir tela de login
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, 100);
            }
        } else if (id == R.id.nav_sobre) {
            mostrarDialogSobre();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void mostrarDialogLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Sair")
                .setMessage("Deseja realmente sair da sua conta?")
                .setPositiveButton("Sair", (dialog, which) -> {
                    authRepository.logout();
                    atualizarUIUsuarioDeslogado();
                    // Recriar activity para resetar o estado
                    recreate();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogSobre() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sobre_titulo)
                .setMessage(getString(R.string.sobre_desenvolvedor) + "\n\n" +
                        getString(R.string.sobre_disciplina) + "\n\n" +
                        getString(R.string.sobre_instituicao) + "\n\n" +
                        getString(R.string.sobre_objetivo))
                .setPositiveButton(R.string.fechar, null)
                .setIcon(R.drawable.ic_logo_splash);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Login realizado com sucesso
            verificarEstadoLogin();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verificar estado de login sempre que a activity resumir
        verificarEstadoLogin();
    }

    // Método público para fragments acessarem o estado de admin
    public boolean isAdmin() {
        return isAdmin;
    }
}
package com.example.cuidapoa;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
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
import com.example.cuidapoa.view.LoginActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Instalar a SplashScreen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configurar Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Configurar Navigation Component
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        // Definir os destinos de nível superior (que mostram o menu hambúrguer)
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home) // Apenas home mostra hambúrguer
                .setOpenableLayout(drawerLayout)
                .build();

        // Conectar NavigationUI com a ActionBar
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Configurar listener do Navigation Drawer
        navigationView.setNavigationItemSelectedListener(this);
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
            // Abrir tela de login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 100);
        } else if (id == R.id.nav_sobre) {
            mostrarDialogSobre();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
            if (data != null && data.getBooleanExtra("usuario_logado", false)) {
                Snackbar.make(drawerLayout, "Bem-vindo, Gestor!", Snackbar.LENGTH_SHORT).show();

                // Marcar como admin para mostrar funcionalidades extras
                getIntent().putExtra("is_admin", true);

                // TODO: Atualizar menu para mostrar opções do gestor
            }
        }
    }
}
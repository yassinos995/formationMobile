package com.example.proform;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class BaseDrawerActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        setupDrawer();
    }

    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    gohome();
                    return true;
                } else if (itemId == R.id.nav_list_employers) {
                    openListEmployersActivity();
                    return true;
                } else if (itemId == R.id.nav_list_commands) {
                    openListCommandsActivity();
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    // Handle settings menu item
                    return true;
                } else if (itemId == R.id.nav_info) {
                    // Handle info menu item
                    return true;
                } else if (itemId == R.id.nav_share) {
                    shareApp();
                    return true;
                } else if (itemId == R.id.nav_logout) {
                    logout();
                    return true;
                } else {
                    // Handle other menu items
                    drawerLayout.closeDrawers();
                    return true;
                }
            });
        }
    }


    private void openListCommandsActivity() {
        Intent intent = new Intent(this, listcommand.class);
        startActivity(intent);
    }

    private void shareApp() {
        Toast.makeText(this, "Mazelna ma gadinahech", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void openListEmployersActivity() { Intent intent = new Intent(this, listemp.class);
        startActivity(intent);

    }

    private void gohome() {
        finish();
    }
}

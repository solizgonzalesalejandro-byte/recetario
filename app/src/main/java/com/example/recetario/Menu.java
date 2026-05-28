package com.example.recetario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class Menu extends AppCompatActivity {

    private MaterialCardView cardReciclaje, cardAceite, cardEsterilizacion, cardNuevaCategoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        cardReciclaje = findViewById(R.id.cardReciclaje);
        cardEsterilizacion = findViewById(R.id.cardEsterilizacion);
        cardAceite = findViewById(R.id.cardAceite);
        cardNuevaCategoria = findViewById(R.id.cardNuevoPunto);
    }

    private void setupClickListeners() {
        cardNuevaCategoria.setOnClickListener(v -> mostrarFormularioNuevaCategoria());

        cardReciclaje.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("categoria", "Reciclaje");
            startActivity(intent);
        });

        cardAceite.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("categoria", "Aceite Usado");
            startActivity(intent);
        });

        cardEsterilizacion.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("categoria", "Esterilización");
            startActivity(intent);
        });
    }

    private void mostrarFormularioNuevaCategoria() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_nueva_categoria, null);

        TextInputEditText etNombreCat = view.findViewById(R.id.fNombreCategoria);
        TextInputEditText etJustificacion = view.findViewById(R.id.fJustificacion);
        RadioGroup rgColor = view.findViewById(R.id.fColorGroup);

        view.findViewById(R.id.btnEnviarCategoria).setOnClickListener(v -> {
            String nombre = etNombreCat.getText().toString().trim();
            String justificacion = etJustificacion.getText().toString().trim();

            int selectedId = rgColor.getCheckedRadioButtonId();
            RadioButton rbSeleccionado = view.findViewById(selectedId);
            String color = (rbSeleccionado != null) ? rbSeleccionado.getText().toString() : "Ninguno";

            if (nombre.isEmpty()) {
                etNombreCat.setError("Debes asignar un nombre a la categoría");
                return;
            }
            if (justificacion.length() < 10) {
                etJustificacion.setError("Por favor, explica un poco más la importancia");
                return;
            }

            String mensaje = "Sugerencia enviada: " + nombre + " (" + color + ")";
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }
}
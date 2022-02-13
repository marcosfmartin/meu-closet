package br.ufc.smd.meucloset.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufc.smd.meucloset.R;
import br.ufc.smd.meucloset.model.Usuario;

public class NovaCategoriaActivity extends AppCompatActivity implements View.OnClickListener {

    EditText edtNomeCategoria;
    Button btnSalvarCategoria;
    ListView listaCategorias;
    ArrayList<String> categoriaArrayList;

    private Usuario usuario;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_categoria);

        edtNomeCategoria     = findViewById(R.id.edtNomeCategoria);
        btnSalvarCategoria   = findViewById(R.id.btnSalvarCategoria);

        listaCategorias = findViewById(R.id.lista_categorias);
        categoriaArrayList = new ArrayList<>();

        btnSalvarCategoria.setOnClickListener(this);

        db = FirebaseFirestore.getInstance();

        //Dados da Intent Anterior
        Intent quemChamou = this.getIntent();
        if (quemChamou != null) {
            Bundle params = quemChamou.getExtras();
            if (params != null) {
                //Recuperando o Usuario
                usuario = (Usuario) params.getSerializable("usuario");
            }
        }

        loadDatainListview(usuario.getUsuario());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnSalvarCategoria) {
            Map<String, Object> mapa = new HashMap<>();
            mapa.put("nome", edtNomeCategoria.getText().toString());

            db.collection("usuarios")
                    .document(usuario.getUsuario())
                    .collection("categorias")
                    .add(mapa)
                    .addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            Toast.makeText(NovaCategoriaActivity.this, "Nova categoria cadastrada...", Toast.LENGTH_SHORT).show();
                            Log.d("TAG", "Novo produto cadastrado...");

                            /*
                            Intent intentListaProdutos = new Intent(NovaCategoriaActivity.this, ListaProdutosActivity.class);
                            intentListaProdutos.putExtra("usuario", usuario);
                            startActivity(intentListaProdutos);
                            finish();
                             */
                            loadDatainListview(usuario.getUsuario());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NovaCategoriaActivity.this, "Erro ao cadastrar categoria...", Toast.LENGTH_SHORT).show();
                            Log.d("TAG", "Erro ao cadastrar produto...", e);
                        }
                    });
        }
    }


    private void loadDatainListview(String usuario) {
        // below line is use to get data from Firebase firestore using collection in android.
        db.collection("usuarios").document(usuario)
                .collection("categorias")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        categoriaArrayList.clear();
                        // after getting the data we are calling on success method and inside this method we are checking if the received query snapshot is empty or not.
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // if the snapshot is not empty we are hiding our progress bar and adding our data in a list.
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot d : list) {
                                categoriaArrayList.add(d.getData().get("nome").toString());
                            }
                            // after that we are passing our array list to our adapter class.
                            ArrayAdapter adapter = new ArrayAdapter(NovaCategoriaActivity.this, android.R.layout.simple_list_item_1, categoriaArrayList);
                            // after passing this array list to our adapter class we are setting our adapter to our list view.
                            listaCategorias.setAdapter(adapter);
                        } else {
                            // if the snapshot is empty we are displaying a toast message.
                            Toast.makeText(NovaCategoriaActivity.this, "Não há dados cadastrados ainda...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e) {
                // we are displaying a toast message when we get any error from Firebase.
                Toast.makeText(NovaCategoriaActivity.this, "Erro ao ler dados..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
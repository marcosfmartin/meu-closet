package br.ufc.smd.meucloset.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.ufc.smd.meucloset.R;
import br.ufc.smd.meucloset.model.Produto;
import br.ufc.smd.meucloset.model.ProdutoAdapter;
import br.ufc.smd.meucloset.model.Usuario;

public class ListaProdutosActivity extends AppCompatActivity {

    // creating a variable for our list view, arraylist and firebase Firestore.
    ListView listaViewProdutos;
    ArrayList<Produto> produtoArrayList;
    FirebaseFirestore db;

    private FloatingActionButton btnAdicionaProduto;

    private Usuario usuario;
    private Produto produtoSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_produtos);

        listaViewProdutos = findViewById(R.id.lista_produtos);
        produtoArrayList = new ArrayList<>();

        listaViewProdutos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                produtoSelecionado = (Produto) parent.getItemAtPosition(position);
                return false;
            }
        });
        registerForContextMenu(listaViewProdutos);

        btnAdicionaProduto = (FloatingActionButton) findViewById(R.id.btnAdicionaProduto);
        btnAdicionaProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(usuario.getUsuario() != null) {
                    Intent intent = new Intent(ListaProdutosActivity.this, NovoProdutoActivity.class);
                    intent.putExtra("usuario", usuario);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // initializing our variable for firebase firestore and getting its instance.
        db = FirebaseFirestore.getInstance();

        Intent quemChamou = this.getIntent();
        if (quemChamou != null) {
            Bundle params = quemChamou.getExtras();
            if (params != null) {
                usuario = (Usuario) params.getSerializable("usuario");
                if (usuario != null) {
                    loadDatainListview(usuario.getUsuario());
                }
            }
        }
    }

    private void loadDatainListview(String usuario) {
        // below line is use to get data from Firebase firestore using collection in android.

        db.collection("usuarios").document(usuario)
                .collection("produtos")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // after getting the data we are calling on success method and inside this method we are checking if the received query snapshot is empty or not.
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // if the snapshot is not empty we are hiding our progress bar and adding our data in a list.
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot d : list) {
                                // after getting this list we are passing that list to our object class.
                                Produto produto = d.toObject(Produto.class);
                                // after getting data from Firebase we are storing that data in our array list
                                produto.setId(d.getId().toString());
                                produtoArrayList.add(produto);
                            }
                            // after that we are passing our array list to our adapter class.
                            ProdutoAdapter adapter = new ProdutoAdapter(ListaProdutosActivity.this, produtoArrayList);
                            // after passing this array list to our adapter class we are setting our adapter to our list view.
                            listaViewProdutos.setAdapter(adapter);
                        } else {
                            // if the snapshot is empty we are displaying a toast message.
                            Toast.makeText(ListaProdutosActivity.this, "Não há dados cadastrados ainda...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // we are displaying a toast message when we get any error from Firebase.
                Toast.makeText(ListaProdutosActivity.this, "Erro ao ler dados..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuItem editarProduto = menu.add("Editar...");
        //MenuItem compartilharQRCode = menu.add("Compartilhar QRCode...");
        super.onCreateContextMenu(menu, v, menuInfo);
/*
        compartilharQRCode.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });
*/
        editarProduto.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(ListaProdutosActivity.this, AlteraProdutoActivity.class);
                intent.putExtra("usuario", usuario);
                intent.putExtra("idProduto", produtoSelecionado.getId());
                startActivity(intent);
                finish();
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemMenuSair:
                Intent intencaoPerfil = new Intent(this, LoginActivity.class);
                intencaoPerfil.putExtra("usuario", (Serializable) null);
                startActivity(intencaoPerfil);
                break;
            case R.id.itemMenuCategoria:
                Intent intencaoCategoria = new Intent(this, NovaCategoriaActivity.class);
                intencaoCategoria.putExtra("usuario", (Serializable) usuario);
                startActivity(intencaoCategoria);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_menu, menu);
        return true;
    }
}
package br.ufc.smd.meucloset.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufc.smd.meucloset.R;
import br.ufc.smd.meucloset.model.Produto;
import br.ufc.smd.meucloset.model.Usuario;

public class AlteraProdutoActivity extends AppCompatActivity
        implements View.OnClickListener {

    EditText  edtNomeProduto;
    EditText  edtDescricaoProduto;
    EditText  edtPrecoCompraProduto;
    EditText  edtPrecoVendaProduto;
    Spinner   spnCategoria;
    Switch    swtDisponivelProduto;
    Button    btnSalvarProduto;
    Button    btnTirarFoto;
    ImageView imgFotoProduto;

    FirebaseFirestore db;

    Produto produto;

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    String currentPhotoPath;
    String uriFotoFirestore;
    StorageReference storageReference;

    private Usuario usuario;
    private String idProduto;

    private List<String> categorias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_produto);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        produto = new Produto();

        edtNomeProduto        = findViewById(R.id.edtNomeProduto);
        edtDescricaoProduto   = findViewById(R.id.edtDescricaoProduto);
        edtPrecoCompraProduto = findViewById(R.id.edtPrecoCompraProduto);
        edtPrecoVendaProduto  = findViewById(R.id.edtPrecoVendaProduto);
        swtDisponivelProduto  = findViewById(R.id.swtDisponivelProduto);
        btnSalvarProduto      = findViewById(R.id.btnSalvarProduto);
        btnTirarFoto          = findViewById(R.id.btnTirarFoto);
        imgFotoProduto        = findViewById(R.id.imgFotoProduto);
        spnCategoria          = (Spinner) findViewById(R.id.spnCategoria);

        btnSalvarProduto.setOnClickListener(this);
        btnTirarFoto.setOnClickListener(this);

        //Dados da Intent Anterior
        Intent quemChamou = this.getIntent();
        if (quemChamou != null) {
            Bundle params = quemChamou.getExtras();
            if (params != null) {
                //Recuperando o Usuario
                usuario = (Usuario) params.getSerializable("usuario");
                idProduto = params.getString("idProduto");
            }
        }

        // Teste - ini
        db.collection("usuarios")
                .document(usuario.getUsuario())
                .collection("produtos")
                .document(idProduto)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot d = task.getResult();
                            if (d.exists()) {
                                produto.setNome(d.getData().get("nome").toString());
                                produto.setDescricao(d.getData().get("descricao").toString());
                                produto.setPrecoCompra(Double.parseDouble(d.getData().get("precoCompra").toString()));
                                produto.setPrecoVenda(Double.parseDouble(d.getData().get("precoVenda").toString()));
                                produto.setDisponivel(Boolean.parseBoolean(d.getData().get("disponivel").toString()));
                                if(d.getData().get("categoria") != null) {
                                    produto.setCategoria(d.getData().get("categoria").toString());
                                }

                                if(d.getData().get("urlFoto") != null) {
                                    uriFotoFirestore = d.getData().get("urlFoto").toString();
                                    Picasso.get().load(d.getData().get("urlFoto").toString()).resize(320, 320).into(imgFotoProduto);
                                    produto.setUrlFoto(d.getData().get("urlFoto").toString());
                                }

                                edtNomeProduto.setText(d.getData().get("nome").toString());
                                edtDescricaoProduto.setText(d.getData().get("descricao").toString());
                                edtPrecoCompraProduto.setText(d.getData().get("precoCompra").toString());
                                edtPrecoVendaProduto.setText(d.getData().get("precoVenda").toString());
                                swtDisponivelProduto.setChecked(Boolean.parseBoolean(d.getData().get("disponivel").toString()));
                            } else {
                                // if the snapshot is empty we are displaying a toast message.
                                Toast.makeText(AlteraProdutoActivity.this, "Não há dados cadastrados ainda...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
        // Teste - fim

        categorias = new ArrayList<String>();
        db.collection("usuarios").document(usuario.getUsuario())
                .collection("categorias")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // after getting the data we are calling on success method and inside this method we are checking if the received query snapshot is empty or not.
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // if the snapshot is not empty we are hiding our progress bar and adding our data in a list.
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot d : list) {
                                categorias.add(d.getData().get("nome").toString());
                            }

                            spnCategoria.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                    produto.setCategoria(categorias.get(pos));
                                }
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(AlteraProdutoActivity.this, android.R.layout.simple_spinner_item, categorias);
                            adapter.notifyDataSetChanged();
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spnCategoria.setAdapter(adapter);
                            spnCategoria.setSelection(categorias.indexOf(produto.getCategoria()));
                        } else {
                            // if the snapshot is empty we are displaying a toast message.
                            Toast.makeText(AlteraProdutoActivity.this, "Não há dados cadastrados ainda...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e) {
                // we are displaying a toast message when we get any error from Firebase.
                Toast.makeText(AlteraProdutoActivity.this, "Erro ao ler categorias..", Toast.LENGTH_SHORT).show();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {

        if(v == btnSalvarProduto) {
            Map<String, Object> mapa = new HashMap<>();
            mapa.put("nome",edtNomeProduto.getText().toString());
            mapa.put("descricao",edtDescricaoProduto.getText().toString());
            mapa.put("precoCompra", Double.parseDouble(edtPrecoCompraProduto.getText().toString()));
            mapa.put("precoVenda", Double.parseDouble(edtPrecoVendaProduto.getText().toString()));
            mapa.put("disponivel", swtDisponivelProduto.isChecked());
            mapa.put("urlFoto", uriFotoFirestore);

            db.collection("usuarios")
                    .document(usuario.getUsuario())
                    .collection("produtos")
                    .document(idProduto)
                    .update(mapa)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(AlteraProdutoActivity.this, "Produto atualizado...", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "Novo produto cadastrado...");

                    Intent intentListaProdutos = new Intent(AlteraProdutoActivity.this, ListaProdutosActivity.class);
                    intentListaProdutos.putExtra("usuario", usuario);
                    startActivity(intentListaProdutos);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AlteraProdutoActivity.this, "Erro ao cadastrar produto...", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "Falhou ao atualizar");
                }
            });
        }

        if(v == btnTirarFoto) {
            askCameraPermissions();
        }
    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @SuppressLint("MissingSuperCall")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Permissão para câmera é necessária...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == CAMERA_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                File f = new File(currentPhotoPath);

                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                Bitmap bitmapReduzido = Bitmap.createScaledBitmap(bitmap, 320, 320, true);
                imgFotoProduto.setImageBitmap(bitmapReduzido);
                imgFotoProduto.setScaleType(ImageView.ScaleType.FIT_XY);

                //imgFotoProduto.setImageURI(Uri.fromFile(f));
                Log.d("TAG", "Absolute Url of Image is " + Uri.fromFile(f));

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

                uploadImageToFirebase(f.getName(), contentUri);
            }
        }
    }

    private void uploadImageToFirebase(String name, Uri contentUri) {
        final StorageReference image = storageReference.child("pictures/" + name);
        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        uriFotoFirestore = uri.toString();
                        Log.d("TAG", "onSuccess: Uploaded Image URl is " + uri.toString());
                    }
                });
                Toast.makeText(AlteraProdutoActivity.this, "Upload de foto com sucesso.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AlteraProdutoActivity.this, "Erro no upload da foto...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,   /* prefix    */
                ".jpg",    /* suffix    */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "br.ufc.smd.meucloset.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
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
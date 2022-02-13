package br.ufc.smd.meucloset.model;

import android.content.Context;
import android.graphics.Color;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import com.squareup.picasso.Picasso;

import br.ufc.smd.meucloset.R;

public class ProdutoAdapter extends ArrayAdapter<Produto> {

    // constructor for our list view adapter.
    public ProdutoAdapter(@NonNull Context context, ArrayList<Produto> produtoArrayList) {
        super(context, 0, produtoArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // below line is use to inflate the layout for our item of list view.
        View listitemView = convertView;
        if (listitemView == null) {
            listitemView = LayoutInflater.from(getContext()).inflate(R.layout.image_lv_item, parent, false);
        }

        // after inflating an item of listview item we are getting data from array list inside our modal class.
        Produto produto = getItem(position);

        // initializing our UI components of list view item.
        TextView txvDescricaoList = listitemView.findViewById(R.id.txvDescricaoList);
        ImageView imgProdutoList = listitemView.findViewById(R.id.imgProdutoList);

        // after initializing our items we are setting data to our view. below line is use to set data to our text view.
        StringBuilder sb = new StringBuilder();
        sb.append(produto.getNome()); sb.append(System.getProperty("line.separator"));
        sb.append(" [PC: R$ "); sb.append(produto.getPrecoCompra());
        sb.append(" / PV: R$ "); sb.append(produto.getPrecoVenda());sb.append("]");

        if(!produto.isDisponivel())
            txvDescricaoList.setBackgroundColor(Color.LTGRAY);

        txvDescricaoList.setText(sb.toString());

        // in below line we are using Picasso to load image from URL in our Image VIew.
        Picasso.get().load(produto.getUrlFoto()).into(imgProdutoList);

        // below line is use to add item click listener for our item of list view.
        /*
        listitemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on the item click on our list view. we are displaying a toast message.
                Toast.makeText(getContext(), "Item clicked is : " + produto.getNome(), Toast.LENGTH_SHORT).show();
            }
        });
         */
        return listitemView;
    }
}


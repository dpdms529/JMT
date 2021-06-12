package org.techtown.jmt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    Context context;
    ArrayList<Document> items;
    EditText editText;
    TextView address;
    RecyclerView recyclerView;

    public SearchAdapter(ArrayList<Document> items, Context context, EditText editText, TextView address, RecyclerView recyclerView) {
        this.context = context;
        this.items = items;
        this.editText = editText;
        this.address = address;
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public void addItem(Document item) {
        items.add(item);
    }


    public void clear() {
        items.clear();
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(items.get(position).getId());
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.search_item, viewGroup, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int i) {
        final Document model = items.get(i);
        holder.placeNameText.setText(model.getPlaceName());
        holder.addressText.setText(model.getAddressName());
        holder.placeNameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText(model.getPlaceName());
                address.setText(model.getAddressName());
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView placeNameText;
        TextView addressText;

        public SearchViewHolder(@NonNull final View itemView) {
            super(itemView);
            placeNameText = itemView.findViewById(R.id.ltem_location_tv_placename);
            addressText = itemView.findViewById(R.id.ltem_location_tv_address);
        }
    }
}

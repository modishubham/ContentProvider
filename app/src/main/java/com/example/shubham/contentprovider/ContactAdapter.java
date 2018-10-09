package com.example.shubham.contentprovider;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    ArrayList<Model> contact;
    Context context;


    public ContactAdapter(ArrayList<Model> contact, Context context) {
        this.context = context;
        this.contact = contact;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_row_layout, viewGroup, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int i) {
        Model contacts = (Model) contact.get(i);
        holder.name.setText(contacts.getName());
        holder.number.setText(contacts.getNumber());
    }

    @Override
    public int getItemCount() {
        return contact.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {

        TextView name, number;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_name);
            number = itemView.findViewById(R.id.tv_number);
        }
    }
}

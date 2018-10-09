package com.example.shubham.contentprovider;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int WRITE_CONTACTS_REQUEST_CODE = 1;
    private static final int READ_CONTACTS_REQUEST_CODE = 2;
    RecyclerView recyclerView;
    ArrayList<Model> phoneContact;
    ContentResolver contentResolver;
    ContactAdapter contactAdapter;
    EditText name, number;
    Button btnDelete, btnAddContact;

    String[] perms = {"android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.editText);
        number = findViewById(R.id.editTextNumber);
        btnDelete = findViewById(R.id.btnDelete);
        btnAddContact = findViewById(R.id.btnAddContact);

        ActivityCompat.requestPermissions(this, perms, 200);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteContact();
            }
        });

        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact();
            }
        });

    }


    private void createContactList() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fetchContacts();
        contactAdapter = new ContactAdapter(phoneContact, this);
        recyclerView.setAdapter(contactAdapter);
    }

    private ArrayList<Model> fetchContacts() {
        phoneContact = new ArrayList<>();
        contentResolver = getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Log.e("data", "Name-" + name + " Number" + number);
                Model contact = new Model();
                contact.setName(name);
                contact.setNumber(number);
                phoneContact.add(contact);
            }
        }
        return phoneContact;
    }

    private void deleteContact() {

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_CONTACTS ) == PackageManager.PERMISSION_GRANTED) {
             //if permission is granted
            contentResolver = getContentResolver();
            String contactName = name.getText().toString();

            if (contactName.equals("")) {
                Toast.makeText(getBaseContext(), "Name can not empty", Toast.LENGTH_LONG).show();
            } else {
                ArrayList<Model> mycontact = phoneContact;
                Integer position = null;
                for (int i = 0; i < mycontact.size(); i++) {
                    if (mycontact.get(i).getName().equals(contactName)) {
                        position = i;
                    }
                }
                contactAdapter.notifyItemRemoved(position);
                Uri uri = ContactsContract.RawContacts.CONTENT_URI;
                long deleteId = contentResolver.delete(uri, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY + "=?", new String[]{contactName});
                if (deleteId > 0) {
                    Toast.makeText(getApplicationContext(), "Contact deleted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Contact not deleted", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Permission Required", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, perms, 200);
        }


    }

    private void addContact() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_CONTACTS ) == PackageManager.PERMISSION_GRANTED) {
            ArrayList<ContentProviderOperation> cops = new ArrayList<ContentProviderOperation>();
            cops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "")
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, "")
                    .build());
            cops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name.getText().toString())
                    .build());
            cops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number.getText().toString())
                    .build());
            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, cops);
                Toast.makeText(getApplicationContext(), "Contact added successfully", Toast.LENGTH_LONG).show();
                Model contact = new Model();
                contact.setName(name.getText().toString());
                contact.setNumber(number.getText().toString());
                phoneContact.add(contact);
                contactAdapter.notifyItemInserted(phoneContact.size() - 1);
                recyclerView.getLayoutManager().scrollToPosition(phoneContact.size() - 1);
            } catch (Exception exception) {
                Log.i("exception in add", exception.getMessage());
            }
        } else {
            Toast.makeText(getApplicationContext(), "Permission Required", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, perms, 200);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case 200:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"contact permission is granted",Toast.LENGTH_LONG).show();
                    createContactList();
                } else {
                    Toast.makeText(this,"contact permission is not granted",Toast.LENGTH_LONG).show();
                }
        }
    }

}

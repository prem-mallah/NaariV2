package com.example.naariv2;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.naariv2.Contacts.ContactModel;
import com.example.naariv2.Contacts.CustomAdapter;
import com.example.naariv2.Contacts.DbHelper;
import com.example.naariv2.Contacts.ContactModel;
import com.example.naariv2.Contacts.CustomAdapter;
import com.example.naariv2.Contacts.DbHelper;

import java.util.List;

public class SavedContacts extends AppCompatActivity {

    ListView listView;
    DbHelper db;
    List<ContactModel> list;
    CustomAdapter customAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_contacts);


        listView = findViewById(R.id.ListView);
        db = new DbHelper(this);
        list = db.getAllContacts();
        customAdapter = new CustomAdapter(this, list);
        listView.setAdapter(customAdapter);


    }
}
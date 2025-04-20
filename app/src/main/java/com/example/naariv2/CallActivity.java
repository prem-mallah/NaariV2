package com.example.naariv2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class CallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        Button call1;
        call1=findViewById(R.id.call1);
        call1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+100));
                callIntent.setData(Uri.parse("tel:"+1299));
                callIntent.setData(Uri.parse("tel:"+1299));
                callIntent.setData(Uri.parse("tel:"+1299));
                callIntent.setData(Uri.parse("tel:"+1299));

                startActivity(callIntent);
            }
        });

    }
}
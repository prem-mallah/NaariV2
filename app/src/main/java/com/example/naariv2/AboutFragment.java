package com.example.naariv2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


public class AboutFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);
//////
        ImageView vesit_icon = view.findViewById(R.id.vesit_icon);
        TextView vesit_text = view.findViewById(R.id.vesit_text);


        vesit_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vesit_text.setTextColor(ContextCompat.getColor(getContext(), R.color.teal_700));
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vesit.ves.ac.in/"));
                startActivity(intent);
            }
        });


        vesit_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vesit_icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.teal_700));
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vesit.ves.ac.in/"));
                startActivity(intent);

            }
        });


////////

        ImageView email_icon = view.findViewById(R.id.email_icon);
        TextView email_text = view.findViewById(R.id.email_text);

        email_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email_text.setTextColor(ContextCompat.getColor(getContext(), R.color.teal_700));
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:2024.prem.mallah@ves.ac.in"));
                startActivity(intent);

            }
        });


        email_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email_icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.teal_700));
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:2024.prem.mallah@ves.ac.in"));
                startActivity(intent);

            }
        });

        //////////


        TextView privacy = view.findViewById(R.id.privacy);

        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                privacy.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_500));
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/premmallah/"));
                startActivity(intent);

            }
        });


        TextView terms = view.findViewById(R.id.terms);

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                terms.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_500));
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/premmallah/"));
                startActivity(intent);

            }
        });




        TextView app_info = view.findViewById(R.id.app_info);

        app_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                app_info.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_500));

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                Uri uri= Uri.fromParts("package",getPackageName(),null);
                intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                startActivity(intent);

            }
        });


        /////////


        return view;

    }

}
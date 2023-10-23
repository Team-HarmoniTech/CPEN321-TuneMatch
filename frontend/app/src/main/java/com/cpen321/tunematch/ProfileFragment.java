package com.cpen321.tunematch;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_profile, container, false);

        Button friendsListBtn = view.findViewById(R.id.friendsListBtn);
        friendsListBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
//                Intent favCityIntent = new Intent(this, FavouriteCity.class);
//                startActivity(favCityIntent);
            }

        });

        return view;
    }
}

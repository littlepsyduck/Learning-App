package com.example.learning_app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.learning_app.R;

public class FriendsTabFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_friends, container, false);

        AppCompatButton btnFindFriends = view.findViewById(R.id.btnFindFriends);
        btnFindFriends.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.streak_content_container, new FriendsSearchFragment());
                transaction.addToBackStack(null); // Allows user to press back to return here
                transaction.commit();
            }
        });

        return view;
    }
}

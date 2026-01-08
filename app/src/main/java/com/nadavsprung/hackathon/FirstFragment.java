package com.nadavsprung.hackathon;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class FirstFragment extends Fragment {

    private List<String> subjects = Arrays.asList(
            "מתמטיקה", "פיזיקה", "כימיה", "ביולוגיה",
            "מדעי המחשב", "אלקטרוניקה", "אנגלית", "ספרות",
            "לשון והבעה עברית", "היסטוריה", "תנך",
            "מדעים", "גיאוגרפיה", "אזרחות", "מחשבת ישראל" , "מקצועות נוספים"
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.subjectsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SubjectsAdapter adapter = new SubjectsAdapter(getContext(), subjects, subject -> {
            // Navigate to SubjectDetailActivity with selected subject
            android.content.Intent intent = new android.content.Intent(getContext(), SubjectDetailActivity.class);
            intent.putExtra("subject", subject);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        return view;
    }
}


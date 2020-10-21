package com.example.finalproject;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GuardianFragment} factory method to
 * create an instance of this fragment.
 */
public class GuardianFragment extends Fragment {

    /**
     * The data on the article passed from the GuardianActivity
     */
    private Bundle articleData;

    private AppCompatActivity parentActivity;

    public GuardianFragment() {
        // Required empty public constructor
    }


    /**
     *
     * Inflates the frame layout and sets the fields of the views
     *
     * @param inflater
     * The LayoutInflator that inflates the new layout
     * @param container
     * @param savedInstanceState
     * @return
     * The View inflated
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        articleData = getArguments();

        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.guardian_frame, container, false);

        TextView nameView = result.findViewById(R.id.fragment_article_name);
        nameView.setText(articleData.getString("name"));

        TextView urlView = result.findViewById(R.id.fragment_article_url);
        urlView.setText(articleData.getString("url"));

        TextView sectionNameView = result.findViewById(R.id.fragment_section_name);
        sectionNameView.setText(articleData.getString("sectionName"));


        Button hideButton = (Button) result.findViewById(R.id.guardian_hide_button);
        hideButton.setOnClickListener(clk -> {
            parentActivity.getSupportFragmentManager().beginTransaction().remove(this).commit();
            parentActivity.finish();
        });
        return result;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //context will either be FragmentExample for a tablet, or EmptyActivity for phone
        parentActivity = (AppCompatActivity) context;
    }

}

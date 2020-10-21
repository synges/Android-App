package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArticleDetailsFragment extends Fragment {

    private Bundle articleData;
    private long id;
    private AppCompatActivity parentActivity;

    public ArticleDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // get Bundle data from the BBCNewsReader and id for clicked article
        articleData = getArguments();
        id = articleData.getLong(BBCActivity.ARTICLE_TITLE);

        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_bbcarticle_details, container, false);

        // Set the fragment textviews with the data from the article
        TextView title = (TextView) result.findViewById(R.id.titleFragment_TextView);
        title.setText(articleData.getString(BBCActivity.ARTICLE_TITLE));

        TextView description = (TextView) result.findViewById(R.id.descriptionFragment_TextView);
        description.setText(articleData.getString(BBCActivity.ARTICLE_DESCRIPTION));

        TextView date = (TextView) result.findViewById(R.id.dateFragment_TextView);
        date.setText(articleData.getString(BBCActivity.ARTICLE_DATE));

        TextView link = (TextView) result.findViewById(R.id.urlFragment_TextView);
        link.setText(articleData.getString(BBCActivity.ARTICLE_URL));

//        // Checkbox indicating if article is favorited
//        CheckBox cb = (CheckBox) result.findViewById(R.id.favFragment_CheckBox);
//        if (articleData.getBoolean(BBCActivity.ARTICLE_FAV)) {
//            cb.setChecked(true);
//        }

        // If PHONE and If the hide button is clicked, remove the fragment and go back to chat
        Button hideButton = (Button) result.findViewById(R.id.btnFragment_Btn);
        hideButton.setOnClickListener(clk -> {
            parentActivity.getSupportFragmentManager().beginTransaction().remove(this).commit();
//            if (cb.isChecked()) {
//                Intent data = new Intent();
//                data.putExtra("id", id);
//                parentActivity.setResult(500, data);
//            }
//            else if (!cb.isChecked()) {
//                parentActivity.setResult(400);
//            }
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

package cmpt276.project.UI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cmpt276.project.R;
import cmpt276.project.model.ScoreRecording;
import cmpt276.project.model.ScoreRecordingManager;

//to show top five high score
public class HighScoreActivity extends AppCompatActivity {

    public static final String EDITOR_HIGH_SCORE_ARRAY = "editor high score array";
    public static final String SHARED_PREFS_HIGH_SCORE = "shared prefs high score";
    private ScoreRecordingManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

        manager = ScoreRecordingManager.getInstance();
        manager.print();
        setupHighScore();
        saveHighScores(this, manager);

//
//        //to reset high score
//        Button btReset = findViewById(R.id.ResetButton);
//        btReset.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                manager.resetHighScore();
//            }
//        });

    }

    public static void populateScoreRecording(ScoreRecordingManager manager){
        manager.addNewScore(new ScoreRecording(0, "kk", "June 16"));
        manager.addNewScore(new ScoreRecording(45, "kk", "June 16"));
        manager.addNewScore(new ScoreRecording(20, "kk", "June 16"));
        manager.addNewScore(new ScoreRecording(60, "kk", "June 16"));
        manager.addNewScore(new ScoreRecording(80, "kk", "June 16"));
        manager.addNewScore(new ScoreRecording(100, "kk", "June 16"));
    }

    private void setupHighScore() {
        LinearLayout layout = findViewById(R.id.linearScoreBoard);

        for(int i = 0; i < manager.getNumScores(); i++){
            TextView text = new TextView(this);
            text.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f
            ));
            String msg = manager.getScoreArray().get(i).getTimeBySeconds() + " sec "
                    + manager.getScoreArray().get(i).getName() + " on "
                    + manager.getScoreArray().get(i).getDate();
            text.setText(msg);
            text.setTextColor(Color.parseColor("#000000"));
            text.setTextSize(18);
            text.setGravity(Gravity.CENTER);

            layout.addView(text);
        }
    }

    public static void saveHighScores(Context context, ScoreRecordingManager manager){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_HIGH_SCORE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(manager.getScoreArray());
        editor.putString(EDITOR_HIGH_SCORE_ARRAY, json);
        editor.apply();
    }

    public static ArrayList<ScoreRecording> getSavedHighScore(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_HIGH_SCORE, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(EDITOR_HIGH_SCORE_ARRAY, null);
        Type type = new TypeToken<ArrayList<ScoreRecording>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public static Intent makeIntent(Context context){
        return new Intent(context, HighScoreActivity.class);
    }
}
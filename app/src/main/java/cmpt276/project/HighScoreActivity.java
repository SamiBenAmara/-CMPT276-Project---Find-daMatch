package cmpt276.project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import cmpt276.project.model.ScoreRecording;
import cmpt276.project.model.ScoreRecordingManager;

public class HighScoreActivity extends AppCompatActivity {

    ScoreRecordingManager manager;

    public HighScoreActivity(ScoreRecordingManager manager){
        this.manager = manager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

        setUpHighScore();

        Button btReset = findViewById(R.id.ResetButton);
        btReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.resetHighScore();
            }
        });
    }

    private void setUpHighScore() {
        //Implement set up high score:

        TextView tv = findViewById(R.id.ScoreRecording);

        manager.selectionSort();

        for (int i = 0; i < 5; i++){
            String msg = String.format(manager.scoreArray[i].getTimeBySeconds() + " sec "
                    + manager.scoreArray[i].getName()  + " on "
                    + manager.scoreArray[i].getDate());
            tv.setText(msg);
        }
    }
}
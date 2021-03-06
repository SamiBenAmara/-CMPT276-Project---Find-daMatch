package cmpt276.project.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import cmpt276.project.R;

/**
 * HELP SCREEN
 * Displays how to play the game, a hyperlink to the 276 website and the necessary citations as required.
 */
public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        TextView txtBox = findViewById(R.id.textBox);
        txtBox.setMovementMethod(LinkMovementMethod.getInstance());

        Button btn = findViewById(R.id.buttonBack);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public static Intent makeIntent(Context context){
        return new Intent(context, HelpActivity.class);
    }
}
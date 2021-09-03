package com.favepc.reader.rfidreaderutility;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class About extends AppCompatActivity {

    ListView teamList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);

        String[] memberList = this.getResources().getStringArray(R.array.team_members);
        teamList = (ListView)findViewById(R.id.aboutTeam);
        ArrayAdapter<String> teamMembers = new ArrayAdapter<String>(this, R.layout.activity_listview,R.id.textView,memberList );
        teamList.setAdapter(teamMembers);
        Button closeBtn = findViewById(R.id.close_about);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
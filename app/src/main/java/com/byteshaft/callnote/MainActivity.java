package com.byteshaft.callnote;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements Switch.OnCheckedChangeListener
        , Button.OnClickListener {

    Helpers mHelpers;
    private boolean mViewCreated;
    DataBaseHelpers mDbHelpers;
    ArrayList<String> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHelpers = new Helpers(getApplicationContext());
        Switch toggleSwitch = (Switch) findViewById(R.id.aSwitch);
        mDbHelpers = new DataBaseHelpers(getApplicationContext());
        arrayList = mDbHelpers.retrieveByNotesOrNumber(SqliteHelpers.NOTES_COLUMN, "yo");
        ListView modeList = new ListView(this);
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, R.id.listView_main, arrayList);
        modeList.setAdapter(modeAdapter);
        toggleSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Intent intent = new Intent(getApplicationContext(), OverlayService.class);
        if (isChecked) {
            startService(intent);
        } else {
            stopService(intent);
        }
    }

    public void openActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), ContactsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(new Intent(this, ContactsActivity.class));
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_overlay:
                if (mViewCreated) {
                    OverlayHelpers.removePopupNote();
                    mViewCreated = false;
                } else {
                    OverlayHelpers.showPopupNoteForContact("+923422347000");
                    mViewCreated = true;
                }
        }

        class NotesListAdapter extends ArrayAdapter<String> {

            public NotesListAdapter(Context context, int resource, ArrayList<String> videos) {
                super(context, resource, videos);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = getLayoutInflater();
                    convertView = inflater.inflate(R.layout.row, parent, false);
                    holder = new ViewHolder();
                    holder.title = (TextView) convertView.findViewById(R.id.FilePath);
                    holder.time = (TextView) convertView.findViewById(R.id.tv);
                    holder.thumbnail = (ImageView) convertView.findViewById(R.id.Thumbnail);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.title.setText(mVideosTitles[position]);
                holder.time.setText(
                        mHelper.getFormattedTime((mHelper.getDurationForVideo(position))));
                holder.position = position;
                if (BitmapCache.getBitmapFromMemCache(String.valueOf(position)) == null) {
                    holder.thumbnail.setImageURI(null);
                    new ThumbnailCreationTask(getApplicationContext(),
                            holder, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    holder.thumbnail.setImageBitmap(BitmapCache.getBitmapFromMemCache
                            (String.valueOf(position)));
                }
                return convertView;
            }
        }
    }
}
package edu.spu.teamroot.voicecloud;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ExclusionActivity extends ActionBarActivity {
    /*
     * Member variables
     */

    private ListView mListView;
    private Button mAddButton;
    private ArrayAdapter<String> adapter;
    private List<String> wordArrayList;

    /*
     * Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(R.style.ExclusionTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exclusion);

        // Don't load here... will wipe out hit counts. Instead, only load on creation of MainActivity
        //ExclusionList.getInstance().load();

        mListView = (ListView) findViewById(R.id.exclusionList);

        wordArrayList = new ArrayList<>(ExclusionList.getInstance().excludeList.keySet());
        adapter = new ArrayAdapter<>(this, R.layout.exclusion_row, wordArrayList);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                removeItemFromList(position);
            }
        });

        mAddButton = (Button) findViewById(R.id.add_button);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAdd();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        ExclusionList.getInstance().save();
    }

    protected void removeItemFromList(final int position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(
                ExclusionActivity.this);

        alert.setTitle("Delete");
        alert.setMessage("Do you want delete this item?");
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String word = wordArrayList.get(position);
                wordArrayList.remove(word);
                ExclusionList.getInstance().removeWord(word);
                adapter.notifyDataSetChanged();
                adapter.notifyDataSetInvalidated();

            }
        });
        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openAdd()
    {
        AlertDialog.Builder addalert = new AlertDialog.Builder(ExclusionActivity.this);
        final LayoutInflater inflater = this.getLayoutInflater();

        addalert.setTitle("Add Words");

        // Set up the input
        final EditText et = new EditText(ExclusionActivity.this);
        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        et.setPadding(50, 50, 50, 50);
        addalert.setView(et);

        addalert.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String input = et.getText().toString();
                if (input.length() > 0) {

                    // Split up input string into words
                    String[] words = input.toLowerCase().split(" ");

                    // Add words entered to exclusion list
                    for (String word : words) {
                        // Clean up words and remove punctuation
                        word = word.replaceAll("[^\\w\\d'-]", "");

                        boolean hasChars = word.replaceAll("['-]", "").length() > 0;

                        if (hasChars) {
                            if (ExclusionList.getInstance().addWord(word)) {
                                // If word is not already in the list
                                wordArrayList.add(word);

                                // Dynamically remove word from cloud
                                Word wordObj = WordCloud.getInstance().getWord(word);
                                if (wordObj != null) {
                                    WordCloud.getInstance().removeWord(wordObj);
                                }
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                }
            }
        });
        addalert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        addalert.show();
    }
}
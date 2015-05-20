package edu.spu.teamroot.voicecloud;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class ExclusionActivity extends ActionBarActivity {

    /*
     * Member variables
     */

    ListView mListView;
    ArrayAdapter<String> adapter;
    List<String> wordArrayList;

    /*
     * Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.setTheme(R.style.ExclusionTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exclusion);

        mListView = (ListView) findViewById(R.id.exclusionList);

        wordArrayList = new ArrayList<String>(ExclusionList.getInstance().excludeList.keySet());
        adapter = new ArrayAdapter<String>(this, R.layout.exclusion_row, wordArrayList);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                removeItemFromList(position);
            }
        });
    }

    protected void removeItemFromList(final int position) {
        final int deletePosition = position;

        AlertDialog.Builder alert = new AlertDialog.Builder(
                ExclusionActivity.this);

        alert.setTitle("Delete");
        alert.setMessage("Do you want delete this item?");
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String word = wordArrayList.get(deletePosition);
                wordArrayList.remove(word);
                ExclusionList.getInstance().removeWord(word);
                adapter.notifyDataSetChanged();
                adapter.notifyDataSetInvalidated();

            }
        });
        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_add) {
        //    openAdd();
        //}

        return super.onOptionsItemSelected(item);
    }
    void openAdd()
    {
        AlertDialog.Builder addalert = new AlertDialog.Builder(ExclusionActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();

        final View v = inflater.inflate(R.layout.exclusion_add, null);

        final EditText et = (EditText)v.findViewById(R.id.add_item);

        addalert.setView(v);
        addalert.setTitle("Exclusion List");
        addalert.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String input = et.getText().toString();
                if(null!=input&&input.length()>0){
                    wordArrayList.add(input);
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
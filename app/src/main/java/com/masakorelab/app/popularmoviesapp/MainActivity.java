package com.masakorelab.app.popularmoviesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;




public class MainActivity extends ActionBarActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final String MOVIEFRAGMENT_TAG = "MFTAG";

    private String mSortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            //Not sure why cannot resolve add() by using getSupportFragmentManager()
            //Even though after importing android.support.v4.app.Fragment
            getSupportFragmentManager().beginTransaction().add(R.id.container, new MovieFragment(), MOVIEFRAGMENT_TAG).commit();
            mSortOrder = Utility.getPreferredSortOrder(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sortOrder = Utility.getPreferredSortOrder(this);

        if (sortOrder != null && !sortOrder.equals(mSortOrder)) {
            MovieFragment mf = (MovieFragment)getSupportFragmentManager().findFragmentByTag(MOVIEFRAGMENT_TAG);
            if (null != mf){
                mf.onSortOrderChange();
            }
            mSortOrder = sortOrder;
        }
    }
}

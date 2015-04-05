package com.wbch.testapp;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.metaio.tools.io.AssetsManager;
import java.io.IOException;

public class MainActivity extends ActionBarActivity {

    /**
     * Declaramos texto de opciones e iconos
     */
    String TITLES[] = {"Inicio", "Direcciones", "Realidad Aumentada", "Ajustes", "Info"};
    int ICONS[] = {R.drawable.home_dark, R.drawable.globe_dark, R.drawable.reality_dark, R.drawable.settings, R.drawable.info};

    /**
     * Declaramos el nombre de usuario, su correo y su avatar
     */
    String NAME = "Walter Béjar Chacón";
    String EMAIL = "wbejarch@gmail.com";
    int PROFILE = R.drawable.avatar;

    private Toolbar toolbar;

    /* Declaramos lo necesario para recycler view */
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout Drawer;

    ActionBarDrawerToggle mDrawerToggle;

    /* Declaramos lo necesario para metaio (AR) */
    private AssetsExtracter mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        /* FAB */
        findViewById(R.id.floatingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Se presiono el floating button", Toast.LENGTH_SHORT).show();
            }
        });

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new MyAdapter(TITLES, ICONS, NAME, EMAIL, PROFILE);

        mRecyclerView.setAdapter(mAdapter);

        final GestureDetector mGestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e){
                return true;
            }
        });

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

                if(child != null  && mGestureDetector.onTouchEvent(motionEvent)){

                    int childs = recyclerView.getChildCount();

                    for(int i = 1; i < childs; i++) {
                        View mView = recyclerView.getChildAt(i);
                        mView.findViewById(R.id.rowMenu).setBackgroundColor(Color.parseColor("#FFFFFF"));
                        ((ImageView) mView.findViewById(R.id.rowIcon)).clearColorFilter();
                        ((TextView) mView.findViewById(R.id.rowText)).setTextColor(getResources().getColor(R.color.fontColorMenu));
                    }

                    child.findViewById(R.id.rowMenu).setBackgroundColor(Color.parseColor("#EEEEEE"));
                    ((ImageView)child.findViewById(R.id.rowIcon)).setColorFilter(getResources().getColor(R.color.primaryColor));
                    ((TextView) child.findViewById(R.id.rowText)).setTextColor(getResources().getColor(R.color.primaryColor));

                    Drawer.closeDrawers();
                    Toast.makeText(MainActivity.this, "La opcion seleccionada es: " + recyclerView.getChildPosition(child), Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

            }
        });

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        Drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, Drawer, toolbar, R.string.drawer_open, R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }
        };

        Drawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        //Llamamos al extractor en segundo plano
        mTask = new AssetsExtracter();
        mTask.execute(0);
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
            return true;
        }

        if(id == R.id.mapActivity) {
            startActivity(new Intent(this, MapsActivity.class));
        }

        if(id == R.id.arActivity) {
            startActivity(new Intent(this, MetaioActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    //Extracter de Metaio
    private class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean>
    {

        @Override
        protected void onPreExecute()
        {
        }

        @Override
        protected Boolean doInBackground(Integer... params)
        {
            try
            {
                // Extract all assets and overwrite existing files if debug build
                AssetsManager.extractAllAssets(getApplicationContext(), BuildConfig.DEBUG);
            }
            catch (IOException e)
            {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if (result)
            {
                // Start AR Activity on success
                Toast.makeText(MainActivity.this, "Se cargaron los assets.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                // Show a toast with an error message
                Toast.makeText(MainActivity.this, "Error al extraer los assets", Toast.LENGTH_SHORT).show();
            }
        }

    }
}

package pushpo.emapp.activity;


import android.Manifest;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import io.github.yavski.fabspeeddial.FabSpeedDial;
import pushpo.emapp.R;
import pushpo.emapp.adapter.MenuItemListAdapter;
import pushpo.emapp.jcls.PlaceDetailProvider;


public class MenuActivity extends AppCompatActivity {

	//View Reference Variable
	FloatingActionButton fab, fab_tips, fab_chat999, fab_call999;
	TextView txt_tips, txt_chat_999, txt_call_999;
	Animation fabopen, fabclose, rotateforward, rotatebackward;
	boolean isOpen = false;

	private RecyclerView mRecyclerView;
	private GridLayoutManager mGridLayoutManager;
	private MenuItemListAdapter mMenuItemListAdapter;
	private String[] itemString;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mToggle;
	private NavigationView mNavigationView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

		//fab button

		fab = (FloatingActionButton) findViewById(R.id.fab);
		fab_tips = (FloatingActionButton) findViewById(R.id.fab_tips);
		fab_chat999 = (FloatingActionButton) findViewById(R.id.fab_chat999);
		fab_call999 = (FloatingActionButton) findViewById(R.id.fab_call999);

		txt_call_999 = (TextView) findViewById(R.id.txt_call_999);
		txt_chat_999 = (TextView) findViewById(R.id.txt_chat_999);
		txt_tips = (TextView) findViewById(R.id.txt_tips);



		fabopen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
		fabclose = AnimationUtils.loadAnimation(this, R.anim.fab_close);

		rotateforward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
		rotatebackward = AnimationUtils.loadAnimation(this, R.anim.rotate_backward);

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				animatefab();

			}
		});


		fab_call999.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				animatefab();
				Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel:999"));
				if (ActivityCompat.checkSelfPermission(MenuActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling
					//    ActivityCompat#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for ActivityCompat#requestPermissions for more details.
					return;
				}
				startActivity(intent);
			}
		});
		fab_chat999.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				animatefab();



			}
		});
		fab_tips.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				animatefab();


			}
		});


        Toolbar actionBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(actionBar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.draw_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        //set the drawerListener
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.app_name);
        actionBar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.location_favourite_icon:
                        startActivity(new Intent(MenuActivity.this, FavouritePlaceListActivity.class));
                        mDrawerLayout.closeDrawers();
                        break;

                    case R.id.share_icon:
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey, Checkout Emergency Map Application (EMAPP)");
                        startActivity(Intent.createChooser(shareIntent, "Share App.."));
                        mDrawerLayout.closeDrawers();
                        break;

                    case R.id.about_icon:
                        Dialog aboutDialog = new Dialog(MenuActivity.this, R.style.AboutDialog);
                        aboutDialog.setTitle(getString(R.string.about));
                        aboutDialog.setContentView(R.layout.about);
                        aboutDialog.show();
                        mDrawerLayout.closeDrawers();
                        break;
                }
                return false;
            }
        });

        itemString = PlaceDetailProvider.placeTagName;
        mMenuItemListAdapter = new MenuItemListAdapter(this, itemString);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mGridLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(36);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        mRecyclerView.setAdapter(mMenuItemListAdapter);

    }

    private void animatefab()
	{
		if (isOpen)
		{
			fab.startAnimation(rotatebackward);
			fab_call999.startAnimation(fabclose);
			fab_chat999.startAnimation(fabclose);
			fab_tips.startAnimation(fabclose);
			txt_call_999.startAnimation(fabclose);
			txt_chat_999.startAnimation(fabclose);
			txt_tips.startAnimation(fabclose);


			fab_call999.setClickable(false);
			fab_chat999.setClickable(false);
			fab_tips.setClickable(false);

			isOpen= false;
		}
		else
		{
			fab.startAnimation(rotateforward);
			fab_call999.startAnimation(fabopen);
			fab_chat999.startAnimation(fabopen);
			fab_tips.startAnimation(fabopen);
			txt_call_999.startAnimation(fabopen);
			txt_chat_999.startAnimation(fabopen);
			txt_tips.startAnimation(fabopen);

			fab_call999.setClickable(true);
			fab_chat999.setClickable(true);
			fab_tips.setClickable(true);

			isOpen= true;
		}
	}




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate menu to add items to action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.removeItem(R.id.share_icon);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setQueryHint(getString(R.string.search_hint));

        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(this, PlaceSearchResultActivity.class)));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



}

package com.piche.wimc;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Process;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ZoomControls;

public class WimcActivity extends MapActivity {
	private PrefsValues mPrefsValues;
	private MyLocationOverlay mMyLocationOverlay;
	private LinearLayout linearLayout;
	private MapView mapView;
	private ZoomControls mZoom;
	private String strView = "";
	private int iMapViewType = 0;
    private List<Overlay> mapOverlays;
	private ItemsOverlay ioLocation;
	private Menu mnuWimc;
    private boolean bSavedLocation;
	public GeoPoint mMarkedLocation;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        //key api map home : 0EO15Zk8QjbSObBQF6wjyDuUE_wiYE52IIapHgw 
    	//key api map work : 0EO15Zk8QjbQj21VPuggM24lLV0W3dRk7MdUFIw
		//key api map final: 0EO15Zk8QjbQ9hBTeaMDTLKdpBIy0OTX1tjoy2A
    	
    	//requestWindowFeature(Window.FEATURE_NO_TITLE); 
    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);        
        
        mPrefsValues = new PrefsValues(this);
        mMarkedLocation = mPrefsValues.getSavedLocation(mMarkedLocation);
        
        if(mMarkedLocation.getLatitudeE6() != 0 && mMarkedLocation.getLongitudeE6() != 0)
        	bSavedLocation = true;
        else
        	bSavedLocation = false;
        
        linearLayout = (LinearLayout) findViewById(R.id.zoomview);
        mapView = (MapView) findViewById(R.id.mapview);
        mZoom = (ZoomControls) mapView.getZoomControls();
        
        linearLayout.addView(mZoom);
        
        mMyLocationOverlay = new MyLocationOverlay(this, mapView);
        mMyLocationOverlay.runOnFirstFix(new Runnable() { public void run() {
        	mapView.getController().animateTo(mMyLocationOverlay.getMyLocation());
        }});
        
        mapView.getOverlays().add(mMyLocationOverlay);
        mapView.getController().setZoom(17);
        mapView.setClickable(true);
        mapView.setEnabled(true);
        
        if(mapView.isSatellite())
        	iMapViewType = 1;
        
        updateLocation();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mMyLocationOverlay.enableMyLocation();
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	mnuWimc = menu;
    	
        // Inflate the currently selected menu XML resource.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        
        if(bSavedLocation)
        	mnuWimc.getItem(0).setVisible(false);
        else
        	mnuWimc.getItem(1).setVisible(false);
        
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

        	case R.id.mnu_mark:
        		doMarkLocation();
        		item.setVisible(false);
        		mnuWimc.getItem(1).setVisible(true);
                return true;
            
        	case R.id.mnu_delete:
        		doDeleteLocation();
        		item.setVisible(false);
        		mnuWimc.getItem(0).setVisible(true);
                return true;
                
            case R.id.mnu_settings:
            	AlertDialog adSelectCar = new AlertDialog.Builder(WimcActivity.this)
            	.setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("Settings")
                .setSingleChoiceItems(R.array.select_view, iMapViewType, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String[] strViews = getResources().getStringArray(R.array.select_view);
                        strView = strViews[whichButton];
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	if(strView.equals("Satellite View")){
                    		mapView.setSatellite(true);
                    		iMapViewType = 1;
                    	}
                    	else{
                    		mapView.setSatellite(false);
                    		iMapViewType = 0;
                    	}
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked No so do some stuff */
                    }
                })
                .create();
            	
            	adSelectCar.show();

                return true;
               
            case R.id.mnu_exit:
            	Stop();
                return true;
                
            default:
                if (!item.hasSubMenu()) {
                    Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                }
                break;
        }
        
        return false;
    }
	
	private void doMarkLocation() {
        mMarkedLocation = mMyLocationOverlay.getMyLocation();

        updateLocation();
        
        mPrefsValues.setSavedLocation(mMarkedLocation);
    }
	
	private void updateLocation(){
		if (mMarkedLocation != null) {
            OverlayItem overlayitem = new OverlayItem(mMarkedLocation, "", "");
            
            Drawable dCarIcone = this.getResources().getDrawable(R.drawable.mark);
            ioLocation = new ItemsOverlay(dCarIcone);
            
            ioLocation.addOverlay(overlayitem);
            mapOverlays = mapView.getOverlays();
            mapOverlays.add(ioLocation);

            mapView.invalidate();
        }
	}
	
	private void doDeleteLocation() {
		mapOverlays.remove(ioLocation);
		
		mMarkedLocation = null;
		
		mapView.performClick();
		mapView.refreshDrawableState();
		
		mPrefsValues.deleteLocation();
		
		mapView.invalidate();
    }
	
	private class GpsLocationListener implements LocationListener {
		@Override
		public void onProviderDisabled(String arg0) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
		
		@Override
	    public void onLocationChanged(Location location) {
			
	    }
	}

	private void Stop() {
    	mMyLocationOverlay.disableMyLocation();
        super.onStop();
        super.finish();
        Process.killProcess(Process.myPid());
    }
}
package com.studiomoob.main;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.studiomoob.cidadesinvisiveis.view.RegionOverlay;

public class ReloadDataMapaViewRunnable  implements Runnable{

	private List<Overlay> listOfOverlays; 
	private List<GeoPoint> points; 
	
	@Override
	public void run() {
		for (GeoPoint point : points) {
			RegionOverlay regionOverlay = new RegionOverlay(point,10);			
			listOfOverlays.add(regionOverlay);
		}
		
	}

	public ReloadDataMapaViewRunnable(List<Overlay> listOfOverlays,
			List<GeoPoint> points) {
		super();
		this.listOfOverlays = listOfOverlays;
		this.points = points;
	}

}

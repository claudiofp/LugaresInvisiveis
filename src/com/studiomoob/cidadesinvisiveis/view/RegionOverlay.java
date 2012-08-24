package com.studiomoob.cidadesinvisiveis.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class RegionOverlay extends Overlay {

	private static int CIRCLERADIUS = 0;
	private GeoPoint geopoint;
	private int myCircleRadius;
	Point point = new Point();
	Paint circle = new Paint(Paint.ANTI_ALIAS_FLAG);
	private long systemTime= -1 ;
	
	public RegionOverlay(GeoPoint point, int myRadius) {
		this.geopoint = point;
	    CIRCLERADIUS = myRadius; 
	}
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {

	     Projection projection = mapView.getProjection();
	     projection.toPixels(geopoint, point);

	     circle.setColor(Color.parseColor("#F70019"));
	     circle.setAlpha(100); 
	     

	     myCircleRadius = metersToRadius(CIRCLERADIUS, mapView,
	     (double) geopoint.getLatitudeE6() / 1000000);

	     canvas.drawCircle(point.x, point.y, myCircleRadius, circle);       

	}
	public static int metersToRadius(float meters, MapView map, double latitude) {
	    return (int) (map.getProjection().metersToEquatorPixels(meters) * (1 / Math
	            .cos(Math.toRadians(latitude))));
	}
	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
	    switch (event.getAction()) {
	    case MotionEvent.ACTION_DOWN:
	        if ((System.currentTimeMillis() - systemTime) < 250) {
	            mapView.getController().zoomIn();
	        }
	        systemTime = System.currentTimeMillis();
	        break;
	    }

	    return false;
	}

}

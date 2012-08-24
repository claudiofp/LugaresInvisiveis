package com.studiomoob.cidadesinvisiveis.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class CustomUserLocationOverlay extends Overlay {

	private GeoPoint pointToDraw;
	private Resources resources;
	private int iconID;

	public CustomUserLocationOverlay(GeoPoint point,Resources resources,int iconID)
	{
		this.pointToDraw = point;
		this.resources = resources;
		this.iconID = iconID;
	}
	public void setPointToDraw(GeoPoint point) {
		pointToDraw = point;
	}

	public GeoPoint getPointToDraw() {
		return pointToDraw;
	}

	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		super.draw(canvas, mapView, shadow);

		// convert point to pixels
		Point screenPts = new Point();
		mapView.getProjection().toPixels(pointToDraw, screenPts);

		// add marker
		Bitmap bmp = BitmapFactory.decodeResource(resources,iconID);
		canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 24, null);
		return true;
	}

}

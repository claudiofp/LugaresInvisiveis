package com.studiomoob.main;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.studiomoob.cidadesinvisiveis.R;
import com.studiomoob.cidadesinvisiveis.audio.ControlAudio;
import com.studiomoob.cidadesinvisiveis.view.GifMovieView;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends MapActivity implements LocationListener,
		OnCompletionListener, OnErrorListener {

	ProgressDialog progressDialog;

	List<Overlay> mapOverlays;
	Drawable userDrawable;

	LocationManager locationManager;

	private MapController mapController;
	private MapView mapView;
	private enum APP_STATE {
		WAITING_GPS, RECORDING, PLAYING, LISTENING, RETRIEVING
	}

	private APP_STATE appState;

	private ImageButton recordButton;
	private ImageButton gpsButton;
	private ControlAudio controlAudio;
	Location currentLocation = null;
	ParseFile file = null;

	protected ArrayList<ParseObject> listDataObjects;

	private MyLocationOverlay myLocationOverlay;

	/**
	 * First method call in activity
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		appState = APP_STATE.LISTENING;

		// Setup mapView
		mapView = (MapView) findViewById(R.id.mapview);
		mapController = mapView.getController();
		mapView.setBuiltInZoomControls(true);

		// Start GPS Service
		this.startServiceGPS();

		// Setup record process
		controlAudio = new ControlAudio(this);
		recordButton = (ImageButton) findViewById(R.id.btnRecord);
		recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				actionRecordButton();
			}
		});
		recordButton.setClickable(false);
		recordButton.setImageResource(R.drawable.icon_gravar_desabilitado);

		gpsButton = (ImageButton) findViewById(R.id.btnGPS);
		gpsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if(currentLocation != null)
				{
					GeoPoint point = new GeoPoint((int) (currentLocation
							.getLatitude() * 1e6), (int) (currentLocation
							.getLongitude() * 1e6));

					// Map zoom to user location
					mapController.animateTo(point);
					mapController.setZoom(20);

					reloadUserLocation(currentLocation);
				}
			}
		});

		this.stopChrono();

		this.reloadDataMapaView();

	}

	private void changeBarStyle() {

		ImageView barStats = (ImageView) findViewById(R.id.staticBar);
		LinearLayout container = (LinearLayout) findViewById(R.id.containerControls);
		GifMovieView gifView = null;
		InputStream stream = null;
		switch (this.appState) {
		case RECORDING:
			barStats.setVisibility(View.INVISIBLE);

			try {
				stream = getAssets().open("barra_gravando.gif");
				gifView = new GifMovieView(this, stream);
				gifView.setId(1423);
				gifView.setLayoutParams(new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
				container.addView(gifView, 0);

			} catch (IOException e) {
				e.printStackTrace();
			}

			break;
		case PLAYING:
			barStats.setVisibility(View.INVISIBLE);
			try {
				stream = getAssets().open("barra_playing.gif");
				gifView = new GifMovieView(this, stream);
				gifView.setId(1423);
				gifView.setLayoutParams(new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
				container.addView(gifView, 0);
				recordButton.setClickable(false);
				recordButton
						.setImageResource(R.drawable.icon_gravar_desabilitado);

			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case LISTENING:
			barStats.setVisibility(View.VISIBLE);
			gifView = (GifMovieView) findViewById(1423);
			container.removeView(gifView);
		default:
			break;
		}
	}

	private void reloadUserLocation() {
		try {

			if (currentLocation != null) {
				
				// Insert user location pin
				myLocationOverlay = new MyLocationOverlay(this, mapView);
				myLocationOverlay.enableCompass();
				myLocationOverlay.enableMyLocation();				
				mapView.getOverlays().add(myLocationOverlay);

				this.checkPlayAudio();
			}

		} catch (Exception e) {
			Log.e("tag", e.getMessage());
		}
	}

	private void reloadUserLocation(Location location) {
		if (location != null) {
			currentLocation = location;
			this.reloadUserLocation();
		}

	}

	/********************************************************
	 * Management chronometer
	 *********************************************************/
	private void startChrono() {
		Chronometer chrono = (Chronometer) findViewById(R.id.chrono);
		chrono.setVisibility(View.VISIBLE);
		chrono.setBase(SystemClock.elapsedRealtime());
		chrono.start();
	}

	private void stopChrono() {
		Chronometer chrono = (Chronometer) findViewById(R.id.chrono);
		chrono.stop();
		chrono.setVisibility(View.INVISIBLE);
		chrono.setText("00:00");

	}

	/********************************************************
	 * Management record and play actions
	 *********************************************************/
	private void actionRecordButton() {
		if (this.appState == APP_STATE.LISTENING) // Start record
		{
			try {
				this.stopServiceGPS();
				if (controlAudio.startRecording()) {

					this.appState = APP_STATE.RECORDING;
					this.startChrono();
					this.changeBarStyle();
					recordButton
							.setImageResource(R.drawable.icon_gravar_gravando);
				}
			} catch (Exception e) {
				this.stopChrono();
				this.startServiceGPS();
				this.appState = APP_STATE.LISTENING;
				recordButton.setImageResource(R.drawable.icon_gravar_s1);
				this.changeBarStyle();
			}
		} else if (this.appState == APP_STATE.RECORDING) // Stop record
		{
			try {
				this.appState = APP_STATE.LISTENING;
				this.startServiceGPS();
				this.stopChrono();
				this.changeBarStyle();
				controlAudio.stopRecording();
				recordButton.setImageResource(R.drawable.icon_gravar_s1);

				if (currentLocation != null) {
					progressDialog = ProgressDialog.show(MainActivity.this, "",
							"Enviando gravação...");

					SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
					String format = s.format(new java.util.Date());

					file = new ParseFile(format + ".3gp",
							controlAudio.readBytesRecordFile());

					file.saveInBackground(new SaveCallback() {

						@Override
						public void done(ParseException e) {
							if (e == null) {

								ParseObject dataAudio = new ParseObject(
										"DataAudio");
								ParseGeoPoint point = new ParseGeoPoint(
										currentLocation.getLatitude(),
										currentLocation.getLongitude());
								dataAudio.put("location", point);
								dataAudio.put("audioFile", file);

								try {
									dataAudio.save();

								} catch (Exception e2) {

								} finally {
									progressDialog.dismiss();

									listDataObjects.add(dataAudio);

									ArrayList<GeoPoint> listPoints = new ArrayList<GeoPoint>();
									GeoPoint pointTmp = new GeoPoint(
											(int) (currentLocation
													.getLatitude() * 1e6),
											(int) (currentLocation
													.getLongitude() * 1e6));
									listPoints.add(pointTmp);
									ReloadDataMapaViewRunnable runnable = new ReloadDataMapaViewRunnable(
											mapView.getOverlays(), listPoints);
									MainActivity.this.runOnUiThread(runnable);
									controlAudio.shouldPlay(dataAudio
											.getObjectId());
									controlAudio.startPlayCurrentRecord();
									appState = APP_STATE.PLAYING;
									recordButton.setClickable(false);
									recordButton
											.setImageResource(R.drawable.icon_gravar_desabilitado);

								}

							} else {
								progressDialog.dismiss();
							}

						}
					}, new ProgressCallback() {

						@Override
						public void done(Integer progress) {

							if (!progressDialog.isShowing())
								progressDialog = ProgressDialog.show(
										MainActivity.this, "",
										"Enviando gravação... " + progress
												+ "%");
							else
								progressDialog
										.setMessage("Enviando gravação... "
												+ progress + "%");
						}
					});
				}

			} catch (Exception e) {
				Log.i("gps", e.getMessage());

			}
		}
	}

	private void checkPlayAudio() {

		if (currentLocation != null
				&& (this.appState == APP_STATE.LISTENING || this.appState == APP_STATE.PLAYING)) {

			try {
				Boolean findPlayFile = false;
				Boolean nearbyRecord = false;
				ParseGeoPoint currentLocationGeoPoint = new ParseGeoPoint(
						currentLocation.getLatitude(),
						currentLocation.getLongitude());
				for (ParseObject dataObject : listDataObjects) {

					ParseGeoPoint pointData = dataObject
							.getParseGeoPoint("location");
					long distanceInMeters = (long) (currentLocationGeoPoint
							.distanceInKilometersTo(pointData) * 1000);
					if (distanceInMeters < 10) {
						findPlayFile = true;

						if (controlAudio.shouldPlay(dataObject.getObjectId())) {
							ParseFile file = (ParseFile) dataObject
									.get("audioFile");
							controlAudio.startPlaying(file.getData(), this,
									this);
							this.appState = APP_STATE.PLAYING;
							this.changeBarStyle();
							break;
						}
					} else if (distanceInMeters < 20) {
						nearbyRecord = true;
					}

				}

				if (!findPlayFile)
					controlAudio.stopPlaying();
				if (nearbyRecord) {
					recordButton.setClickable(false);
					recordButton
							.setImageResource(R.drawable.icon_gravar_desabilitado);
				}

				if (!findPlayFile && !nearbyRecord) {
					recordButton.setImageResource(R.drawable.icon_gravar_s1);
					recordButton.setClickable(true);
				}

			} catch (Exception e) {

			}
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		appState = APP_STATE.LISTENING;
		controlAudio.stopPlaying();
		this.changeBarStyle();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		controlAudio.stopPlaying();
		this.changeBarStyle();
		return false;
	}

	/********************************************************
	 * Management of GPS
	 *********************************************************/
	private void startServiceGPS() {

		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
		//		0, this);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, this);

	}

	private void stopServiceGPS() {
		locationManager.removeUpdates(this);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		this.reloadUserLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {

		recordButton.setClickable(false);
		appState = APP_STATE.WAITING_GPS;
		recordButton.setImageResource(R.drawable.icon_gravar_desabilitado);

	}

	@Override
	public void onProviderEnabled(String provider) {

		appState = APP_STATE.LISTENING;

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	/********************************************************
	 * Search for information on the server
	 *********************************************************/
	private void reloadDataMapaView() {
		this.appState = APP_STATE.RETRIEVING;
		final ParseQuery query = new ParseQuery("DataAudio");
		new Thread() {
			public void run() {
				try {

					listDataObjects = query.find();

					if (listDataObjects.size() > 0) {
						ArrayList<GeoPoint> listPoints = new ArrayList<GeoPoint>();
						for (ParseObject dataObject : listDataObjects) {
							ParseGeoPoint pointData = dataObject
									.getParseGeoPoint("location");

							GeoPoint point = new GeoPoint(
									(int) (pointData.getLatitude() * 1e6),
									(int) (pointData.getLongitude() * 1e6));

							listPoints.add(point);
						}
						ReloadDataMapaViewRunnable runnable = new ReloadDataMapaViewRunnable(
								mapView.getOverlays(), listPoints);
						MainActivity.this.runOnUiThread(runnable);
					}
					appState = APP_STATE.LISTENING;
					checkPlayAudio();

				} catch (Exception e) {

					Log.e("tag", e.getMessage());

				}
			}
		}.start();
	}
}
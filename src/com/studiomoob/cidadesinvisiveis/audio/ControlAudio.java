package com.studiomoob.cidadesinvisiveis.audio;

import android.os.Environment;
import android.content.Context;
import android.util.Log;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaRecorder;
import android.media.MediaPlayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ControlAudio {
	private static final String LOG_TAG = "AudioRecordTest";
	private static String mFileName = null;
	private MediaRecorder mRecorder = null;
	private MediaPlayer mPlayer = null;
	
	String idObject;
	
	public void startPlaying(byte[] bytes,OnCompletionListener listener,OnErrorListener listenerError)
	{
		if(this.isPlaying())
			this.stopPlaying();	
		mPlayer = new MediaPlayer();
		try {
			
			File outputFile = File.createTempFile("tmp",null);			
			FileOutputStream stream = new FileOutputStream(outputFile.getAbsolutePath()); 
			stream.write(bytes);
			stream.close();
			
			mPlayer.setDataSource(outputFile.getAbsolutePath());
			mPlayer.prepare();
			mPlayer.start();
			mPlayer.setLooping(true);
			mPlayer.setOnCompletionListener(listener);
			mPlayer.setOnErrorListener(listenerError);
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}
	}
	public void startPlayCurrentRecord() {
		if(this.isPlaying())
			this.stopPlaying();
		
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mFileName);
			mPlayer.prepare();
			mPlayer.start();
			mPlayer.setLooping(true);
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}
	}
	public boolean shouldPlay(String idObject)
	{
		boolean should = false;
		if(idObject != this.idObject)
		{
			this.idObject = idObject;
			should = true;
		}
		else if (!this.isPlaying())
			should = true;	
		return  should;
	}	
	
	public void stopPlaying() {
		if(mPlayer != null)
		{
			mPlayer.release();
			mPlayer = null;
		}
		
	}

	public Boolean startRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);		
		mRecorder.setOutputFile(mFileName);		
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);


		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
			return false;
		}

		mRecorder.start();
		return true;
	}

	public void stopRecording() {
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}

	public ControlAudio(Context context) {		
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += "/audiorecordtest.3gp";
	}
	
	public byte[] readBytesRecordFile()
			throws IOException {
		
		InputStream inputStream = new FileInputStream(mFileName);
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}

		// and then we can return your byte array.
		return byteBuffer.toByteArray();
	}
	public boolean isPlaying()
	{
		boolean playing = false;
		if(mPlayer != null)
			playing = mPlayer.isPlaying();		
		return playing;
	}

}

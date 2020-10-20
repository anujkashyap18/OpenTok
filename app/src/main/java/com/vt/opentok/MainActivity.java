package com.vt.opentok;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

	final int RC_VIDEO_APP_PERM = 124;
	String API_KEY = "";
	String SESSION_ID = "";
	String TOKEN = "";
	String LOG_TAG = MainActivity.class.getSimpleName ( );
	Button screenShare;
	private Session mSession;
	private Publisher mPublisher;
	private Subscriber mSubscriber;
	private FrameLayout mPublisherViewContainer;
	private FrameLayout mSubscriberViewContainer;

	@Override
	protected void onCreate ( Bundle savedInstanceState ) {
		super.onCreate ( savedInstanceState );
		setContentView ( R.layout.activity_main );

		mPublisherViewContainer = findViewById ( R.id.publisher_container );
		mSubscriberViewContainer = findViewById ( R.id.subscriber_container );
		screenShare = findViewById ( R.id.screen_share );

		screenShare.setOnClickListener ( new View.OnClickListener ( ) {
			@Override
			public void onClick ( View v ) {
				Intent intent = new Intent ( MainActivity.this , ScreenActivity.class );
				startActivity ( intent );
//				mSession.disconnect ();
//				mPublisher.getSession ().disconnect ();
			}
		} );

//
//		StringRequest stringRequest = new StringRequest ( Request.Method.POST , url , response -> {
//
//			Log.d ( getClass ( ).getSimpleName ( ) , "RESPONSE : " + response );
//			try {
//				JSONObject jsonObject = new JSONObject ( response );
//				TOKEN = jsonObject.getString ( "token" );
//				SESSION_ID = jsonObject.getString ( "session_id" );
//
//
//			} catch ( JSONException e ) {
//				e.printStackTrace ( );
//			}
//		} , error -> Toast.makeText ( MainActivity.this , "error" + error , Toast.LENGTH_SHORT ).show ( ) ) {
//			@Override
//			protected Map < String, String > getParams ( ) {
//				Map < String, String > hm = new HashMap <> ( );
//				hm.put ( "session_id" , "" );
//				hm.put ( "password" , "" );
//				return hm;
//			}
//		};
//		Volley.newRequestQueue ( this ).add ( stringRequest );
		requestPermissions ( );
	}

	@Override
	public void onRequestPermissionsResult ( int requestCode , @NonNull String[] permissions , @NonNull int[] grantResults ) {
		super.onRequestPermissionsResult ( requestCode , permissions , grantResults );
		EasyPermissions.onRequestPermissionsResult ( requestCode , permissions , grantResults , this );
	}

	@AfterPermissionGranted ( RC_VIDEO_APP_PERM )
	private void requestPermissions ( ) {
		String[] perms = { Manifest.permission.INTERNET , Manifest.permission.CAMERA , Manifest.permission.RECORD_AUDIO };
		if ( EasyPermissions.hasPermissions ( this , perms ) ) {
			Log.d ( getClass ( ).getSimpleName ( ) , "PERMISSIONS : " );

			mSession = new Session.Builder ( this , API_KEY , SESSION_ID ).build ( );
			Log.d ( getClass ( ).getSimpleName ( ) , "xyz" + mSession.getSessionId ( ) );
			mSession.setSessionListener ( this );
			mSession.connect ( TOKEN );


		}
		else {
			EasyPermissions.requestPermissions ( this , "This app needs access to your camera and mic to make video calls" , RC_VIDEO_APP_PERM , perms );
		}
	}

	@Override
	public void onConnected ( Session session ) {

		Log.i ( LOG_TAG , "Session Connected" );

		mPublisher = new Publisher.Builder ( this ).build ( );
		mPublisher.setPublisherListener ( this );


		mPublisherViewContainer.addView ( mPublisher.getView ( ) );

		if ( mPublisher.getView ( ) instanceof GLSurfaceView ) {
			( ( GLSurfaceView ) mPublisher.getView ( ) ).setZOrderOnTop ( true );
		}

		mSession.publish ( mPublisher );

	}

	@Override
	public void onDisconnected ( Session session ) {
		Log.d ( getClass ( ).getSimpleName ( ) , "session" + session );
	}

	@Override
	public void onStreamReceived ( Session session , Stream stream ) {
		Log.i ( LOG_TAG , "Stream Received" );

		if ( mSubscriber == null ) {
			mSubscriber = new Subscriber.Builder ( this , stream ).build ( );
			mSession.subscribe ( mSubscriber );
			mSubscriberViewContainer.addView ( mSubscriber.getView ( ) );
		}
	}

	@Override
	public void onStreamDropped ( Session session , Stream stream ) {
//		Log.i ( LOG_TAG , "Stream Dropped" );
		Log.d ( getClass ( ).getSimpleName ( ) , "stream" + stream );


		if ( mSubscriber != null ) {
			mSubscriber = null;
			mSubscriberViewContainer.removeAllViews ( );
		}
	}

	@Override
	public void onError ( Session session , OpentokError opentokError ) {

	}

	@Override
	public void onStreamCreated ( PublisherKit publisherKit , Stream stream ) {

	}

	@Override
	public void onStreamDestroyed ( PublisherKit publisherKit , Stream stream ) {

	}

	@Override
	public void onError ( PublisherKit publisherKit , OpentokError opentokError ) {

	}
}
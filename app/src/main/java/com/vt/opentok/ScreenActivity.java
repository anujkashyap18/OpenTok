package com.vt.opentok;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ScreenActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, Session.SessionListener, Publisher.PublisherListener {

	private static final String TAG = "screen-sharing " + ScreenActivity.class.getSimpleName ( );

	private static final int RC_SETTINGS_SCREEN_PERM = 123;
	private static final int RC_VIDEO_APP_PERM = 124;

	private Session mSession;
	private Publisher mPublisher;
	private RelativeLayout mPublisherViewContainer;
	private WebView mWebViewContainer;
	String API_KEY = "";
	String SESSION_ID = "";
	String TOKEN = "";


	@Override
	protected void onCreate ( Bundle savedInstanceState ) {
		super.onCreate ( savedInstanceState );
		setContentView ( R.layout.activity_screen );

		mPublisherViewContainer = findViewById ( R.id.publisherview );
		mWebViewContainer = findViewById ( R.id.webview );

		requestPermissions ( );
	}

	@Override
	protected void onPause ( ) {
		Log.d ( TAG , "onPause" );

		super.onPause ( );

		if ( mSession == null ) {
			return;
		}
		mSession.onPause ( );

		if ( isFinishing ( ) ) {
			disconnectSession ( );
		}
	}

	@Override
	protected void onResume ( ) {
		Log.d ( TAG , "onResume" );

		super.onResume ( );

		if ( mSession == null ) {
			return;
		}
		mSession.onResume ( );
	}

	@Override
	protected void onDestroy ( ) {
		Log.d ( TAG , "onDestroy" );

		disconnectSession ( );

		super.onDestroy ( );
	}

	@Override
	public void onRequestPermissionsResult ( int requestCode , @NonNull String[] permissions , @NonNull int[] grantResults ) {
		super.onRequestPermissionsResult ( requestCode , permissions , grantResults );
		EasyPermissions.onRequestPermissionsResult ( requestCode , permissions , grantResults , this );
	}

	@Override
	public void onPermissionsGranted ( int requestCode , List < String > perms ) {
		Log.d ( TAG , "onPermissionsGranted:" + requestCode + ":" + perms.size ( ) );
	}

	@Override
	public void onPermissionsDenied ( int requestCode , List < String > perms ) {
		Log.d ( TAG , "onPermissionsDenied:" + requestCode + ":" + perms.size ( ) );

		if ( EasyPermissions.somePermissionPermanentlyDenied ( this , perms ) ) {
			new AppSettingsDialog.Builder ( this ).setTitle ( getString ( R.string.title_settings_dialog ) ).setRationale ( getString ( R.string.rationale_ask_again ) ).setPositiveButton ( getString ( R.string.setting ) ).setNegativeButton ( getString ( R.string.cancel ) ).setRequestCode ( RC_SETTINGS_SCREEN_PERM ).build ( ).show ( );
		}
	}

	@AfterPermissionGranted ( RC_VIDEO_APP_PERM )
	private void requestPermissions ( ) {
		String[] perms = { Manifest.permission.INTERNET , Manifest.permission.CAMERA , Manifest.permission.RECORD_AUDIO };
		if ( EasyPermissions.hasPermissions ( this , perms ) ) {
			mSession = new Session.Builder ( ScreenActivity.this , API_KEY , SESSION_ID ).build ( );
			mSession.setSessionListener ( this );
			mSession.connect ( TOKEN );
		}
		else {
			EasyPermissions.requestPermissions ( this , getString ( R.string.rationale_video_app ) , RC_VIDEO_APP_PERM , perms );
		}
	}

	@SuppressLint ( "SetJavaScriptEnabled" )
	@Override
	public void onConnected ( Session session ) {
		Log.d ( TAG , "onConnected: Connected to session " + session.getSessionId ( ) );

		ScreenSharingCapture screenCapturer = new ScreenSharingCapture ( ScreenActivity.this , mWebViewContainer );

		mPublisher = new Publisher.Builder ( ScreenActivity.this ).name ( "publisher" ).capturer ( screenCapturer ).build ( );
		mPublisher.setPublisherListener ( this );
		mPublisher.setPublisherVideoType ( PublisherKit.PublisherKitVideoType.PublisherKitVideoTypeCamera );
		mPublisher.setAudioFallbackEnabled ( false );

		mWebViewContainer.setWebViewClient ( new WebViewClient ( ) );
		WebSettings webSettings = mWebViewContainer.getSettings ( );
		webSettings.setJavaScriptEnabled ( true );
		mWebViewContainer.setLayerType ( View.LAYER_TYPE_SOFTWARE , null );
		mWebViewContainer.loadUrl ( "https://www.google.com" );

		mPublisher.setStyle ( BaseVideoRenderer.STYLE_VIDEO_SCALE , BaseVideoRenderer.STYLE_VIDEO_FILL );
		mPublisherViewContainer.addView ( mPublisher.getView ( ) );

		mSession.publish ( mPublisher );
	}

	@Override
	public void onDisconnected ( Session session ) {
		Log.d ( TAG , "onDisconnected: disconnected from session " + session.getSessionId ( ) );

		mSession = null;
	}

	@Override
	public void onError ( Session session , OpentokError opentokError ) {
		Log.d ( TAG , "onError: Error (" + opentokError.getMessage ( ) + ") in session " + session.getSessionId ( ) );

		Toast.makeText ( this , "Session error. See the logcat please." , Toast.LENGTH_LONG ).show ( );
		finish ( );
	}

	@Override
	public void onStreamReceived ( Session session , Stream stream ) {
		Log.d ( TAG , "onStreamReceived: New stream " + stream.getStreamId ( ) + " in session " + session.getSessionId ( ) );
	}

	@Override
	public void onStreamDropped ( Session session , Stream stream ) {
		Log.d ( TAG , "onStreamDropped: Stream " + stream.getStreamId ( ) + " dropped from session " + session.getSessionId ( ) );
	}

	@Override
	public void onStreamCreated ( PublisherKit publisherKit , Stream stream ) {
		Log.d ( TAG , "onStreamCreated: Own stream " + stream.getStreamId ( ) + " created" );
	}

	@Override
	public void onStreamDestroyed ( PublisherKit publisherKit , Stream stream ) {
		Log.d ( TAG , "onStreamDestroyed: Own stream " + stream.getStreamId ( ) + " destroyed" );
	}

	@Override
	public void onError ( PublisherKit publisherKit , OpentokError opentokError ) {
		Log.d ( TAG , "onError: Error (" + opentokError.getMessage ( ) + ") in publisher" );

		Toast.makeText ( this , "Session error. See the logcat please." , Toast.LENGTH_LONG ).show ( );
		finish ( );
	}

	private void disconnectSession ( ) {
		if ( mSession == null ) {
			return;
		}

		if ( mPublisher != null ) {
			mPublisherViewContainer.removeView ( mPublisher.getView ( ) );
			mSession.unpublish ( mPublisher );
			mPublisher.destroy ( );
			mPublisher = null;
		}
		mSession.disconnect ( );
	}
}

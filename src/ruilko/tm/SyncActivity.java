package ruilko.tm;

import java.util.UUID;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SyncActivity extends Activity implements OnClickListener {
	private static final String TAG = "ServiceDialerMain";
	Button buttonStart, buttonStop;
	TextView viewLog;
	SyncThread syncing;
	EditText urlView;
	
	String localUuid;

	private Handler uiCallback = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String text = msg.getData().getString("text");
			viewLog.setText( text );
			Log.d(TAG, "got message: " + text);

			if( msg.what==SyncThread.Events.FINISHED.ordinal() )
			{
				Log.e(TAG, "Thread is stopped");
				buttonStart.setEnabled(true);
				buttonStop.setEnabled(false);
				try {
					syncing.interrupt();
					syncing.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Restore preferences
		SharedPreferences settings = getSharedPreferences("tm", 0);
		localUuid = settings.getString("localUuid", UUID.randomUUID().toString());
		
		
		// Widgets
		setContentView(R.layout.main);

		buttonStart = (Button) findViewById(R.id.buttonStart);
		buttonStop = (Button) findViewById(R.id.buttonStop);
		viewLog = (TextView) findViewById(R.id.viewLog);
		urlView = (EditText) findViewById(R.id.urlView);

		buttonStart.setOnClickListener(this);
		buttonStop.setOnClickListener(this);

		buttonStart.setEnabled(true);
		buttonStop.setEnabled(false);
	}

	@Override
	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.buttonStart:
			Log.d(TAG, "onClick: starting sync");
			buttonStart.setEnabled(false);
			buttonStop.setEnabled(true);
			
			viewLog.setText( "Starting sync thread..." );

			syncing = new SyncThread(uiCallback, urlView.getText().toString(), localUuid);
			syncing.start();
			// startService(new Intent(this, ServiceImpl.class));
			break;
		case R.id.buttonStop:
			Log.d(TAG, "onClick: stopping sync");
			buttonStart.setEnabled(true);
			buttonStop.setEnabled(false);

			viewLog.setText( "Stopping sync thread..." );
			try {
				syncing.interrupt();
				syncing.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
			// stopService(new Intent(this, ServiceImpl.class));
			break;
		}
	}
}
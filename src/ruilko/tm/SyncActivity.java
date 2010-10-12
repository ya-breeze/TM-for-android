package ruilko.tm;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SyncActivity extends Activity implements OnClickListener {
	private static final String TAG = "ServiceDialerMain";
	Button buttonStart, buttonStop;
	TextView viewLog;
	SyncThread syncing;

	private Handler uiCallback = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			viewLog.setText( msg.getData().getString("text") );

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
		setContentView(R.layout.main);

		buttonStart = (Button) findViewById(R.id.buttonStart);
		buttonStop = (Button) findViewById(R.id.buttonStop);
		viewLog = (TextView) findViewById(R.id.viewLog);

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

			syncing = new SyncThread(uiCallback);
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
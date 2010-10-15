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
	EditText hostView;
	EditText portView;
	private DbAccessor dbAccessor;

	String localUuid;

	private Handler uiCallback = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String text = msg.getData().getString("text");
			viewLog.setText(text);
			Log.d(TAG, "got message: " + text);

			if (msg.what == SyncThread.Events.FINISHED.ordinal()) {
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if( syncing!=null ) {
			try {
				syncing.interrupt();
				syncing.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		dbAccessor.close();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Restore preferences
		SharedPreferences settings = getSharedPreferences("tm", 0);
		localUuid = settings.getString("localUuid", UUID.randomUUID()
				.toString());

		// Widgets
		setContentView(R.layout.main);

		buttonStart = (Button) findViewById(R.id.buttonStart);
		buttonStop = (Button) findViewById(R.id.buttonStop);
		viewLog = (TextView) findViewById(R.id.viewLog);
		hostView = (EditText) findViewById(R.id.hostView);
		portView = (EditText) findViewById(R.id.portView);

		buttonStart.setOnClickListener(this);
		buttonStop.setOnClickListener(this);

		buttonStart.setEnabled(true);
		buttonStop.setEnabled(false);
		
		// Create Db object
		dbAccessor = new DbAccessor(this);
	}

	@Override
	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.buttonStart:
			Log.d(TAG, "onClick: starting sync");
			buttonStart.setEnabled(false);
			buttonStop.setEnabled(true);

			viewLog.setText("Starting sync thread...");
			int port = 0;
			try {
				port = Integer.parseInt(portView.getText().toString());
			}
			catch(NumberFormatException e) {
				Log.e(TAG, "Wrong port - " + portView.getText().toString());
			}
			syncing = new SyncThread(dbAccessor, uiCallback, hostView.getText().toString(),
					port, localUuid);
			syncing.start();
			break;
		case R.id.buttonStop:
			Log.d(TAG, "onClick: stopping sync");
			buttonStart.setEnabled(true);
			buttonStop.setEnabled(false);

			viewLog.setText("Stopping sync thread...");
			try {
				syncing.interrupt();
				syncing.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		}
	}
}
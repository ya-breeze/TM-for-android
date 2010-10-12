package ruilko.tm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SyncThread extends Thread {
	private static final String TAG = "SyncThread";
    private static final int HTTP_STATUS_OK = 200;

    private byte[] sBuffer = new byte[512];    
    private Handler handler;

	public SyncThread(Handler _handler) {
		handler = _handler;
	}
	
	public enum Events {
		TEXT, FINISHED
	}
	
	void inform(String _text, Events _event) {
		Message msg = handler.obtainMessage(_event.ordinal());
		Bundle bndl = new Bundle();
		bndl.putString("text", _text);
		msg.setData( bndl );
		handler.sendMessage( msg );
	}

	@Override
	public void run() {
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet("http://192.168.7.45");
//		request.setHeader("User-Agent", sUserAgent);

		try {
			HttpResponse response = client.execute(request);
			inform(response.toString(), Events.TEXT);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			inform(status.toString(), Events.TEXT);
			if (status.getStatusCode() != HTTP_STATUS_OK) {
//				throw new ApiException("Invalid response from server: "
//						+ status.toString());
			}

			// Pull content stream from response
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();

			ByteArrayOutputStream content = new ByteArrayOutputStream();

			// Read response into a buffered stream
			int readBytes = 0;
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}

			// Return result from buffered stream
			inform( new String(content.toByteArray()), Events.FINISHED );
//			 new String(content.toByteArray());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			inform( "Error on syncing" + e.toString(), Events.FINISHED );
			e.printStackTrace();
//			throw new ApiException("Problem communicating with API", e);
		}
	}
}

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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

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
		int TIMEOUT_MILLISEC = 10000; //=10sec
		 HttpParams httpParams = new BasicHttpParams();;
		 HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
		 HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
		 
		HttpClient client = new DefaultHttpClient(httpParams);
		String url = "http://192.168.7.45/json";
		Log.d(TAG, "Getting from server - " + url);
		HttpGet request = new HttpGet(url);
//		request.setHeader("User-Agent", sUserAgent);

		try {
			HttpResponse response = client.execute(request);
			inform(response.toString(), Events.TEXT);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			inform(status.toString(), Events.TEXT);
			if (status.getStatusCode() != HTTP_STATUS_OK) {
				inform( "Invalid response from server: " + status.toString(), Events.FINISHED );
				return;
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
			inform( new String(content.toByteArray()), Events.TEXT );
//			 new String(content.toByteArray());
            // Drill into the JSON response to find the content body
            JSONObject resp = (JSONObject) new JSONTokener(content.toString()).nextValue();
//            JSONObject query = resp.getJSONObject("uuid");
//            JSONObject pages = query.getJSONObject("pages");
//            JSONObject page = pages.getJSONObject((String) pages.keys().next());
//            JSONArray revisions = page.getJSONArray("revisions");
//            JSONObject revision = revisions.getJSONObject(0);
			String uuid = resp.getString("uuid");
			inform( uuid, Events.FINISHED );
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			inform( "Error on syncing" + e.toString(), Events.FINISHED );
			e.printStackTrace();
		} catch (JSONException e) {
			Log.e(TAG, e.toString());
			inform( "Error on syncing" + e.toString(), Events.FINISHED );
			e.printStackTrace();
		}
	}
	
//	String - uuid, int - current time getServerInfo(String _url) {
//	}
}

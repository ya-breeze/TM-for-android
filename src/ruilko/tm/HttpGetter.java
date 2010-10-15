package ruilko.tm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

public class HttpGetter {
	private static final String TAG = "HttpGetter";
	private int TIMEOUT_MILLISEC = 10000; // = 10sec
	private static final int HTTP_STATUS_OK = 200;
	private byte[] sBuffer = new byte[512];
	private MessageCallback messageCallback;

	public class HttpGetterException extends Exception {
		private static final long serialVersionUID = -7507930376160696109L;

		public HttpGetterException(String msg) {
			super(msg);
		}
	}
	
	public HttpGetter(MessageCallback cb) {
		messageCallback = cb;
	}

	public String getResponse(URI url, HashMap<String, String> headers,
			String body) throws HttpGetterException {
		Log.d(TAG, "Getting from server - " + url);

		// prepare request
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
		HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
		HttpClient client = new DefaultHttpClient(httpParams);
		HttpGet request = new HttpGet(url);
        for ( Map.Entry<String, String> entry : headers.entrySet() )
        {
	        String key = entry.getKey();
	        String value = entry.getValue();
			request.setHeader(key, value);
        }
        // TODO Adding body

		try {
			messageCallback.inform( "Connecting to " + request.getURI().getHost() );
			HttpResponse response = client.execute(request);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK)
				throw new HttpGetterException("Invalid response from server: " + status.toString());

			// Pull content stream from response
			messageCallback.inform( "Processing response..." );
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();

			ByteArrayOutputStream content = new ByteArrayOutputStream();

			// Read response into a buffered stream
			int readBytes = 0;
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}

			String result = new String(content.toByteArray());

			return result;
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
			throw new HttpGetterException("IO Error: " + e.toString());
		}
	}
}

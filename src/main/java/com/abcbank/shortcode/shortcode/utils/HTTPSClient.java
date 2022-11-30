package com.abcbank.shortcode.shortcode.utils;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class HTTPSClient {

	static OkHttpClient client = getUnsafeOkHttpClient();
	
	public static String sendHttpsRequest(String urlString, String data, String requestMethod, HashMap<String,String> otherHeaders, String contentType) {
		log.debug("urlString " + urlString);
		boolean post = true;
		if(contentType.equalsIgnoreCase("text")) {
			post = false;
			contentType = "text/plain";
		} else if(contentType.equalsIgnoreCase("json")) {
			contentType = "application/json";
		} else if(contentType.equalsIgnoreCase("xml")) {
			contentType = "application/xml";
		} else if(contentType.equalsIgnoreCase("soap")) {
			contentType = "application/soap+xml; charset=UTF-8";
		} else if(contentType.equalsIgnoreCase("soap2")) {
			contentType = "text/xml; charset=UTF-8";
		} else {
			contentType = "application/" + contentType.toLowerCase();
		}

		RequestBody body = RequestBody.create(MediaType.parse(contentType), data);

		Request.Builder requestBuilder = new Request.Builder().url(urlString);
		requestBuilder.addHeader("Content-Type", contentType);
		
		for (Map.Entry<String,String> entry : otherHeaders.entrySet()) {
			String key = entry.getKey();
            String value = entry.getValue();
            if(key != null && value != null)
                requestBuilder.addHeader(key, value);
		}
		
		if(post == true)
			requestBuilder.post(body);

		Call call = client.newCall(requestBuilder.build());
		Response response;
		String responseString = null;
		try {
			response = call.execute();
			responseString = response.body().string();
			response.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return responseString;
	}
	
	public static String sendHttpsRequest(String urlString, String data, String requestMethod, String authorization, String contentType) {
		boolean post = true;
		if(contentType.equalsIgnoreCase("text")) {
			post = false;
			contentType = "text/plain";
		} else if(contentType.equalsIgnoreCase("json")) {
			contentType = "application/json";
		} else if(contentType.equalsIgnoreCase("xml")) {
			contentType = "application/xml";
		} else if(contentType.equalsIgnoreCase("soap")) {
			contentType = "application/soap+xml; charset=UTF-8";
		} else if(contentType.equalsIgnoreCase("soap2")) {
			contentType = "text/xml; charset=UTF-8";
		} else {
			contentType = "application/" + contentType.toLowerCase();
		}

		RequestBody body = RequestBody.create(MediaType.parse(contentType), data);

		Request.Builder requestBuilder = new Request.Builder().url(urlString);
		requestBuilder.addHeader("Content-Type", contentType);
		if(authorization != null)
			requestBuilder.addHeader("Authorization", authorization);
		if(post == true)
			requestBuilder.post(body);

		Call call = client.newCall(requestBuilder.build());
		Response response;
		String responseString = null;
		try {
			response = call.execute();
			responseString = response.body().string();
			response.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return responseString;
	}

	private static OkHttpClient getUnsafeOkHttpClient() {
		try {
			// Create a trust manager that does not validate certificate chains
			final TrustManager[] trustAllCerts = new TrustManager[]{
					new X509TrustManager() {
						@Override
						public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
								String authType) throws CertificateException {
						}

						@Override
						public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
								String authType) throws CertificateException {
						}

						@Override
						public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[0];
						}
					}
			};

			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			// Create an ssl socket factory with our all-trusting manager
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			return new OkHttpClient.Builder()
					.connectTimeout(60, TimeUnit.SECONDS)
					.writeTimeout(60, TimeUnit.SECONDS)
				    .readTimeout(60, TimeUnit.SECONDS)
					.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
					.hostnameVerifier(new HostnameVerifier() {
						@Override
						public boolean verify(String hostname, SSLSession session) {
							return true;
						}
					}).build();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
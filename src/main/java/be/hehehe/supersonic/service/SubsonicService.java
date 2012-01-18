package be.hehehe.supersonic.service;

import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.subsonic.restapi.Response;

import be.hehehe.supersonic.utils.URLBuilder;

@Named
public class SubsonicService {

	@Inject
	PreferencesService preferencesService;

	@SuppressWarnings("unchecked")
	public Response invoke(String method, Param... params) throws Exception {
		String responseString = IOUtils.toString(invokeBinary(method, params));
		System.out.println(responseString);

		JAXBContext context = JAXBContext.newInstance(Response.class
				.getPackage().getName());
		Unmarshaller unmarshaller = context.createUnmarshaller();
		JAXBElement<Response> jaxbResponse = (JAXBElement<Response>) unmarshaller
				.unmarshal(new StringReader(responseString));

		return jaxbResponse.getValue();
	}

	public InputStream invokeBinary(String method, Param... params)
			throws Exception {

		URLBuilder builder = new URLBuilder(
				preferencesService.getSubsonicHostname() + "/rest/" + method
						+ ".view");
		builder.addParam("u", preferencesService.getSubsonicLogin());
		builder.addParam("p", preferencesService.getSubsonicPassword());
		builder.addParam("v", "1.7.0");
		builder.addParam("c", "supersonic");
		for (Param param : params) {
			builder.addParam(param.getName(), param.getValue());
		}

		URL url = new URL(builder.build());
		System.out.println(url.toString());

		URLConnection connection = null;
		if (preferencesService.isProxyEnabled()) {
			Proxy proxy = new Proxy(
					preferencesService.getProxyType(),
					new InetSocketAddress(
							preferencesService.getProxyHostname(),
							Integer.parseInt(preferencesService.getProxyPort())));
			connection = url.openConnection(proxy);
			if (preferencesService.isProxyAuthRequired()) {
				String password = preferencesService.getProxyLogin() + ":"
						+ preferencesService.getProxyPassword();
				String encodedPassword = Base64.encodeBase64String(password
						.getBytes());
				connection.setRequestProperty("Proxy-Authorization",
						encodedPassword);
			}
		} else {
			connection = url.openConnection();
		}
		connection.setConnectTimeout(30000);
		connection.setReadTimeout(30000);

		return url.openStream();

	}

	public static class Param {
		private String name;
		private String value;

		public Param(String value) {
			this("id", value);
		}

		public Param(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}
}

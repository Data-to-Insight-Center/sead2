package edu.umich.icpsr.sead.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import edu.umich.icpsr.util.IOUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResearchDataSet {

	protected static class DataResponseExtractor implements ResponseExtractor<File> {
		private File dir;
		private String fileName;

		public DataResponseExtractor(File dir, String fileName) {
			this.dir = dir;
			this.fileName = fileName;
		}

		public File extractData(ClientHttpResponse response) throws IOException {
			File file = new File(dir, fileName);
			try {
				InputStream is = response.getBody();
				IOUtils.streamOut(is, new FileOutputStream(file));
			} catch (Exception e) {
				LOG.error("", e);
				throw new RuntimeException("", e);
			}
			return file;
		}

		public File getDir() {
			return dir;
		}

		public String getFileName() {
			return fileName;
		}

		public void setDir(File dir) {
			this.dir = dir;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

	}

	private static final Logger LOG = Logger.getLogger(ResearchDataSet.class);

	@JsonProperty("@type")
	private List<String> type;
	@JsonProperty("http://purl.org/dc/elements/1.1/creator")
	private String creator;

	@JsonProperty("http://purl.org/dc/elements/1.1/identifier")
	private String identifier;

	@JsonProperty("http://purl.org/dc/elements/1.1/title")
	private String title;

	@JsonProperty("http://purl.org/dc/terms/created")
	private String created;

	@JsonProperty("http://purl.org/dc/terms/hasPart")
	@JsonDeserialize(using = StringOrListValueDes.class)
	private List<String> hasPart;

	@JsonProperty("http://www.w3.org/2000/01/rdf-schema#label")
	private String label;

	@JsonProperty("http://purl.org/dc/elements/1.1/date")
	private String date;

	@JsonProperty("http://purl.org/dc/elements/1.1/format")
	private String format;

	@JsonProperty("http://purl.org/dc/terms/abstract")
	private String abstractText;
	@JsonProperty("tag:tupeloproject.org,2006:/2.0/files/length")
	private String length;

	@JsonProperty("http://www.openarchives.org/ore/terms/similarTo")
	private FileObject fileObject;

	@JsonProperty("tag:tupeloproject.org,2006:/2.0/gis/hasGeoPoint")
	private GeoPoint geoPoint;

	public ResearchDataSet() {
		super();
	}

	public List<String> getType() {
		return type;
	}

	public void setType(List<String> type) {
		this.type = type;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public List<String> getHasPart() {
		return hasPart;
	}

	public void setHasPart(List<String> hasPart) {
		this.hasPart = hasPart;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public FileObject getFileObject() {
		return fileObject;
	}

	public void setFileObject(FileObject fileObject) {
		this.fileObject = fileObject;
	}

	public GeoPoint getGeoPoint() {
		return geoPoint;
	}

	public void setGeoPoint(GeoPoint geoPoint) {
		this.geoPoint = geoPoint;
	}

	public File downloadContents(final File dir, final HttpClientContext clientContext) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory() {
			@Override
			protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
				// share the client store and cookie
				return clientContext;
			}
		});
		restTemplate.execute(fileObject.getId(), HttpMethod.GET, new RequestCallback() {
			public void doWithRequest(ClientHttpRequest request) throws IOException {
			}
		}, new DataResponseExtractor(dir, getLabel()));
		return new File(dir, getLabel());
	}
}
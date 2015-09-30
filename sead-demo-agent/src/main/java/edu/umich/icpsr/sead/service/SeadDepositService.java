package edu.umich.icpsr.sead.service;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.umich.icpsr.sead.Config;
import edu.umich.icpsr.sead.dao.SeadDAO;
import edu.umich.icpsr.sead.model.ResearchDataSet;
import edu.umich.icpsr.sead.model.ResearchObject;
import edu.umich.icpsr.sead.model.ResearchObjectOreMap;
import edu.umich.icpsr.sead.model.ResearchObjectRequest;
import edu.umich.icpsr.sead.model.ResearchObjectRequest.Status;

/**
 * Service performs following actions to support integration between SEAD C&P
 * services and OpenICPSR
 * <ul>
 * <li>Call C&P services to find matching deposit requests pending to be handled
 * by open ICPSR</li>
 * <li>Update the status back to C&P so that requests are de-queued</li>
 * <li>Validate the user profile information</li>
 * <li>Create new user profile in openICPSR if required</li>
 * <li>Create project folder structure in open ICPSR</li>
 * <li>Move files to virus scanning folder</li>
 * </ul>
 * 
 * @author harshau
 *
 */
@Component
public class SeadDepositService {
	private static final String SEAD_DATASET = "http://cet.ncsa.uiuc.edu/2007/Dataset";

	private static final String SEAD_COLLECTION = "http://cet.ncsa.uiuc.edu/2007/Collection";

	private static final Logger LOG = Logger.getLogger(SeadDepositService.class);

	@Autowired
	Config config;

	@Value("${shared.sead.upload.dir}")
	String uploadDir;

	@Autowired
	SeadDAO seadDAO;

	public List<ResearchObjectRequest> processPendingDeposits(String researchObjectUrl, PrintWriter writer, boolean sendStatus, boolean sendDOI) {
		HttpClientContext localContext = HttpClientContext.create();
		if (!seadDAO.authenticate(localContext)) {
			throw new SecurityException("Device is not authenticated...");
		}
		writer.println("Device is authenticated successfully.");
		writer.flush();

		writer.println("Invoking " + researchObjectUrl);
		writer.flush();
		List<ResearchObjectRequest> researchObjects = seadDAO.getResearchObjectsRequests(researchObjectUrl);

		writer.println("Found " + researchObjects.size() + " records.");
		writer.flush();

		for (ResearchObjectRequest roReq : researchObjects) {
			String roRequestUrl = "https://sead-test.ncsa.illinois.edu/sead-cp/cp/researchobjects/";
			Object reqId = roReq.getAggregation().get("Identifier");
			String researchObjectUrlPath = roRequestUrl + reqId;
			try {
				writer.println("Invoking " + researchObjectUrlPath);
				writer.flush();
				ResearchObjectRequest roObjectDetail = seadDAO.getResearchObjectsRequest(researchObjectUrlPath);
				LOG.info("Processing deposit request " + roReq);
				writer.println("Processing deposit request  " + roObjectDetail);
				writer.flush();
				// FIXME Remove hard coding
				String roIdUrl = roObjectDetail.getAggregation().get("@id").toString();
				writer.println("Invoking " + roIdUrl);
				writer.flush();

				ResearchObjectOreMap roOreMap = seadDAO.getResearchObjectOreMap(roObjectDetail);
				ResearchObject ro = roOreMap.getDescribes();
				List<ResearchDataSet> dataSet = ro.getAggregates();
				HashMap<String, ResearchDataSet> dirs = new HashMap<String, ResearchDataSet>();
				HashMap<String, ResearchDataSet> files = new HashMap<String, ResearchDataSet>();
				File stagingDir = new File("/temp/sead/downloads");
				stagingDir.mkdirs();

				for (ResearchDataSet ds : dataSet) {
					if (ds.getHasPart() != null && !ds.getHasPart().isEmpty() && ds.getType().contains(SEAD_COLLECTION)) {
						dirs.put(ds.getIdentifier(), ds);
						writer.println("Found collection " + ds.getIdentifier());
						writer.flush();
					} else if (ds.getType().contains(SEAD_DATASET)) {
						files.put(ds.getIdentifier(), ds);
						writer.println("Found binary file " + ds.getIdentifier());
						writer.flush();
					}
				}

				HashSet<String> processed = new HashSet<String>();

				for (ResearchDataSet ds : dataSet) {
					processDataset(localContext, stagingDir, dirs, files, processed, ds, writer);
				}
				if (sendStatus) {
					writer.println("Sending status Contents downloaded successfully... for " + reqId);
					writer.flush();
					sendDownloadStatus(researchObjectUrlPath, "Pending", "Contents downloaded successfully...");
				}
				if (sendDOI) {
					writer.println("Sending status with DOI for " + reqId);
					writer.flush();
					sendDownloadStatus(researchObjectUrlPath, "Success", "DOI is http://doi.dummy.org/" + reqId);
				}
			} catch (Exception e) {
				if (sendStatus) {
					writer.println("Sending status failure for " + reqId);
					writer.flush();
					sendDownloadStatus(researchObjectUrlPath, "Failure", "Error while handling request...");
				}
				writer.println("ERROR :: " + e.toString());
				writer.flush();
				LOG.error("", e);
			} finally {

			}
		}
		return researchObjects;
	}

	private void sendDownloadStatus(String researchObjectUrlPath, String stage, String message) {
		Status status = new Status();
		status.setDate(new SimpleDateFormat("MMM dd, yyyy h:mm:ss a").format(new Date()));
		status.setReporter("demo-agent");
		status.setStage(stage);
		status.setMessage(message);
		seadDAO.postStatus(researchObjectUrlPath + "/status", status);
	}

	private void processDataset(HttpClientContext localContext, File stagingDir, HashMap<String, ResearchDataSet> dirs, HashMap<String, ResearchDataSet> files, HashSet<String> processed,
			ResearchDataSet ds, PrintWriter writer) {
		Timestamp nowTS = new Timestamp(System.currentTimeMillis());
		if (ds.getHasPart() != null && !ds.getHasPart().isEmpty() && ds.getType().contains(SEAD_COLLECTION)) {
			if (processed.add(ds.getIdentifier())) {
				List<String> parts = ds.getHasPart();
				for (String part : parts) {
					if (dirs.containsKey(part)) {
						ResearchDataSet childDs = dirs.get(part);
						processDataset(localContext, stagingDir, dirs, files, processed, childDs, writer);
					} else if (files.containsKey(part)) {
						ResearchDataSet fileData = files.get(part);
						try {
							fileData.downloadContents(stagingDir, localContext);
							writer.println("Downloaded contents from " + fileData.getFileObject().getId());
							writer.flush();
						} catch (Exception e) {
							writer.println("ERROR :: " + e.toString());
							writer.flush();
							LOG.error("", e);
							continue;
						}
					}
				}
			}
		} else if (ds.getType().contains(SEAD_DATASET)) {
			ds.downloadContents(stagingDir, localContext);
			writer.println("Downloaded contents from " + ds.getFileObject().getId());
			writer.flush();
		}
	}

}

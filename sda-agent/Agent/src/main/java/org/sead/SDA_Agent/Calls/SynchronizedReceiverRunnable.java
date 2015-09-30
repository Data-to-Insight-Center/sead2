package Calls;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import APIcalls.NewOREmap;
import APIcalls.Shimcalls;
import Driver.DummySDA;
import Driver.FileManager;
import Driver.ZipDirectory;
import Engine.DOI;
import Engine.PropertiesReader;
import Engine.SFTP;

public class SynchronizedReceiverRunnable implements Runnable{
	private String propertyPath; 
	private ArrayList<String> errorLinks;

	public SynchronizedReceiverRunnable(String propertyPath){
		this.propertyPath = propertyPath;
	}
	
	
	
	public void run() throws IllegalMonitorStateException{
		boolean runInfinite = true;
		
		PropertiesReader property = new PropertiesReader(this.propertyPath);
		
		//while(runInfinite){
			
			
			Shimcalls call = new Shimcalls(property);
			JSONArray allResearchObjects = call.getResearchObjectsList_repo();
			
			
			for (Object item : allResearchObjects.toArray()){
			//JSONObject item_new = (JSONObject) allResearchObjects.get(0);
				
				JSONObject item_new = (JSONObject) item;
				call.getObjectID(item_new, "Identifier");		
				String identifiter = call.getID();
				System.out.println("Start a new request: "+ identifiter);
				String doi_url = null;
				
				if (identifiter == null){
					System.err.println("[SDA Agent : Cannot get Identifier of RO]");
				}else{
					this.errorLinks = new ArrayList<String>();
					String rootPath = null;
					try{
						JSONObject pulishObject = call.getResearchObject_cp(identifiter);
						
						call.getObjectID(pulishObject,"@id");
						
						String ore_url = call.getID();
						
						if (ore_url == null){
							System.err.println("[SDA Agent : Cannot get ORE map file of RO");
						}else{
							
							try{
								JSONObject ore = call.getResearchObjectORE(ore_url);
								
								NewOREmap oreMap = new NewOREmap(ore);
								JSONObject newOREmap = oreMap.getNewOREmap();
								
								try{
									DummySDA dummySDA = new DummySDA(property, newOREmap, ore);
									errorLinks = dummySDA.getErrorLinks();
									rootPath = dummySDA.getRootPath();
									
									try{
										ZipDirectory zipDir = new ZipDirectory(rootPath);
										
										try{
											
											SFTP sftp = new SFTP(this.propertyPath, rootPath+".tar");
											try{
												String target = property.getProperties().get("LandingPage") + "?tag="+identifiter;
												System.out.println(target);
												DOI doi = new DOI(target, ore);
												doi_url = doi.getDoi();
												
											}catch(Exception e){
												e.printStackTrace();
												System.err.println("[SDA Agent : Cannot generate DOI]");
											}
											//call.updateStatus(doi_url, identifiter);
											System.out.println(doi_url);
											FileManager manager = new FileManager();
											manager.removeTempFile(rootPath+".tar");
											manager.removeTempFolder(rootPath);	
										}catch(Exception e){
											e.printStackTrace();
											System.err.println("[SDA Agent: Cannot deposit RO zip file into SDA]");
										}
									}catch(Exception e){
										e.printStackTrace();
										System.err.println("[SDA Agent : Cannot compress downloaded RO folder]");
									}
								}catch(Exception e){
									e.printStackTrace();
									System.err.println("[SDA Agent : Cannot download RO into local server]");
								}
							}catch(Exception e){
								e.printStackTrace();
								System.err.println("[SDA Agent : Cannot access ORE link]");
							}
						}
					}catch(Exception e){
						e.printStackTrace();
						System.err.println("[SDA Agent: Cannot access RO link]");
					}
				}
			}
					
			
			try {
				Thread.sleep(5000);
				System.out.println("Finish Sleeping");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	//}
}

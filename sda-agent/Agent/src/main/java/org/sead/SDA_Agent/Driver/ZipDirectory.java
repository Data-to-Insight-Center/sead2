package Driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipDirectory {
	
	private String rootPath;
	private List<File> fileList;
	
	public ZipDirectory(String rootPath){
		this.rootPath = rootPath;
		this.fileList = new ArrayList<File>();
		File root = new File(this.rootPath);
		
		getAllFiles(root,this.fileList);
		writeZipFile(root, this.fileList);
		
	}
	
	
	
	public void getAllFiles(File dir, List<File> fileList){
		try{
			File[] files = dir.listFiles();
			for (File file : files){
				fileList.add(file);
				if (file.isDirectory()){
					getAllFiles(file, fileList);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	
	
	public void writeZipFile(File dirToZip, List<File> fileList){
		try{
			FileOutputStream fos = new FileOutputStream(dirToZip.getCanonicalPath()+".tar");
			ZipOutputStream zos = new ZipOutputStream(fos);
			
			for (File file : fileList){
				if (!file.isDirectory()){
					addToZip(dirToZip, file, zos);
				}
			}
			zos.close();
			fos.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	public void addToZip(File dirToZip, File file, ZipOutputStream zos){
	
		try {
			FileInputStream fis = new FileInputStream(file);
			try {
				String zipFilePath = file.getCanonicalPath().substring(dirToZip.getCanonicalPath().length()+1, 
						file.getCanonicalPath().length());

				ZipEntry zipEntry = new ZipEntry(zipFilePath);
				zos.putNextEntry(zipEntry);
				
				byte[] bytes = new byte[1024];
				int length;
				while ((length = fis.read(bytes)) >= 0){
					zos.write(bytes, 0, length);
				}
				
				zos.closeEntry();
				fis.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args){
		//ZipDirectory a = new ZipDirectory("/Users/yuluo/Downloads/1234");
		
	}

}

package org.sead.sda.agent.driver;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipDirectory {

    public ZipDirectory(String rootPath) throws Exception {
        List<File> fileList = new ArrayList<File>();
        File root = new File(rootPath);

        getAllFiles(root, fileList);
//        writeZipFile(root, fileList);

        // create tar file
        FileOutputStream dest = new FileOutputStream(rootPath + ".tar");
        TarOutputStream out = new TarOutputStream(new BufferedOutputStream(dest));
        writeTarFile(null, rootPath, out);
        out.close();
    }


    public void getAllFiles(File dir, List<File> fileList) throws Exception {
        File[] files = dir.listFiles();
        for (File file : files) {
            fileList.add(file);
            if (file.isDirectory()) {
                getAllFiles(file, fileList);
            }
        }
    }

    public void writeZipFile(File dirToZip, List<File> fileList) throws Exception {
        FileOutputStream fos = new FileOutputStream(dirToZip.getCanonicalPath() + ".zip");
        ZipOutputStream zos = new ZipOutputStream(fos);

        for (File file : fileList) {
            if (!file.isDirectory()) {
                addToZip(dirToZip, file, zos);
            }
        }
        zos.close();
        fos.close();
    }

    public void addToZip(File dirToZip, File file, ZipOutputStream zos) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        String zipFilePath = file.getCanonicalPath().substring(dirToZip.getCanonicalPath().length() + 1,
                file.getCanonicalPath().length());

        ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }
        zos.closeEntry();
        fis.close();
    }

    public void writeTarFile(String parent, String path, TarOutputStream out) throws IOException {
        BufferedInputStream origin;
        File f = new File( path );
        String files[] = f.list();
        parent = ( ( parent == null ) ? ( f.isFile() ) ? "" : f.getName() + "/" : parent + f.getName() + "/" );

        for (String file : files) {
            File fe = f;
            byte data[] = new byte[2048];

            if (f.isDirectory()) {
                fe = new File(f, file);
            }

            if (fe.isDirectory()) {
                String[] fl = fe.list();
                if (fl != null && fl.length != 0) {
                    writeTarFile(parent, fe.getPath(), out);
                } else {
                    TarEntry entry = new TarEntry(fe, parent + file + "/");
                    out.putNextEntry(entry);
                }
                continue;
            }

            FileInputStream fi = new FileInputStream(fe);
            origin = new BufferedInputStream(fi);

            TarEntry entry = new TarEntry(fe, parent + file);
            out.putNextEntry(entry);

            int count;
            while ((count = origin.read(data)) != -1) {
                out.write(data, 0, count);
            }

            out.flush();
            origin.close();
        }
    }

}

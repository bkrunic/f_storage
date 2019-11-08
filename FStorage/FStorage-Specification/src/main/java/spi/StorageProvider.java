package spi;


import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author bkrunic
 */


public interface StorageProvider {
    /**
     * @param username name of user
     * @param password password of user
     *                 logs the user to the storage, maintaining only one user at the time-last one that logged in
     */
    void login(String username, String password);

    /**
     * @param nameToBeAdded name to be added
     * @param password      create a password
     * @return true or false, depends if the operation was success
     */
    boolean addAdmin(String nameToBeAdded, String password);

    /**
     * @param nameToBeAdded name to be added
     * @param password      create a password
     * @return true or false, depends if the operation was success
     */
    boolean addUser(String nameToBeAdded, String password);

    /**
     * @return checks for user priveleges in the user hiearchy, there are 3 levels
     */

    String userPriveleges();

    /**
     * @param extension extension to be forbidden
     * @param set       set, depending of the storage kind
     * @param file      depends of storage
     * @return
     */

    boolean forbExt(String extension, HashSet<String> set, File file);

    /**
     * initializes a storage, must be called first
     *
     * @return
     */
    boolean initializeStorage();

    /**
     * @param files         accepts a list a file that needs to be zipped
     * @param targetZipFile destination, where to store zipped file
     * @throws IOException
     */
    default void zipFile(File[] files, File targetZipFile) throws IOException {
        try {
            FileOutputStream fos = new FileOutputStream(targetZipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            byte[] buffer = new byte[128];
            for (int i = 0; i < files.length; i++) {
                File currentFile = files[i];
                if (!currentFile.isDirectory()) {
                    ZipEntry entry = new ZipEntry(currentFile.getName());
                    FileInputStream fis = new FileInputStream(currentFile);
                    zos.putNextEntry(entry);
                    int read = 0;
                    while ((read = fis.read(buffer)) != -1) {
                        zos.write(buffer, 0, read);
                    }
                    zos.closeEntry();
                    fis.close();
                }
            }
            zos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            msgHandler("File not found : " + e);
        }

    } //done

    /**
     * @param file depends of storage, deserializes a map from a given file
     * @return returns the map
     */
    default HashMap<String, String> deSerializeMap(File file) {
        HashMap<String, String> map = null;

        if (file.length() == 0)
            return map = new HashMap<>();

        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            map = (HashMap) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return null;
        }
        System.out.println("Deserialized HashMap..");

        return map;

    }

    /**
     * @param map  serializes a map from a given file
     * @param file accepts a file
     */

    default void serializeMap(HashMap<String, String> map, File file) {

        try {
            FileOutputStream fos =
                    new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(map);
            oos.close();
            fos.close();
            System.out.printf("Serialized HashMap data is saved in " + file);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * @param file deserializes a set from a given file
     * @return returns set
     */

    default HashSet<String> deSerializeSet(File file) {
        if (file.length() == 0) {
            HashSet<String> set = new HashSet<>();
            return set;
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        HashSet<String> hashSet = null;
        try {
            hashSet = (HashSet<String>) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            msgHandler("File not found");
            e.printStackTrace();
        }
        return hashSet;
    }

    /**
     * @param file    to store a set
     * @param hashSet to be serialized
     * @param path helps the serialization
     * @throws IOException
     */

    default void serializeSet(String path,File file, HashSet<String> hashSet) throws IOException {

        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        oos.writeObject(hashSet);

    }

    /**
     * @param message makes error handling smoother
     */

    default void msgHandler(String message) {
        System.err.println("\nUnexpected exception! " + message);

    }

    /**
     * @param file for which metadata is added to
     * @return returns that file for further transfers
     */

    default File addMetadata(File file) {
        HashMap<String, String> map = new HashMap();
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter optional metadata, in key-value format,\n for example -author,Van Gogh. \n You can add as many pairs as you want, but when you want to stop just write STOP.");
        String buffer = "";
        while (true) {
            buffer = sc.nextLine();
            if (!buffer.equalsIgnoreCase("stop")) {
                String[] token = buffer.split(",");
                map.put(token[0], token[1]);
            } else {
                break;
            }
        }


        String[] token = file.getName().split("\\.");

        File JSONFile = new File(token[0] + ".json");

        try {
            JSONFile.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject json = new JSONObject(map);
        try (FileWriter fw = new FileWriter(JSONFile)) {
            fw.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSONFile;


    }

    /**
     * @param path    path where do you create dirs
     * @param fNumber number of file that needs to be created
     * @return returns true or false depends of if it was success
     */

    boolean makedir(String path, int fNumber);

    /**
     * @param path deletes a file from a given path
     * @return delets true or false depends of it was success
     */
    boolean deleteFile(String path);

    /**
     * @param sourcePath transfers a file from sourcepath
     * @param targetPath destination where file is transfered
     * @return
     */
    boolean transferFromStorage(String sourcePath, String targetPath);

    /**
     * @param files accepts a list of files that will be transfered
     * @param path  path is a destination
     * @return returns true or false whether it was success
     * @throws IOException
     */

    boolean transferToStorage(File[] files, String path) throws IOException;

    /**
     * @param sourcePath destination path
     * @param targetPath path where it will be moved
     * @return true or false whether it was success
     * @throws IOException
     */

    boolean moveFileToDirectory(String sourcePath, String targetPath) throws IOException;

    /**
     * @param inputDir   search destination-it goes limitless depths
     * @param targetName search criteria
     * @return returns a list of file paths
     */

    ArrayList<String> fileSearch(String inputDir, String targetName);

    /**
     * @param inputDir        search destination-it goes limitless depths
     * @param targetExtension search criteria
     * @return returns a list of file paths
     */


    ArrayList<String> extensionSearch(String inputDir, String targetExtension);

    /**
     * @param inputDir searches for files in given dir
     * @return returns a list of paths
     */
    ArrayList<String> filesInDirSearch(String inputDir);

    /**
     * @param inputDir searches for directiories in given dir
     * @return returns a list of paths
     */

    ArrayList<String> DirInDirSearch(String inputDir);

    /**
     * @param inputDir searches for files with metadata on given path
     * @return returns a list of paths
     */

    ArrayList<String> metaDataFiles(String inputDir);

    /**
     * @param inputDir searches for files with no metadata on given path
     * @return returns a list of paths
     */

    ArrayList<String> noMetaDataFiles(String inputDir);


}

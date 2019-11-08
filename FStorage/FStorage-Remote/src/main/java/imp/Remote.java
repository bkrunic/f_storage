package imp;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author bkrunic
 */
public class Remote implements spi.StorageProvider {
    private static final String rootFile = "/root";
    private static final String ACCESS_TOKEN = "3jRxnk1yenAAAAAAAAAAIeZY7UCChoNy98VmcPsO4sQ8SaT6Nuqpr_x4N_IqcgWj";
    private static Path resourceDirectory = Paths.get("src", "main", "resources");
    private static String url = resourceDirectory.toFile().getAbsolutePath().concat("\\").replace("target\\", "");
    private static DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
    private static DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
    private File metaHash = new File(url + "remotemetaHash.txt");
    private File noMetaHash = new File(url + "remotenoMetaHash.txt");
    private File forbExtFile = new File(url + "remoteforbExt.txt");
    private File adminsFile = new File(url + "remoteadminsFile.txt");
    private File usersFile = new File(url + "remoteusersFile.txt");
    private HashMap<String, String> admins = deSerializeMap(adminsFile);
    private HashMap<String, String> users = deSerializeMap(usersFile);
    private HashSet<String> metaDataFiles = deSerializeSet(metaHash);
    private HashSet<String> noMetaDataFiles = deSerializeSet(noMetaHash);
    private HashSet<String> forbExtSet = deSerializeSet(forbExtFile);
    private String currentUser = "";

    public static String getUrl() {
        return url;
    }

    public static String getRootFile() {
        return rootFile;
    }

    public boolean initializeStorage() {
        if (!currentUser.equals("root")) {
            msgHandler("Only root users can initialize storage");
            return false;
        }
        try {
            CreateFolderResult root = client.files().createFolderV2(rootFile);
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean makedir(String path, int fNumber) {
        if (!userPriveleges().equals("admin")) return false;
        int helper = 0;
        try {
            helper = client.files().listFolder(path).getEntries().size();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        for (int i = helper; i <= helper + fNumber; i++) {
            try {
                client.files().createFolderV2(path + "/" + "s" + i);
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }
        return true;

    } //done

    public boolean deleteFile(String path) {
        if (!userPriveleges().equals("admin")) return false;
        try {
            client.files().deleteV2(path);
        } catch (DbxException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean transferFromStorage(String sourcePath, String targetPath) {
        if (!userPriveleges().equals("admin")) return false;
        try {
            //output file for download --> storage location on local system to download file
            OutputStream downloadFile = new FileOutputStream(targetPath);
            try {
                FileMetadata metadata = client.files().downloadBuilder(sourcePath)
                        .download(downloadFile);
            } finally {
                downloadFile.close();
                return true;
            }
        }
        //exception handled
        catch (IOException e) {
            //error downloading file
            msgHandler("Unable to download file to local system\n Error: " + e);
        }

        return false;
    }

    public boolean transferToStorage(File[] files, String path) throws IOException {
        if (!userPriveleges().equals("admin")) {
            msgHandler("Only admins may transfer files!");
            return false;
        }
        Scanner sc = new Scanner(System.in);
        String buffer = "";
        for (File file : files) {
            System.out.println("Do you want to add metadata for file: " + file.getName() + "? Y/N");
            buffer = sc.nextLine();
            boolean moveJSON = true;
            if (buffer.equalsIgnoreCase("Y")) {
                File json = addMetadata(file);
                moveJSON = moveFileToDirectory(json.getPath(), path);
                System.out.println("Metadata added");
                metaDataFiles.add(path + "/" + file.getPath());
            } else {
                noMetaDataFiles.add(path + "/" + file.getPath());
            }

            boolean moveFile = false;
            moveFile = moveFileToDirectory(file.getPath(), path);
            if (moveFile && moveJSON) {
                System.out.println("Succesfully transfered!");
            }
            serializeSet(path + "/" + file.getPath(), metaHash, this.metaDataFiles);
            serializeSet(path + "/" + file.getPath(), noMetaHash, this.noMetaDataFiles);

        }


        return true;
    }

    public boolean moveFileToDirectory(String sourcePath, String targetPath) throws IOException {
        for (String ex : forbExtSet) {
            if (sourcePath.endsWith(ex)) {
                msgHandler("Extension is forbiden in your storage!");
                return false;
            }
        }
        File sourceFile = new File(sourcePath);
        InputStream in = new FileInputStream(sourceFile);
        try {
            client.files().uploadBuilder(targetPath + "/" + sourceFile.getName()).uploadAndFinish(in);
        } catch (DbxException e) {
            e.printStackTrace();
        }


        return true;
    }

    public ArrayList<String> fileSearch(String inputDir, String targetName) {
        ArrayList<String> paths = new ArrayList<>();
        SearchResult searchResult = null;
        try {
            searchResult = client.files().searchBuilder(inputDir, targetName).start();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        List<SearchMatch> res = searchResult.getMatches();
        for (SearchMatch metadata : res) {
            paths.add(metadata.getMetadata().getPathLower());
        }
        return paths;
    }

    public ArrayList<String> extensionSearch(String inputDir, String targetExtension) {
        ArrayList<String> paths = new ArrayList<>();
        SearchResult searchResult = null;
        try {
            searchResult = client.files().searchBuilder(inputDir, targetExtension).start();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        List<SearchMatch> res = searchResult.getMatches();
        for (SearchMatch metadata : res) {
            paths.add(metadata.getMetadata().getPathLower());
        }
        return paths;
    }

    public ArrayList<String> filesInDirSearch(String inputDir) {

        ListFolderResult result = null;
        ArrayList<String> names = new ArrayList<>();
        try {
            result = client.files().listFolder(inputDir);
        } catch (DbxException e) {
            e.printStackTrace();
        }
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                names.add(metadata.getName());
            }

            if (!result.getHasMore()) {
                break;
            }

            try {
                result = client.files().listFolderContinue(result.getCursor());
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }

        return names;
    }

    public ArrayList<String> DirInDirSearch(String inputDir) {
        ListFolderResult result = null;
        ArrayList<String> names = new ArrayList<>();
        try {
            result = client.files().listFolder(inputDir);
        } catch (DbxException e) {
            e.printStackTrace();
        }
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                String name = metadata.getName();
                if (!name.contains("."))
                    names.add(name);
            }

            if (!result.getHasMore()) {
                break;
            }

            try {
                result = client.files().listFolderContinue(result.getCursor());
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }

        return names;
    }

    public ArrayList<String> metaDataFiles(String inputDir) {
        ArrayList<String> paths = new ArrayList<>();
        for (String path : metaDataFiles) {
            if (path.startsWith(inputDir))
                paths.add(path);
        }
        return paths;
    }

    public ArrayList<String> noMetaDataFiles(String inputDir) {
        ArrayList<String> paths = new ArrayList<>();
        for (String path : noMetaDataFiles) {
            if (path.startsWith(inputDir))
                paths.add(path);
        }
        return paths;
    }

    public boolean addUser(String nameToBeAdded, String password) {
        if (userPriveleges().equals("admin")) {
            users.put(nameToBeAdded, password);
            serializeMap(users, usersFile);
            return true;
        }
        msgHandler("Only admins can add users");
        return false;
    } //done

    public boolean addAdmin(String nameToBeAdded, String password) {
        if (currentUser.equals("root")) {
            if (password.isEmpty()) password = "1";
            admins.put(nameToBeAdded, password);
            serializeMap(admins, adminsFile);
            return true;
        }
        msgHandler("Only root users can add admins");
        return false;
    } //done

    public String userPriveleges() {
        for (Map.Entry<String, String> entry : admins.entrySet()) {
            if (currentUser.equals(entry.getKey()))
                return "admin";
        }

        for (Map.Entry<String, String> entry : users.entrySet()) {
            if (currentUser.equals(entry.getKey()))
                return "user";
        }

        if (currentUser.equals("root"))
            return "admin";
        //adminove privilegije su svakako podskup od root-a
        msgHandler("Such username doesnt exist!");
        return null;
    }

    public boolean forbExt(String extension, HashSet<String> set, File file) {
        if (!userPriveleges().equals("admin")) {
            msgHandler("Only admins can forbid extensions");
            return false;
        }
        set.add(extension);
        try {
            serializeSet(extension, file, set);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void login(String username, String password) {
        if (username.equals("root") && password.equals("bogdan98")) {
            currentUser = "root";
            return;
        }
        int flag = 0;
        for (Map.Entry<String, String> entry : admins.entrySet()) {
            if (password.equals(entry.getValue()) && username.equals(entry.getKey())) {
                currentUser = username;
                flag = 1;
                break;
            }
        }
        if (flag == 0)
            msgHandler("Such user doesn't exist");


    }

    public File getMetaHash() {
        return metaHash;
    }

    public File getNoMetaHash() {
        return noMetaHash;
    }

    public File getForbExtFile() {
        return forbExtFile;
    }

    public File getAdminsFile() {
        return adminsFile;
    }

    public File getUsersFile() {
        return usersFile;
    }

    public HashMap<String, String> getAdmins() {
        return admins;
    }

    public HashMap<String, String> getUsers() {
        return users;
    }

    public HashSet<String> getMetaDataFiles() {
        return metaDataFiles;
    }

    public HashSet<String> getNoMetaDataFiles() {
        return noMetaDataFiles;
    }

    public HashSet<String> getForbExtSet() {
        return forbExtSet;
    }

    public String getCurrentUser() {
        return currentUser;
    }


}



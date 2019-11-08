package imp;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author bkrunic
 */
public class Local implements spi.StorageProvider {

    private static Path resourceDirectory = Paths.get("src", "main", "resources");
    private static String url = resourceDirectory.toFile().getAbsolutePath().concat("\\").replace("target\\", "");
    private static String rootFile = "";
    private File metaHash = new File(url + "localmetaHash.txt");
    private File noMetaHash = new File(url + "localnoMetaHash.txt");
    private File forbExtFile = new File(url + "localforbExt.txt");
    private File adminsFile = new File(url + "localadminsFile.txt");
    private File usersFile = new File(url + "localusersFile.txt");
    private HashMap<String, String> admins = deSerializeMap(adminsFile);
    private HashMap<String, String> users = deSerializeMap(usersFile);
    private HashSet<String> metaDataFiles = deSerializeSet(metaHash);
    private HashSet<String> noMetaDataFiles = deSerializeSet(noMetaHash);
    private HashSet<String> forbExtSet = deSerializeSet(forbExtFile);
    private String currentUser = "root";

    public static String getUrl() {
        return url;
    }

    public static String getRootFile() {
        return rootFile;
    }

    public boolean initializeStorage() {
        if (currentUser.equals("root")) {
            String userHomePath = System.getProperty("user.home");

            rootFile = userHomePath + "\\root";
            File file = new File(rootFile);
            file.mkdir();
            return true;
        }
        msgHandler("Only root user can initialize storage");
        return false;
    }

    public boolean makedir(String path, int fNumber) {
        if (userPriveleges().equals("admin")) {
            File file = new File(path);
            int helper = file.listFiles().length;

            for (int i = helper; i < helper + fNumber; i++) {
                file = new File(path + "\\" + "s" + i);
                file.mkdir();
            }
            return true;

        }

        return false;
    }

    public boolean deleteFile(String path) {
        if (userPriveleges().equals("admin")) {

            File folder = new File(path);
            folder.delete();
            return true;
        }
        msgHandler("Onlu admins may delete files");
        return false;
    }

    public boolean transferFromStorage(String sourcePath, String targetPath) {
        if (!userPriveleges().equals("admin"))
            return false;
        boolean transfer = moveFileToDirectory(sourcePath, targetPath);
        if (transfer) {
            System.out.println("Succesfully transfered: " + sourcePath);
            return true;
        }
        msgHandler("Transfer failed");
        return false;
    }

    public boolean transferToStorage(File[] files, String path) throws IOException {
        if (!userPriveleges().equals("admin"))
            return false;
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
                metaDataFiles.add(path + "\\" + file.getPath());

            } else {
                noMetaDataFiles.add(path + "\\" + file.getPath());
            }

            boolean moveFile = moveFileToDirectory(file.getPath(), path);
            if (moveFile && moveJSON) {
                System.out.println("Succesfully transfered!");
            } else msgHandler("Failed transfer");
            serializeSet(path + "\\" + file.getPath(), metaHash, this.metaDataFiles);
            serializeSet(path + "\\" + file.getPath(), noMetaHash, this.noMetaDataFiles);
        }
        return true;


    }

    public boolean moveFileToDirectory(String sourcePath, String targetPath) {
        for (String ex : forbExtSet) {
            if (sourcePath.endsWith(ex)) {
                msgHandler("Extension is forbiden in your storage!");
                return false;
            }
        }

        File sourceFile = new File(sourcePath);
        File tDir = new File(targetPath);
        if (tDir.exists()) {
            String newFilePath = targetPath + File.separator + sourceFile.getName();
            File movedFile = new File(newFilePath);
            if (movedFile.exists())
                movedFile.delete();
            return sourceFile.renameTo(new File(newFilePath));
        } else {
            msgHandler("unable to move file " + sourceFile.getName() + " to directory " + targetPath + " -> target directory does not exist");
            return false;
        }
    }

    public ArrayList<String> fileSearch(String inputDir, String targetName) {

        ArrayList<String> files = new ArrayList<>();
        Queue<File> queue = new LinkedList<>();
        queue.add(new File(inputDir));

        while (!queue.isEmpty()) {

            File current = queue.poll();

            File[] fileDirList = current.listFiles();

            if (fileDirList != null) {
                for (File fd : fileDirList) {
                    if (fd.isDirectory())
                        queue.add(fd);
                    else if (fd.getName().equals(targetName))
                        files.add(fd.getPath());
                }
            }
        }
        return files;
    }

    public ArrayList<String> extensionSearch(String inputDir, String targetExtension) {
        Queue<File> queue = new LinkedList<>();
        ArrayList<String> files = new ArrayList<>();
        queue.add(new File(inputDir));

        while (!queue.isEmpty()) {

            File current = queue.poll();

            File[] fileDirList = current.listFiles();

            if (fileDirList != null) {
                for (File fd : fileDirList) {
                    if (fd.isDirectory())
                        queue.add(fd);
                    else if (fd.getName().endsWith(targetExtension))
                        files.add(fd.getPath());
                }
            }
        }
        return files;


    }

    public ArrayList<String> filesInDirSearch(String inputDir) {
        File dir = new File(inputDir);
        ArrayList<String> files = new ArrayList<>();
        for (File f : dir.listFiles()) {
            if (f.isFile())
                files.add(f.getName());
        }
        return files;

    }

    public ArrayList<String> DirInDirSearch(String inputDir) {
        File dir = new File(inputDir);
        ArrayList<String> dirs = new ArrayList<>();
        for (File f : dir.listFiles()) {
            if (f.isDirectory())
                dirs.add(f.getName());
        }
        return dirs;

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
        int flag = 0;
        if (username.equalsIgnoreCase("root") && password.equalsIgnoreCase("bogdan98")) {
            currentUser = "root";
            return;
        }
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

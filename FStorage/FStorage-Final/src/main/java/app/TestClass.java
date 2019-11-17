package app;

import imp.Remote;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author bkrunic
 */
public class TestClass {

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        imp.Remote storage = new imp.Remote();
        //imp.Local storage = new imp.Local();
        String fun = "";
        String rootFile = "";


        while (true) {
            System.out.println("Choose the wanted function:" +
                    "initialize,login,makedir,add admin,add user,transfer to storage,\ntransfer from storage,forbid extension" +
                    "zip files,delete,no meta files search,\nmeta files search,extension search" +
                    "file search,files in dir,dir in dir");

            fun = sc.nextLine();
            switch (fun) {
                case "initialize":
                    storage.initializeStorage();
                    rootFile = storage.getRootFile();

                    break;
                case "login":
                    System.out.println("Enter username:");
                    String username = sc.nextLine();
                    System.out.println("Enter password");
                    String password = sc.nextLine();
                    storage.login(username, password);
                    System.out.println("Current user is:" + storage.getCurrentUser());
                    break;
                case "makedir":
                    System.out.println("Enter the path:");
                    String path = rootFile.concat(sc.nextLine());
                    System.out.println("Enter the number of folders:");
                    Integer num = Integer.valueOf(sc.nextLine());
                    storage.makedir(path, num);
                    break;
                case "add admin":
                case "add user":
                    System.out.println("Enter username you want do add:");
                    String adminUsername = sc.nextLine();
                    System.out.println("Create a password");
                    String adminPassword = sc.nextLine();
                    storage.addAdmin(adminUsername, adminPassword);
                    break;
                case "transfer to storage":
                    System.out.println("Enter number of files you want to transfer");
                    Integer fileNum = Integer.valueOf(sc.nextLine());
                    System.out.println("Enter the path where you want to transfer:");
                    String transferPath = rootFile.concat(sc.nextLine());
                    List<File> list = new ArrayList<>();
                    for (int i = 0; i < fileNum; i++) {
                        System.out.println("Enter path of files you want to transfer separately in each line:");
                        String filePath = sc.nextLine();
                        File file = new File(filePath);
                        file.createNewFile();
                        list.add(file);
                        System.out.println("File added to the list");
                    }
                    File[] files = list.stream().toArray(File[]::new);
                    storage.transferToStorage(files, transferPath);
                    System.out.println("Files transfered to:" + transferPath);
                    break;
                case "transfer from storage":
                    System.out.println("Enter the source path: ");
                    String sourcePath = rootFile.concat(sc.nextLine());
                    System.out.println("Enter the target path: ");
                    String targetPath = sc.nextLine();
                    storage.transferFromStorage(sourcePath, targetPath);
                    System.out.println("Transfered to: " + targetPath);
                    break;
                case "forbid extension":
                    System.out.println("Enter the extension");
                    String forEx = sc.nextLine();
                    storage.forbExt(forEx, storage.getForbExtSet(), storage.getForbExtFile());
                    System.out.println("Extension forbiden:" + forEx + ".To remove you need to delete the file!");

                case "zip files":
                    System.out.println("Enter the path of target file");
                    String targetZipFile = rootFile.concat(sc.nextLine());
                    File targetFile = new File(targetZipFile);
                    targetFile.createNewFile();
                    System.out.println("Enter number of files you want to zip");
                    Integer zipNum = Integer.valueOf(sc.nextLine());
                    List<File> zipList = new ArrayList<>();
                    for (int i = 0; i < zipNum; i++) {
                        System.out.println("Enter path of files you want to transfer separately in each line:");
                        String filePath = sc.nextLine();
                        File file = new File(filePath);
                        file.createNewFile();
                        zipList.add(file);
                        System.out.println("File added to the list");
                    }
                    File[] zipFiles = zipList.stream().toArray(File[]::new);
                    storage.zipFile(zipFiles, targetFile);
                    System.out.println("Files zipped to: " + targetZipFile);
                    break;
                case "delete":
                    System.out.println("Enter the path");
                    String deletePath = rootFile.concat(sc.nextLine());
                    System.out.println(deletePath);
                    storage.deleteFile(deletePath);
                    break;
                case "no meta files search":
                    System.out.println("Enter the path");
                    String noMetaPath = rootFile.concat(sc.nextLine());
                    System.out.println(storage.noMetaDataFiles(noMetaPath));
                    break;
                case "meta files search":
                    System.out.println("Enter the path");
                    String metaPath = rootFile.concat(sc.nextLine());
                    System.out.println(storage.metaDataFiles(metaPath));
                    break;
                case "extension search":
                    System.out.println("Enter the path");
                    String exPath = rootFile.concat(sc.nextLine());
                    System.out.println("Enter the extension");
                    String ex = sc.nextLine();
                    System.out.println(storage.extensionSearch(exPath, ex));
                    break;
                case "file search":
                    System.out.println("Enter the path");
                    String fileSearchPath = rootFile.concat(sc.nextLine());
                    System.out.println("Enter the name");
                    String fileSearchName = sc.nextLine();
                    System.out.println(storage.fileSearch(fileSearchPath, fileSearchName));
                    break;
                case "files in dir":
                    System.out.println("Enter the path");
                    String filesInDirPath = rootFile.concat(sc.nextLine());
                    System.out.println(storage.filesInDirSearch(filesInDirPath));
                    break;
                case "dir in dir":
                    System.out.println("Enter the path");
                    String dirInDir = rootFile.concat(sc.nextLine());
                    System.out.println(storage.DirInDirSearch(dirInDir));
                    break;
                default:
                    break;
            }
        }


    }

}


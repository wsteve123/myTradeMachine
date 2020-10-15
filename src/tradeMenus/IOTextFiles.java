/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import positions.myUtils;

/**
 *
 * @author earlie87
 */
public class IOTextFiles {

    //private String homeDirectory = "/Users/earlie87/NetBeansProjects/myTradeMachine/src/supportFiles/";
    final private String homeDirectory = myUtils.getMyWorkingDirectory() + "/src/supportFiles/";

    public class ioRdTextFiles {
        /*
        1) call ioRdTextfiles which will OpenRd with filename and append doesn't matter
        2) call read to read text
        3) call closeRd to close file.
        */
        String fname = homeDirectory;

        DataOutputStream dos;
        FileInputStream fis;
        BufferedReader bir;
        BufferedWriter bow;

        private void openRd(String fileName, boolean append) {
            fname = fileName;

            try {
                fis = new FileInputStream(fname);
                bir = new BufferedReader(new InputStreamReader(fis));
            } catch (Exception e) {
                System.out.println("error reading text from: " + fname + "(" + e + ").");
                System.out.println("file does not exist " + fname);
                try {
                    bow = new BufferedWriter(new FileWriter(fname, false));
                    fis = new FileInputStream(fname);
                } catch (IOException ex) {
                    Logger.getLogger(createOptionFilesForExchangesDialogForm.class.getName()).log(Level.SEVERE, null, ex);
                }

                bir = new BufferedReader(new InputStreamReader(fis));
            }
        }

        void closeRd() {
            try {
                bir.close();

            } catch (Exception e) {
                System.out.println("error closing file: " + fname + "(" + e + ").");
            }
            fname = homeDirectory;
        }

        ioRdTextFiles(String fileName, boolean append) {
            fname = homeDirectory;

            this.openRd(fname + fileName, append);

        }
        String getDirectory(){
            return homeDirectory;
        }
        String read(Boolean str) {
            String rdStr = null;
            try {
                rdStr = bir.readLine();
            } catch (IOException ex) {
                Logger.getLogger(createOptionFilesForExchangesDialogForm.class.getName()).log(Level.SEVERE, null, ex);
            }
            return rdStr;
        }
        
    }
    
    public class ioWrTextFiles {

        String fname = homeDirectory;
        /*
        1) call ioWrTextfiles which will OpenWr with filename and append (true == append, false == new file)
        2) call write to write text
        3) call closeWr to close file.
        */
        FileOutputStream fos;
        BufferedWriter bow;
        DataOutputStream dos;
        FileInputStream fis;
        BufferedReader bir;

        void openWr(String fileName, boolean append) {
            fname = fileName;

            try {
                //fos = new FileOutputStream(fileName);
                //dos = new DataOutputStream(fos);
                bow = new BufferedWriter(new FileWriter(fname, append));

                try {
                    fis = new FileInputStream(fname);
                    bir = new BufferedReader(new InputStreamReader(fis));

                } catch (Exception e) {
                    System.out.println("file does not exist " + fname);
                    bow = new BufferedWriter(new FileWriter(fname, false));
                    fis = new FileInputStream(fname);
                    bir = new BufferedReader(new InputStreamReader(fis));

                }

            } catch (Exception e) {
                System.out.println("error writing text to: " + fileName + "(" + e + ").");
            }
        }

        void closeWr() {
            try {
                bow.close();

            } catch (Exception e) {
                System.out.println("error closing file: " + fname + "(" + e + ").");
            }
            fname = homeDirectory;
        }

        ioWrTextFiles(String fileName, boolean append) {
            fname = homeDirectory;
            this.openWr(fname + fileName, append);
        }

        boolean write(String str) {

            try {
                if (str != null) {
                    bow.write(str);
                    bow.newLine();
                    bow.flush();
                }
            } catch (Exception e) {
                System.out.println("error write to file: " + fname + "(" + e + ").");
            }
            return true;
        }

        String read(Boolean str) {
            String rdStr = null;
            try {
                rdStr = bir.readLine();
            } catch (IOException ex) {
                Logger.getLogger(createOptionFilesForExchangesDialogForm.class.getName()).log(Level.SEVERE, null, ex);
            }
            return rdStr;
        }
    }
    
    public class ioDeleteTextFiles {
        String fname = homeDirectory;
        File f;
        String fileToDelete;
        ioDeleteTextFiles(String fileName) {
            fname = homeDirectory;
            fileToDelete = fileName;
            f = new File(fname + fileName);
            
        }
        public boolean delete(){
            boolean ret = true;
            
            if (!f.exists() == true) {
                System.out.println("File Does not Exists!!");
                ret = false;
            } else {
                System.out.println("Delete: deleted " + fileToDelete + "portfolio.");
                
                // Attempt to delete it
                boolean success = f.delete();

                if (!success) {
                    ret = false;
                    throw new IllegalArgumentException("Delete: deletion failed");                  
                }
            }
            return (ret);
        }
    }
    public class ioListDirectory{
        private String dirName = "";
        private String ext = "";
        File dir = null;
        List <File> list = new ArrayList<File>();
        int sz = 0;
        public ioListDirectory(String dirToList, String extension) {
            dirName = dirToList;
            ext = extension;
            dir = new File(dirName);
        }

        public List<File> getList() {
            list = Arrays.asList(dir.listFiles(new FilenameFilter(){
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("." + ext);
                }
            }));
            sz = list.size();
            return list;
        }
        public int getSize(){
            return sz;
        }
    }
    boolean doesFileExist(String filename) {
            File file = new File(homeDirectory + filename);
            if (file.exists() && !file.isDirectory()) {
                return true;
            }
            return false;
        }
}

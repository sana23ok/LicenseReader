package edu.epam.fop.io;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class LicenseReader {

    private void processDirectory(File dir, BufferedWriter bw) {
        if (!dir.exists() || !dir.canExecute()) {
            System.err.println("Directory is not executable or does not exist.");
            return;
        }

        File[] items = dir.listFiles();
        if (items == null) {
            return; // Handle cases where permissions or other issues prevent reading
        }

        for (File item : items) {
            if (item.isDirectory()) {
                processDirectory(item, bw); // Recursive traversal
            } else if (item.isFile()) {
                // Process individual files
                String result = new ProcessFile(item).processFile();
                if (result != null) {
                    try {
                        bw.write(result); // Write the processed result to the output file
                    } catch (IOException e) {
                        System.err.println("Error writing to the output file: " + e.getMessage());
                    }
                }
            }
        }
    }

    public void validateFiles(File root, File outputFile){
        // collectLicenses метод повинен підтвердити, що root:
        //не нульовий root, outputFile
        if (root==null || outputFile==null)
            throw new IllegalArgumentException("Path cant be null!");

        //існує, читабельний
        if(!root.exists() || !root.canRead())
            throw new IllegalArgumentException("Path does not exist or cant be readable!");

        //якщо це каталог, то він є виконуваним
        if(root.isDirectory() && !root.canExecute())
            throw new IllegalArgumentException("Root is not a executable directory!");
    }

    public void collectLicenses(File root, File outputFile){
        validateFiles(root, outputFile);

        try {
            Files.newBufferedWriter(outputFile.toPath(), StandardOpenOption.TRUNCATE_EXISTING).close();
        } catch (Exception e) {
            // Handle the potential exception
            System.out.println("An error occurred.");
            e.printStackTrace();
        }


        try {
            // Clear the output file before writing new data
            Files.newBufferedWriter(outputFile.toPath(), StandardOpenOption.TRUNCATE_EXISTING).close();
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile, true));
            // Process files in the root directory
            if (root.isDirectory()) {
                File[] items = root.listFiles();
                if (items == null) {
                    return; // Handle cases where permissions or other issues prevent reading
                }

                for (File item : items) {
                    if (item.isDirectory()) {
                        processDirectory(item, bw); // Recursive traversal
                    } else if (item.isFile()) {
                        // Process individual files
                        ProcessFile p = new ProcessFile(item);
                        String result = p.processFile();
                        if (result != null) {
                            try {
                                bw.write(result); // Write the processed result to the output file
                            } catch (IOException e) {
                                System.err.println("Error writing to the output file: " + e.getMessage());
                            }
                        }
                    }
                }
            } else {
                // Process single file
                ProcessFile pr = new ProcessFile(root);
                bw.write(pr.processFile());
            }
        } catch (IOException e) {
            // Handle the potential exception
            System.err.println("An error occurred while processing files.");
            e.printStackTrace();
        }





        try (BufferedReader br = new BufferedReader(new FileReader(String.valueOf(outputFile.toPath())))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

//        if(root.isDirectory() && root.canExecute()){
//            File[] items = root.listFiles();
//
//            if (items == null) {
//                return; // Could be due to permissions or other issues.
//            }
//
//            for (File item : items) {
//                if (item.isDirectory()) {
//                    System.out.println("Directory: " + item.getAbsolutePath());
//                    //processDirectory(item);
//                } else if (item.isFile()) {
//                    System.out.println("processFile");
//                    // You can perform additional processing on each file.
//                }
//            }
//        }else{
//            if(isLicenseFile(root)) System.out.println("processFile");
//        }


    }
}


/*package edu.epam.fop.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class LicenseReader {

    public void validateFiles(File root, File outputFile) {
        if (root == null || outputFile == null) {
            throw new IllegalArgumentException("Path cannot be null!");
        }

        if (!root.exists() || !root.canRead()) {
            throw new IllegalArgumentException("Path does not exist or is not readable!");
        }

        if (root.isDirectory() && !root.canExecute()) {
            throw new IllegalArgumentException("Directory is not executable!");
        }
    }

    public void collectLicenses(File root, File outputFile) {
        validateFiles(root, outputFile);

        // Ensure the output file exists and is cleared
        ensureFileExists(outputFile);
        try (BufferedWriter bw = Files.newBufferedWriter(outputFile.toPath(), StandardOpenOption.TRUNCATE_EXISTING)) {
            // Traverse the directory to process license files
            processDirectory(root, bw);
        } catch (IOException e) {
            System.err.println("Error initializing output file: " + e.getMessage());
        }

        // Print the output file's content
        try (BufferedReader br = new BufferedReader(new FileReader(outputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading output file: " + e.getMessage());
        }
    }

    private void ensureFileExists(File file) {
        if (!file.exists()) {
            try {
                Files.createDirectories(file.toPath().getParent()); // Ensure parent directories exist
                file.createNewFile(); // Create the file if it doesn't exist
            } catch (IOException e) {
                System.err.println("Error ensuring file exists: " + e.getMessage());
            }
        }
    }

    private void processDirectory(File dir, BufferedWriter bw) {
        if (!dir.exists() || !dir.canExecute()) {
            System.err.println("Directory is not executable or does not exist.");
            return;
        }

        File[] items = dir.listFiles();
        if (items == null) {
            return; // Handle cases where permissions or other issues prevent reading
        }

        for (File item : items) {
            if (item.isDirectory()) {
                processDirectory(item, bw); // Recursive traversal
            } else if (item.isFile()) {
                // Process individual files
                String result = new ProcessFile(item, bw).processFile();
                if (result != null) {
                    try {
                        bw.write(result); // Write the processed result to the output file
                    } catch (IOException e) {
                        System.err.println("Error writing to the output file: " + e.getMessage());
                    }
                }
            }
        }
    }
}*/




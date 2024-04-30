package edu.epam.fop.io;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class LicenseReader {

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
                File[] files = root.listFiles();

                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            // Process individual files
                            ProcessFile pr = new ProcessFile(file, outputFile);
                            bw.write(pr.processFile());
                        }
                    }
                }
            } else {
                // Process single file
                ProcessFile pr = new ProcessFile(root, outputFile);
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

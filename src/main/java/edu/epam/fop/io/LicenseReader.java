package edu.epam.fop.io;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class ProcessFile {
    private static File in;
    private static  File out;
    private static final String LICENSE_START_MARKER = "---";
    private static final String LICENSE_END_MARKER = "---";

    public ProcessFile(File root) {
        in = root;
    }



    private static String parseFile(File in) {
        String res = "";
        Map<String, String> licenseProperties = new HashMap<>();
        boolean inLicenseBlock = false;

        try (BufferedReader br = new BufferedReader(new FileReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (LICENSE_START_MARKER.equals(line)) {
                    inLicenseBlock = true;
                } else if (LICENSE_END_MARKER.equals(line)) {
                    if (!inLicenseBlock) {
                        throw new IllegalArgumentException("Invalid license file: " + in.getName());
                    }
                    break;
                } else if (inLicenseBlock && line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        licenseProperties.put(key, value);
                    }
                }
            }

            if (!inLicenseBlock) {
                throw new IllegalArgumentException("Missing license block in file: " + in.getName());
            }

            if (!licenseProperties.containsKey("License")) {
                throw new IllegalArgumentException("Missing License in file: " + in.getName());
            }

            if (!licenseProperties.containsKey("Issued by")) {
                throw new IllegalArgumentException("Missing Issued by in file: " + in.getName());
            }

            if (!licenseProperties.containsKey("Issued on")) {
                throw new IllegalArgumentException("Missing Issued on in file: " + in.getName());
            }

            String licenseName = licenseProperties.get("License");
            String issuedBy = licenseProperties.get("Issued by");
            String issuedOn = licenseProperties.get("Issued on");
            String expiresOn = licenseProperties.getOrDefault("Expires on", "unlimited");

            res = String.format(
                    "License for %s is %s issued by %s [%s - %s]%n",
                    in.getName(), licenseName, issuedBy, issuedOn, expiresOn
            );

        } catch (Exception e) {
            System.err.println("Error processing file: " + in.getAbsolutePath() + " - " + e.getMessage());
        }

        return res;
    }





    private static boolean isLicenseFile(File file) {
        boolean hasStartMarker = false;
        boolean hasEndMarker = false;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().equals(LICENSE_START_MARKER)) {
                    if (!hasStartMarker) {
                        hasStartMarker = true;
                    } else {
                        hasEndMarker = true;
                        break;
                    }
                }
            }

            if (!hasStartMarker || !hasEndMarker) {
                throw new IllegalArgumentException("Invalid license file: " + file.getAbsolutePath());
            }

        } catch (Exception e) { // Handle unchecked exceptions
            throw new IllegalArgumentException("Error reading file: " + file.getAbsolutePath() + " - " + e.getMessage());
        }

        return hasStartMarker && hasEndMarker;
    }

    private static boolean checkDateFormat(String date) {
        // Check if the date is in the format yyyy-mm-dd and contains only digits and "-"
        return date.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    private static boolean checkAllDependencies(File file) {
        boolean hasLicense = false;
        boolean hasIssuedOn = false;
        boolean hasIssuedBy = false;
        boolean hasExpiresOn = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("License:")) {
                    hasLicense = true;
                } else if (line.startsWith("Issued on:")) {
                    hasIssuedOn = true;
                    // Extract date and check format
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        String date = parts[1].trim();
                        if (!checkDateFormat(date)) {
                            return false; // Invalid date format
                        }
                    }
                } else if (line.startsWith("Issued by:")) {
                    hasIssuedBy = true;
                } else if (line.startsWith("Expires on:")) {
                    hasExpiresOn = true;
                    // Extract date and check format
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        String date = parts[1].trim();
                        if (!checkDateFormat(date)) {
                            return false;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading file: " + file.getAbsolutePath() + " - " + e.getMessage());
        }

        return hasLicense && hasIssuedOn && hasIssuedBy;
    }

    public static String processFile() {
        String licence = "";
        if(isLicenseFile(in)){
            if(checkAllDependencies(in)) {
                licence = parseFile(in);
            } else {
                throw new IllegalArgumentException("Missing item");
            }
        }else{
            throw new IllegalArgumentException("File is not licence");
        }

        if (!Objects.equals(licence, " ")){
            return licence;
        }
        throw new IllegalArgumentException("File is not licence");
    }
}

public class LicenseReader {
    private static void processDirectory(File dir, BufferedWriter bw) {
        if (!dir.exists() || !dir.canExecute()) {
            throw new IllegalArgumentException("Directory is not executable or does not exist.");
            //return;
        }

        File[] items = dir.listFiles();
        if (items == null) {
            throw new IllegalArgumentException();
            //return; // Handle cases where permissions or other issues prevent reading
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
                        throw new IllegalArgumentException("Error writing to the output file: " + e.getMessage());
                    }
                }
            }
        }
    }

    public static void validateFiles(File root, File outputFile) {
        // collectLicenses метод повинен підтвердити, що root:
        //не нульовий root, outputFile
        if (root == null || outputFile == null)
            throw new IllegalArgumentException("Path cant be null!");

        //існує, читабельний
        if (!root.exists() || !root.canRead())
            throw new IllegalArgumentException("Path does not exist or cant be readable!");

        //якщо це каталог, то він є виконуваним
        if (root.isDirectory() && !root.canExecute())
            throw new IllegalArgumentException("Root is not a executable directory!");
    }

    public static void collectLicenses(File root, File outputFile) {
        validateFiles(root, outputFile);

        try {
            // Clear the output file before writing new data
            BufferedWriter bw = Files.newBufferedWriter(outputFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
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
                        try {
                            String result = p.processFile();
                            if (result != null) {
                                bw.write(result); // Write the processed result to the output file
                            }
                        } catch (IllegalArgumentException e) {
                            // Ignore non-license files and continue processing
                            //throw new IllegalArgumentException(e);
                        }
                    }
                }
            } else {
                // Process single file
                ProcessFile pr = new ProcessFile(root);
                try {
                    bw.write(pr.processFile());
                } catch (IllegalArgumentException e) {
                    // Ignore non-license files
                    throw new IllegalArgumentException(e);
                }
            }
            bw.close(); // Close the writer after writing all data
        } catch (IOException e) {
            // Handle the potential exception
            throw new IllegalArgumentException("An error occurred while processing files.");
            //e.printStackTrace();
        }
    }
}




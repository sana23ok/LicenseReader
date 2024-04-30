package edu.epam.fop.io;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProcessFile {
    private static File in;
    private static  File out;
    private static final String LICENSE_START_MARKER = "---";

    public ProcessFile(File root, File outputFile) {
        in = root;
        out = outputFile;
    }

    private static String parseFile(File in) {
        // res must be already cleaned
        String res = "";
        Map<String, String> licenseProperties = new HashMap<>();
        boolean inLicenseBlock = false;

        try (BufferedReader br = new BufferedReader(new FileReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (LICENSE_START_MARKER.equals(line)) {
                    inLicenseBlock = !inLicenseBlock; // Toggle between start and end
                } else if (inLicenseBlock && line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        licenseProperties.put(key, value);
                    }
                }
            }

            // Check if the required properties are present
            if (checkAllDependencies(in)) {
                String licenseName = licenseProperties.get("License");
                String issuedBy = licenseProperties.get("Issued by");
                String issuedOn = licenseProperties.get("Issued on");
                String expiresOn = licenseProperties.getOrDefault("Expires on", "unlimited");

                res = String.format(
                        "License for %s is %s issued by %s [%s - %s]%n",
                        in.getName(), licenseName, issuedBy, issuedOn, expiresOn
                );

//                bw.write(output); // Write to the result file
//                bw.flush(); // Ensure the data is written to the file
            } else {
                System.err.println("Invalid license dependencies in file: " + in.getAbsolutePath());
            }

        } catch (Exception e) { // Handle unchecked exceptions
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
        } catch (Exception e) { // Handle unchecked exceptions
            System.err.println("Error reading file: " + file.getAbsolutePath() + " - " + e.getMessage());
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
                            return false; // Invalid date format
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + file.getAbsolutePath() + " - " + e.getMessage());
        }

        return hasLicense && hasIssuedOn && hasIssuedBy;
    }

    public static String processFile() {
        String licence = "";

        System.out.println(isLicenseFile(in));
        if(isLicenseFile(in)){
            if(checkAllDependencies(in))
                licence = parseFile(in);
        }else{
            throw new IllegalArgumentException("File is not licence");
        }

        if (!Objects.equals(licence, " ")){
            return licence;
        }

        throw new IllegalArgumentException("File is not licence");
    }
}

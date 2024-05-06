package edu.epam.fop.io;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
//"java.lang",
//"java.util",
//"java.io",
//"java.time",
//"java.time.format",
//"edu.epam.fop.io"

public class ProcessFile {
    private static File in;
    private static  File out;
    private static final String LICENSE_START_MARKER = "---";

    public ProcessFile(File root) {
        in = root;
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


/*package edu.epam.fop.io;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProcessFile {
    private static final String LICENSE_START_MARKER = "---";

    private File in;
    private BufferedWriter out; // Use BufferedWriter for output

    public ProcessFile(File root, BufferedWriter outputWriter) {
        this.in = root;
        this.out = outputWriter; // Assign the BufferedWriter to output
    }

    public String processFile() {
        if (!isLicenseFile(in)) {
            return null; // Return null if not a valid license file
        }

        if (!checkAllDependencies(in)) {
            System.err.println("Invalid license dependencies in file: " + in.getAbsolutePath());
            return null; // Return null if required dependencies are missing
        }

        return parseFile(in); // Get the license information to write
    }

    private boolean isLicenseFile(File file) {
        boolean hasStartMarker = false;
        boolean hasEndMarker = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().equals(LICENSE_START_MARKER)) {
                    if (!hasStartMarker) {
                        hasStartMarker = true;
                    } else {
                        hasEndMarker = true;
                        break; // Break when both start and end markers are found
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return hasStartMarker && hasEndMarker;
    }

    private static boolean checkDateFormat(String date) {
        return date != null && date.matches("\\d{4}-\\d{2}-\\d{2}"); // yyyy-mm-dd format
    }

    private static boolean checkAllDependencies(File file) {
        boolean hasLicense = false;
        boolean hasIssuedOn = false;
        boolean hasIssuedBy = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("License:")) {
                    hasLicense = true;
                } else if (line.startsWith("Issued on:")) {
                    hasIssuedOn = true;
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        String date = parts[1].trim();
                        if (!checkDateFormat(date)) {
                            return false; // Invalid date format
                        }
                    }
                } else if (line.startsWith("Issued by:")) {
                    hasIssuedBy = true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return hasLicense && hasIssuedOn && hasIssuedBy;
    }

    private static String parseFile(File in) {
        Map<String, String> licenseProperties = new HashMap<>();
        boolean inLicenseBlock = false;

        try (BufferedReader br = new BufferedReader(new FileReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (LICENSE_START_MARKER.equals(line)) {
                    inLicenseBlock = !inLicenseBlock;
                } else if (inLicenseBlock && line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        licenseProperties.put(key, value);
                    }
                }
            }

            if (licenseProperties.containsKey("License") &&
                    licenseProperties.containsKey("Issued by") &&
                    licenseProperties.containsKey("Issued on")) {
                String licenseName = licenseProperties.get("License");
                String issuedBy = licenseProperties.get("Issued by");
                String issuedOn = licenseProperties.get("Issued on");
                String expiresOn = licenseProperties.getOrDefault("Expires on", "unlimited");

                return String.format(
                        "License for %s is %s issued by %s [%s - %s]%n",
                        in.getName(), licenseName, issuedBy, issuedOn, expiresOn
                );
            }
        } catch (IOException e) {
            System.err.println("Error parsing file: " + e.getMessage());
        }

        return null; // Return null if parsing fails or dependencies are missing
    }
}*/

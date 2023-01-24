package edu.epam.fop.io;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeArchives;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeJars;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.core.importer.ImportOptions;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test for non-functional requirements")
public class ComplianceTest {

  private static final Set<String> ALLOWED_PACKAGES = Set.of(
      "java.lang",
      "java.util",
      "java.io",
      "java.time",
      "java.time.format",
      "edu.epam.fop.io"
  );

  private static final Set<Class<?>> ALLOWED_CLASSES = Set.of(
      File.class,
      IOException.class,
      FileWriter.class,
      FileReader.class,
      BufferedWriter.class,
      BufferedReader.class
  );

  @Test
  void shouldHaveAccessOnlyToBufferedIO() {
    final Set<Class<?>> foundClasses = ConcurrentHashMap.newKeySet();
    classes().should().onlyAccessClassesThat(new DescribedPredicate<>("are allowed by task requirements") {
          @Override
          public boolean apply(JavaClass input) {
            return ALLOWED_PACKAGES.stream().anyMatch(input.getPackageName()::equals) && (
                !input.getPackageName().equals("java.io") ||
                    ALLOWED_CLASSES.stream()
                        .filter(input::isAssignableFrom)
                        .peek(foundClasses::add)
                        .findAny().isPresent()
            );
          }
        })
        .because("LicenseReader must use only buffered writer/reader for IO interactions")
        .check(new ClassFileImporter().importClasspath(
                new ImportOptions()
                    .with(new DoNotIncludeTests())
                    .with(new DoNotIncludeJars())
                    .with(new DoNotIncludeArchives())
            )
        );
    if (!foundClasses.contains(BufferedWriter.class)) {
      throw new AssertionError("Write IO operations must be buffered");
    }
    if (!foundClasses.contains(BufferedReader.class)) {
      throw new AssertionError("Read IO operations must be buffered");
    }
  }
}

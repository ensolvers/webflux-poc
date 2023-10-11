package com.ensolvers.fox.ses;

import javax.mail.MessagingException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateUtils {
  /**
   * Takes a html document with placeholders and a map of replacements, and replaces every value for the values sent in
   * the map
   *
   * @param template     The html for the email
   * @param replacements A map containing for every placeholder, what value it should be replaced to
   * @return The final html with all replacements made
   * @throws MessagingException When a placeholder wasn't replaced
   */
  public static String replace(String template, Map<String, String> replacements) throws MessagingException {
    for (Map.Entry<String, String> entry : replacements.entrySet()) {
      template = template.replace(entry.getKey(), entry.getValue());
    }

    validateEmptyReplacements(template);

    return template;
  }

  /**
   * Validates that every placeholder in the document has been replaced successfully
   *
   * @param body The html document to validate
   * @throws MessagingException When a placeholder wasn't replaced
   */
  public static void validateEmptyReplacements(String body) throws MessagingException {
    Pattern pattern = Pattern.compile("[$][{]\\S+[}]");
    Matcher matcher = pattern.matcher(body);
    StringBuilder allMatches = new StringBuilder("Placeholder wasn't replaced: \n");
    boolean placeholderMissing = false;
    while (matcher.find()) {
      allMatches.append(matcher.group()).append("\n");
      placeholderMissing = true;
    }

    if (placeholderMissing) {
      throw new MessagingException(allMatches.toString());
    }
  }
}

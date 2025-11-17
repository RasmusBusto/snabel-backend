package no.snabel.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/api/docs")
@Produces(MediaType.TEXT_HTML)
public class DocsResource {

    private static final String MISSING_API_PATH = "../docs/missing_api.md";

    @GET
    @Path("/missing-apis")
    @PermitAll
    public Response getMissingApis() {
        try {
            String markdown = Files.readString(Paths.get(MISSING_API_PATH));
            String html = convertMarkdownToHtml(markdown);
            return Response.ok(html).build();
        } catch (IOException e) {
            String errorHtml = generateErrorHtml("Could not read missing_api.md file: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorHtml)
                    .build();
        }
    }

    private String convertMarkdownToHtml(String markdown) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Missing API Endpoints</title>\n");
        html.append("    <style>\n");
        html.append("        body {\n");
        html.append("            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;\n");
        html.append("            max-width: 1200px;\n");
        html.append("            margin: 0 auto;\n");
        html.append("            padding: 2rem;\n");
        html.append("            line-height: 1.6;\n");
        html.append("            color: #333;\n");
        html.append("            background-color: #f9fafb;\n");
        html.append("        }\n");
        html.append("        h1 {\n");
        html.append("            color: #1f2937;\n");
        html.append("            border-bottom: 3px solid #4f46e5;\n");
        html.append("            padding-bottom: 0.5rem;\n");
        html.append("            margin-bottom: 1.5rem;\n");
        html.append("        }\n");
        html.append("        h2 {\n");
        html.append("            color: #4f46e5;\n");
        html.append("            margin-top: 2rem;\n");
        html.append("            margin-bottom: 1rem;\n");
        html.append("        }\n");
        html.append("        h3 {\n");
        html.append("            color: #6366f1;\n");
        html.append("            margin-top: 1.5rem;\n");
        html.append("        }\n");
        html.append("        pre {\n");
        html.append("            background-color: #1f2937;\n");
        html.append("            color: #f3f4f6;\n");
        html.append("            padding: 1rem;\n");
        html.append("            border-radius: 0.5rem;\n");
        html.append("            overflow-x: auto;\n");
        html.append("            font-size: 0.875rem;\n");
        html.append("        }\n");
        html.append("        code {\n");
        html.append("            background-color: #e5e7eb;\n");
        html.append("            color: #ef4444;\n");
        html.append("            padding: 0.125rem 0.25rem;\n");
        html.append("            border-radius: 0.25rem;\n");
        html.append("            font-size: 0.875rem;\n");
        html.append("            font-family: 'Courier New', Courier, monospace;\n");
        html.append("        }\n");
        html.append("        pre code {\n");
        html.append("            background-color: transparent;\n");
        html.append("            color: #f3f4f6;\n");
        html.append("            padding: 0;\n");
        html.append("        }\n");
        html.append("        ul, ol {\n");
        html.append("            margin: 1rem 0;\n");
        html.append("            padding-left: 2rem;\n");
        html.append("        }\n");
        html.append("        li {\n");
        html.append("            margin: 0.5rem 0;\n");
        html.append("        }\n");
        html.append("        hr {\n");
        html.append("            border: none;\n");
        html.append("            border-top: 2px solid #e5e7eb;\n");
        html.append("            margin: 2rem 0;\n");
        html.append("        }\n");
        html.append("        strong {\n");
        html.append("            color: #1f2937;\n");
        html.append("            font-weight: 600;\n");
        html.append("        }\n");
        html.append("        .comment {\n");
        html.append("            color: #6b7280;\n");
        html.append("            font-style: italic;\n");
        html.append("        }\n");
        html.append("        .container {\n");
        html.append("            background-color: white;\n");
        html.append("            padding: 2rem;\n");
        html.append("            border-radius: 0.5rem;\n");
        html.append("            box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");

        String[] lines = markdown.split("\n");
        boolean inCodeBlock = false;
        String codeBlockContent = "";
        String codeBlockLanguage = "";

        for (String line : lines) {
            // Handle code blocks
            if (line.trim().startsWith("```")) {
                if (!inCodeBlock) {
                    // Start code block
                    inCodeBlock = true;
                    codeBlockLanguage = line.trim().substring(3).trim();
                    codeBlockContent = "";
                } else {
                    // End code block
                    html.append("<pre><code>");
                    html.append(escapeHtml(codeBlockContent));
                    html.append("</code></pre>\n");
                    inCodeBlock = false;
                    codeBlockContent = "";
                }
                continue;
            }

            if (inCodeBlock) {
                codeBlockContent += line + "\n";
                continue;
            }

            // Handle HTML comments
            if (line.trim().startsWith("<!--")) {
                html.append("<p class=\"comment\">").append(escapeHtml(line.trim())).append("</p>\n");
                continue;
            }

            // Handle headers
            if (line.startsWith("### ")) {
                html.append("<h3>").append(escapeHtml(line.substring(4))).append("</h3>\n");
            } else if (line.startsWith("## ")) {
                html.append("<h2>").append(escapeHtml(line.substring(3))).append("</h2>\n");
            } else if (line.startsWith("# ")) {
                html.append("<h1>").append(escapeHtml(line.substring(2))).append("</h1>\n");
            }
            // Handle horizontal rules
            else if (line.trim().equals("---")) {
                html.append("<hr>\n");
            }
            // Handle unordered lists
            else if (line.trim().startsWith("- ")) {
                html.append("<ul><li>").append(processInlineMarkdown(line.substring(line.indexOf("- ") + 2))).append("</li></ul>\n");
            }
            // Handle empty lines
            else if (line.trim().isEmpty()) {
                html.append("<br>\n");
            }
            // Regular paragraphs
            else if (!line.trim().isEmpty()) {
                html.append("<p>").append(processInlineMarkdown(line)).append("</p>\n");
            }
        }

        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    private String processInlineMarkdown(String text) {
        text = escapeHtml(text);

        // Bold text
        Pattern boldPattern = Pattern.compile("\\*\\*([^*]+)\\*\\*");
        Matcher boldMatcher = boldPattern.matcher(text);
        text = boldMatcher.replaceAll("<strong>$1</strong>");

        // Inline code
        Pattern codePattern = Pattern.compile("`([^`]+)`");
        Matcher codeMatcher = codePattern.matcher(text);
        text = codeMatcher.replaceAll("<code>$1</code>");

        return text;
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String generateErrorHtml(String errorMessage) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Error</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; padding: 2rem; }\n" +
                "        .error { color: #ef4444; background-color: #fee; padding: 1rem; border-radius: 0.5rem; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"error\">\n" +
                "        <h2>Error</h2>\n" +
                "        <p>" + escapeHtml(errorMessage) + "</p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}

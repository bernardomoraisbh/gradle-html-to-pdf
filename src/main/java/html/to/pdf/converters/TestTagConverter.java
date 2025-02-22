//package html.to.pdf.converters;
package html.to.pdf.converters;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TestTagConverter {

    public static String processTemplate(Object rootObject, String template) throws Exception {
        return processTemplateRecursively(rootObject, template, new HashMap<>(), 0);
    }

    private static String processTemplateRecursively(Object currentObject, String template, Map<Integer, List<?>> listContext, int level) throws Exception {
        String processedTemplate = template;

        // Process $L{} lists first to handle nested list contexts
        processedTemplate = processLists(currentObject, processedTemplate, listContext, level, new Stack<>());

        // Process $P{} parameters
        processedTemplate = processParameters(currentObject, processedTemplate);

        // Process $RN{} or $RNL{} conditionals
        boolean isListContext = level > 0;
        processedTemplate = processConditionals(currentObject, processedTemplate, isListContext);

        return processedTemplate;
    }

    private static String processParameters(Object currentObject, String template) throws Exception {
        Pattern parameterPattern = Pattern.compile(Pattern.quote("$P{") + "(.*?)" + Pattern.quote("}"));
        Matcher matcher = parameterPattern.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String path = matcher.group(1).trim();
            Object value = resolvePath(currentObject, path);
            matcher.appendReplacement(result, Matcher.quoteReplacement(value != null ? value.toString() : ""));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private static String processConditionals(Object currentObject, String template, boolean isListContext) throws Exception {
        String openingTag = isListContext ? "$RNL{" : "$RN{";
        String closingTagPrefix = isListContext ? "$RNL{/" : "$RN{/";
        Pattern conditionalPattern = Pattern.compile(Pattern.quote(openingTag) + "([a-zA-Z0-9_.]*?)" + Pattern.quote("}"));
        Matcher matcher = conditionalPattern.matcher(template);
        StringBuilder result = new StringBuilder();
        Stack<String> tagStack = new Stack<>(); // Stack to track nested tags

        boolean matchFound = false;
        int contador = 0;
        int startIndex = 0;
        int endIndex = 0;
        int previousStartIndex = 0;
        int previousEndIndex = 0;
        String previousTag = "";
        String previousTagEnding = "";
        while (matcher.find()) {
            matchFound = true;
            String path = matcher.group(1).trim();
            Object value = resolvePath(currentObject, path);
            boolean condition = evaluateCondition(value);

            startIndex = matcher.end();
            endIndex = findMatchingEnd(template, startIndex, openingTag.substring(0, 4), path, tagStack); // Pass tagStack
            String conditionalContent = template.substring(startIndex, endIndex);
            StringBuilder processedContent = new StringBuilder();

            if(contador == 0) {
                // Start the result string with content before the RN
                result.append(template, 0, startIndex - (openingTag + path + "}").length());
            } else {
                // Concat the content before the current $RN and after the previous $RN
                result.append(template, previousEndIndex + previousTagEnding.length(), startIndex - (openingTag + path + "}").length());
            }

            if (condition) {
                // Process the nested content recursively
                processedContent.append(processTemplateRecursively(currentObject, conditionalContent, new HashMap<>(), 0));
                result.append(processedContent);
            }

            // Adjust matcher region to skip past the processed block (including closing tag)
            int nextRegionStart = endIndex + closingTagPrefix.length() + path.length() + 1;
            matcher.region(nextRegionStart, template.length());

            // Remove the corresponding opening tag from the stack
            if (!tagStack.isEmpty() && tagStack.peek().equals(openingTag + path)) {
                tagStack.pop();
            }
            contador += 1;
            previousStartIndex = startIndex;
            previousEndIndex = endIndex;
            previousTag = openingTag + path + "}";
            previousTagEnding = openingTag + "/" + path + "}";
        }

        if(!matchFound) {
            return template;
        } else {
            // Concat content after last $RN after the loop ending
            result.append(template, previousEndIndex + previousTagEnding.length(), template.length());
            return result.toString();
        }
    }

    private static String processLists(Object currentObject, String template, Map<Integer, List<?>> listContext, int level, Stack<String> tagStack) throws Exception {
        Pattern listPattern = Pattern.compile(Pattern.quote("$L{") + "([a-zA-Z0-9_.]*?)" + Pattern.quote("}"));
        Matcher matcher = listPattern.matcher(template);
        StringBuilder result = new StringBuilder();

        boolean matchFound = false;
        int contador = 0;
        while (matcher.find()) {
            matchFound = true;
            String path = matcher.group(1).trim();
            String openingTag = "$L{" + path + "}";
            String endTag = "$L{/" + path + "}";
            Object listObj = resolvePath(currentObject, path);

            if (!(listObj instanceof List<?>)) {
                throw new IllegalArgumentException("Path " + path + " does not resolve to a List.");
            }

            List<?> list = (List<?>) listObj;
            listContext.put(level, list);

            int startIndex = matcher.end();
            int endIndex = findMatchingEnd(template, startIndex, "$L", path, tagStack); // Pass tagStack
            String listContent = template.substring(startIndex, endIndex);

            StringBuilder listResult = new StringBuilder();
            for (Object item : list) {
                listResult.append(processTemplateRecursively(item, listContent, listContext, level + 1));
            }
            if(contador > 0) {
                var newResult = result.toString().replace(openingTag + listContent + endTag, listResult.toString());
                result.replace(0, result.length(), newResult);
            } else {
                result.append(template.replace(openingTag + listContent + endTag, listResult.toString()));
            }
//            matcher.appendReplacement(result, Matcher.quoteReplacement(listResult.toString()));

            // Adjust matcher region to skip past the processed block (including closing tag)
            int nextRegionStart = endIndex + ("$L{/" + path + "}").length();
            matcher.region(nextRegionStart, template.length());

            // Remove the corresponding opening tag from the stack
            if (!tagStack.isEmpty() && tagStack.peek().equals("$L{" + path)) {
                tagStack.pop();
            }
            contador += 1;
        }
        if(!matchFound) {
            return template;
        } else {
            return result.toString();
        }
    }


    private static Object resolvePath(Object object, String path) throws Exception {
        String[] parts = path.split("\\.");
        Object current = object;

        for (String part : parts) {
            if (current == null) return null;
            if (part.equals(object.getClass().getSimpleName())) continue;
            Field field;
            try {
                field = current.getClass().getDeclaredField(part);
                field.setAccessible(true);
                current = field.get(current);
            } catch (NoSuchFieldException e) {
                Method method;
                try {
                    String methodName = part.startsWith("get") ? part : "get" + capitalize(part);
                    method = current.getClass().getMethod(methodName);
                    current = method.invoke(current);
                } catch (NoSuchMethodException ex) {
                    throw new IllegalArgumentException("Cannot resolve path: " + path);
                }
            }
        }

        return current;
    }

    private static boolean evaluateCondition(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String string) {
            return !string.isEmpty();
        } else if (value instanceof Number number) {
            return number.doubleValue() != 0;
        }
        return value != null;
    }

    private static int findMatchingEnd(String template, int startIndex, String tag, String path, Stack<String> tagStack) {
        String startTagPattern = Pattern.quote(tag) + "\\{([\\w.]+)}";
        String endTagPattern = Pattern.quote(tag) + "\\{/([\\w.]+)}";
        String endTag = tag + "{/" + path + "}";
        int depth = 1;
        int currentIndex = startIndex;

        Pattern openingTagPattern = Pattern.compile(startTagPattern);
        Pattern endingTagPattern = Pattern.compile(endTagPattern);

        while (currentIndex < template.length()) {
            Matcher openingMatcher = openingTagPattern.matcher(template.substring(currentIndex));
            Matcher endMatcher = endingTagPattern.matcher(template.substring(currentIndex));

            int nextStart = openingMatcher.find() ? currentIndex + openingMatcher.start() : -1;
            int nextEnd = endMatcher.find() ? currentIndex + endMatcher.start() : -1;

            if (nextStart != -1 && (nextEnd == -1 || nextStart < nextEnd)) {
                depth++;
                tagStack.push(tag + "{" + openingMatcher.group(1) + "}");
                currentIndex = nextStart + openingMatcher.group(0).length();
            } else if (nextEnd != -1) {
                depth--;
                currentIndex = nextEnd + endMatcher.group(0).length();
                if (depth == 0 && template.substring(nextEnd).startsWith(endTag)) {
                    return nextEnd;
                }
            } else {
                break;
            }
        }

        if (depth > 0) {
            throw new IllegalArgumentException("Mismatched tags: No closing tag for " + tag + "{" + path + "}");
        }

        return currentIndex;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}

//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//
//public class TestTagConverter {
//
//    public static String processTemplate(Object rootObject, String template) throws Exception {
//        return processTemplateRecursively(rootObject, template, new HashMap<>(), 0);
//    }
//
//    private static String processTemplateRecursively(Object currentObject, String template, Map<Integer, List<?>> listContext, int level) throws Exception {
//        String processedTemplate = template;
//
//        // Process $L{} lists first to handle nested list contexts
//        processedTemplate = processLists(currentObject, processedTemplate, listContext, level);
//
//        // Process $P{} parameters
//        processedTemplate = processParameters(currentObject, processedTemplate);
//
//        // Process $RN{} or $RNL{} conditionals
//        boolean isListContext = level > 0;
//        processedTemplate = processConditionals(currentObject, processedTemplate, isListContext);
//
//        return processedTemplate;
//    }
//
//    private static String processParameters(Object currentObject, String template) throws Exception {
//        Pattern parameterPattern = Pattern.compile(Pattern.quote("$P{") + "(.*?)" + Pattern.quote("}"));
//        Matcher matcher = parameterPattern.matcher(template);
//        StringBuffer result = new StringBuffer();
//
//        while (matcher.find()) {
//            String path = matcher.group(1).trim();
//            Object value = resolvePath(currentObject, path);
//            matcher.appendReplacement(result, Matcher.quoteReplacement(value != null ? value.toString() : ""));
//        }
//
//        matcher.appendTail(result);
//        return result.toString();
//    }
//
//    private static String processConditionals(Object currentObject, String template, boolean isListContext) throws Exception {
//        String openingTag = isListContext ? "$RNL{" : "$RN{";
//        String closingTagPrefix = isListContext ? "$RNL{/" : "$RN{/";
//        Pattern conditionalPattern = Pattern.compile(Pattern.quote(openingTag) + "(.*?)" + Pattern.quote("}"));
//        Matcher matcher = conditionalPattern.matcher(template);
//        StringBuffer result = new StringBuffer();
//
//        while (matcher.find()) {
//            String path = matcher.group(1).trim();
//            Object value = resolvePath(currentObject, path);
//            boolean condition = evaluateCondition(value);
//
//            int startIndex = matcher.end();
//            int endIndex = findMatchingEnd(template, startIndex, openingTag.substring(0, 4), path);
//            String conditionalContent = template.substring(startIndex, endIndex);
//
//            if (condition) {
//                String recursiveReturn = Matcher.quoteReplacement(processTemplateRecursively(currentObject, conditionalContent, new HashMap<>(), 0));
//                matcher.appendReplacement(result, recursiveReturn);
//            } else {
//                matcher.appendReplacement(result, "");
//            }
//
//            matcher.region(endIndex + closingTagPrefix.length() + path.length() + 1, template.length());
//        }
//
//        matcher.appendTail(result);
//        return result.toString();
//    }
//
//    private static String processLists(Object currentObject, String template, Map<Integer, List<?>> listContext, int level) throws Exception {
//        Pattern listPattern = Pattern.compile(Pattern.quote("$L{") + "(.*?)" + Pattern.quote("}"));
//        Matcher matcher = listPattern.matcher(template);
//        StringBuilder result = new StringBuilder();
//
//        while (matcher.find()) {
//            String path = matcher.group(1).trim();
//            Object listObj = resolvePath(currentObject, path);
//
//            if (!(listObj instanceof List<?>)) {
//                throw new IllegalArgumentException("Path " + path + " does not resolve to a List.");
//            }
//
//            List<?> list = (List<?>) listObj;
//            listContext.put(level, list);
//
//            int startIndex = matcher.end();
//            int endIndex = findMatchingEnd(template, startIndex, "$L", path);
//            String listContent = template.substring(startIndex, endIndex);
//
//            StringBuilder listResult = new StringBuilder();
//            for (Object item : list) {
//                listResult.append(processTemplateRecursively(item, listContent, listContext, level + 1));
//            }
//
//            matcher.appendReplacement(result, Matcher.quoteReplacement(listResult.toString()));
//            matcher.region((endIndex + "$L{/" + path + "}").length(), template.length());
//        }
//
//        matcher.appendTail(result);
//        return result.toString();
//    }
//
//    private static Object resolvePath(Object object, String path) throws Exception {
//        String[] parts = path.split("\\.");
//        Object current = object;
//
//        for (String part : parts) {
//            if (current == null) return null;
//            if (part.equals(object.getClass().getSimpleName())) continue;
//            Field field;
//            try {
//                field = current.getClass().getDeclaredField(part);
//                field.setAccessible(true);
//                current = field.get(current);
//            } catch (NoSuchFieldException e) {
//                Method method;
//                try {
//                    method = current.getClass().getMethod("get" + capitalize(part));
//                    current = method.invoke(current);
//                } catch (NoSuchMethodException ex) {
//                    throw new IllegalArgumentException("Cannot resolve path: " + path);
//                }
//            }
//        }
//
//        return current;
//    }
//
//    private static boolean evaluateCondition(Object value) {
//        if (value instanceof Boolean) {
//            return (Boolean) value;
//        } else if (value instanceof String string) {
//            return !string.isEmpty();
//        } else if (value instanceof Number number) {
//            return number.doubleValue() != 0;
//        }
//        return value != null;
//    }
//
//    private static int findMatchingEnd(String template, int startIndex, String tag, String path) {
//        String startTagPattern = Pattern.quote(tag) + "\\{([\\w.]+)}"; // Matches opening tags (no slash after '{')
//        String endTagPattern = Pattern.quote(tag) + "\\{/([\\w.]+)}"; // Matches opening tags (no slash after '{')
//        String endTag = tag + "{/" + path + "}"; // Exact closing tag
//        int depth = 1; // Start at depth 1 since we are inside an opening tag
//        int currentIndex = startIndex;
//
//        Pattern openingTagPattern = Pattern.compile(startTagPattern);
//        Pattern endingTagPattern = Pattern.compile(endTagPattern);
//
//        while (currentIndex < template.length()) {
//            // Check for the next opening tag
//            Matcher openingMatcher = openingTagPattern.matcher(template.substring(currentIndex));
//            int nextStart = openingMatcher.find() ? currentIndex + openingMatcher.start() : -1;
//
//            // Check for the next closing tag
//            Matcher endMatcher = endingTagPattern.matcher(template.substring(currentIndex));
//            int nextEnd = endMatcher.find() ? currentIndex + endMatcher.start() : -1;
//
//            // Determine which tag appears next
//            if (nextStart != -1 && (nextEnd == -1 || nextStart < nextEnd)) {
//                // Found another opening tag before the closing tag
//                depth++;
//                currentIndex = nextStart + 1; // Move past the opening tag
//            } else if (nextEnd != -1) {
//                // Found a closing tag
//                depth--;
//                currentIndex = nextEnd + endTag.length(); // Move past the closing tag
//                if (depth == 0) {
//                    return nextEnd; // Return the index of the closing tag
//                }
//            } else {
//                break; // No more tags to process
//            }
//        }
//
//        if(depth == 1 && currentIndex == template.length() && template.endsWith(endTag)){
//            return currentIndex;
//        }
//
//        // If we exit the loop without depth reaching 0, throw an error
//        if (depth > 0) {
//            throw new IllegalArgumentException("Mismatched tags: No closing tag for " + tag + "{" + path + "}");
//        }
//
//        return currentIndex; // Safely return current index if no errors
//    }
//
//    private static String capitalize(String str) {
//        if (str == null || str.isEmpty()) return str;
//        return str.substring(0, 1).toUpperCase() + str.substring(1);
//    }
//
//}

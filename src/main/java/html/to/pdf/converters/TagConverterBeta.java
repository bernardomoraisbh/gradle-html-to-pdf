package html.to.pdf.converters;

import html.to.pdf.container.StringFragmentContainer;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.capitalize;

public class TagConverterBeta {

    public static String processTemplate(Object root, String template) {
        var rootFragment = new StringFragmentContainer(template, 0, false, root);
        var arrayDeque = new ArrayDeque<StringFragmentContainer>();
        arrayDeque.add(rootFragment);
        return processTemplateRecursively(rootFragment);
    }

    private static String processTemplateRecursively(StringFragmentContainer fragmentQueueContainer) {
        String response = "";
        response = processLists(fragmentQueueContainer);
        response = processConditionals(fragmentQueueContainer);
        return response;
    }

//    private void skipListsInsideConditionals(String textFragment, int startIndexConditional, Matcher matcherConditional) {
//        Pattern openListPattern = Pattern.compile(Pattern.quote("$L{") + "([a-zA-Z0-9_.]*?)" + Pattern.quote("}"));
//        Matcher matcherOpeningTag = openListPattern.matcher(textFragment);
//        matcherOpeningTag.region(startIndexConditional, textFragment.length());
//        if(matcherOpeningTag.find()) {
//            String path = matcherOpeningTag.group(1).trim(); // Find the first list opening tag object path
//            String openingTag = "$L{" + path + "}";
//            String endTag = "$L{/" + path + "}";
//            Pattern closeListPattern = Pattern.compile(Pattern.quote("$L{/") + path + Pattern.quote("}")); // First closing list $L{/path}
//            int startIndex = matcherOpeningTag.end();
//            int endIndex = 0;
//            Matcher matcherCloseTag = closeListPattern.matcher(textFragment.substring(startIndex));
//            if(matcherCloseTag.find()) {
//                endIndex = matcherCloseTag.start();
//            } else {
//                // Exception for opened list tag without closing match
//                throw new RuntimeException("Error searching for " + endTag);
//            }
//        }
//    }

    private static String processConditionals(StringFragmentContainer fragmentQueueContainer) {
        String conditionalTagPrefix = fragmentQueueContainer.isListContext() ? "$RNL{" : "$RN{";
        Pattern openConditionalPattern = Pattern.compile(Pattern.quote(conditionalTagPrefix) + "([a-zA-Z0-9_.]*?)" + Pattern.quote("}"));
        Matcher matcherOpeningConditionalTag = openConditionalPattern.matcher(fragmentQueueContainer.getTextFragment());
        String response = "";
        boolean matchFound = false;
        while (matcherOpeningConditionalTag.find()) {
            matchFound = true;
            String path = matcherOpeningConditionalTag.group(1).trim(); // Find the first conditional opening tag object path
            String openingTag = conditionalTagPrefix + path + "}";
            String endTag = conditionalTagPrefix + "/" + path + "}";
            Pattern closeConditionalPattern = Pattern.compile(Pattern.quote(conditionalTagPrefix + "/") + path + Pattern.quote("}")); // First closing conditional tag path
            int startIndex = matcherOpeningConditionalTag.end();
            int endIndex = 0;
            String conditionalContent = "";
            Matcher matcherCloseTag = closeConditionalPattern.matcher(fragmentQueueContainer.getTextFragment().substring(startIndex));
            boolean condition = false;
            if(matcherCloseTag.find()) {
                endIndex = matcherCloseTag.start();
                Object value = resolvePath(fragmentQueueContainer.getFragmentNode(), path);
                conditionalContent = fragmentQueueContainer.getTextFragment().substring(startIndex, endIndex + startIndex);
                condition = evaluateCondition(value);
            } else {
                // Exception for opened list tag without closing match
                throw new RuntimeException("Error searching for " + endTag);
            }
            // Replace the conditional content with the processed content conditional
            String conditionalContentWithTags = new StringBuilder(openingTag).append(conditionalContent).append(endTag).toString();
            if(condition) {
                var recursiveFragmentReturn = processTemplateRecursively(new StringFragmentContainer(conditionalContent, fragmentQueueContainer.getLevel() + 1, fragmentQueueContainer.isListContext(), fragmentQueueContainer.getFragmentNode()));
                response = fragmentQueueContainer.getTextFragment().replace(conditionalContentWithTags, recursiveFragmentReturn);
            } else {
                response = fragmentQueueContainer.getTextFragment().replace(conditionalContentWithTags, "");
            }
            // Adjust matcher region to skip past the processed block (including closing tag)
            endIndex = startIndex + (endIndex + endTag.length()); // OPENING TAG + CONTENT + CLOSING TAG
            matcherOpeningConditionalTag.region(endIndex, fragmentQueueContainer.getTextFragment().length());
        }
        // If there's no $RN or $RNL in current textFragment, just return the fragment itself
        if(!matchFound){
            return fragmentQueueContainer.getTextFragment();
        }
        return response;
    }

    private static String processLists(StringFragmentContainer fragmentQueueContainer) {
        Pattern openListPattern = Pattern.compile(Pattern.quote("$L{") + "([a-zA-Z0-9_.]*?)" + Pattern.quote("}")); // $L{}
        Matcher matcherOpeningTag = openListPattern.matcher(fragmentQueueContainer.getTextFragment());
        String response = "";
        boolean matchFound = false;
        while (matcherOpeningTag.find()) {
            matchFound = true;
            String path = matcherOpeningTag.group(1).trim(); // Find the first list opening tag object path
            String openingTag = "$L{" + path + "}";
            String endTag = "$L{/" + path + "}";
            Pattern closeListPattern = Pattern.compile(Pattern.quote("$L{/") + path + Pattern.quote("}")); // First closing list $L{/path}
            int startIndex = matcherOpeningTag.end();
            int endIndex = 0;
            String listContent = "";
            StringBuilder listResult = new StringBuilder();
            // Try to find the matching close tag for our path
            Matcher matcherCloseTag = closeListPattern.matcher(fragmentQueueContainer.getTextFragment().substring(startIndex));
            if(matcherCloseTag.find()) {
                endIndex = matcherCloseTag.start();
                // Get the content between the start tag and closing tag
                listContent = fragmentQueueContainer.getTextFragment().substring(startIndex, endIndex + startIndex);
                // No empty list tag allowed
                if(StringUtils.isBlank(listContent)) {
                    throw new RuntimeException("Erro ao localizar texto dentro de $L{" + path + "}...$L{/" + path + "}");
                }
                // Recover the list from the object path
                Object listObj = resolvePath(fragmentQueueContainer.getFragmentNode(), path);
                if (!(listObj instanceof List<?>)) {
                    throw new IllegalArgumentException("Path " + path + " does not resolve to a List.");
                }
                List<?> list = (List<?>) listObj;
                // Concatenate the new string for each processed element of the object list
                for (Object item : list) {
                    listResult.append(processTemplateRecursively(new StringFragmentContainer(listContent, fragmentQueueContainer.getLevel() + 1, true, item)));
                }
            } else {
                // Exception for opened list tag without closing match
                throw new RuntimeException("Error searching for " + endTag);
            }
            // Replace the list content with the processed list return
            String listContentWithTags = new StringBuilder(openingTag).append(listContent).append(endTag).toString();
            response = fragmentQueueContainer.getTextFragment().replace(listContentWithTags, listResult.toString());
            // Adjust matcher region to skip past the processed block (including closing tag)
            endIndex = startIndex + (endIndex + endTag.length()); // OPENING TAG + CONTENT + CLOSING TAG
            matcherOpeningTag.region(endIndex, fragmentQueueContainer.getTextFragment().length());
        }
        // If there's no $L in current textFragment, just return the fragment itself
        if(!matchFound){
            return fragmentQueueContainer.getTextFragment();
        }
        return response;
    }

    // Recover an object based on the nested path string representation
    private static Object resolvePath(Object object, String path) {
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
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Method method;
                try {
                    method = current.getClass().getMethod("get" + capitalize(part));
                    current = method.invoke(current);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
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
}

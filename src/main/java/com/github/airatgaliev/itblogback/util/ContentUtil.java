package com.github.airatgaliev.itblogback.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

public class ContentUtil {

  public static String createHtmlPreview(String htmlContent, int maxLength) {
    Document document = Jsoup.parseBodyFragment(htmlContent);
    Element body = document.body();

    int[] currentLength = {0}; // Для отслеживания текущей длины текста
    boolean[] textTruncated = {false}; // Для отслеживания, была ли выполнена обрезка

    // Обрезаем контент
    truncateElement(body, maxLength, currentLength, textTruncated);

    // Если текст был обрезан, добавляем многоточие в конце
    if (textTruncated[0]) {
      addEllipsisToEnd(body);
    }

    return body.html();
  }

  private static void truncateElement(Element element, int maxLength, int[] currentLength,
      boolean[] textTruncated) {
    Element nextSibling = null;
    for (Element child : element.children()) {
      if (textTruncated[0]) {
        child.remove();
      } else if (isHeadingTag(child)) {
        // Если достигли заголовка и текущая длина меньше maxLength
        if (currentLength[0] < maxLength) {
          // Сохраняем следующий элемент для последующей обработки
          nextSibling = child.nextElementSibling();
          child.remove(); // Удаляем заголовок
        } else {
          // Если текущая длина превышает maxLength, удаляем заголовок и все последующее
          child.remove();
        }
      } else {
        // Рекурсивно обрабатываем дочерние элементы
        truncateElement(child, maxLength, currentLength, textTruncated);
      }
    }

    for (TextNode textNode : element.textNodes()) {
      if (textTruncated[0]) {
        textNode.remove();
      } else {
        String text = textNode.text();
        int remainingLength = maxLength - currentLength[0];
        if (text.length() + currentLength[0] > maxLength) {
          String truncatedText = truncateTextAtSentenceBoundary(text, remainingLength);
          textNode.text(truncatedText);
          currentLength[0] = maxLength; // Завершаем обрезку
          textTruncated[0] = true; // Устанавливаем флаг обрезки текста
          return;
        } else {
          currentLength[0] += text.length();
        }
      }
    }

    // Удаляем последующие элементы, если достигнут лимит
    if (textTruncated[0] && nextSibling != null) {
      nextSibling.remove();
    }
  }

  private static void addEllipsisToEnd(Element element) {
    // Находим последний текстовый узел в текущем элементе
    TextNode lastTextNode = findLastTextNode(element);
    if (lastTextNode != null) {
      String existingText = lastTextNode.text();
      if (!existingText.endsWith("...")) {
        lastTextNode.text(existingText + "...");
      }
    }
  }

  private static TextNode findLastTextNode(Element element) {
    // Находим последний текстовый узел в текущем элементе и его потомках
    TextNode lastTextNode = null;
    for (Element child : element.children()) {
      TextNode childLastTextNode = findLastTextNode(child);
      if (childLastTextNode != null) {
        lastTextNode = childLastTextNode;
      }
    }

    if (element.textNodes().size() > 0) {
      lastTextNode = element.textNodes().get(element.textNodes().size() - 1);
    }

    return lastTextNode;
  }

  private static boolean isHeadingTag(Element element) {
    // Проверяем, является ли элемент заголовком (h1, h2, h3, ...)
    String tagName = element.tagName();
    return tagName.equals("h1") || tagName.equals("h2") || tagName.equals("h3") ||
        tagName.equals("h4") || tagName.equals("h5") || tagName.equals("h6");
  }

  private static String truncateTextAtSentenceBoundary(String text, int maxLength) {
    if (maxLength >= text.length()) {
      return text;
    }

    String truncated = text.substring(0, maxLength);
    int lastSentenceBoundary = findLastSentenceBoundary(truncated);

    // Если нашли конец предложения в пределах maxLength, обрезаем там
    if (lastSentenceBoundary != -1) {
      return text.substring(0, lastSentenceBoundary).trim();
    }

    // В противном случае обрезаем до maxLength
    return truncated.trim();
  }

  private static int findLastSentenceBoundary(String text) {
    // Поиск границы предложения
    int lastPeriod = text.lastIndexOf('.');
    int lastExclamation = text.lastIndexOf('!');
    int lastQuestion = text.lastIndexOf('?');

    int maxIndex = Math.max(lastPeriod, Math.max(lastExclamation, lastQuestion));
    return maxIndex + 1;
  }
}
package com.evershop.testdata;

/**
 * Test data class for SearchTests.
 * Contains search keywords and URLs used in search functionality testing.
 */
public class SearchData {

    public static final String BASE_URL = "https://demo.evershop.io";

    // --- Search keywords ---

    /** Valid exact product name */
    public static final String EXACT_PRODUCT_NAME = "Nike zoom fly";

    /** Valid search keyword that finds products */
    public static final String VALID_KEYWORD = "zoom";

    /** Non-existent product for no results test */
    public static final String NON_EXISTENT_PRODUCT = "abcs";

    /** Keyword with leading/trailing spaces */
    public static final String KEYWORD_WITH_SPACES = " zoom ";

    /** Trimmed version of keyword with spaces */
    public static final String KEYWORD_TRIMMED = "zoom";

    /** Multiple words search */
    public static final String MULTIPLE_WORDS = "nike zoom";

    /** Single character search */
    public static final String SINGLE_CHARACTER = "z";

    // --- Factory methods ---

    /** Get exact product name for successful search */
    public static String getExactProductName() {
        return EXACT_PRODUCT_NAME;
    }

    /** Get valid keyword for successful search */
    public static String getValidKeyword() {
        return VALID_KEYWORD;
    }

    /** Get non-existent product for no results */
    public static String getNonExistentProduct() {
        return NON_EXISTENT_PRODUCT;
    }

    /** Get keyword with spaces */
    public static String getKeywordWithSpaces() {
        return KEYWORD_WITH_SPACES;
    }

    /** Get trimmed version of spaced keyword */
    public static String getKeywordTrimmed() {
        return KEYWORD_TRIMMED;
    }

    /** Get multiple words for search */
    public static String getMultipleWords() {
        return MULTIPLE_WORDS;
    }

    /** Get single character for search */
    public static String getSingleCharacter() {
        return SINGLE_CHARACTER;
    }

    /** Get base URL for the application */
    public static String getBaseUrl() {
        return BASE_URL;
    }
}

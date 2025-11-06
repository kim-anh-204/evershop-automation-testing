# Evershop E2E Automation Testing

Evershop automation testing framework with Selenium WebDriver and TestNG, featuring separate Allure reporting for different test suites.

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Chrome/Firefox browser

### Installation
```bash
git clone https://github.com/kim-anh-204/evershop-automation.git
cd evershop-automation
mvn clean compile
```

## 🧪 Running Tests

### Run Search Tests (7 test cases)
```bash
mvn test -Psearch
```
- Generate: `target/allure-report-search/index.html`
- Coverage: Product search functionality validation

### Run Add to Cart Tests (15 test cases)
```bash
mvn test -Paddtocart
```
- Generate: `target/allure-report-addtocart/index.html`
- Coverage: Address validation and checkout functionality

### Run All Tests
```bash
mvn test -Psearch && mvn test -Paddtocart
```

## 📊 Viewing Reports

### Search Report
- Open: `target/allure-report-search/index.html`
- Features: Pie chart, 7 test cases (85.7% PASSED, 14.3% FAILED)

### Add to Cart Report
- Open: `target/allure-report-addtocart/index.html`
- Features: Pie chart, 15 test cases (26.7% PASSED, 73.3% FAILED)

## 🎯 Test Suites Overview

### 🔍 Search Test Suite
- **Test Count**: 7 methods
- **Coverage**:
  - Search by exact product name
  - Search by keyword
  - Search non-existent products
  - Search with spaces
  - Search multiple words
  - Search empty input
  - Search single character

### 🛒 Add to Cart Test Suite
- **Test Count**: 15 methods
- **Coverage**:
  - Address form validation
  - Successful address creation
  - Field validation (name, phone, address, city, postcode)
  - Data length validation
  - Error message verification
  - Default address editing

## 🏗️ Project Structure

```
evershop-automation/
├── src/
│   ├── main/java/com/evershop/
│   │   ├── pages/          # Page Object classes
│   │   ├── testdata/       # Test data utilities
│   │   └── utils/          # Helper utilities
│   ├── test/java/com/evershop/
│   │   ├── tests/          # Test classes
│   │   └── utils/          # Test utilities
│   └── test/resources/     # TestNG configurations
├── target/                 # Build artifacts (generated)
├── pom.xml                 # Maven configuration
└── .gitignore
```

## 🛠️ Technologies Used

- **Selenium WebDriver 4.15.0** - Web browser automation
- **TestNG 7.8.0** - Testing framework
- **Allure 2.24.0** - Test reporting with separate suites
- **WebDriverManager 5.6.2** - Browser driver management
- **Maven Surefire Plugin** - Test execution with profiles
- **Lombok 1.18.32** - Code generation

## 📋 Maven Profiles

### Profile: `search`
- Runs: `SearchTests.java`
- Results: `target/allure-results-search/`
- Report: `target/allure-report-search/`

### Profile: `addtocart`
- Runs: `AddAddressTest.java`
- Results: `target/allure-results-addtocart/`
- Report: `target/allure-report-addtocart/`

## 🎨 Report Features

### Professional UI
- Modern gradient backgrounds
- Responsive design for all devices
- Interactive pie charts with hover effects
- Color-coded test status (green/red)
- Clean typography and spacing

### Interactive Charts
- **Canvas-based pie charts**
- **Real-time PASSED/FAILED visualization**
- **Legend with test counts and percentages**
- **Gradient animations and effects**

### Test Summary
- Total test count
- Pass/fail statistics
- Execution duration
- Color-coded metrics

## 🚀 CI/CD Ready

This project is configured for CI/CD pipelines:

```bash
# Build and test commands for CI
mvn clean compile
mvn test -Psearch
mvn test -Paddtocart
```

## 📝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/new-feature`)
3. Commit changes (`git commit -m 'Add new feature'`)
4. Push to branch (`git push origin feature/new-feature`)
5. Create Pull Request

## 📄 License

This project is proprietary software - All Rights Reserved.

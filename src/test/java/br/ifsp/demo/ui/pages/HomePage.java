package br.ifsp.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HomePage extends BasePage {

    private final By newRootNameInput = By.xpath("//div[label[text()='Nova raiz']]//input");
    private final By createRootButton = By.xpath("//button[contains(., 'Criar raiz')]");

    private final By createAsChildCheckbox = By.id("toggle-child");

    private final By categoryParentSelectTrigger = By.xpath("//div[label[text()='Categoria pai']]//button[@role='combobox']");
    private final By newChildNameInput = By.xpath("//div[label[text()='Nome da subcategoria']]//input");
    private final By createChildButton = By.xpath("//button[contains(., 'Criar Subcategoria')]");

    private final By actionCategorySelectTrigger = By.xpath("//div[label[text()='Selecionar categoria']]//button[@role='combobox']");

    private final By renameTextInput = By.xpath("//div[label[text()='Renomear Categoria']]//input");
    private final By renameButton = By.xpath("//button[contains(., 'Renomear')]");

    private final By deleteButton = By.xpath("//button[contains(., 'Remover')]");

    private final By moveParentSelectTrigger = By.xpath("//div[label[text()='Mover “Selecionada” para dentro de…']]//button[@role='combobox']");
    private final By moveButton = By.xpath("//button[contains(., 'Mover')]");

    private final By goalsSection = By.xpath("//h2[contains(text(),'Metas') or contains(text(),'Metas de Gastos')]");
    private final By goalRootSelectTrigger = By.xpath("//div[label[contains(text(),'Categoria raiz')]]//button[@role='combobox']");
    private final By goalMonthInput = By.xpath("//label[contains(text(), 'Mês (YYYY-MM)')]/following::input[1]");
    private final By goalLimitInput = By.xpath("//label[contains(text(),'Limite')]/following::input[1]");
    private final By goalSaveButton = By.xpath("//button[contains(., 'Definir') or contains(., 'Ajustar')]");
    private final By goalLimitValue = By.xpath("//div[contains(text(),'Limite')]/span");
    private final By goalStatusText = By.xpath("//div[contains(@class,'font-semibold')]");
    private final By goalCategorySelectTrigger = By.xpath("//div[label[contains(text(),'Categoria raiz')]]//button[@role='combobox']");

    private final By expenseCategorySelectTrigger = By.xpath("//div[label[contains(text(),'Categoria')]]//button[@role='combobox']");
    private final By expenseDescriptionInput = By.id("description");
    private final By expenseAmountInput = By.id("amount");
    private final By expenseDateInput = By.xpath("//input[@type='date']");
    private final By expenseAddButton = By.xpath("//button[contains(.,'Adicionar Transação')]");

    private final By transactionTypeSelectTrigger = By.xpath("//div[label[text()='Tipo']]//button[@role='combobox']");
    private final By toastMessage = By.xpath("//li[contains(@class, 'toast')]//div[contains(@class, 'font-semibold')] | //div[contains(@class, 'toast')]//div[contains(@class, 'font-semibold')]");


    public HomePage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    private void selectOption(By selectTrigger, String optionText) {
        WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(selectTrigger));
        trigger.click();

        sleep(500);

        String xpathOption = "//div[@role='option']//span[contains(normalize-space(.), '" + optionText + "')] | //div[@role='option'][contains(normalize-space(.), '" + optionText + "')]";
        By optionLocator = By.xpath(xpathOption);

        try {
            WebElement option = wait.until(ExpectedConditions.presenceOfElementLocated(optionLocator));

            scrollToElement(optionLocator);

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", trigger);
            throw new RuntimeException("Não foi possível encontrar/clicar na opção: " + optionText, e);
        }

        sleep(500);
    }

    public void selectCategoryForAction(String categoryName) {
        selectOption(actionCategorySelectTrigger, categoryName);
    }

    public void createRootCategory(String name) {
        if (driver.findElement(createAsChildCheckbox).isSelected()) {
            click(createAsChildCheckbox);
            sleep(200);
        }

        type(newRootNameInput, name);
        click(createRootButton);
    }

    public void createChildCategory(String parentName, String childName) {
        if (!driver.findElement(createAsChildCheckbox).isSelected()) {
            click(createAsChildCheckbox);
            sleep(200);
        }

        selectOption(categoryParentSelectTrigger, parentName);
        type(newChildNameInput, childName);
        click(createChildButton);
    }

    public void renameSelectedCategory(String newName) {
        type(renameTextInput, newName);
        click(renameButton);
    }

    public void deleteSelectedCategory() {
        click(deleteButton);
    }

    public void moveSelectedCategory(String newParentName) {
        selectOption(moveParentSelectTrigger, newParentName);
        click(moveButton);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    public void openGoalsSection() {
        scrollToElement(goalsSection);
    }

    public void createGoal(String rootCategoryName, String month, String limit) {
        openGoalsSection();
        selectOption(goalRootSelectTrigger, rootCategoryName);
        type(goalMonthInput, month);
        type(goalLimitInput, limit);
        click(goalSaveButton);
    }

    public void createGoalWithoutCategory(String month, String limit) {
        openGoalsSection();
        type(goalMonthInput, month);
        type(goalLimitInput, limit);
        click(goalSaveButton);
    }

    public String getDisplayedGoalLimit() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(goalLimitValue));
        return driver.findElement(goalLimitValue).getText();
    }

    public String getGoalStatus() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(goalStatusText));
        return driver.findElement(goalStatusText).getText();
    }

    public void selectGoalMonth(String month) {
        scrollToElement(goalMonthInput);
        WebElement monthInput = wait.until(ExpectedConditions.visibilityOfElementLocated(goalMonthInput));
        monthInput.clear();
        monthInput.sendKeys(month);

        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(goalSaveButton));
        saveBtn.click();
    }

    public void selectGoalCategory(String categoryName) {
        selectOption(goalCategorySelectTrigger, categoryName);
        wait.until(driver -> !getGoalStatus().isEmpty());
    }

    public void addTransaction(String description, String amount, String type, String date, String categoryName) {
        WebElement descInput = wait.until(ExpectedConditions.visibilityOfElementLocated(expenseDescriptionInput));
        descInput.clear();
        descInput.sendKeys(description);

        WebElement amtInput = wait.until(ExpectedConditions.visibilityOfElementLocated(expenseAmountInput));
        amtInput.clear();
        amtInput.sendKeys(amount);

        selectOption(transactionTypeSelectTrigger, type);

        WebElement dateInput = wait.until(ExpectedConditions.visibilityOfElementLocated(expenseDateInput));
        dateInput.clear();
        dateInput.sendKeys(date);

        selectOption(expenseCategorySelectTrigger, categoryName);

        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(expenseAddButton));
        addButton.click();
    }

    public void addTransaction(String description, String amount, String type, String date) {
        WebElement descInput = wait.until(ExpectedConditions.visibilityOfElementLocated(expenseDescriptionInput));
        descInput.clear();
        descInput.sendKeys(description);

        WebElement amtInput = wait.until(ExpectedConditions.visibilityOfElementLocated(expenseAmountInput));
        amtInput.clear();
        amtInput.sendKeys(amount);

        selectOption(transactionTypeSelectTrigger, type);

        WebElement dateInput = wait.until(ExpectedConditions.visibilityOfElementLocated(expenseDateInput));
        dateInput.clear();
        dateInput.sendKeys(date);

        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(expenseAddButton));
        addButton.click();
    }

    public String getToastText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(toastMessage)).getText();
    }

    public void createExpense(String categoryName, String description, double amount, String date) {
        addTransaction(description, String.valueOf(amount), "Despesa (DEBIT)", date, categoryName);
    }
}
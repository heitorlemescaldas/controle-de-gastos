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
            // ignore
        }
    }
}
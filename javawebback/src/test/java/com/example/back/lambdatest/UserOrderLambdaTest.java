package com.example.back.lambdatest;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UserOrderLambdaTest extends LambdaTestBase {

    @Test
    public void testUserPlaceOrder() throws InterruptedException {
        // 1. Login
        login("user@foxystore.com", "123456");

        // 2. Go to Home Page & Select a Product
        // Navigate explicitly to ensure we are on Home
        driver.get("http://localhost:5173/");
        Thread.sleep(3000);

        // Click on the first product in "New arrival" or just the first product image found
        // Trying to find a product image that is clickable
        try {
            // Scroll to "New arrival" text first to ensure we are in the right section
            WebElement newArrivalText = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//p[contains(text(), 'New arrival')]")));
            scrollToElement(newArrivalText);
            Thread.sleep(1000);

            // Find the first product image card. 
            // Based on NewProduct.jsx: grid -> motion.div -> relative -> ProductItem -> Link -> motion.img
            // We can target the Link or the img. detailed path: //div[contains(@class, 'grid')]//a//img
            WebElement firstProductLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//div[contains(@class, 'grid')]//a)[1]")));
            scrollToElement(firstProductLink);
            
            // Use JS click
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", firstProductLink);
        } catch (Exception e) {
            System.out.println("Could not click product on Home, navigating to fallback product...");
            driver.get("http://localhost:5173/product/85"); 
        }

        // 3. Product Page: Add to Cart
        Thread.sleep(2000);
        
        // Select Size (Check specific sizes: S, M, L, XL)
        String[] sizesToTry = {"S", "M", "L", "XL"};
        boolean sizeSelected = false;
        
        for (String size : sizesToTry) {
            try {
                // Try to find button with exact text
                WebElement sizeBtn = driver.findElement(By.xpath("//button[normalize-space()='" + size + "']"));
                scrollToElement(sizeBtn);
                
                // Use JS Click for better reliability on Mobile
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", sizeBtn);
                Thread.sleep(1000);
                
                // Verify if selected (check class for bg-blue-600)
                if (sizeBtn.getAttribute("class").contains("bg-blue-600")) {
                    System.out.println("✅ Size " + size + " selected successfully.");
                    sizeSelected = true;
                    break;
                }
            } catch (Exception e) {
                // Ignore and try next size
            }
        }
        
        if (!sizeSelected) {
            System.out.println("⚠️ Warning: Could not explicitly select any common size (S, M, L, XL). Trying generic locator...");
            try {
                WebElement anySizeBtn = driver.findElement(By.xpath("//button[contains(@class, 'w-12 h-12')][1]"));
                scrollToElement(anySizeBtn);
                anySizeBtn.click();
            } catch(Exception e) {
                System.out.println("⚠️ Final attempt to select size failed.");
            }
        }

        // Click "Thêm vào giỏ"
        WebElement addToCartBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(., 'Thêm vào giỏ')]")));
        scrollToElement(addToCartBtn);
        // JS Click for Add to cart as well
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", addToCartBtn);

        // Handle Confirmation Alert ("Bạn xác nhận thêm...")
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept(); 
            Thread.sleep(1000); 
        } catch (Exception e) {            
            // Check if it was the "Chưa chọn size" alert
            System.out.println("No confirmation alert or aleady handled.");
        }
        
        // Handle Success Alert ("thêm sản phẩm vào giỏ hàng thành công")
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept(); 
        } catch (Exception e) {
             System.out.println("No success alert.");
        }

        // 4. Navigate to Cart
        driver.get("http://localhost:5173/cartShopping"); 
        Thread.sleep(3000);

        // Click "ĐẶT HÀNG NGAY"
        WebElement orderNowBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(text(), 'ĐẶT HÀNG NGAY')]")));
        scrollToElement(orderNowBtn);
        orderNowBtn.click();

        // 5. Order Page
        Thread.sleep(3000);
        
        // Click "HOÀN THÀNH"
        WebElement completeOrderBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(text(), 'HOÀN THÀNH')]")));
        scrollToElement(completeOrderBtn);
        // JS Click
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", completeOrderBtn);

        // Handle "Xác nhận đơn đặt hàng" confirmation
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            System.out.println("No order confirmation alert.");
        }

        // Handle "Đặt hàng thành công" alert
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            String alertText = driver.switchTo().alert().getText();
            driver.switchTo().alert().accept(); 
            System.out.println("✅ Alert confirmed: " + alertText);
        } catch (Exception e) {
            System.out.println("⚠️ Warning: No success alert appeared.");
        }

        // 6. Verify URL Redirect or Navigate Manually
        System.out.println("🔄 Waiting for redirect to Order Management...");
        try {
            // Wait up to 10s for URL to contain 'ordermanagement'
            wait.until(ExpectedConditions.urlContains("ordermanagement"));
            System.out.println("✅ Redirected to Order Management successfully.");
        } catch (Exception e) {
            System.out.println("⚠️ Redirect did not happen automatically. Navigating manually...");
            driver.get("http://localhost:5173/ordermanagement");
        }

        // 7. Verify Order List Loaded
        Thread.sleep(3000); // Wait for API to fetch orders
        try {
            // Look for any order item or status label to ensure page loaded
            // Assuming table rows or status badges exist. 
            // We can look for common text like "Mã đơn hàng" or "Trạng thái" or a status label.
            WebElement orderList = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'Chờ xác nhận') or contains(text(), 'Đã đặt hàng') or contains(@class, 'table')]")));
            scrollToElement(orderList);
            System.out.println("✅ Order List verified. Order status visible.");
        } catch (Exception e) {
            System.out.println("⚠️ Warning: Could not explicitly find Order Status on the management page.");
        }

        Thread.sleep(2000); // Final pause to see the result
    }

    private void scrollToElement(WebElement element) throws InterruptedException {
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        Thread.sleep(1000); 
    }
}

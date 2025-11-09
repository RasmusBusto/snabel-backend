#!/usr/bin/env python3
"""
Selenium test for the Client Management GUI
Tests the full login and client management flow
"""

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options
import time
import sys

def test_client_management_gui():
    """Test the client management GUI with Selenium"""

    # Setup Chrome with headless option
    chrome_options = Options()
    chrome_options.add_argument('--headless')
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')
    chrome_options.add_argument('--disable-gpu')

    driver = None

    try:
        print("Starting Selenium test...")
        driver = webdriver.Chrome(options=chrome_options)
        driver.set_page_load_timeout(30)
        wait = WebDriverWait(driver, 10)

        # Step 1: Load the page
        print("\n1. Loading http://localhost:8080/clients.html")
        driver.get("http://localhost:8080/clients.html")
        print(f"   Page title: {driver.title}")

        # Step 2: Check login form is visible
        print("\n2. Checking login form...")
        login_form = wait.until(EC.presence_of_element_located((By.ID, "loginForm")))
        print(f"   Login form visible: {login_form.is_displayed()}")

        # Step 3: Fill in credentials
        print("\n3. Entering credentials (snabel/snabeltann)...")
        username_field = driver.find_element(By.ID, "username")
        password_field = driver.find_element(By.ID, "password")

        username_field.send_keys("snabel")
        password_field.send_keys("snabeltann")
        print("   Credentials entered")

        # Step 4: Submit login form
        print("\n4. Submitting login form...")
        login_button = driver.find_element(By.CSS_SELECTOR, "form button[type='submit']")
        login_button.click()

        # Wait a bit for the request to complete
        time.sleep(2)

        # Step 5: Check for errors
        print("\n5. Checking for login errors...")
        try:
            error_div = driver.find_element(By.ID, "loginError")
            if error_div.text and not "hidden" in error_div.get_attribute("class"):
                print(f"   ERROR: {error_div.text}")
                return False
            else:
                print("   No visible error message")
        except:
            print("   No error div found")

        # Step 6: Check if main app is visible
        print("\n6. Checking if main app is visible...")
        try:
            main_app = driver.find_element(By.ID, "mainApp")
            is_visible = main_app.is_displayed()
            print(f"   Main app visible: {is_visible}")

            if is_visible:
                print("   ✅ LOGIN SUCCESSFUL!")

                # Check user display
                user_display = driver.find_element(By.ID, "userDisplay")
                print(f"   User display: {user_display.text}")

                return True
            else:
                print("   ❌ Main app not visible - login failed")

        except Exception as e:
            print(f"   ❌ Could not find main app: {e}")

        # Step 7: Check browser console logs
        print("\n7. Checking browser console logs...")
        logs = driver.get_log('browser')
        if logs:
            print("   Console logs:")
            for log in logs:
                print(f"     [{log['level']}] {log['message']}")
        else:
            print("   No console logs")

        # Step 8: Check network activity
        print("\n8. Getting page source for debugging...")
        with open('/tmp/selenium_page_source.html', 'w') as f:
            f.write(driver.page_source)
        print("   Page source saved to /tmp/selenium_page_source.html")

        # Take screenshot
        driver.save_screenshot('/tmp/selenium_screenshot.png')
        print("   Screenshot saved to /tmp/selenium_screenshot.png")

        return False

    except Exception as e:
        print(f"\n❌ ERROR: {e}")
        import traceback
        traceback.print_exc()

        if driver:
            try:
                driver.save_screenshot('/tmp/selenium_error.png')
                print("Error screenshot saved to /tmp/selenium_error.png")
            except:
                pass

        return False

    finally:
        if driver:
            driver.quit()
            print("\n9. Browser closed")

if __name__ == "__main__":
    print("=" * 60)
    print("Selenium Test: Client Management GUI")
    print("=" * 60)

    success = test_client_management_gui()

    print("\n" + "=" * 60)
    if success:
        print("✅ TEST PASSED")
        sys.exit(0)
    else:
        print("❌ TEST FAILED")
        sys.exit(1)

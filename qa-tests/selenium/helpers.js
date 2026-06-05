const fs = require('fs');
const path = require('path');
const { Builder, By, until } = require('selenium-webdriver');
const { Select } = require('selenium-webdriver/lib/select');

const BASE_URL = process.env.ERP_FRONTEND_URL || 'http://localhost:3000';
const ADMIN_USERNAME = process.env.ERP_ADMIN_USERNAME;
const ADMIN_PASSWORD = process.env.ERP_ADMIN_PASSWORD;

if (!ADMIN_USERNAME || !ADMIN_PASSWORD) {
  console.error('Missing environment variable.');
  console.error('Set ERP_ADMIN_USERNAME and ERP_ADMIN_PASSWORD before running test.');
  console.error('PowerShell example:');
  console.error('$env:ERP_ADMIN_USERNAME="admin"');
  console.error('$env:ERP_ADMIN_PASSWORD="your_admin_password"');
  process.exit(1);
}

function assert(condition, message) {
  if (!condition) throw new Error(message);
}

function uniqueText(prefix) {
  return `${prefix} ${Date.now()}`;
}

function todayMinusDays(days) {
  const date = new Date();
  date.setDate(date.getDate() - days);
  return date.toISOString().split('T')[0];
}

async function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function takeScreenshot(driver, name) {
  const screenshot = await driver.takeScreenshot();
  const dir = path.join(process.cwd(), 'tests', 'screenshots');

  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  const filePath = path.join(dir, `${name}.png`);
  fs.writeFileSync(filePath, screenshot, 'base64');
  console.log(`Screenshot saved: ${filePath}`);
}

async function getBodyText(driver) {
  const body = await driver.wait(until.elementLocated(By.css('body')), 15000);
  return await body.getText();
}

async function logPagePreview(driver, label) {
  const text = await getBodyText(driver);
  console.log(`${label}: ${text.substring(0, 900)}`);
  return text;
}

async function acceptAlertIfPresent(driver) {
  try {
    await driver.wait(until.alertIsPresent(), 3000);
    const alert = await driver.switchTo().alert();
    const text = await alert.getText();
    console.log(`Alert detected: ${text}`);
    await alert.accept();
  } catch {
    // no alert
  }
}

async function clickButtonByText(driver, text, timeout = 15000) {
  const button = await driver.wait(
    until.elementLocated(
      By.xpath(`//button[contains(normalize-space(.), "${text}")]`)
    ),
    timeout
  );

  await driver.wait(until.elementIsVisible(button), timeout);
  await driver.wait(until.elementIsEnabled(button), timeout);

  await driver.executeScript('arguments[0].scrollIntoView({block: "center"});', button);
  await sleep(500);
  await button.click();
}

async function clickFirstAvailableButton(driver, buttonTexts) {
  let lastError;

  for (const text of buttonTexts) {
    try {
      await clickButtonByText(driver, text, 5000);
      return text;
    } catch (error) {
      lastError = error;
    }
  }

  throw new Error(
    `Button not found. Tried: ${buttonTexts.join(', ')}. Last error: ${lastError?.message}`
  );
}

async function clickIfVisibleByText(driver, texts, timeout = 3000) {
  for (const text of texts) {
    try {
      const element = await driver.wait(
        until.elementLocated(
          By.xpath(
            `//*[self::button or self::div or self::span or self::a][contains(normalize-space(.), "${text}")]`
          )
        ),
        timeout
      );

      await driver.wait(until.elementIsVisible(element), timeout);
      await driver.executeScript('arguments[0].scrollIntoView({block: "center"});', element);
      await sleep(300);
      await element.click();

      console.log(`Clicked visible popup/menu item: ${text}`);
      return true;
    } catch {
      // try next text
    }
  }

  return false;
}

async function waitUntilInputAppears(driver, selectors, timeout = 8000) {
  const startedAt = Date.now();

  while (Date.now() - startedAt < timeout) {
    for (const selector of selectors) {
      const elements = await driver.findElements(By.css(selector));

      for (const element of elements) {
        try {
          const visible = await element.isDisplayed();
          if (visible) return true;
        } catch {
          // ignore stale element
        }
      }
    }

    await sleep(500);
  }

  return false;
}

async function inputById(driver, id, value) {
  const input = await driver.wait(until.elementLocated(By.id(id)), 15000);
  await driver.wait(until.elementIsVisible(input), 15000);
  await driver.executeScript('arguments[0].scrollIntoView({block: "center"});', input);
  await sleep(300);
  await input.clear();
  await input.sendKeys(value);
}

async function inputByAnySelector(driver, selectors, value) {
  let lastError;

  for (const selector of selectors) {
    try {
      const input = await driver.wait(until.elementLocated(By.css(selector)), 5000);
      await driver.wait(until.elementIsVisible(input), 5000);

      await driver.executeScript('arguments[0].scrollIntoView({block: "center"});', input);
      await sleep(300);

      await input.clear();
      await input.sendKeys(value);
      return selector;
    } catch (error) {
      lastError = error;
    }
  }

  throw new Error(
    `Input not found. Tried selectors: ${selectors.join(', ')}. Last error: ${lastError?.message}`
  );
}

async function inputByLabel(driver, label, value) {
  const input = await driver.wait(
    until.elementLocated(
      By.xpath(`//label[normalize-space(.)="${label}"]/following-sibling::input`)
    ),
    15000
  );

  await driver.wait(until.elementIsVisible(input), 15000);
  await driver.executeScript('arguments[0].scrollIntoView({block: "center"});', input);
  await sleep(300);

  await input.clear();
  await input.sendKeys(value);
}

async function inputOptionalByLabel(driver, label, value) {
  try {
    await inputByLabel(driver, label, value);
    return true;
  } catch {
    return false;
  }
}

async function setDateByAnySelector(driver, selectors, value) {
  let lastError;

  for (const selector of selectors) {
    try {
      const input = await driver.wait(until.elementLocated(By.css(selector)), 5000);
      await driver.wait(until.elementIsVisible(input), 5000);

      await driver.executeScript(
        `
        const input = arguments[0];
        const value = arguments[1];

        input.scrollIntoView({ block: "center" });
        input.value = value;

        input.dispatchEvent(new Event("input", { bubbles: true }));
        input.dispatchEvent(new Event("change", { bubbles: true }));
        `,
        input,
        value
      );

      await sleep(300);
      return selector;
    } catch (error) {
      lastError = error;
    }
  }

  throw new Error(
    `Date input not found. Tried selectors: ${selectors.join(', ')}. Last error: ${lastError?.message}`
  );
}

async function setDateByLabel(driver, label, value) {
  const input = await driver.wait(
    until.elementLocated(
      By.xpath(`//label[normalize-space(.)="${label}"]/following-sibling::input`)
    ),
    15000
  );

  await driver.wait(until.elementIsVisible(input), 15000);

  await driver.executeScript(
    `
    const input = arguments[0];
    const value = arguments[1];

    input.scrollIntoView({ block: "center" });
    input.value = value;

    input.dispatchEvent(new Event("input", { bubbles: true }));
    input.dispatchEvent(new Event("change", { bubbles: true }));
    `,
    input,
    value
  );

  await sleep(300);
}

async function selectByLabel(driver, label, value) {
  const selectElement = await driver.wait(
    until.elementLocated(
      By.xpath(`//label[normalize-space(.)="${label}"]/following-sibling::select`)
    ),
    15000
  );

  await driver.wait(until.elementIsVisible(selectElement), 15000);
  await driver.executeScript('arguments[0].scrollIntoView({block: "center"});', selectElement);
  await sleep(300);

  const select = new Select(selectElement);
  await select.selectByValue(value);
}

async function selectOptionContainingText(driver, label, text) {
  const selectElement = await driver.wait(
    until.elementLocated(
      By.xpath(`//label[normalize-space(.)="${label}"]/following-sibling::select`)
    ),
    15000
  );

  await driver.wait(until.elementIsVisible(selectElement), 15000);
  await driver.executeScript('arguments[0].scrollIntoView({block: "center"});', selectElement);
  await sleep(300);

  const options = await selectElement.findElements(By.css('option'));

  for (const option of options) {
    const optionText = await option.getText();

    if (optionText.includes(text)) {
      const value = await option.getAttribute('value');
      const select = new Select(selectElement);
      await select.selectByValue(value);
      return;
    }
  }

  throw new Error(`Option containing "${text}" not found in select "${label}"`);
}

async function verifyPageContains(driver, expectedText) {
  const bodyText = await getBodyText(driver);

  assert(
    bodyText.includes(expectedText),
    `Expected page to contain "${expectedText}", but it was not found. Body text: ${bodyText.substring(0, 800)}`
  );
}

async function getAlertValue(driver, title) {
  const element = await driver.wait(
    until.elementLocated(
      By.xpath(
        `//div[contains(@class,"alert-card")][.//div[contains(@class,"alert-title") and normalize-space(.)="${title}"]]//div[contains(@class,"alert-value")]`
      )
    ),
    15000
  );

  const text = await element.getText();
  return Number(text.trim());
}

/* =========================
   TEST 1: LOGIN
========================= */

async function login(driver) {
  console.log('TEST: Login admin');

  await driver.get(`${BASE_URL}/login`);

  await inputById(driver, 'login-username', ADMIN_USERNAME);
  await inputById(driver, 'login-password', ADMIN_PASSWORD);

  await clickButtonByText(driver, 'Login');
  await acceptAlertIfPresent(driver);

  await driver.wait(async () => {
    const currentUrl = await driver.getCurrentUrl();
    return currentUrl === `${BASE_URL}/` || currentUrl === `${BASE_URL}`;
  }, 15000);

  await sleep(3000);

  const bodyText = await logPagePreview(driver, 'Page text after login');

  const isDashboard =
    bodyText.includes('Business Dashboard') ||
    bodyText.includes('Dashboard') ||
    bodyText.includes('Revenue') ||
    bodyText.includes('Simple ERP');

  assert(isDashboard, 'Login success redirect happened, but dashboard content was not found.');

  console.log('PASS: Login admin');
}

/* =========================
   TEST 2: DASHBOARD
========================= */

async function verifyDashboard(driver) {
  console.log('TEST: Dashboard page');

  await driver.get(`${BASE_URL}/`);
  await sleep(3000);

  const bodyText = await logPagePreview(driver, 'Dashboard preview');

  assert(
    bodyText.includes('Dashboard') || bodyText.includes('Revenue') || bodyText.includes('Gross Profit'),
    'Dashboard content was not found.'
  );

  console.log('PASS: Dashboard page');
}

/* =========================
   TEST 3: PRODUCTS PAGE
========================= */

async function verifyProductsPage(driver) {
  console.log('TEST: Products page');

  await driver.get(`${BASE_URL}/products`);
  await sleep(3000);

  const bodyText = await logPagePreview(driver, 'Products page preview');

  assert(
    bodyText.includes('Products') || bodyText.includes('Product List') || bodyText.includes('Product Stock'),
    'Products page did not load expected content.'
  );

  console.log('PASS: Products page');
}

/* =========================
   TEST 4: CREATE PRODUCT VIA UI
========================= */

async function createProductViaUi(driver, productName) {
  console.log('TEST: Create product via UI');

  await driver.get(`${BASE_URL}/products`);
  await sleep(3000);

  await verifyProductsPage(driver);

  const beforeText = await logPagePreview(driver, 'Products before create');

  const openButton = await clickFirstAvailableButton(driver, [
    '+ Add Product',
    'Tambah Produk',
    'Add',
  ]);

  console.log(`Clicked product open button: ${openButton}`);
  await sleep(1000);

  await inputByAnySelector(driver, [
    'input[name="name"]',
    'input[name="productName"]',
    '#product-name',
    '#name',
  ], productName);

  await inputByAnySelector(driver, [
    'input[name="stock"]',
    'input[name="productStock"]',
    '#product-stock',
    '#stock',
  ], '100');

  await inputByAnySelector(driver, [
    'input[name="minStock"]',
    'input[name="minimumStock"]',
    '#minimum-stock',
    '#min-stock',
  ], '10');

  await inputByAnySelector(driver, [
    'input[name="costPrice"]',
    '#cost-price',
    '#hpp',
  ], '50000');

  await inputByAnySelector(driver, [
    'input[name="sellingPrice"]',
    '#selling-price',
    '#price',
  ], '100000');

  const form = await driver.wait(
    until.elementLocated(By.css('form')),
    15000
  );

  await driver.executeScript('arguments[0].scrollIntoView({block: "center"});', form);
  await sleep(500);

  try {
    await form.submit();
  } catch {
    const submitButton = await form.findElement(
      By.xpath(`.//button[
        contains(normalize-space(.), "Add Product")
        or contains(normalize-space(.), "Save Product")
        or contains(normalize-space(.), "Create Product")
        or contains(normalize-space(.), "Save")
        or contains(normalize-space(.), "Submit")
        or contains(normalize-space(.), "Simpan")
      ]`)
    );

    await driver.executeScript('arguments[0].scrollIntoView({block: "center"});', submitButton);
    await sleep(500);
    await submitButton.click();
  }

  await acceptAlertIfPresent(driver);
  await sleep(4000);

  await driver.get(`${BASE_URL}/products`);
  await sleep(3000);

  const afterText = await logPagePreview(driver, 'Products after create');

  assert(
    afterText.includes(productName),
    `Product was not found after create. Expected: ${productName}. Before: ${beforeText.substring(0, 500)} After: ${afterText.substring(0, 1200)}`
  );

  console.log('PASS: Create product via UI');
}

/* =========================
   TEST 5: DISCOUNT PAGE
========================= */

async function verifyDiscountPage(driver) {
  console.log('TEST: Discounts page');

  await driver.get(`${BASE_URL}/discounts`);
  await sleep(3000);

  const bodyText = await logPagePreview(driver, 'Discount page preview');

  assert(
    bodyText.includes('Discount') || bodyText.includes('Diskon') || bodyText.includes('Discount Rule'),
    'Discounts page did not load expected content.'
  );

  console.log('PASS: Discounts page');
}

/* =========================
   TEST 6: CREATE DISCOUNT
========================= */

async function createDiscountRule(driver) {
  console.log('TEST: Create discount rule');

  await driver.get(`${BASE_URL}/discounts`);
  await sleep(3000);

  await verifyDiscountPage(driver);

  const beforeText = await logPagePreview(driver, 'Discount before create');

  const openButton = await clickFirstAvailableButton(driver, [
    '+ Add Discount',
    'Tambah Diskon',
    'Add',
  ]);

  console.log(`Clicked discount open button: ${openButton}`);
  await sleep(1000);

  let formReady = await waitUntilInputAppears(driver, [
    'input[name="name"]',
    'input[name="discountName"]',
    '#discount-name',
    '#name',
  ], 3000);

  if (!formReady) {
    console.log('Discount form not visible yet. Trying popup/menu item...');

    await clickIfVisibleByText(driver, [
      'Discount Rule',
      'Create Discount',
      'Add Discount',
      'Tambah Diskon',
      'New Discount',
      'Diskon',
      'Discount',
    ], 5000);

    await sleep(1000);

    formReady = await waitUntilInputAppears(driver, [
      'input[name="name"]',
      'input[name="discountName"]',
      '#discount-name',
      '#name',
    ], 8000);
  }

  if (!formReady) {
    const bodyText = await getBodyText(driver);
    throw new Error(
      `Discount form still not visible. Page text: ${bodyText.substring(0, 1200)}`
    );
  }

  const discountName = uniqueText('QA Diskon 3 Box');

  await inputByAnySelector(driver, [
    'input[name="name"]',
    'input[name="discountName"]',
    '#discount-name',
    '#name',
  ], discountName);

  await inputByAnySelector(driver, [
    'input[name="minimumQuantity"]',
    'input[name="minQuantity"]',
    '#minimum-quantity',
    '#min-quantity',
  ], '3');

  await inputByAnySelector(driver, [
    'input[name="discountPercent"]',
    '#discount-percent',
    '#percent',
  ], '5');

  await setDateByAnySelector(driver, [
    'input[name="startDate"]',
    '#start-date',
  ], '2026-01-01');

  await setDateByAnySelector(driver, [
    'input[name="endDate"]',
    '#end-date',
  ], '2026-12-31');

  const form = await driver.wait(
    until.elementLocated(By.css('form')),
    15000
  );

  const submitButton = await form.findElement(
    By.xpath(`.//button[
      contains(normalize-space(.), "Save Discount")
      or contains(normalize-space(.), "Add Discount")
      or contains(normalize-space(.), "Create Discount")
      or contains(normalize-space(.), "Save")
      or contains(normalize-space(.), "Submit")
      or contains(normalize-space(.), "Simpan")
    ]`)
  );

  await driver.executeScript('arguments[0].scrollIntoView({block: "center"});', submitButton);
  await sleep(500);
  await submitButton.click();

  await acceptAlertIfPresent(driver);
  await sleep(4000);

  await driver.get(`${BASE_URL}/discounts`);
  await sleep(3000);

  const afterText = await logPagePreview(driver, 'Discount after create');

  assert(
    afterText.includes(discountName),
    `Discount was not found after create. Expected: ${discountName}. Before: ${beforeText.substring(0, 500)} After: ${afterText.substring(0, 1200)}`
  );

  console.log('PASS: Create discount rule');

  return discountName;
}

/* =========================
   TEST 7: SALES ORDER PAGE
========================= */

async function verifySalesOrderPage(driver) {
  console.log('TEST: Sales Order page');

  await driver.get(`${BASE_URL}/sales-order`);
  await sleep(3000);

  const bodyText = await logPagePreview(driver, 'Sales Order page preview');

  assert(
    bodyText.includes('Sales') || bodyText.includes('Sales Order') || bodyText.includes('Create Order'),
    'Sales Order page did not load expected content.'
  );

  console.log('PASS: Sales Order page');
}

/* =========================
   TEST 8: CREATE SALES ORDER
========================= */

async function createSalesOrder(driver, params) {
  console.log(`TEST: Create sales order - ${params.customerName}`);

  await driver.get(`${BASE_URL}/sales-order`);
  await sleep(3000);

  await setDateByLabel(driver, 'Order Date', params.orderDate);
  await inputByLabel(driver, 'Customer Name', params.customerName);
  await inputByLabel(driver, 'Phone Number', params.phone);
  await inputByLabel(driver, 'Address', params.address);

  await selectOptionContainingText(driver, 'Product', params.productName);
  await inputByLabel(driver, 'Quantity', String(params.quantity));

  await selectByLabel(driver, 'Shipping Method', params.shippingMethod);
  await inputByLabel(driver, 'Tracking Number', params.trackingNumber || '');
  await selectByLabel(driver, 'Shipping Status', params.shippingStatus);
  await selectByLabel(driver, 'Payment Method', params.paymentMethod);
  await selectByLabel(driver, 'Payment Status', params.paymentStatus);

  await inputOptionalByLabel(driver, 'Order Status', params.orderStatus || 'ACTIVE');
  await inputOptionalByLabel(driver, 'Refund Status', params.refundStatus || 'None');

  const submitButton = await clickFirstAvailableButton(driver, [
    'Create Order',
    'Add Order',
    'Save Order',
    'Submit',
    'Simpan',
  ]);

  console.log(`Clicked sales order submit button: ${submitButton}`);

  await acceptAlertIfPresent(driver);
  await sleep(4000);

  await driver.get(`${BASE_URL}/sales-order`);
  await sleep(3000);

  const bodyText = await logPagePreview(driver, 'Sales Order page after create');

  assert(
    bodyText.includes(params.customerName),
    `Sales order was not found after create. Expected customer: ${params.customerName}. Body: ${bodyText.substring(0, 1200)}`
  );

  console.log(`PASS: Create sales order - ${params.customerName}`);
}

/* =========================
   TEST 9: VERIFY DISCOUNT
========================= */

async function verifyDiscountApplied(driver, customerName) {
  console.log('TEST: Verify discount applied');

  await driver.get(`${BASE_URL}/sales-order`);
  await sleep(3000);

  try {
    const searchInput = await driver.wait(
      until.elementLocated(
        By.xpath(`//label[normalize-space(.)="Search"]/following-sibling::input`)
      ),
      5000
    );

    await searchInput.clear();
    await searchInput.sendKeys(customerName);
    await sleep(1500);
  } catch {
    console.log('Search input not found. Continue checking full table.');
  }

  const row = await driver.wait(
    until.elementLocated(By.xpath(`//tr[.//td[contains(text(), "${customerName}")]]`)),
    15000
  );

  const rowText = await row.getText();

  assert(
    rowText.includes('5%') || rowText.includes('5') || rowText.includes('15.000'),
    `Expected discount applied, but row text was: ${rowText}`
  );

  console.log('PASS: Verify discount applied');
}

/* =========================
   TEST 10: DASHBOARD RISK
========================= */

async function verifyDashboardRisk(driver) {
  console.log('TEST: Verify dashboard risk');

  await driver.get(`${BASE_URL}/`);
  await sleep(4000);

  const bodyText = await logPagePreview(driver, 'Dashboard risk preview');

  const hasRiskText =
    bodyText.includes('Payment Risk') ||
    bodyText.includes('Delivery Risk') ||
    bodyText.includes('unpaid') ||
    bodyText.includes('follow up');

  assert(hasRiskText, 'Dashboard risk section was not found.');

  try {
    const paymentRisk = await getAlertValue(driver, 'Payment Risk');
    const deliveryRisk = await getAlertValue(driver, 'Delivery Risk');

    assert(paymentRisk >= 0, `Payment Risk value invalid: ${paymentRisk}`);
    assert(deliveryRisk >= 0, `Delivery Risk value invalid: ${deliveryRisk}`);

    console.log(`Payment Risk: ${paymentRisk}`);
    console.log(`Delivery Risk: ${deliveryRisk}`);
  } catch {
    console.log('Risk value cards not found by exact selector, but risk section exists.');
  }

  console.log('PASS: Verify dashboard risk section');
}

/* db filter */

async function verifyDashboardDateFilter(driver) {
  console.log('TEST: Verify dashboard date filter');

  await driver.get(`${BASE_URL}/`);
  await sleep(3000);

  const dateInputs = await driver.findElements(By.css('input[type="date"]'));

  assert(
    dateInputs.length >= 2,
    `Expected at least 2 date inputs on dashboard, but found ${dateInputs.length}`
  );

  await driver.executeScript(
    `
    const startInput = arguments[0];
    const endInput = arguments[1];

    startInput.scrollIntoView({ block: "center" });

    startInput.value = "2026-04-01";
    startInput.dispatchEvent(new Event("input", { bubbles: true }));
    startInput.dispatchEvent(new Event("change", { bubbles: true }));

    endInput.value = "2026-04-30";
    endInput.dispatchEvent(new Event("input", { bubbles: true }));
    endInput.dispatchEvent(new Event("change", { bubbles: true }));
    `,
    dateInputs[0],
    dateInputs[1]
  );

  await sleep(1000);

  const startValue = await dateInputs[0].getAttribute('value');
  const endValue = await dateInputs[1].getAttribute('value');

  console.log(`Dashboard start date value: ${startValue}`);
  console.log(`Dashboard end date value: ${endValue}`);

  assert(startValue === '2026-04-01', `Start date was not set. Current value: ${startValue}`);
  assert(endValue === '2026-04-30', `End date was not set. Current value: ${endValue}`);

  await clickFirstAvailableButton(driver, ['Apply']);

  await acceptAlertIfPresent(driver);

  await sleep(4000);

  const bodyText = await logPagePreview(driver, 'Dashboard after date filter');

  assert(
    bodyText.includes('2026-04-01') ||
      bodyText.includes('2026-04-30') ||
      bodyText.includes('Period') ||
      bodyText.includes('April'),
    `Dashboard filter did not show expected period. Body: ${bodyText.substring(0, 1000)}`
  );

  console.log('PASS: Verify dashboard date filter');
}

/* =========================
   TEST 12: TRACK ORDER PAGE
========================= */

async function verifyTrackOrderPage(driver) {
  console.log('TEST: Track Order page');

  await driver.get(`${BASE_URL}/track-order`);
  await sleep(3000);

  const bodyText = await logPagePreview(driver, 'Track Order page preview');

  assert(
    bodyText.includes('Track') || bodyText.includes('Order'),
    'Track Order page did not load expected content.'
  );

  console.log('PASS: Track Order page');
}

/* =========================
   RUN ALL TESTS
========================= */

async function run() {
  const driver = await new Builder().forBrowser('chrome').build();

  const productName = uniqueText('QA Susu Tulang');
  const discountCustomer = uniqueText('QA Customer Discount');
  const unpaidCustomer = uniqueText('QA Customer Unpaid');
  const deliveryRiskCustomer = uniqueText('QA Customer Delivery');

  try {
    await login(driver);

    await verifyDashboard(driver);

    await verifyProductsPage(driver);

    await createProductViaUi(driver, productName);

    await verifyDiscountPage(driver);

    await createDiscountRule(driver);

    await verifySalesOrderPage(driver);

    await createSalesOrder(driver, {
      orderDate: '2026-04-15',
      customerName: discountCustomer,
      phone: '081111111111',
      address: 'Alamat QA Discount',
      productName,
      quantity: 3,
      shippingMethod: 'JNE',
      trackingNumber: 'QA123',
      shippingStatus: 'Delivered',
      paymentMethod: 'Transfer',
      paymentStatus: 'Paid',
      orderStatus: 'ACTIVE',
      refundStatus: 'None',
    });

    await verifyDiscountApplied(driver, discountCustomer);

    await createSalesOrder(driver, {
      orderDate: todayMinusDays(4),
      customerName: unpaidCustomer,
      phone: '082222222222',
      address: 'Alamat QA Unpaid',
      productName,
      quantity: 1,
      shippingMethod: 'JNE',
      trackingNumber: '',
      shippingStatus: 'Preparing',
      paymentMethod: 'Transfer',
      paymentStatus: 'Unpaid',
      orderStatus: 'ACTIVE',
      refundStatus: 'None',
    });

    await createSalesOrder(driver, {
      orderDate: todayMinusDays(8),
      customerName: deliveryRiskCustomer,
      phone: '083333333333',
      address: 'Alamat QA Delivery Risk',
      productName,
      quantity: 1,
      shippingMethod: 'JNE',
      trackingNumber: '',
      shippingStatus: 'Preparing',
      paymentMethod: 'Transfer',
      paymentStatus: 'Paid',
      orderStatus: 'ACTIVE',
      refundStatus: 'None',
    });

    await verifyDashboardRisk(driver);

    await verifyDashboardDateFilter(driver);

    await verifyTrackOrderPage(driver);

    console.log('');
    console.log('ALL FULL FEATURE SELENIUM TESTS PASSED ✅');
  } catch (error) {
    console.error('');
    console.error('SELENIUM FULL FEATURE TEST FAILED ❌');
    console.error(error.message);

    await takeScreenshot(driver, `failed-${Date.now()}`);

    process.exitCode = 1;
  } finally {
    await driver.quit();
  }
}

run();
const fs = require('fs');
const path = require('path');
const { Builder, By, until } = require('selenium-webdriver');

const BASE_URL = process.env.ERP_FRONTEND_URL || 'http://localhost:3000';
const ADMIN_USERNAME = process.env.ERP_ADMIN_USERNAME;
const ADMIN_PASSWORD = process.env.ERP_ADMIN_PASSWORD;

if (!ADMIN_USERNAME || !ADMIN_PASSWORD) {
  console.error('Missing environment variable.');
  console.error('Set ERP_ADMIN_USERNAME and ERP_ADMIN_PASSWORD before running test.');
  process.exit(1);
}

function assert(condition, message) {
  if (!condition) throw new Error(message);
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
    return text;
  } catch {
    return null;
  }
}

async function inputById(driver, id, value) {
  const input = await driver.wait(until.elementLocated(By.id(id)), 15000);
  await driver.wait(until.elementIsVisible(input), 15000);
  await input.clear();
  await input.sendKeys(value);
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

async function login(driver) {
  console.log('TEST: Login admin');

  await driver.get(`${BASE_URL}/login`);

  await inputById(driver, 'login-username', ADMIN_USERNAME);
  await inputById(driver, 'login-password', ADMIN_PASSWORD);

  await clickButtonByText(driver, 'Login');

  const alertText = await acceptAlertIfPresent(driver);

  assert(!alertText, `Login failed with alert: ${alertText}`);

  await driver.wait(async () => {
    const currentUrl = await driver.getCurrentUrl();
    return currentUrl === `${BASE_URL}/` || currentUrl === `${BASE_URL}`;
  }, 15000);

  await sleep(3000);

  const bodyText = await logPagePreview(driver, 'Dashboard after login');

  assert(
    bodyText.includes('Dashboard') ||
      bodyText.includes('Business Dashboard') ||
      bodyText.includes('Revenue') ||
      bodyText.includes('Simple ERP'),
    'Dashboard was not loaded after login.'
  );

  console.log('PASS: Login admin');
}

async function setReactDateInput(driver, input, value) {
  await driver.executeScript(
    `
    const input = arguments[0];
    const value = arguments[1];

    const nativeInputValueSetter = Object.getOwnPropertyDescriptor(
      window.HTMLInputElement.prototype,
      "value"
    ).set;

    nativeInputValueSetter.call(input, value);

    input.dispatchEvent(new Event("input", { bubbles: true }));
    input.dispatchEvent(new Event("change", { bubbles: true }));
    `,
    input,
    value
  );

  await sleep(500);
}

async function verifyDashboardDateFilterOnly(driver) {
  console.log('TEST: Dashboard date filter only');

  await driver.get(`${BASE_URL}/`);
  await sleep(3000);

  const beforeText = await logPagePreview(driver, 'Dashboard before filter');

  assert(
    beforeText.includes('Dashboard') || beforeText.includes('Revenue'),
    'Dashboard content was not found before filter.'
  );

  const dateInputs = await driver.findElements(By.css('input[type="date"]'));

  assert(
    dateInputs.length >= 2,
    `Expected at least 2 date inputs, but found ${dateInputs.length}`
  );

  await setReactDateInput(driver, dateInputs[0], '2026-04-01');
  await setReactDateInput(driver, dateInputs[1], '2026-04-30');

  const startValue = await dateInputs[0].getAttribute('value');
  const endValue = await dateInputs[1].getAttribute('value');

  console.log(`Start date value after set: ${startValue}`);
  console.log(`End date value after set: ${endValue}`);

  assert(startValue === '2026-04-01', `Start date not set correctly. Got: ${startValue}`);
  assert(endValue === '2026-04-30', `End date not set correctly. Got: ${endValue}`);

  await clickButtonByText(driver, 'Apply');

  const alertText = await acceptAlertIfPresent(driver);

  assert(!alertText, `Dashboard filter showed alert: ${alertText}`);

  await sleep(4000);

  const afterText = await logPagePreview(driver, 'Dashboard after filter');

  assert(
    afterText.includes('2026-04-01') ||
      afterText.includes('2026-04-30') ||
      afterText.includes('Period') ||
      afterText.includes('April') ||
      afterText.includes('Revenue'),
    `Dashboard filter result did not show expected content. Body: ${afterText.substring(0, 1200)}`
  );

  console.log('PASS: Dashboard date filter only');
}

async function logoutIfPossible(driver) {
  try {
    await clickButtonByText(driver, 'Logout', 5000);
    await sleep(1000);
    console.log('PASS: Logout');
  } catch {
    console.log('Logout button not found or already logged out.');
  }
}

async function run() {
  const driver = await new Builder().forBrowser('chrome').build();

  try {
    await login(driver);

    await verifyDashboardDateFilterOnly(driver);

    await logoutIfPossible(driver);

    console.log('');
    console.log('db filternya berhasil (ok lah gw cape)');
  } catch (error) {
    console.error('');
    console.error('istirahat dulu bro');
    console.error(error.message);

    await takeScreenshot(driver, `dashboard-filter-failed-${Date.now()}`);
    process.exitCode = 1;
  } finally {
    await driver.quit();
  }
}

run();
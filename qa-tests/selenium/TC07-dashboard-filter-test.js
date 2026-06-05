const {
  BASE_URL,
  By,
  assert,
  sleep,
  logPagePreview,
  login,
  logoutIfPossible,
  clickFirstAvailableButton,
  acceptAlertIfPresent,
  runTestCase,
} = require('./helpers');

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

async function testDashboardFilter(driver) {
  await login(driver);

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

  await clickFirstAvailableButton(driver, ['Apply']);

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
    'Dashboard filter result did not show expected content.'
  );

  await logoutIfPossible(driver);
}

runTestCase('TC07 Dashboard Filter', testDashboardFilter);
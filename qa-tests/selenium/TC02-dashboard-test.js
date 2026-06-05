const {
  BASE_URL,
  assert,
  sleep,
  logPagePreview,
  login,
  logoutIfPossible,
  runTestCase,
} = require('./helpers');

async function testDashboard(driver) {
  await login(driver);

  await driver.get(`${BASE_URL}/`);
  await sleep(3000);

  const bodyText = await logPagePreview(driver, 'Dashboard preview');

  assert(
    bodyText.includes('Dashboard') ||
      bodyText.includes('Revenue') ||
      bodyText.includes('Gross Profit'),
    'Dashboard content was not found.'
  );

  await logoutIfPossible(driver);
}

runTestCase('TC02 Dashboard Page', testDashboard);
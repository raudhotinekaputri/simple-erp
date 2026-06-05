const {
  BASE_URL,
  assert,
  sleep,
  logPagePreview,
  login,
  logoutIfPossible,
  runTestCase,
} = require('./helpers');

async function testDashboardRisk(driver) {
  await login(driver);

  await driver.get(`${BASE_URL}/`);
  await sleep(4000);

  const bodyText = await logPagePreview(driver, 'Dashboard risk preview');

  const hasRiskText =
    bodyText.includes('Payment Risk') ||
    bodyText.includes('Delivery Risk') ||
    bodyText.includes('unpaid') ||
    bodyText.includes('follow up');

  assert(hasRiskText, 'Dashboard risk section was not found.');

  await logoutIfPossible(driver);
}

runTestCase('TC06 Dashboard Risk', testDashboardRisk);
const {
  BASE_URL,
  assert,
  sleep,
  logPagePreview,
  login,
  logoutIfPossible,
  runTestCase,
} = require('./helpers');

async function testTrackOrderPage(driver) {
  await login(driver);

  await driver.get(`${BASE_URL}/track-order`);
  await sleep(3000);

  const bodyText = await logPagePreview(driver, 'Track Order page preview');

  assert(
    bodyText.includes('Track') || bodyText.includes('Order'),
    'Track Order page did not load expected content.'
  );

  await logoutIfPossible(driver);
}

runTestCase('TC08 Track Order Page', testTrackOrderPage);
const {
  BASE_URL,
  assert,
  uniqueText,
  sleep,
  logPagePreview,
  login,
  logoutIfPossible,
  clickFirstAvailableButton,
  inputByAnySelector,
  setDateByAnySelector,
  runTestCase,
} = require('./helpers');

async function testDiscount(driver) {
  await login(driver);

  await driver.get(`${BASE_URL}/discounts`);
  await sleep(3000);

  const pageText = await logPagePreview(driver, 'Discount page preview');

  assert(
    pageText.includes('Discount') ||
      pageText.includes('Diskon') ||
      pageText.includes('Discount Rule'),
    'Discounts page did not load expected content.'
  );

  const discountName = uniqueText('QA Diskon 3 Box');

  await clickFirstAvailableButton(driver, [
    '+ Add Discount',
    'Tambah Diskon',
    'Add',
  ]);

  await sleep(1000);

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

  const form = await driver.findElement({ css: 'form' });

  try {
    await form.submit();
  } catch {
    await clickFirstAvailableButton(driver, [
      'Save Discount',
      'Add Discount',
      'Create Discount',
      'Save',
      'Submit',
      'Simpan',
    ]);
  }

  await sleep(4000);

  await driver.get(`${BASE_URL}/discounts`);
  await sleep(3000);

  const afterText = await logPagePreview(driver, 'Discount after create');

  assert(
    afterText.includes(discountName),
    `Discount was not found after create. Expected: ${discountName}`
  );

  await logoutIfPossible(driver);
}

runTestCase('TC04 Discount Create', testDiscount);
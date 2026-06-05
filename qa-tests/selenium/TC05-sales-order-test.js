const {
  BASE_URL,
  assert,
  uniqueText,
  sleep,
  logPagePreview,
  login,
  logoutIfPossible,
  setDateByLabel,
  inputByLabel,
  inputOptionalByLabel,
  selectByLabel,
  selectOptionContainingText,
  clickFirstAvailableButton,
  runTestCase,
} = require('./helpers');

async function testSalesOrder(driver) {
  await login(driver);

  await driver.get(`${BASE_URL}/sales-order`);
  await sleep(3000);

  const pageText = await logPagePreview(driver, 'Sales Order page preview');

  assert(
    pageText.includes('Sales') ||
      pageText.includes('Sales Order') ||
      pageText.includes('Create Order'),
    'Sales Order page did not load expected content.'
  );

  const customerName = uniqueText('QA Customer Sales');

  await setDateByLabel(driver, 'Order Date', '2026-04-15');
  await inputByLabel(driver, 'Customer Name', customerName);
  await inputByLabel(driver, 'Phone Number', '081111111111');
  await inputByLabel(driver, 'Address', 'Alamat QA Sales Order');

  await selectOptionContainingText(driver, 'Product', 'Susu');
  await inputByLabel(driver, 'Quantity', '1');

  await selectByLabel(driver, 'Shipping Method', 'JNE');
  await inputByLabel(driver, 'Tracking Number', 'QA123');
  await selectByLabel(driver, 'Shipping Status', 'Delivered');
  await selectByLabel(driver, 'Payment Method', 'Transfer');
  await selectByLabel(driver, 'Payment Status', 'Paid');

  await inputOptionalByLabel(driver, 'Order Status', 'ACTIVE');
  await inputOptionalByLabel(driver, 'Refund Status', 'None');

  await clickFirstAvailableButton(driver, [
    'Create Order',
    'Add Order',
    'Save Order',
    'Submit',
    'Simpan',
  ]);

  await sleep(4000);

  await driver.get(`${BASE_URL}/sales-order`);
  await sleep(3000);

  const afterText = await logPagePreview(driver, 'Sales Order after create');

  assert(
    afterText.includes(customerName),
    `Sales order was not found after create. Expected customer: ${customerName}`
  );

  await logoutIfPossible(driver);
}

runTestCase('TC05 Sales Order Create', testSalesOrder);
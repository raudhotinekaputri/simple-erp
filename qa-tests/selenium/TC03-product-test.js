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
  runTestCase,
} = require('./helpers');

async function testProduct(driver) {
  await login(driver);

  await driver.get(`${BASE_URL}/products`);
  await sleep(3000);

  const pageText = await logPagePreview(driver, 'Products page preview');

  assert(
    pageText.includes('Products') ||
      pageText.includes('Product List') ||
      pageText.includes('Product Stock'),
    'Products page did not load expected content.'
  );

  const productName = uniqueText('QA Product');

  await clickFirstAvailableButton(driver, [
    '+ Add Product',
    'Tambah Produk',
    'Add',
  ]);

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

  const form = await driver.findElement({ css: 'form' });

  try {
    await form.submit();
  } catch {
    await clickFirstAvailableButton(driver, [
      'Add Product',
      'Save Product',
      'Create Product',
      'Save',
      'Submit',
      'Simpan',
    ]);
  }

  await sleep(4000);

  await driver.get(`${BASE_URL}/products`);
  await sleep(3000);

  const afterText = await logPagePreview(driver, 'Products after create');

  assert(
    afterText.includes(productName),
    `Product was not found after create. Expected: ${productName}`
  );

  await logoutIfPossible(driver);
}

runTestCase('TC03 Product Create', testProduct);
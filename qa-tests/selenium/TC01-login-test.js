const {
  login,
  logoutIfPossible,
  runTestCase,
} = require('./helpers');

async function testLogin(driver) {
  await login(driver);
  await logoutIfPossible(driver);
}

runTestCase('TC01 Login Admin', testLogin);